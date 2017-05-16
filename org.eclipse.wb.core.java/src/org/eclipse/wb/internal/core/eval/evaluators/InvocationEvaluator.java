/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.eval.evaluators;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.core.eval.InvocationEvaluatorInterceptor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.AstReflectionUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Implementation of {@link IExpressionEvaluator} for {@link MethodInvocation}.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class InvocationEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    if (expression instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) expression;
      return evaluate(context, invocation);
    }
    if (expression instanceof SuperMethodInvocation) {
      SuperMethodInvocation invocation = (SuperMethodInvocation) expression;
      return evaluate(context, invocation);
    }
    if (expression instanceof ClassInstanceCreation) {
      ClassInstanceCreation creation = (ClassInstanceCreation) expression;
      return evaluate(context, creation, typeBinding, typeQualifiedName);
    }
    // we don't understand given expression
    return AstEvaluationEngine.UNKNOWN;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MethodInvocation
  //
  ////////////////////////////////////////////////////////////////////////////
  private Object evaluate(EvaluationContext context, MethodInvocation invocation) throws Exception {
    // prepare binding
    IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(invocation);
    Assert.isNotNull(methodBinding);
    //
    Expression invocationExpression = invocation.getExpression();
    if (AstNodeUtils.isStatic(methodBinding)) {
      ITypeBinding targetTypeBinding = methodBinding.getDeclaringClass();
      Assert.isNotNull2(targetTypeBinding, "{0} {1}", invocation, methodBinding);
      return invokeMethod(context, null, targetTypeBinding, invocation, methodBinding);
    } else {
      // local instance method
      if (invocationExpression == null) {
        MethodDeclaration localMethod = AstNodeUtils.getLocalMethodDeclaration(invocation);
        if (localMethod != null) {
          return evaluateLocalMethodInvocation(context, localMethod, invocation);
        }
      }
      // prepare value of expression
      Object expressionValue = AstEvaluationEngine.evaluate(context, invocationExpression);
      Assert.isTrueException(
          expressionValue != null,
          ICoreExceptionConstants.EVAL_NULL_INVOCATION_EXPRESSION,
          context.getSource(invocation));
      // invoke method
      return invokeMethod(context, expressionValue, null, invocation, methodBinding);
    }
  }

  private Object evaluateLocalMethodInvocation(EvaluationContext context,
      MethodDeclaration methodDeclaration,
      MethodInvocation invocation) throws Exception {
    // try to evaluate method with "@wbp.eval.method.return parameterName" tag
    Javadoc javadoc = methodDeclaration.getJavadoc();
    if (javadoc != null) {
      for (TagElement tag : DomGenerics.tags(javadoc)) {
        if (StringUtils.equals(tag.getTagName(), "@wbp.eval.method.return")) {
          List<ASTNode> fragments = DomGenerics.fragments(tag);
          if (!fragments.isEmpty() && fragments.get(0) instanceof TextElement) {
            TextElement textElement = (TextElement) tag.fragments().get(0);
            String returnSource = textElement.getText().trim();
            return evaluateInvocationSourceExpression(
                context,
                methodDeclaration,
                invocation,
                returnSource);
          }
        }
      }
    }
    // try to evaluate simple "return Expr;"
    if (methodDeclaration.parameters().isEmpty()) {
      List<Statement> statements = DomGenerics.statements(methodDeclaration);
      if (statements.size() == 1 && statements.get(0) instanceof ReturnStatement) {
        ReturnStatement returnStatement = (ReturnStatement) statements.get(0);
        try {
          return AstEvaluationEngine.evaluate(context, returnStatement.getExpression());
        } catch (Throwable e) {
          throw new DesignerException(ICoreExceptionConstants.EVAL_LOCAL_METHOD_INVOCATION,
              e,
              context.getSource(invocation));
        }
      }
    }
    // in general case we can not invoke local instance method
    throw new DesignerException(ICoreExceptionConstants.EVAL_LOCAL_METHOD_INVOCATION,
        context.getSource(invocation));
  }

  /**
   * @return the result of evaluation for {@link String} expression based on values of arguments
   *         from {@link MethodInvocation}. It can use parameter names of {@link MethodDeclaration}.
   */
  private Object evaluateInvocationSourceExpression(EvaluationContext context,
      MethodDeclaration methodDeclaration,
      MethodInvocation invocation,
      String expressionSource) throws Exception {
    int index = -1;
    List<SingleVariableDeclaration> parameters = DomGenerics.parameters(methodDeclaration);
    for (SingleVariableDeclaration parameter : parameters) {
      if (parameter.getName().getIdentifier().equals(expressionSource)) {
        index = parameters.indexOf(parameter);
        break;
      }
    }
    Assert.isTrue(
        index != -1,
        "Can not evaluate %s for %s invocation of %s",
        expressionSource,
        invocation,
        methodDeclaration);
    return AstEvaluationEngine.evaluate(context, DomGenerics.arguments(invocation).get(index));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SuperMethodInvocation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We mark root {@link ASTNode} with this key to specify that currently invoked {@link Method} was
   * initiated by {@link SuperMethodInvocation}.
   */
  public static final String SUPER_MI_KEY = "SUPER_MI_KEY";

  private Object evaluate(EvaluationContext context, SuperMethodInvocation invocation)
      throws Exception {
    // prepare target
    Object thisValue = AstEvaluationEngine.evaluate(context, null);
    // prepare method
    Method method;
    {
      IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(invocation);
      Assert.isNotNull(methodBinding);
      method = getReflectionMethod(thisValue.getClass(), methodBinding);
    }
    // prepare argument values
    Object[] argumentValues = getArgumentValues(context, DomGenerics.arguments(invocation), true);
    // invoke method
    try {
      invocation.getRoot().setProperty(SUPER_MI_KEY, Boolean.TRUE);
      return method.invoke(thisValue, argumentValues);
    } catch (Throwable e) {
      throw new DesignerException(ICoreExceptionConstants.EVAL_SUPER_METHOD,
          e,
          context.getSource(invocation),
          method.toString(),
          getArguments_toString(argumentValues),
          AstEvaluationEngine.getUserStackTrace(e));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassInstanceCreation
  //
  ////////////////////////////////////////////////////////////////////////////
  private Object evaluate(EvaluationContext context,
      ClassInstanceCreation creation,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    // prepare binding
    IMethodBinding methodBinding = AstNodeUtils.getCreationBinding(creation);
    Assert.isNotNull(methodBinding);
    // ignore "inner not static"
    if (typeBinding.isMember() && !AstNodeUtils.isStatic(typeBinding)) {
      return null;
    }
    // prepare argument values
    List<Expression> argumentExpressions = DomGenerics.arguments(creation);
    Object[] argumentValues = getArgumentValues(context, argumentExpressions, true);
    argumentValues = updateForVarArgs(context, methodBinding, argumentValues);
    // create object
    if (typeBinding.isAnonymous()) {
      return createAnonymousObject(context, creation, typeBinding, methodBinding, argumentValues);
    } else {
      // prepare constructor
      Class<?> clazz = getTypeBindingClass(context, typeBinding);
      Constructor<?> constructor = getReflectionConstructor(clazz, methodBinding);
      try {
        // try to use interceptors
        {
          Object result =
              tryInterceptors(context, creation, typeBinding, clazz, constructor, argumentValues);
          if (result != AstEvaluationEngine.UNKNOWN) {
            return result;
          }
        }
        // create object using constructor
        fixThisExpressionArguments(constructor, argumentExpressions, argumentValues);
        return constructor.newInstance(argumentValues);
      } catch (Throwable e) {
        if (e instanceof InvocationTargetException) {
          e = ((InvocationTargetException) e).getCause();
        }
        throwAlternativeException(e);
        throw createConstructorProblemException(context, creation, argumentValues, constructor, e);
      }
    }
  }

  private static Object createAnonymousObject(final EvaluationContext context,
      final ClassInstanceCreation creation,
      ITypeBinding typeBinding,
      IMethodBinding methodBinding,
      Object[] argumentValues) throws Exception {
    ITypeBinding typeBindingConcrete = typeBinding;
    while (typeBindingConcrete.isAnonymous() || AstNodeUtils.isAbstract(typeBindingConcrete)) {
      typeBindingConcrete = typeBindingConcrete.getSuperclass();
    }
    // allow interceptors to evaluate
    for (InvocationEvaluatorInterceptor interceptor : getInterceptors()) {
      Object result =
          interceptor.evaluateAnonymous(
              context,
              creation,
              typeBinding,
              typeBindingConcrete,
              methodBinding,
              argumentValues);
      if (result != AstEvaluationEngine.UNKNOWN) {
        return result;
      }
    }
    // evaluate listeners and handlers
    if (isAnonymousEventListener(typeBinding)) {
      return AstEvaluationEngine.createAnonymousInstance(context, methodBinding, argumentValues);
    }
    // fail
    throw new AnonymousEvaluationError();
  }

  /**
   * @return <code>true</code> if given {@link ITypeBinding} is event listener, so we should create
   *         its instance. We need this because if this listener is part of constructor, then
   *         component may check that listener is not <code>null</code>.
   */
  private static boolean isAnonymousEventListener(ITypeBinding typeBinding) {
    if (typeBinding == null) {
      return false;
    }
    // this type
    String name = typeBinding.getName();
    if (name.endsWith("Listener") || name.endsWith("Handler")) {
      return true;
    }
    // interfaces
    for (ITypeBinding intf : typeBinding.getInterfaces()) {
      if (isAnonymousEventListener(intf)) {
        return true;
      }
    }
    // super class
    return isAnonymousEventListener(typeBinding.getSuperclass());
  }

  /**
   * @param methodBinding
   *          the {@link IMethodBinding} of constructor.
   *
   * @return the instance of anonymous {@link ClassInstanceCreation}, intercepting methods using
   *         given {@link Callback}.
   */
  public static Object createAnonymousInstance(EvaluationContext context,
      IMethodBinding methodBinding,
      Object[] argumentValues,
      Callback callback) throws Exception {
    Assert.isNotNull(callback);
    ITypeBinding typeBinding = methodBinding.getDeclaringClass();
    Class<?> creationClass = getTypeBindingClass(context, typeBinding.getSuperclass());
    Class<?>[] creationInterfaces = getClasses(context, typeBinding.getInterfaces());
    Class<?>[] argumentTypes = getClasses(context, methodBinding.getParameterTypes());
    // create object using Enhancer
    Enhancer enhancer = new Enhancer();
    enhancer.setClassLoader(context.getClassLoader());
    enhancer.setSuperclass(creationClass);
    enhancer.setInterfaces(creationInterfaces);
    enhancer.setCallback(callback);
    return enhancer.create(argumentTypes, argumentValues);
  }

  /**
   * Sometimes we known that some pieces of code in Internet or samples are not compatible with
   * WindowBuilder and we want to show specific exception/message for them.
   *
   * @throws Error
   *           to show instead of original {@link Throwable}.
   */
  private static void throwAlternativeException(Throwable e) {
    List<InvocationEvaluatorInterceptor> interceptors = getInterceptors();
    for (InvocationEvaluatorInterceptor interceptor : interceptors) {
      Throwable result = interceptor.rewriteException(e);
      if (result instanceof Error) {
        throw (Error) result;
      }
    }
  }

  private static Object tryInterceptors(EvaluationContext context,
      ClassInstanceCreation creation,
      ITypeBinding typeBinding,
      Class<?> clazz,
      Constructor<?> explicitConstructor,
      Object[] argumentValues) throws Exception {
    for (InvocationEvaluatorInterceptor interceptor : getInterceptors()) {
      Object result =
          interceptor.evaluate(
              context,
              creation,
              typeBinding,
              clazz,
              explicitConstructor,
              argumentValues);
      if (result != AstEvaluationEngine.UNKNOWN) {
        return result;
      }
    }
    return AstEvaluationEngine.UNKNOWN;
  }

  /**
   * If {@link ThisExpression} argument values is not compatible with parameter, replace it with
   * <code>null</code>.
   */
  private static void fixThisExpressionArguments(Constructor<?> constructor,
      List<Expression> argumentExpressions,
      Object[] argumentValues) {
    Class<?>[] parameterTypes = constructor.getParameterTypes();
    Assert.isTrue(
        parameterTypes.length == argumentValues.length,
        "Incompatible count of parameters %s and arguments %s",
        parameterTypes.length,
        argumentValues.length);
    for (int i = 0; i < parameterTypes.length; i++) {
      // empty varArgs
      if (i >= argumentExpressions.size()) {
        break;
      }
      // check for ThisExpression
      if (argumentExpressions.get(i) instanceof ThisExpression) {
        Class<?> parameterType = parameterTypes[i];
        Object argument = argumentValues[i];
        if (argument != null && !parameterType.isAssignableFrom(argument.getClass())) {
          argumentValues[i] = null;
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Finds method using {@link IMethodBinding} and invokes it for given object instance.
   */
  private static Object invokeMethod(EvaluationContext context,
      Object targetValue,
      ITypeBinding targetTypeBinding,
      MethodInvocation invocation,
      IMethodBinding methodBinding) throws Exception {
    // prepare class
    Class<?> clazz;
    if (targetTypeBinding != null) {
      clazz = getTypeBindingClass(context, targetTypeBinding);
    } else {
      clazz = targetValue.getClass();
    }
    // prepare argument values
    Object[] argumentValues = getArgumentValues(context, DomGenerics.arguments(invocation), false);
    argumentValues = updateForVarArgs(context, methodBinding, argumentValues);
    // prepare method
    Method method = getReflectionMethod(clazz, methodBinding);
    // try to use interceptors
    {
      Object result =
          tryInterceptors(context, invocation, methodBinding, clazz, method, argumentValues);
      if (result != AstEvaluationEngine.UNKNOWN) {
        return result;
      }
    }
    // invoke method
    try {
      return method.invoke(targetValue, argumentValues);
    } catch (Throwable e) {
      throw new DesignerException(ICoreExceptionConstants.EVAL_METHOD,
          e,
          context.getSource(invocation),
          method.toString(),
          getArguments_toString(argumentValues));
    }
  }

  private static Object tryInterceptors(EvaluationContext context,
      MethodInvocation invocation,
      IMethodBinding methodBinding,
      Class<?> clazz,
      Method method,
      Object[] argumentValues) {
    for (InvocationEvaluatorInterceptor interceptor : getInterceptors()) {
      Object result =
          interceptor.evaluate(context, invocation, methodBinding, clazz, method, argumentValues);
      if (result != AstEvaluationEngine.UNKNOWN) {
        return result;
      }
    }
    return AstEvaluationEngine.UNKNOWN;
  }

  /**
   * @return the {@link Class} corresponding given {@link ITypeBinding}.
   */
  private static Class<?> getTypeBindingClass(EvaluationContext context, ITypeBinding typeBinding)
      throws Exception {
    ClassLoader classLoader = context.getClassLoader();
    return AstReflectionUtils.getClass(classLoader, typeBinding);
  }

  /**
   * @return array of {@link Class}'s for array of {@link ITypeBinding}.
   */
  private static Class<?>[] getClasses(EvaluationContext context, ITypeBinding[] typeBindings)
      throws Exception {
    Class<?>[] classes = new Class<?>[typeBindings.length];
    for (int i = 0; i < typeBindings.length; i++) {
      ITypeBinding typeBinding = typeBindings[i];
      classes[i] = getTypeBindingClass(context, typeBinding);
    }
    return classes;
  }

  /**
   * @return the reflection {@link Method} from given class for given {@link IMethodBinding}
   */
  private static Method getReflectionMethod(Class<?> clazz, IMethodBinding binding)
      throws Exception {
    String signature = AstNodeUtils.getMethodSignature(binding);
    Method method;
    // ask interceptors
    for (InvocationEvaluatorInterceptor interceptor : getInterceptors()) {
      method = interceptor.resolveMethod(clazz, signature);
      if (method != null) {
        return method;
      }
    }
    //
    method = AstReflectionUtils.getMethod(clazz, binding);
    // done
    Assert.isNotNull2(method, "Method {0} not found in {1}", signature, clazz);
    return method;
  }

  /**
   * @return the reflection {@link Constructor} from given {@link Class}.
   */
  private static Constructor<?> getReflectionConstructor(Class<?> clazz, IMethodBinding binding)
      throws Exception {
    String signature = AstNodeUtils.getMethodSignature(binding);
    Constructor<?> constructor;
    // usual signature
    {
      constructor = ReflectionUtils.getConstructorBySignature(clazz, signature);
    }
    // not found, try generic signature
    if (constructor == null) {
      IMethodBinding genericBinding = binding.getMethodDeclaration();
      if (genericBinding != binding) {
        String genericSignature = AstNodeUtils.getMethodSignature(genericBinding);
        constructor = ReflectionUtils.getConstructorByGenericSignature(clazz, genericSignature);
      }
    }
    // done
    Assert.isNotNull2(constructor, "No constructor {0} in {1}.", signature, clazz);
    return constructor;
  }

  /**
   * @return values of given {@link Expression} list.
   */
  private static Object[] getArgumentValues(EvaluationContext context,
      List<Expression> arguments,
      boolean forConstructor) throws Exception {
    int argumentCount = arguments.size();
    Object argumentValues[] = new Object[argumentCount];
    for (int i = 0; i < argumentCount; i++) {
      Expression argument = arguments.get(i);
      try {
        argumentValues[i] = AstEvaluationEngine.evaluate(context, argument);
      } catch (Error e) {
        if (forConstructor && AnonymousEvaluationError.is(e)) {
          argumentValues[i] = null;
        } else {
          throw e;
        }
      }
    }
    return argumentValues;
  }

  private static Object[] updateForVarArgs(EvaluationContext context,
      IMethodBinding methodBinding,
      Object[] values) throws Exception {
    ClassLoader classLoader = context.getClassLoader();
    return AstReflectionUtils.updateForVarArgs(classLoader, methodBinding, values);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exception utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link DesignerException} that describes problem during
   *         {@link ClassInstanceCreation} evaluation.
   */
  private static DesignerException createConstructorProblemException(EvaluationContext context,
      ClassInstanceCreation creation,
      Object[] argumentValues,
      Constructor<?> constructor,
      Throwable e) throws DesignerException {
    return new DesignerException(ICoreExceptionConstants.EVAL_CONSTRUCTOR,
        e,
        context.getSource(creation),
        ReflectionUtils.getShortConstructorString(constructor),
        getArguments_toString(argumentValues),
        AstEvaluationEngine.getUserStackTrace(e));
  }

  /**
   * @return {@link String} presentation of given argument values, safely.
   */
  public static String getArguments_toString(final Object[] arguments) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        return ArrayUtils.toString(arguments);
      }
    }, "<Exception during arguments.toString()>");
  }

  /**
   * @return all registered {@link InvocationEvaluatorInterceptor}'s.
   */
  private static List<InvocationEvaluatorInterceptor> getInterceptors() {
    return ExternalFactoriesHelper.getElementsInstances(
        InvocationEvaluatorInterceptor.class,
        "org.eclipse.wb.core.invocationEvaluatorInterceptors",
        "interceptor");
  }
}
