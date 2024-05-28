/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.creation;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.core.eval.ExecutionFlowUtils2;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.EvaluationEventListener;
import org.eclipse.wb.internal.core.eval.evaluators.InvocationEvaluator;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SuperConstructorAccessor;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupportUtils;
import org.eclipse.wb.internal.core.model.variable.MethodParameterVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.AstReflectionUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.TypeCache;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * Implementation of {@link CreationSupport} for subclasses.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public final class ThisCreationSupport extends CreationSupport {
	private final MethodDeclaration m_constructor;
	private SuperConstructorInvocation m_invocation;
	private ConstructorDescription m_description;
	private CreationSupportUtils m_utils;
	private EditorState m_editorState;
	private boolean m_interceptOnlyDuringExecution;
	private Predicate<Method> m_methodInterceptorPredicate = o -> true;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ThisCreationSupport(MethodDeclaration constructor) {
		m_constructor = constructor;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return "this: " + getComponentClass().getName();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setJavaInfo(JavaInfo javaInfo) throws Exception {
		super.setJavaInfo(javaInfo);
		m_editorState = JavaInfoUtils.getState(javaInfo);
		m_interceptOnlyDuringExecution =
				JavaInfoUtils.hasTrueParameter(javaInfo, "interceptOnlyDuringExecution");
		m_methodInterceptorPredicate = createInterceptionPredicate_excludeByPackage(m_javaInfo);
		prepareSuperConstructorInvocation(javaInfo);
		// attempt to expose children after executing each Statement
		m_javaInfo.addBroadcastListener(new EvaluationEventListener() {
			@Override
			public void evaluateAfter(EvaluationContext context, ASTNode node) throws Exception {
				if (shouldTryToExposeChildren(node)) {
					// create new exposed children
					duringParsing_createExposedChildren();
					// set object again, to get objects of exposed children (problem during refresh)
					m_javaInfo.setObject(m_javaInfo.getObject());
				}
			}

			private boolean shouldTryToExposeChildren(ASTNode node) {
				if (node instanceof ExpressionStatement) {
					Expression expression = ((ExpressionStatement) node).getExpression();
					return expression instanceof SuperMethodInvocation;
				}
				return false;
			}
		});
		// prepare Utils
		m_utils = new CreationSupportUtils(m_javaInfo);
	}

	/**
	 * Tries to find {@link SuperConstructorInvocation} and prepare {@link #m_invocation}, etc.
	 */
	private void prepareSuperConstructorInvocation(JavaInfo javaInfo) {
		if (javaInfo.getDescription().getComponentClass().isInterface()) {
			return;
		}
		// check constructor body
		m_invocation = findSuperConstructorInvocation(m_constructor);
		if (m_invocation != null) {
			IMethodBinding constructorBinding = AstNodeUtils.getSuperBinding(m_invocation);
			m_description = m_javaInfo.getDescription().getConstructor(constructorBinding);
			ComponentDescriptionHelper.ensureInitialized(
					m_javaInfo.getEditor().getJavaProject(),
					m_description);
		}
	}

	private SuperConstructorInvocation findSuperConstructorInvocation(MethodDeclaration constructor) {
		List<Statement> statements = DomGenerics.statements(constructor);
		if (statements.isEmpty()) {
			return null;
		}
		Statement statement = statements.get(0);
		if (statement instanceof ConstructorInvocation invocation) {
			constructor = AstNodeUtils.getConstructor(invocation);
			Assert.isNotNull(constructor, "Can not find constructor declaration for %s", invocation);
			return findSuperConstructorInvocation(constructor);
		} else if (statement instanceof SuperConstructorInvocation) {
			return (SuperConstructorInvocation) statement;
		}
		return null;
	}

	@Override
	public boolean isJavaInfo(ASTNode node) {
		return node == null || node instanceof ThisExpression || node == m_constructor;
	}

	@Override
	public ASTNode getNode() {
		return m_constructor;
	}

	@Override
	public boolean canBeEvaluated() {
		return false;
	}

	/**
	 * @return the {@link MethodDeclaration} of constructor that bounds to this
	 *         {@link ThisCreationSupport}.
	 */
	public MethodDeclaration getConstructor() {
		return m_constructor;
	}

	/**
	 * @return the used {@link SuperConstructorInvocation}, may be <code>null</code>.
	 */
	public SuperConstructorInvocation getInvocation() {
		return m_invocation;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * This key is used to remember {@link JavaInfo} created for {@link SingleVariableDeclaration}.
	 */
	private static final String KEY_PARAMETER_BASED_COMPONENT = "KEY_PARAMETER_BASED_COMPONENT";
	/**
	 * <p>
	 * This field is used to create an enhanced instance of the component class. The
	 * thread-local datatype is used to support concurrent construction of such
	 * instances where each instance uses its own visitor.
	 * </p>
	 * <p>
	 * The field is set before and cleared after the instance has been created.
	 * </p>
	 * <p>
	 * The field has to be static in order to be accessible by
	 * {@link StubMethodInterceptor}.
	 * </p>
	 */
	private static final AtomicReference<MethodInterceptor> PROXY_INTERCEPTOR = new AtomicReference<>();
	/**
	 * <p>
	 * This field is used to create an enhanced class for a given component class.
	 * Given the expensive overhead that comes with the classloader, each class
	 * should only be created once and then reused as much as possible.
	 * </p>
	 * <p>
	 * The primary difference between two enhanced objects (of the same type) is the
	 * {@link ExecutionFlowFrameVisitor} that is used to calculate a custom return
	 * type. This visitor is reference via a local variable, which needs to be set
	 * when an instance is created. The corresponding visitor object is stored in
	 * {@link #PROXY_INTERCEPTOR}.
	 * </p>
	 */
	private static final TypeCache<TypeCache.SimpleKey> PROXY_CACHE = new TypeCache.WithInlineExpunction<>(
			TypeCache.Sort.WEAK);

	@Override
	public Object create(EvaluationContext context, ExecutionFlowFrameVisitor visitor)
			throws Exception {
		Class<?> componentClass = getComponentClass();
		// prepare constructor
		String signature;
		Constructor<?> constructor;
		Object[] argumentValues;
		if (m_invocation != null) {
			IMethodBinding methodBinding = AstNodeUtils.getSuperBinding(m_invocation);
			signature = AstNodeUtils.getMethodSignature(methodBinding);
			constructor = AstReflectionUtils.getConstructor(componentClass, m_invocation);
			List<Expression> arguments = DomGenerics.arguments(m_invocation);
			argumentValues = evaluateExpressions(context, arguments);
			argumentValues =
					AstReflectionUtils.updateForVarArgs(
							context.getClassLoader(),
							methodBinding,
							argumentValues);
		} else {
			signature = "<init>()";
			constructor = ReflectionUtils.getConstructorBySignature(componentClass, "<init>()");
			argumentValues = ArrayUtils.EMPTY_OBJECT_ARRAY;
		}
		if (constructor == null && !componentClass.isInterface()) {
			throw new DesignerException(ICoreExceptionConstants.EVAL_NO_CONSTRUCTOR,
					signature,
					componentClass.getName());
		}
		// create Object
		try {
			Object object = create0(visitor, constructor, componentClass, argumentValues);
			m_javaInfo.setObject(object);
			duringParsing_createExposedChildren();
			return object;
		} catch (DesignerException e) {
			throw e;
		} catch (Throwable e) {
			throw new DesignerException(ICoreExceptionConstants.EVAL_BYTEBUDDY,
					e,
					ReflectionUtils.getShortConstructorString(constructor),
					InvocationEvaluator.getArguments_toString(argumentValues),
					AstEvaluationEngine.getUserStackTrace(e));
		}
	}

	private Object create0(ExecutionFlowFrameVisitor visitor,
			Constructor<?> constructor,
			Class<?> componentClass,
			Object[] argumentValues) throws Exception {
		// use Enhancer
		if (needEnhancer() && hasEnhancer()) {
			return create_usingEnhancer(visitor, constructor, componentClass, argumentValues);
		}
		// create object using Constructor
		return constructor.newInstance(argumentValues);
	}

	private Object create_usingEnhancer(ExecutionFlowFrameVisitor visitor,
			Constructor<?> constructor,
			Class<?> componentClass,
			Object[] argumentValues) {
		createEnhancer(componentClass, visitor);
		// set current ASTNode = body of constructor
		ExecutionFlowDescription flowDescription;
		{
			flowDescription = m_editorState.getFlowDescription();
			flowDescription.enterStatement(m_constructor.getBody());
		}
		// create object
		try {
			synchronized(ThisCreationSupport.class) {
				try {
					PROXY_INTERCEPTOR.set(new ExecutionMethodInterceptor(visitor));
					Class<?> m_enhancer = PROXY_CACHE.find(getClassLoader(), new TypeCache.SimpleKey(componentClass));
					Object instance;
					if (constructor != null) {
						Assert.isTrueException(
								!ReflectionUtils.isPackagePrivate(constructor),
								ICoreExceptionConstants.EVAL_NON_PUBLIC_CONSTRUCTOR,
								constructor);
						instance = m_enhancer.getConstructor(constructor.getParameterTypes()).newInstance(argumentValues);
					} else {
						instance = m_enhancer.getConstructor().newInstance();
					}
					ReflectionUtils.setField(instance, MethodInterceptor.FIELD_INTERCEPTOR, PROXY_INTERCEPTOR.get());
					return instance;
				} finally {
					PROXY_INTERCEPTOR.set(null);
				}
			}
		} catch (ReflectiveOperationException e) {
			throw new DesignerException(ICoreExceptionConstants.EVAL_BYTEBUDDY, e);
		} finally {
			flowDescription.leaveStatement(m_constructor.getBody());
		}
	}

	/**
	 * @return <code>true</code> if we need to use {@link ByteBuddy} for this component, and
	 *         <code>false</code> if this component can be created more lightly, without ByteBuddy.
	 */
	private boolean needEnhancer() {
		ComponentDescription description = m_javaInfo.getDescription();
		if (description.hasTrueParameter("binaryExecutionFlow.no")) {
			return false;
		}
		Class<?> componentClass = description.getComponentClass();
		boolean isSystemClass = componentClass.getClassLoader() == null;
		boolean isEnhanced = ReflectionUtils.getFieldByName(componentClass, MethodInterceptor.FIELD_INTERCEPTOR) != null;
		return !isSystemClass && !isEnhanced;
	}

	/**
	 * @return <code>true</code> if given {@link Class} can access ByteBuddy.
	 */
	private boolean hasEnhancer() {
		try {
			ClassLoader classLoader = getClassLoader();
			return classLoader != null && classLoader.loadClass("net.bytebuddy.ByteBuddy") != null;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * @return the {@link ClassLoader} for {@link JavaInfo} context.
	 */
	private ClassLoader getClassLoader() {
		return JavaInfoUtils.getClassLoader(m_javaInfo);
	}

	/**
	 * Initializes {@link #m_enhancer} field.
	 */
	private void createEnhancer(Class<?> componentClass, final ExecutionFlowFrameVisitor visitor) {
		final TypeCache.SimpleKey proxyKey = new TypeCache.SimpleKey(componentClass);

		DynamicType.Builder<?> builder = new ByteBuddy()
				.subclass(componentClass) //
				.defineField(MethodInterceptor.FIELD_INTERCEPTOR, MethodInterceptor.class, Modifier.PRIVATE)
				.method(ElementMatchers.noneOf(getIgnoredMethods(componentClass))) //
				.intercept(MethodDelegation.to(new StubMethodInterceptor(), MethodInterceptor.class));

		PROXY_CACHE.findOrInsert(getClassLoader(), proxyKey, () -> builder.make().load(getClassLoader(), ClassLoadingStrategy.Default.INJECTION).getLoaded());
	}

	/**
	 * Filter for intercepting {@link Method}'s.
	 */
	private static Method[] getIgnoredMethods(Class<?> clazz) {
		List<Method> ignored = new ArrayList<>();

		// Iterate over all public methods, including inherited
		for (Method method : clazz.getMethods()) {
			// ignore standard Swing methods
			if (method.getDeclaringClass().getClassLoader() == null) {
				ignored.add(method);
			}
		}

		// Iterate over all methods, excluding inherited
		for (Method method : clazz.getDeclaredMethods()) {
			// ignore inaccessible methods
			if (ReflectionUtils.isPrivate(method) || ReflectionUtils.isPackagePrivate(method)) {
				ignored.add(method);
			}
		}

		return ignored.toArray(Method[]::new);
	}

	/**
	 * Allows disable interception of all methods in some package (for example all standard methods of
	 * toolkit), but still allow interception of some specific methods.
	 */
	private static Predicate<Method> createInterceptionPredicate_excludeByPackage(JavaInfo javaInfo) {
		class ExcludedPackage {
			Set<String> exceptions = new HashSet<>();
		}
		final Map<String, ExcludedPackage> excludedPackages = new HashMap<>();
		// prepare excluded packages
		String parameterNamePrefix = "binaryExecutionFlow.dontVisit.package ";
		for (Map.Entry<String, String> entry : JavaInfoUtils.getParameters(javaInfo).entrySet()) {
			String parameterName = entry.getKey();
			if (parameterName.startsWith(parameterNamePrefix)) {
				String packageName = parameterName.substring(parameterNamePrefix.length()).trim();
				ExcludedPackage excludedPackage = new ExcludedPackage();
				String exceptionMethodsString = entry.getValue();
				String[] exceptionMethodsSignatures = StringUtils.split(exceptionMethodsString);
				excludedPackage.exceptions = Set.of(exceptionMethodsSignatures);
				excludedPackages.put(packageName, excludedPackage);
			}
		}
		// use as Predicate
		return method -> {
			String declaringClassName = method.getDeclaringClass().getName();
			String declaringPackage = CodeUtils.getPackage(declaringClassName);
			ExcludedPackage excludedPackage = excludedPackages.get(declaringPackage);
			if (excludedPackage != null) {
				String signature = ReflectionUtils.getMethodSignature(method);
				return excludedPackage.exceptions.contains(signature);
			}
			return true;
		};
	}

	/**
	 * @return <code>true</code> if currently invoked {@link Method} was from
	 *         {@link SuperMethodInvocation}. Removes this flag.
	 */
	private boolean isSuperMethodInvocation() {
		ASTNode root = m_constructor.getRoot();
		try {
			return root.getProperty(InvocationEvaluator.SUPER_MI_KEY) == Boolean.TRUE;
		} finally {
			root.setProperty(InvocationEvaluator.SUPER_MI_KEY, null);
		}
	}

	private Object tryModelMethodInterceptor(Method method, String signature, Object[] args)
			throws Exception {
		// try interceptor in description
		{
			String interceptorName = "this.interceptMethod: " + signature;
			String script = JavaInfoUtils.getParameter(m_javaInfo, interceptorName);
			if (script != null) {
				return ScriptUtils.evaluate(getClassLoader(), script, "model", m_javaInfo);
			}
		}
		// try interceptor in model
		{
			String interceptorName = method.getName() + "_interceptor";
			Method interceptor =
					ReflectionUtils.getMethod(
							m_javaInfo.getClass(),
							interceptorName,
							method.getParameterTypes());
			if (interceptor != null) {
				return interceptor.invoke(m_javaInfo, args);
			}
		}
		// no interceptor
		return AstEvaluationEngine.UNKNOWN;
	}

	private boolean canInterceptMethod(Method method, String signature) throws Exception {
		// method specific flag
		{
			MethodDescription methodDescription = m_javaInfo.getDescription().getMethod(signature);
			if (methodDescription != null
					&& methodDescription.hasTrueTag("binaryExecutionFlow.dontVisit")) {
				return false;
			}
		}
		// class level predicate
		return m_methodInterceptorPredicate.test(method);
	}

	/**
	 * Visits {@link MethodDeclaration} that corresponds to the intercepted {@link Method}.
	 */
	private Object visitMethod(Object obj,
			java.lang.reflect.Method method,
			Object[] args,
			MethodDeclaration methodDeclaration,
			ExecutionFlowFrameVisitor visitor) throws Exception {
		Object result = visitMethod0(obj, method, args, methodDeclaration, visitor);
		result = visitMethod_validator(method, args, result);
		return result;
	}

	/**
	 * Try to apply validator from {@link JavaInfo}.
	 */
	private Object visitMethod_validator(java.lang.reflect.Method method, Object[] args, Object result)
			throws Exception {
		Class<?>[] validatorParameterTypes =
				ArrayUtils.add(method.getParameterTypes(), Object.class);
		String validatorName = method.getName() + "_validator";
		Method validator =
				ReflectionUtils.getMethod(m_javaInfo.getClass(), validatorName, validatorParameterTypes);
		if (validator != null) {
			Object[] validatorArgs = ArrayUtils.add(args, result);
			result = validator.invoke(m_javaInfo, validatorArgs);
		}
		return result;
	}

	/**
	 * Visits {@link MethodDeclaration} that corresponds to the intercepted {@link Method}.
	 */
	private Object visitMethod0(Object obj,
			java.lang.reflect.Method method,
			Object[] args,
			MethodDeclaration methodDeclaration,
			ExecutionFlowFrameVisitor visitor) throws Exception {
		m_javaInfo.setObject(obj);
		duringParsing_createExposedChildren();
		// prepare context
		AstEditor editor = m_javaInfo.getEditor();
		ExecutionFlowDescription flowDescription = m_editorState.getFlowDescription();
		// check if already visited
		{
			boolean simpleReturnMethod = isSimpleReturnMethod(methodDeclaration);
			boolean lazyMethod = LazyVariableSupportUtils.getInformation(methodDeclaration) != null;
			if (!simpleReturnMethod && !lazyMethod) {
				Set<MethodDeclaration> interceptedMethods = m_editorState.getTmp_InterceptedMethods();
				if (interceptedMethods.contains(methodDeclaration)) {
					return ReflectionUtils.getDefaultValue(method.getReturnType());
				}
				interceptedMethods.add(methodDeclaration);
			}
		}
		// create/update parameter based components
		processMethodParameterComponents(editor, flowDescription, method, args, methodDeclaration);
		// include method into execution flow
		if (!flowDescription.isBinaryFlowLocked()) {
			flowDescription.addBinaryFlowMethodAfter(methodDeclaration);
		}
		// visit method
		ExecutionFlowUtils.visit(
				m_editorState.getTmp_visitingContext(),
				flowDescription,
				visitor,
				List.of(methodDeclaration));
		// during execution we remember "return value", so return it here to binary
		return JavaInfoEvaluationHelper.getReturnValue(methodDeclaration);
	}

	/**
	 * @return <code>true</code> if given {@link ASTNode} (initially some {@link MethodDeclaration})
	 *         is simple {@link ReturnStatement} and can be evaluated more than one time, so it is
	 *         safe to visit this {@link MethodDeclaration} several times.
	 */
	private static boolean isSimpleReturnMethod(ASTNode node) {
		if (node instanceof MethodDeclaration methodDeclaration) {
			List<Statement> statements = DomGenerics.statements(methodDeclaration);
			return methodDeclaration.parameters().isEmpty()
					&& statements.size() == 1
					&& isSimpleReturnMethod(statements.get(0));
		}
		if (node instanceof ReturnStatement returnStatement) {
			return isSimpleReturnMethod(returnStatement.getExpression());
		}
		if (node instanceof ParenthesizedExpression parenthesizedExpression) {
			return isSimpleReturnMethod(parenthesizedExpression.getExpression());
		}
		if (node instanceof SimpleName) {
			return true;
		}
		return false;
	}

	/**
	 * Creates or sets {@link Object} for components based on parameters of given
	 * {@link MethodDeclaration}.
	 */
	private void processMethodParameterComponents(AstEditor editor,
			ExecutionFlowDescription flowDescription,
			java.lang.reflect.Method method,
			Object[] args,
			MethodDeclaration methodDeclaration) throws Exception {
		Class<?>[] parameterTypes = method.getParameterTypes();
		List<SingleVariableDeclaration> parameters = DomGenerics.parameters(methodDeclaration);
		// remember values into parameters
		for (int i = 0; i < args.length; i++) {
			SingleVariableDeclaration parameter = parameters.get(i);
			parameter.setProperty(JavaInfoEvaluationHelper.KEY_PARAMETER_HAS_VALUE, Boolean.TRUE);
			parameter.setProperty(JavaInfoEvaluationHelper.KEY_PARAMETER_VALUE, args[i]);
		}
		// create/update JavaInfo
		if (!flowDescription.isBinaryFlowLocked()) {
			for (int i = 0; i < args.length; i++) {
				if (!parameterTypes[i].isPrimitive() && args[i] != null) {
					SingleVariableDeclaration parameter = parameters.get(i);
					// prepare ComponentDescription, note that it may fail
					ComponentDescription componentDescription;
					try {
						componentDescription =
								ComponentDescriptionHelper.getDescription(
										editor,
										m_javaInfo.getDescription(),
										parameter);
						// check if we can create JavaInfo
						if ("true".equals(componentDescription.getParameter("thisCreation.ignoreBind"))) {
							continue;
						}
					} catch (Throwable e) {
						continue;
					}
					// create JavaInfo for parameter
					JavaInfo javaInfo;
					{
						javaInfo =
								JavaInfoUtils.createJavaInfo(
										editor,
										componentDescription,
										new MethodParameterCreationSupport(parameter));
						javaInfo.setVariableSupport(new MethodParameterVariableSupport(javaInfo, parameter));
						javaInfo.setObject(args[i]);
					}
					// append JavaInfo into parser components, to bind later
					parameter.setProperty(KEY_PARAMETER_BASED_COMPONENT, javaInfo);
					m_editorState.getTmp_Components().add(javaInfo);
					ExecutionFlowUtils2.ensurePermanentValue(parameter.getName()).setModel(javaInfo);
				}
			}
		} else {
			for (int i = 0; i < parameters.size(); i++) {
				SingleVariableDeclaration parameter = parameters.get(i);
				JavaInfo javaInfo = (JavaInfo) parameter.getProperty(KEY_PARAMETER_BASED_COMPONENT);
				if (javaInfo != null) {
					javaInfo.setObject(args[i]);
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void duringParsing_createExposedChildren() throws Exception {
		if (GlobalState.isParsing()) {
			m_javaInfo.createExposedChildren();
		}
	}

	private static Object[] evaluateExpressions(EvaluationContext context,
			List<Expression> expressions) throws Exception {
		Object[] values = new Object[expressions.size()];
		for (int i = 0; i < values.length; i++) {
			Expression expression = expressions.get(i);
			values[i] = AstEvaluationEngine.evaluate(context, expression);
		}
		return values;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public void delete() throws Exception {
		JavaInfoUtils.deleteJavaInfo(m_javaInfo, false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	private ComplexProperty m_complexProperty;

	@Override
	public void addProperties(List<Property> properties) throws Exception {
		if (m_complexProperty == null) {
			m_complexProperty = new ComplexProperty("Constructor", "(Constructor properties)");
			m_complexProperty.setCategory(PropertyCategory.system(3));
			m_complexProperty.setModified(true);
			//
			List<Property> subPropertiesList = new ArrayList<>();
			if (m_invocation != null) {
				// add accessors for parameters bound to given property
				for (ParameterDescription parameter : m_description.getParameters()) {
					Property property = m_utils.createProperty(parameter);
					if (property != null) {
						subPropertiesList.add(property);
					}
				}
				// set sub-properties
				if (!subPropertiesList.isEmpty()) {
					m_complexProperty.setProperties(subPropertiesList);
				}
			}
		}
		// add complex property if there are sub-properties
		if (m_complexProperty.getProperties().length != 0) {
			properties.add(m_complexProperty);
		}
	}

	@Override
	public void addAccessors(GenericPropertyDescription propertyDescription,
			List<ExpressionAccessor> accessors) {
		// add accessors for parameters bound to given property
		if (m_invocation != null) {
			for (ParameterDescription parameter : m_description.getParameters()) {
				if (propertyDescription.getId().equals(parameter.getProperty())) {
					accessors.add(new SuperConstructorAccessor(m_invocation,
							parameter.getIndex(),
							parameter.getDefaultSource()));
				}
			}
		}
	}

	/**
	 * The base interface to which all method invocations are delegated to. In case
	 * the intercepted method is abstract, a dummy value should be returned. In case
	 * no custom behavior is specified, its super method should be invoked.
	 */
	public static interface MethodInterceptor {
		String FIELD_INTERCEPTOR = "WINDOWBUILDER$INTERCEPTOR";

		@RuntimeType
		Object intercept(@This Object obj, @Origin Method method, @AllArguments Object[] args,
				@SuperCall(nullIfImpossible = true) Callable<?> superCall) throws Throwable;

		default Object returnDefaultValue(Method method) {
			Class<?> returnType = method.getReturnType();
			return ReflectionUtils.getDefaultValue(returnType);
		}
	}

	/**
	 * This class contains the primary interceptor logic. For each instance of an
	 * enhanced class, we attempt to calculate a custom return type using the given
	 * {@link ExecutionFlowFrameVisitor} instance. This interceptor is stored as a
	 * local variable in the enhanced object.
	 */
	private class ExecutionMethodInterceptor implements MethodInterceptor {
		private final ExecutionFlowFrameVisitor visitor;

		public ExecutionMethodInterceptor(ExecutionFlowFrameVisitor visitor) {
			this.visitor = visitor;
		}

		@Override
		public Object intercept(Object obj, Method method, Object[] args, Callable<?> superCall) throws Throwable {
			// if not in AST execution, then ignore
			if (m_interceptOnlyDuringExecution && !m_editorState.isExecuting()) {
				if (ReflectionUtils.isAbstract(method)) {
					return returnDefaultValue(method);
				}
				return superCall.call();
			}
			// try to find implementation of this method in AST
			if (!method.isBridge() && !isSuperMethodInvocation()) {
				String methodSignature = ReflectionUtils.getMethodSignature(method);
				// handle special SWT methods
				if (methodSignature.equals("isValidSubclass()")) {
					return Boolean.TRUE;
				}
				if (methodSignature.equals("checkSubclass()")) {
					return null;
				}
				// may be model wants to handle method and provide result
				{
					Object result = tryModelMethodInterceptor(method, methodSignature, args);
					if (result != AstEvaluationEngine.UNKNOWN) {
						return result;
					}
				}
				// check if we are allowed to intercept method
				if (!canInterceptMethod(method, methodSignature)) {
					return superCall.call();
				}
				// try to find MethodDeclaration
				MethodDeclaration methodDeclaration;
				{
					TypeDeclaration typeDeclaration = (TypeDeclaration) m_constructor.getParent();
					methodDeclaration = AstNodeUtils.getMethodBySignature(typeDeclaration, methodSignature);
				}
				// OK, we have MethodDeclaration, redirect to it
				if (methodDeclaration != null && !AstNodeUtils.isAbstract(methodDeclaration)) {
					JavaInfoEvaluationHelper.shouldEvaluateReturnValue(methodDeclaration, true);
					return visitMethod(obj, method, args, methodDeclaration, visitor);
				}
			}
			// handle abstract
			if (ReflectionUtils.isAbstract(method)) {
				return returnDefaultValue(method);
			}
			// invoke super
			return superCall.call();
		}
	}

	/**
	 * This class is used as a default interceptor inside the enhanced class, shared
	 * between all instances. The method invocation is delegated to the
	 * instance-scoped interceptor, which is stored as a local variable. In case
	 * this field hasn't been initialized yet (meaning we're calling an enhanced
	 * method from within the constructor), the referee is fetched from the
	 * thread-local store. This store is set before, and cleared after an object is
	 * constructed, so its value is the same as the one, the field is going to be
	 * initialized with.
	 */
	private static class StubMethodInterceptor implements MethodInterceptor {
		@Override
		public Object intercept(Object obj, Method method, Object[] args, Callable<?> superCall) throws Throwable {
			MethodInterceptor referee = (MethodInterceptor) ReflectionUtils.getFieldObject(obj, FIELD_INTERCEPTOR);

			// We may call non-private methods inside the constructor. The field hasn't been
			// initialized at that point in time...
			if (referee == null) {
				referee = PROXY_INTERCEPTOR.get();
			}

			return referee.intercept(obj, method, args, superCall);
		}
	}
}
