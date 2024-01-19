/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.description.helpers;

import static org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper.acceptSafe;

import org.eclipse.wb.core.databinding.xsd.component.ContextFactory;
import org.eclipse.wb.core.databinding.xsd.component.Factory;
import org.eclipse.wb.core.databinding.xsd.component.MethodParameter;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.description.CreationInvocationDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.resource.ResourceInfo;
import org.eclipse.wb.internal.core.model.description.rules.StandardBeanPropertiesRule;
import org.eclipse.wb.internal.core.nls.Messages;
import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.JavaDocUtils;
import org.eclipse.wb.internal.core.utils.reflect.ClassMap;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.UnmarshallerHandler;

/**
 * Helper for accessing descriptions of factories -
 * {@link FactoryMethodDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public class FactoryDescriptionHelper {
	/**
	 * The URI that should be used for all *.wbp-factory.xml files. For backwards
	 * compatibility, this namespace is used instead of any blank namespaces that
	 * are found while parsing.
	 */
	@Deprecated(forRemoval = true)
	private static final String DEFAULT_URI = "http://www.eclipse.org/wb/WBPComponent";

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private FactoryDescriptionHelper() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	private static final ClassMap<Map<String, FactoryMethodDescription>[]> m_descriptionMaps = ClassMap.create();

	/**
	 * Returns factory methods of given {@link Class} and its super classes.
	 *
	 * @return the {@link Map} signature -> {@link FactoryMethodDescription}.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, FactoryMethodDescription> getDescriptionsMap(AstEditor editor, Class<?> factoryClass,
			boolean forStatic) throws Exception {
		// prepare Map[static,non-static]
		Map<String, FactoryMethodDescription>[] signaturesMaps = m_descriptionMaps.get(factoryClass);
		if (signaturesMaps == null) {
			signaturesMaps = new Map[2];
			m_descriptionMaps.put(factoryClass, signaturesMaps);
		}
		// check cache
		int index = forStatic ? 0 : 1;
		Map<String, FactoryMethodDescription> signaturesMap = signaturesMaps[index];
		if (signaturesMap == null) {
			signaturesMap = new TreeMap<>();
			signaturesMaps[index] = signaturesMap;
			// this factory class methods
			{
				Map<String, FactoryMethodDescription> map = getDescriptionsMap0(editor, factoryClass, forStatic);
				signaturesMap.putAll(map);
			}
			// super factory class methods (cached)
			{
				Class<?> superFactoryClass = factoryClass.getSuperclass();
				if (superFactoryClass != null) {
					Map<String, FactoryMethodDescription> map = getDescriptionsMap(editor, superFactoryClass,
							forStatic);
					signaturesMap.putAll(map);
				}
			}
		}
		// done
		return signaturesMap;
	}

	/**
	 * Returns factory methods declared in given {@link Class} (but not in super
	 * classes).
	 *
	 * @return the {@link Map} signature -> {@link FactoryMethodDescription}.
	 */
	private static Map<String, FactoryMethodDescription> getDescriptionsMap0(AstEditor editor, Class<?> factoryClass,
			boolean forStatic) throws Exception {
		try {
			return getDescriptionsMap0Ex(editor, factoryClass, forStatic);
		} catch (Throwable e) {
			EditorState.get(editor).addWarning(new EditorWarning("Can not get factory methods for " + factoryClass, e));
			return new TreeMap<>();
		}
	}

	/**
	 * Implementation of {@link #getDescriptionsMap0(AstEditor, Class, boolean)}
	 * that can throw exceptions.
	 */
	private static Map<String, FactoryMethodDescription> getDescriptionsMap0Ex(AstEditor editor, Class<?> factoryClass,
			boolean forStatic) throws Exception {
		EditorState state = EditorState.get(editor);
		ILoadingContext context = EditorStateLoadingContext.get(state);
		// try to find cached map
		{
			Map<String, FactoryMethodDescription> signaturesMap = state.getFactorySignatures(factoryClass, forStatic);
			if (signaturesMap != null) {
				return signaturesMap;
			}
		}
		//
		String factoryClassName = factoryClass.getName();
		IType factoryType = editor.getJavaProject().findType(factoryClassName);
		if (factoryType == null) {
			return new TreeMap<>();
		}
		Boolean allMethodsAreFactories = null;
		List<FactoryMethodDescription> descriptions = new ArrayList<>();
		// read descriptions from XML
		{
			String descriptionName = factoryClassName.replace('.', '/') + ".wbp-factory.xml";
			ResourceInfo resourceInfo = DescriptionHelper.getResourceInfo(context, factoryClass, descriptionName);
			if (resourceInfo != null) {
				JAXBContext jaxbContext = ContextFactory.createContext();
				AtomicBoolean showDeprecationWarning = new AtomicBoolean();

				// Create the XMLFilter
				XMLFilter filter = new XMLFilterImpl() {
					@Override
					public void endElement(String uri, String localName, String qName) throws SAXException {
						String realUri = uri;
						if (realUri.isBlank()) {
							showDeprecationWarning.set(true);
							realUri = DEFAULT_URI;
						}
						super.endElement(realUri, localName, qName);
					}

					@Override
					public void startElement(String uri, String localName, String qName, Attributes atts)
							throws SAXException {
						String realUri = uri;
						if (realUri.isBlank()) {
							showDeprecationWarning.set(true);
							realUri = DEFAULT_URI;
						}
						super.startElement(realUri, localName, qName, atts);
					}
				};

				// Set the parent XMLReader on the XMLFilter
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				filter.setParent(xr);

				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				UnmarshallerHandler unmarshallerHandler = jaxbUnmarshaller.getUnmarshallerHandler();
				filter.setContentHandler(unmarshallerHandler);

				try (InputStream is = resourceInfo.getURL().openStream()) {
					filter.parse(new InputSource(is));
					Factory factory = (Factory) unmarshallerHandler.getResult();
					descriptions = process(factory, state, factoryClass);
					allMethodsAreFactories = factory.isAllMethodsAreFactories();
				} finally {
					if (showDeprecationWarning.get()) {
						String message = NLS.bind(Messages.FactoryDescriptionHelper_deprecatedNamespace,
								resourceInfo.getURL().getFile());
						DesignerPlugin.log(Status.warning(message));
					}
				}
			}
		}
		// prepare map: signature -> description
		Map<String, FactoryMethodDescription> signaturesMap = new TreeMap<>();
		for (FactoryMethodDescription description : descriptions) {
			signaturesMap.put(description.getSignature(), description);
		}
		// factory flag for not-wbp methods
		if (allMethodsAreFactories == null) {
			allMethodsAreFactories = hasFactorySuffix(factoryType) || hasFactoryTag(factoryType);
		}
		// if no methods from XML, may be no methods at all
		if (!allMethodsAreFactories.booleanValue() && descriptions.isEmpty() && !hasFactoryTagSource(factoryType)) {
			return new TreeMap<>();
		}
		// add descriptions for all methods, using JavaDoc
		{
			// prepare methods
			Method[] methods;
			IMethod[] modelMethods;
			{
				methods = factoryClass.getDeclaredMethods();
				// prepare signatures for "public static" methods
				String[] signatures = new String[methods.length];
				for (int methodIndex = 0; methodIndex < methods.length; methodIndex++) {
					Method factoryMethod = methods[methodIndex];
					// check modifiers
					{
						int modifiers = factoryMethod.getModifiers();
						if (!hasValidVisibility(editor, factoryMethod)) {
							continue;
						}
						if (forStatic && !java.lang.reflect.Modifier.isStatic(modifiers)) {
							continue;
						}
					}
					// check return type
					{
						Class<?> returnType = factoryMethod.getReturnType();
						// method that returns "primitive" can not be factory method
						if (returnType.isPrimitive()) {
							continue;
						}
						// special case - filter out getters
						if (factoryMethod.getParameterTypes().length == 0
								&& factoryMethod.getName().startsWith("get")) {
							continue;
						}
					}
					// OK, valid method
					signatures[methodIndex] = ReflectionUtils.getMethodSignature(factoryMethod);
				}
				// prepare model methods
				modelMethods = CodeUtils.findMethods(factoryType, signatures);
			}
			// prepare factory description for each method
			for (int methodIndex = 0; methodIndex < methods.length; methodIndex++) {
				Method factoryMethod = methods[methodIndex];
				IMethod modelMethod = modelMethods[methodIndex];
				if (modelMethod == null) {
					continue;
				}
				// prepare description
				FactoryMethodDescription description;
				{
					String signature = ReflectionUtils.getMethodSignature(factoryMethod);
					description = signaturesMap.get(signature);
					// create description if not exists
					if (description == null) {
						description = new FactoryMethodDescription(factoryClass);
						description.setName(factoryMethod.getName());
						description.setFactory(allMethodsAreFactories);
						signaturesMap.put(signature, description);
					}
				}
				// configure description
				description.setModelMethod(modelMethod);
				description.setReturnClass(factoryMethod.getReturnType());
				// check parameters
				{
					Class<?>[] parameterTypes = factoryMethod.getParameterTypes();
					for (int parameterIndex = 0; parameterIndex < parameterTypes.length; parameterIndex++) {
						Class<?> parameterType = parameterTypes[parameterIndex];
						// prepare parameter
						ParameterDescription parameterDescription;
						if (parameterIndex < description.getParameters().size()) {
							parameterDescription = description.getParameter(parameterIndex);
						} else {
							parameterDescription = new ParameterDescription();
							parameterDescription.setType(parameterType);
							description.addParameter(parameterDescription);
						}
					}
					//
					description.postProcess();
				}
				// try to mark as factory
				if (!description.isFactory()) {
					updateDescriptionsJavaDoc0(editor, description);
				}
			}
		}
		// remove incompatible instance/static methods
		for (Iterator<FactoryMethodDescription> I = signaturesMap.values().iterator(); I.hasNext();) {
			FactoryMethodDescription description = I.next();
			Method method = ReflectionUtils.getMethodBySignature(factoryClass, description.getSignature());
			int modifiers = method.getModifiers();
			if (forStatic ^ java.lang.reflect.Modifier.isStatic(modifiers)) {
				I.remove();
			}
		}
		// remove descriptions without "factory" flag
		for (Iterator<FactoryMethodDescription> I = signaturesMap.values().iterator(); I.hasNext();) {
			FactoryMethodDescription description = I.next();
			if (!description.isFactory()) {
				I.remove();
			}
		}
		// process JavaDoc
		for (FactoryMethodDescription description : signaturesMap.values()) {
			updateDescriptionsJavaDoc(editor, description);
		}
		// load icons
		for (Map.Entry<String, FactoryMethodDescription> entry : signaturesMap.entrySet()) {
			FactoryMethodDescription description = entry.getValue();
			// prepare icon
			ImageDescriptor icon;
			{
				String signature = entry.getKey();
				String signatureUnix = StringUtils.replaceChars(signature, "(,)", "___");
				String iconPath = factoryClassName.replace('.', '/') + "." + signatureUnix;
				icon = DescriptionHelper.getIcon(context, iconPath);
				description.setIcon(icon);
			}
		}
		// remember descriptions in cache
		state.putFactorySignatures(factoryClass, forStatic, signaturesMap);
		return signaturesMap;
	}

	/**
	 * @return <code>true</code> if given {@link Method} modifiers are valid to
	 *         consider this {@link Method} as possible factory methods. Usually we
	 *         require "public" visibility. However for "local" factory methods we
	 *         allow any visibility.
	 */
	private static boolean hasValidVisibility(AstEditor editor, Method factoryMethod) {
		// ignore visibility, if "local" factory method
		{
			String editorTypeName = editor.getModelUnit().findPrimaryType().getFullyQualifiedName();
			String factoryTypeName = factoryMethod.getDeclaringClass().getName();
			if (editorTypeName.equals(factoryTypeName)) {
				return true;
			}
		}
		// ensure "public" visibility
		int modifiers = factoryMethod.getModifiers();
		return java.lang.reflect.Modifier.isPublic(modifiers);
	}

	/**
	 * @return the {@link FactoryMethodDescription} for given method signature.
	 */
	public static FactoryMethodDescription getDescription(AstEditor editor, Class<?> factoryClass,
			String methodSignature, boolean forStatic) throws Exception {
		Map<String, FactoryMethodDescription> signaturesMap = getDescriptionsMap(editor, factoryClass, forStatic);
		return signaturesMap.get(methodSignature);
	}

	/**
	 * Finds {@link ICompilationUnit}'s with factories.
	 *
	 * @param thePackage the {@link IPackageFragment} to find factories in.
	 *
	 * @return the {@link ICompilationUnit}'s with factories.
	 */
	public static List<ICompilationUnit> getFactoryUnits(AstEditor editor, IPackageFragment thePackage)
			throws Exception {
		List<ICompilationUnit> factoryUnits = new ArrayList<>();
		//
		for (ICompilationUnit unit : thePackage.getCompilationUnits()) {
			String typeName;
			{
				IType primaryType = unit.findPrimaryType();
				if (primaryType == null) {
					continue;
				}
				typeName = primaryType.getFullyQualifiedName();
			}
			// "Factory" suffix
			if (typeName.endsWith("Factory")) {
				if (isFactoryClass(editor, typeName)) {
					factoryUnits.add(unit);
					continue;
				}
			}
			// factory tag in source
			{
				String source = unit.getSource();
				if (source != null && source.contains("@wbp.factory")) {
					if (isFactoryClass(editor, typeName)) {
						factoryUnits.add(unit);
						continue;
					}
				}
			}
			// factory description
			{
				String descriptionName = typeName.replace('.', '/') + ".wbp-factory.xml";
				ClassLoader classLoader = EditorState.get(editor).getEditorLoader();
				if (classLoader.getResource(descriptionName) != null) {
					if (isFactoryClass(editor, typeName)) {
						factoryUnits.add(unit);
						continue;
					}
				}
			}
		}
		//
		return factoryUnits;
	}

	/**
	 * @return <code>true</code> if class has factory methods.
	 */
	public static boolean isFactoryClass(final AstEditor editor, final String typeName) {
		return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
			@Override
			public Boolean runObject() throws Exception {
				ClassLoader classLoader = EditorState.get(editor).getEditorLoader();
				Class<?> clazz = classLoader.loadClass(typeName);
				return !getDescriptionsMap(editor, clazz, true).isEmpty()
						|| !getDescriptionsMap(editor, clazz, false).isEmpty();
			}
		}, false);
	}

	/**
	 * @return <code>true</code> if given {@link MethodInvocation} is invocation of
	 *         factory.
	 */
	public static boolean isFactoryInvocation(final AstEditor editor, final MethodInvocation invocation) {
		return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
			@Override
			public Boolean runObject() throws Exception {
				IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(invocation);
				Class<?> factoryClass = getFactoryClass(methodBinding);
				String signature = AstNodeUtils.getMethodSignature(methodBinding);
				return getDescriptionsMap(editor, factoryClass, true).containsKey(signature)
						|| getDescriptionsMap(editor, factoryClass, false).containsKey(signature);
			}

			private Class<?> getFactoryClass(IMethodBinding methodBinding) throws Exception {
				ITypeBinding factoryTypeBinding = methodBinding.getDeclaringClass();
				String factoryClassName = AstNodeUtils.getFullyQualifiedName(factoryTypeBinding, true);
				ClassLoader classLoader = EditorState.get(editor).getEditorLoader();
				return classLoader.loadClass(factoryClassName);
			}
		}, false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rules
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates the {@link FactoryMethodDescription}'s from the given
	 * {@link Factory}.
	 */
	private static List<FactoryMethodDescription> process(Factory factory, EditorState state, Class<?> declaringClass)
			throws Exception {
		List<FactoryMethodDescription> factoryMethodDescriptions = new ArrayList<>();
		Boolean allMethodsAreFactories;
		// allMethodsAreFactories flag
		{
			allMethodsAreFactories = factory.isAllMethodsAreFactories();
			if (allMethodsAreFactories == null) {
				allMethodsAreFactories = true;
			}
		}
		// methods
		{
			for (Factory.Method method : factory.getMethod()) {
				FactoryMethodDescription factoryMethodDescription = new FactoryMethodDescription(declaringClass);
				factoryMethodDescription.setFactory(allMethodsAreFactories);

				acceptSafe(factoryMethodDescription, method.getName(), FactoryMethodDescription::setName);
				acceptSafe(factoryMethodDescription, method.isExecutable(), FactoryMethodDescription::setExecutable);
				acceptSafe(factoryMethodDescription, method.isFactory(), FactoryMethodDescription::setFactory);
				acceptSafe(factoryMethodDescription, method.getOrder(),
						FactoryMethodDescription::setOrderSpecification);

				for (MethodParameter parameter : method.getParameter()) {
					ComponentDescriptionHelper.addParametersRules(factoryMethodDescription, parameter, state);
				}

				// invocation
				{
					for (Factory.Method.Invocation invocation : method.getInvocation()) {
						CreationInvocationDescription invocationDescription = new CreationInvocationDescription();
						acceptSafe(invocationDescription, invocation.getSignature(),
								CreationInvocationDescription::setSignature);
						// arguments
						acceptSafe(invocationDescription, invocation.getContent(),
								CreationInvocationDescription::setArguments);
						// add
						factoryMethodDescription.addInvocation(invocationDescription);
					}
				}
				// name text
				{
					acceptSafe(factoryMethodDescription, method.getPresentationName(),
							FactoryMethodDescription::setPresentationName);
				}
				// untyped parameters
				{
					Factory.Method.Parameters parameters = method.getParameters();
					if (parameters != null) {
						for (Factory.Method.Parameters.Parameter parameter : parameters.getParameter()) {
							factoryMethodDescription.addParameter(parameter.getName(), parameter.getValue());
						}
					}
				}
				// description with HTML support
				acceptSafe(factoryMethodDescription, method.getDescription(), FactoryMethodDescription::setDescription);
				factoryMethodDescription.postProcess();
				factoryMethodDescriptions.add(factoryMethodDescription);
			}
		}
		return factoryMethodDescriptions;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Description from JavaDoc
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String WBP_FACTORY_TAG = "@wbp.factory";
	private static final String WBP_FACTORY_PARAMETER_SOURCE_TAG = "@wbp.factory.parameter.source ";
	private static final String WBP_FACTORY_PARAMETER_PROPERTY_TAG = "@wbp.factory.parameter.property ";
	private static final String WBP_FACTORY_PARAMETERS_NO_BINDING = "@wbp.factory.parameters.noBinding";

	/**
	 * @return <code>true</code> if given {@link MethodDeclaration} is marked as
	 *         factory method.
	 */
	public static boolean isFactoryMethod(MethodDeclaration methodDeclaration) {
		return AstNodeUtils.hasJavaDocTag(methodDeclaration, WBP_FACTORY_TAG);
	}

	/**
	 * @return <code>true</code> if {@link IType} has name that ends with "Factory",
	 *         so we consider all its methods as factories.
	 */
	private static boolean hasFactorySuffix(IType type) throws Exception {
		return type.getElementName().endsWith("Factory");
	}

	/**
	 * @return <code>true</code> if given {@link IType} has global, class level,
	 *         factory tag, so all methods are marked as factories.
	 */
	private static boolean hasFactoryTag(IType type) throws Exception {
		// check quickly, is IType is from source and has factory tags in source
		if (!hasFactoryTagSource(type)) {
			return false;
		}
		// OK, look more precise
		List<String> javaDocLines = JavaDocUtils.getJavaDocLines(type, false);
		if (javaDocLines != null) {
			for (String javaDocLine : javaDocLines) {
				javaDocLine = javaDocLine.trim();
				// factory flag
				if (javaDocLine.equals(WBP_FACTORY_TAG)) {
					return true;
				}
			}
		}
		// no factory tag
		return false;
	}

	/**
	 * @return <code>true</code> if given {@link IType} has factory tag at all,
	 *         global or for some specific method.
	 */
	private static boolean hasFactoryTagSource(IType type) throws Exception {
		if (type.isBinary()) {
			return false;
		}
		if (!type.getCompilationUnit().isConsistent()) {
			return false;
		}
		return type.getSource().contains(WBP_FACTORY_TAG);
	}

	/**
	 * Updates {@link FactoryMethodDescription} using JavaDoc's in source of factory
	 * class.
	 */
	private static void updateDescriptionsJavaDoc0(AstEditor editor, FactoryMethodDescription description)
			throws Exception {
		// prepare JavaDoc lines
		IMethod modelMethod = description.getModelMethod();
		List<String> javaDocLines = JavaDocUtils.getJavaDocLines(modelMethod, false);
		// check each JavaDoc line
		if (javaDocLines != null) {
			for (String javaDocLine : javaDocLines) {
				javaDocLine = javaDocLine.trim();
				// factory flag
				if (javaDocLine.equals(WBP_FACTORY_TAG)) {
					description.setFactory(true);
				}
			}
		}
	}

	/**
	 * Updates {@link FactoryMethodDescription} using JavaDoc's in source of factory
	 * class.
	 */
	private static void updateDescriptionsJavaDoc(AstEditor editor, FactoryMethodDescription description)
			throws Exception {
		// prepare JavaDoc lines
		List<String> javaDocLines;
		String[] parameterNames;
		{
			IMethod modelMethod = description.getModelMethod();
			parameterNames = modelMethod.getParameterNames();
			javaDocLines = JavaDocUtils.getJavaDocLines(modelMethod, false);
		}
		// set parameter names
		{
			List<ParameterDescription> parameters = description.getParameters();
			for (int i = 0; i < parameters.size(); i++) {
				ParameterDescription parameter = parameters.get(i);
				// set name
				String name = parameterNames[i];
				parameter.setName(name);
				// parameter with name "parent" is parent
				if ("parent".equals(name)) {
					parameter.setParent(true);
				}
			}
		}
		// prepare standard properties
		List<PropertyDescriptor> propertyDescriptors;
		{
			Class<?> componentClass = description.getReturnClass();
			BeanInfo beanInfo = ReflectionUtils.getBeanInfo(componentClass);
			propertyDescriptors = ReflectionUtils.getPropertyDescriptors(beanInfo, componentClass);
		}
		// check each JavaDoc line
		boolean doAutoBindings = true;
		if (javaDocLines != null) {
			for (String javaDocLine : javaDocLines) {
				javaDocLine = javaDocLine.trim();
				// noBinding flag
				if (javaDocLine.equals(WBP_FACTORY_PARAMETERS_NO_BINDING)) {
					doAutoBindings = false;
				}
				// parameter default source
				if (javaDocLine.startsWith(WBP_FACTORY_PARAMETER_SOURCE_TAG)) {
					String parameterString = javaDocLine.substring(WBP_FACTORY_PARAMETER_SOURCE_TAG.length()).trim();
					// prepare parameter
					ParameterDescription parameter = getJavaDocParameter(description, parameterNames, parameterString);
					// set default source
					{
						String parameterSource = StringUtilities.removeFirstWord(parameterString);
						parameter.setDefaultSource(parameterSource);
					}
				}
				// parameter property
				if (javaDocLine.startsWith(WBP_FACTORY_PARAMETER_PROPERTY_TAG)) {
					String parameterString = javaDocLine.substring(WBP_FACTORY_PARAMETER_PROPERTY_TAG.length()).trim();
					// prepare parameter
					ParameterDescription parameter = getJavaDocParameter(description, parameterNames, parameterString);
					// set property
					if (parameter.getProperty() == null) {
						String propertyTitle = StringUtilities.removeFirstWord(parameterString);
						String propertyId = getStandardPropertyId(propertyDescriptors, propertyTitle);
						parameter.setProperty(propertyId);
					}
				}
			}
		}
		// do auto bindings
		if (doAutoBindings) {
			for (ParameterDescription parameter : description.getParameters()) {
				if (parameter.getProperty() == null) {
					String property = getStandardPropertyId(propertyDescriptors, parameter.getName());
					parameter.setProperty(property);
				}
			}
		}
	}

	/**
	 * @return the {@link ParameterDescription} with name specified as first word of
	 *         <code>parameterString</code>.
	 */
	private static ParameterDescription getJavaDocParameter(FactoryMethodDescription description,
			String[] parameterNames, String parameterString) {
		String parameterName = StringUtils.split(parameterString)[0];
		// prepare parameter index
		int parameterIndex;
		if (StringUtils.isNumeric(parameterName)) {
			parameterIndex = Integer.parseInt(parameterName);
		} else {
			parameterIndex = ArrayUtils.indexOf(parameterNames, parameterName);
		}
		// validate index
		if (parameterIndex < 0 || parameterIndex >= parameterNames.length) {
			throw new IllegalArgumentException("Invalid parameter string " + parameterName);
		}
		// OK, return parameter description
		return description.getParameter(parameterIndex);
	}

	/**
	 * @return the id of standard bean property (created using
	 *         {@link StandardBeanPropertiesRule}) with given title or
	 *         <code>null</code> if no such property exists.
	 */
	private static String getStandardPropertyId(List<PropertyDescriptor> propertyDescriptors, String title) {
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			// prepare setter
			Method setMethod = ReflectionUtils.getWriteMethod(propertyDescriptor);
			if (setMethod == null) {
				continue;
			}
			// check title
			if (title.equals(propertyDescriptor.getDisplayName())) {
				return StandardBeanPropertiesRule.getId(setMethod);
			}
		}
		// no such property
		return null;
	}
}
