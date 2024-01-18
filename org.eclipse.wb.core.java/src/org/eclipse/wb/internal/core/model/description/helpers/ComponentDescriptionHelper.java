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

import org.eclipse.wb.core.databinding.xsd.component.Component;
import org.eclipse.wb.core.databinding.xsd.component.Component.Constructors;
import org.eclipse.wb.core.databinding.xsd.component.Component.MethodProperty;
import org.eclipse.wb.core.databinding.xsd.component.Component.MethodSingleProperty;
import org.eclipse.wb.core.databinding.xsd.component.Component.PropertiesAdvanced;
import org.eclipse.wb.core.databinding.xsd.component.Component.PropertiesHidden;
import org.eclipse.wb.core.databinding.xsd.component.Component.PropertiesNoDefaultValue;
import org.eclipse.wb.core.databinding.xsd.component.Component.PropertiesNormal;
import org.eclipse.wb.core.databinding.xsd.component.Component.PropertiesPreferred;
import org.eclipse.wb.core.databinding.xsd.component.Component.PropertyTag;
import org.eclipse.wb.core.databinding.xsd.component.ConfigurablePropertyType;
import org.eclipse.wb.core.databinding.xsd.component.ContextFactory;
import org.eclipse.wb.core.databinding.xsd.component.Creation;
import org.eclipse.wb.core.databinding.xsd.component.ExposingRuleType;
import org.eclipse.wb.core.databinding.xsd.component.ExposingRulesType;
import org.eclipse.wb.core.databinding.xsd.component.MethodParameter;
import org.eclipse.wb.core.databinding.xsd.component.MethodsOrderType;
import org.eclipse.wb.core.databinding.xsd.component.MorphingType;
import org.eclipse.wb.core.databinding.xsd.component.ParameterBaseType;
import org.eclipse.wb.core.databinding.xsd.component.PropertyConfiguration;
import org.eclipse.wb.core.databinding.xsd.component.PropertyConfigurationElements;
import org.eclipse.wb.core.databinding.xsd.component.TagType;
import org.eclipse.wb.core.databinding.xsd.component.TypeParameterType;
import org.eclipse.wb.core.databinding.xsd.component.TypeParametersType;
import org.eclipse.wb.internal.core.model.description.AbstractInvocationDescription;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ComponentDescriptionKey;
import org.eclipse.wb.internal.core.model.description.ConfigurablePropertyDescription;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.description.CreationDescription;
import org.eclipse.wb.internal.core.model.description.CreationInvocationDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.IDescriptionProcessor;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.internal.AbstractConfigurableDescription;
import org.eclipse.wb.internal.core.model.description.internal.PropertyEditorDescription;
import org.eclipse.wb.internal.core.model.description.resource.ClassResourceInfo;
import org.eclipse.wb.internal.core.model.description.resource.ResourceInfo;
import org.eclipse.wb.internal.core.model.description.rules.ConfigurableObjectListParameterRule;
import org.eclipse.wb.internal.core.model.description.rules.ConfigurableObjectParameterRule;
import org.eclipse.wb.internal.core.model.description.rules.CreationTagRule;
import org.eclipse.wb.internal.core.model.description.rules.CreationTypeParametersRule;
import org.eclipse.wb.internal.core.model.description.rules.ExposingRulesRule;
import org.eclipse.wb.internal.core.model.description.rules.MethodOrderDefaultRule;
import org.eclipse.wb.internal.core.model.description.rules.MethodOrderMethodRule;
import org.eclipse.wb.internal.core.model.description.rules.MethodOrderMethodsRule;
import org.eclipse.wb.internal.core.model.description.rules.MethodPropertyRule;
import org.eclipse.wb.internal.core.model.description.rules.MethodTagRule;
import org.eclipse.wb.internal.core.model.description.rules.MethodsOperationRule;
import org.eclipse.wb.internal.core.model.description.rules.ModelClassRule;
import org.eclipse.wb.internal.core.model.description.rules.MorphingNoInheritRule;
import org.eclipse.wb.internal.core.model.description.rules.MorphingTargetRule;
import org.eclipse.wb.internal.core.model.description.rules.ParameterTagRule;
import org.eclipse.wb.internal.core.model.description.rules.PropertyCategoryRule;
import org.eclipse.wb.internal.core.model.description.rules.PropertyDefaultRule;
import org.eclipse.wb.internal.core.model.description.rules.PropertyGetterRule;
import org.eclipse.wb.internal.core.model.description.rules.PropertyTagRule;
import org.eclipse.wb.internal.core.model.description.rules.PublicFieldPropertiesRule;
import org.eclipse.wb.internal.core.model.description.rules.SetClassPropertyRule;
import org.eclipse.wb.internal.core.model.description.rules.StandardBeanPropertiesAdvancedRule;
import org.eclipse.wb.internal.core.model.description.rules.StandardBeanPropertiesHiddenRule;
import org.eclipse.wb.internal.core.model.description.rules.StandardBeanPropertiesNoDefaultValueRule;
import org.eclipse.wb.internal.core.model.description.rules.StandardBeanPropertiesNormalRule;
import org.eclipse.wb.internal.core.model.description.rules.StandardBeanPropertiesPreferredRule;
import org.eclipse.wb.internal.core.model.description.rules.StandardBeanPropertiesRule;
import org.eclipse.wb.internal.core.model.description.rules.StandardBeanPropertyTagRule;
import org.eclipse.wb.internal.core.model.description.rules.ToolkitRule;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.AstParser;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ClassMap;
import org.eclipse.wb.internal.core.utils.reflect.IntrospectionHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jface.resource.ImageDescriptor;

import org.apache.commons.digester3.Rule;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;

/**
 * Helper for accessing descriptions of components -
 * {@link ComponentDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ComponentDescriptionHelper {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private ComponentDescriptionHelper() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	private static final ClassMap<ComponentDescription> m_getDescription_Class = ClassMap.create();

	/**
	 * Returns the factory-method specific {@link ComponentDescription}.
	 * <p>
	 * Sometimes you want to add specific tweaks for component, when it is created
	 * using factory. For example - hide some properties, copy factory properties,
	 * etc.
	 */
	public static ComponentDescription getDescription(AstEditor editor,
			FactoryMethodDescription factoryMethodDescription) throws Exception {
		Class<?> componentClass = factoryMethodDescription.getReturnClass();
		// prepare description key
		ComponentDescriptionKey key;
		{
			String signature = factoryMethodDescription.getSignature();
			String signatureUnix = StringUtils.replaceChars(signature, "(,)", "___");
			Class<?> declaringClass = factoryMethodDescription.getDeclaringClass();
			ComponentDescriptionKey declaringKey = new ComponentDescriptionKey(declaringClass);
			key = new ComponentDescriptionKey(componentClass, declaringKey, signatureUnix);
		}
		// get key specific description
		return getKeySpecificDescription(editor, componentClass, key);
	}

	/**
	 * @param editor          the {@link AstEditor} in context of which we work now.
	 * @param hostDescription the {@link ComponentDescription} which child component
	 *                        we expose.
	 * @param method          the {@link Method} that exposes component.
	 *
	 * @return the {@link ComponentDescription} of component exposed using given
	 *         {@link Method}.
	 * @throws Exception if no {@link ComponentDescription} can be found.
	 */
	public static ComponentDescription getDescription(AstEditor editor, ComponentDescription hostDescription,
			Method method) throws Exception {
		Class<?> componentClass = method.getReturnType();
		// prepare description key
		ComponentDescriptionKey key;
		{
			String suffix = method.getName();
			key = new ComponentDescriptionKey(componentClass, hostDescription.getKey(), suffix);
		}
		// get key specific description
		return getKeySpecificDescription(editor, componentClass, key);
	}

	/**
	 * @param editor          the {@link AstEditor} in context of which we work now.
	 * @param hostDescription the {@link ComponentDescription} that has method with
	 *                        given parameter.
	 * @param parameter       the {@link SingleVariableDeclaration} parameter that
	 *                        considered as component.
	 *
	 * @return the {@link ComponentDescription} of component represented by given
	 *         {@link SingleVariableDeclaration}.
	 * @throws Exception if no {@link ComponentDescription} can be found.
	 */
	public static ComponentDescription getDescription(AstEditor editor, ComponentDescription hostDescription,
			SingleVariableDeclaration parameter) throws Exception {
		// prepare parameter Class
		Class<?> parameterClass;
		{
			String parameterClassName = AstNodeUtils.getFullyQualifiedName(parameter.getType(), true);
			parameterClass = EditorState.get(editor).getEditorLoader().loadClass(parameterClassName);
		}
		// prepare suffix
		String suffix;
		{
			MethodDeclaration methodDeclaration = (MethodDeclaration) parameter.getParent();
			String signature = AstNodeUtils.getMethodSignature(methodDeclaration);
			String signatureUnix = StringUtils.replaceChars(signature, "(,)", "___");
			int parameterIndex = DomGenerics.parameters(methodDeclaration).indexOf(parameter);
			suffix = signatureUnix + "." + parameterIndex;
		}
		// prepare DescriptionInfo's for parameter in inheritance hierarchy
		List<ClassResourceInfo> additionalDescriptions = new ArrayList<>();
		{
			Class<?> hostComponentClass = hostDescription.getComponentClass();
			List<Class<?>> types = ReflectionUtils.getSuperHierarchy(hostComponentClass);
			Collections.reverse(types);
			for (Class<?> type : types) {
				ComponentDescriptionKey hostKey = new ComponentDescriptionKey(type);
				// prepare specific ResourceInfo
				ResourceInfo resourceInfo;
				{
					EditorState state = EditorState.get(editor);
					ILoadingContext context = EditorStateLoadingContext.get(state);
					String descriptionPath = hostKey.getName() + "." + suffix + ".wbp-component.xml";
					resourceInfo = DescriptionHelper.getResourceInfo(context, type, descriptionPath);
				}
				// add specific DescriptionInfo
				if (resourceInfo != null) {
					ClassResourceInfo descriptionInfo = new ClassResourceInfo(parameterClass, resourceInfo);
					additionalDescriptions.add(descriptionInfo);
				}
			}
		}
		// get key specific description
		if (additionalDescriptions.isEmpty()) {
			return getDescription(editor, parameterClass);
		} else {
			ComponentDescriptionKey key = new ComponentDescriptionKey(parameterClass, hostDescription.getKey(), suffix);
			return getDescription0(editor, key, additionalDescriptions);
		}
	}

	/**
	 * @param editor         the {@link AstEditor} in context of which we work now.
	 * @param componentClass the {@link Class} of component to get description.
	 *
	 * @return the {@link ComponentDescription} of component with given
	 *         {@link Class}.
	 * @throws Exception if no {@link ComponentDescription} can be found.
	 */
	public static ComponentDescription getDescription(AstEditor editor, Class<?> componentClass) throws Exception {
		ComponentDescription description = m_getDescription_Class.get(componentClass);
		if (description == null) {
			description = getDescription0(editor, componentClass);
			m_getDescription_Class.put(componentClass, description);
		}
		return description;
	}

	/**
	 * Implementation for {@link #getDescription(AstEditor, Class)}.
	 */
	private static ComponentDescription getDescription0(AstEditor editor, Class<?> componentClass) throws Exception {
		// we should use component class that can be loaded, for example ignore
		// anonymous classes
		for (;; componentClass = componentClass.getSuperclass()) {
			String componentClassName = componentClass.getName();
			// stop if not an inner class
			int index = componentClassName.indexOf('$');
			if (index == -1) {
				break;
			}
			// stop if anonymous implementation of some interface
			if (componentClass.getInterfaces().length != 0) {
				break;
			}
			// stop if not an anonymous class
			String innerPart = componentClassName.substring(index + 1);
			if (!StringUtils.isNumeric(innerPart)) {
				break;
			}
		}
		// OK, get description
		ComponentDescriptionKey key = new ComponentDescriptionKey(componentClass);
		return getDescription0(editor, key, Collections.emptyList());
	}

	/**
	 * @param editor             the {@link AstEditor} in context of which we work
	 *                           now.
	 * @param componentClassName the name of {@link Class} of component to get
	 *                           description.
	 *
	 * @return the {@link ComponentDescription} of component with given
	 *         {@link Class}.
	 * @throws Exception if no {@link ComponentDescription} can be found.
	 */
	public static ComponentDescription getDescription(AstEditor editor, String componentClassName) throws Exception {
		Class<?> componentClass = EditorState.get(editor).getEditorLoader().loadClass(componentClassName);
		return getDescription(editor, componentClass);
	}

	/**
	 * @return the {@link ComponentDescription} that is specific to given
	 *         {@link ComponentDescriptionKey}, if exists, or just
	 *         {@link ComponentDescription} for given component {@link Class}.
	 */
	private static ComponentDescription getKeySpecificDescription(AstEditor editor, Class<?> componentClass,
			ComponentDescriptionKey key) throws Exception {
		// prepare optional key-specific ResourceInfo
		ResourceInfo resourceInfo;
		{
			EditorState state = EditorState.get(editor);
			ILoadingContext context = EditorStateLoadingContext.get(state);
			String descriptionPath = key.getName() + ".wbp-component.xml";
			resourceInfo = DescriptionHelper.getResourceInfo(context, componentClass, descriptionPath);
		}
		// if no key-specific, use pure type description
		if (resourceInfo == null) {
			return getDescription(editor, componentClass);
		}
		// OK, get key-specific description
		ClassResourceInfo descriptionInfo = new ClassResourceInfo(componentClass, resourceInfo);
		return getDescription0(editor, key, List.of(descriptionInfo));
	}

	/**
	 * @param editor                     the {@link AstEditor} in context of which
	 *                                   we work now.
	 * @param key                        the {@link ComponentDescriptionKey} of
	 *                                   requested {@link ComponentDescription}.
	 * @param additionalDescriptionInfos additional {@link ClassResourceInfo}'s to
	 *                                   parse after {@link ClassResourceInfo}'s
	 *                                   collected for component {@link Class}. May
	 *                                   be empty, but not <code>null</code>.
	 *
	 * @return the {@link ComponentDescription} of component with given
	 *         {@link Class}.
	 * @throws Exception if no {@link ComponentDescription} can be found.
	 */
	private static ComponentDescription getDescription0(AstEditor editor, ComponentDescriptionKey key,
			List<ClassResourceInfo> additionalDescriptionInfos) throws Exception {
		EditorState state = EditorState.get(editor);
		ILoadingContext context = EditorStateLoadingContext.get(state);
		Class<?> componentClass = key.getComponentClass();
		//
		try {
			// prepare result description
			ComponentDescription componentDescription = new ComponentDescription(key);
			addConstructors(editor.getJavaProject(), componentDescription);
			componentDescription.setBeanInfo(ReflectionUtils.getBeanInfo(componentClass));
			componentDescription.setBeanDescriptor(new IntrospectionHelper(componentClass).getBeanDescriptor());
			// prepare list of description resources, from generic to specific
			LinkedList<ClassResourceInfo> descriptionInfos;
			{
				descriptionInfos = new LinkedList<>();
				DescriptionHelper.addDescriptionResources(descriptionInfos, context, componentClass);
				Assert.isTrueException(!descriptionInfos.isEmpty(), ICoreExceptionConstants.DESCRIPTION_NO_DESCRIPTIONS,
						componentClass.getName());
				// at last append additional description resource
				descriptionInfos.addAll(additionalDescriptionInfos);
			}
			// read descriptions from generic to specific
			for (ClassResourceInfo descriptionInfo : descriptionInfos) {
				ResourceInfo resourceInfo = descriptionInfo.resource;
				// read next description
				{
					componentDescription.setCurrentClass(descriptionInfo.clazz);
					JAXBContext jaxbContext = ContextFactory.createContext();
					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
					Component component = (Component) jaxbUnmarshaller.unmarshal(resourceInfo.getURL());
					process(componentDescription, component, editor);
				}
				// clear parts that can not be inherited
				if (descriptionInfo.clazz != componentClass) {
					componentDescription.clearCreations();
					componentDescription.setDescription(null);
				}
			}
			// set toolkit
			if (componentDescription.getToolkit() == null) {
				for (int i = descriptionInfos.size() - 1; i >= 0; i--) {
					ClassResourceInfo descriptionInfo = descriptionInfos.get(i);
					ToolkitDescription toolkit = descriptionInfo.resource.getToolkit();
					if (toolkit != null) {
						componentDescription.setToolkit(toolkit);
						break;
					}
				}
				Assert.isTrueException(componentDescription.getToolkit() != null,
						ICoreExceptionConstants.DESCRIPTION_NO_TOOLKIT, componentClass.getName());
			}
			// icon, default creation
			setIcon(context, componentDescription, componentClass);
			configureDefaultCreation(componentDescription);
			// final operations
			{
				Assert.isNotNull(componentDescription.getModelClass());
				componentDescription.joinProperties();
			}
			// add to caches
			if (key.isPureComponent() && !"true".equals(componentDescription.getParameter("dontCacheDescription"))
					&& shouldCacheDescriptions_inPackage(descriptionInfos.getLast(), componentClass)) {
				componentDescription.setCached(true);
			}
			// mark for caching presentation
			if (shouldCachePresentation(descriptionInfos.getLast(), componentClass)) {
				componentDescription.setPresentationCached(true);
			}
			// use processors
			for (IDescriptionProcessor processor : getDescriptionProcessors()) {
				processor.process(editor, componentDescription);
			}
			// well, we have result
			return componentDescription;
		} catch (Exception e) {
			throw new DesignerException(ICoreExceptionConstants.DESCRIPTION_LOAD_ERROR, e,
					componentClass.getName());
		}
	}

	/**
	 * Configures default {@link CreationDescription} with valid source.
	 */
	private static void configureDefaultCreation(ComponentDescription componentDescription) {
		Class<?> componentClass = componentDescription.getComponentClass();
		// prepare shortest constructor
		Constructor<?> constructor = ReflectionUtils.getShortestConstructor(componentClass);
		if (constructor == null) {
			return;
		}
		// set default creation
		String source = getDefaultConstructorInvocation(constructor);
		CreationDescription creationDefault = new CreationDescription(componentDescription, null, null);
		creationDefault.setSource(source);
		componentDescription.setCreationDefault(creationDefault);
	}

	/**
	 * TODO move into {@link ReflectionUtils}.
	 *
	 * @return the source for creating {@link Object} using given
	 *         {@link Constructor} with values default for type of each argument.
	 */
	public static String getDefaultConstructorInvocation(Constructor<?> constructor) {
		// prepare Class
		Class<?> componentClass = constructor.getDeclaringClass();
		String componentClassName = ReflectionUtils.getCanonicalName(componentClass);
		// prepare arguments
		String arguments;
		{
			StringBuilder buffer = new StringBuilder();
			for (Class<?> parameter : constructor.getParameterTypes()) {
				String parameterName = ReflectionUtils.getCanonicalName(parameter);
				buffer.append(AstParser.getDefaultValue(parameterName));
				buffer.append(", ");
			}
			arguments = StringUtils.removeEnd(buffer.toString(), ", ");
		}
		// prepare source
		return "new " + componentClassName + "(" + arguments + ")";
	}

	/**
	 * Sets icon for {@link ComponentDescription}.
	 *
	 * @param context              the {@link EditorState} to access environment.
	 * @param componentDescription the {@link ComponentDescription} to set icon for.
	 * @param currentClass         the {@link Class} to check for icon.
	 */
	private static void setIcon(ILoadingContext context, ComponentDescription componentDescription,
			Class<?> currentClass) throws Exception {
		if (currentClass != null) {
			// check current Class
			if (componentDescription.getIcon() == null) {
				ImageDescriptor icon = DescriptionHelper.getIcon(context, currentClass);
				if (icon != null) {
					componentDescription.setIcon(icon);
					return;
				}
			}
			// check interfaces
			for (Class<?> interfaceClass : currentClass.getInterfaces()) {
				if (componentDescription.getIcon() == null) {
					setIcon(context, componentDescription, interfaceClass);
				}
			}
			// check super Class
			if (componentDescription.getIcon() == null) {
				setIcon(context, componentDescription, currentClass.getSuperclass());
			}
		}
	}

	/**
	 * Ensures that {@link AbstractInvocationDescription} is fully initialized. See
	 * {@link AbstractInvocationDescription#setInitialized(boolean)} for more
	 * information.
	 */
	public static void ensureInitialized(final IJavaProject javaProject,
			final AbstractInvocationDescription methodDescription) {
		if (!methodDescription.isInitialized()) {
			methodDescription.setInitialized(true);
			// do initialize
			ExecutionUtils.runIgnore(new RunnableEx() {
				@Override
				public void run() throws Exception {
					IMethod method = CodeUtils.findMethod(javaProject, methodDescription.getDeclaringClass().getName(),
							methodDescription.getSignature());
					if (method != null) {
						String[] parameterNames = method.getParameterNames();
						for (ParameterDescription parameter : methodDescription.getParameters()) {
							if (parameter.getName() == null) {
								int parameterIndex = parameter.getIndex();
								String parameterName = parameterNames[parameterIndex];
								parameter.setName(parameterName);
							}
						}
					}
				}
			});
		}
	}

	/**
	 * Adds {@link ConstructorDescription} for given {@link ComponentDescription}.
	 */
	private static void addConstructors(IJavaProject javaProject, ComponentDescription componentDescription)
			throws Exception {
		Class<?> componentClass = componentDescription.getComponentClass();
		for (Constructor<?> constructor : componentClass.getDeclaredConstructors()) {
			constructor.setAccessible(true);
			ConstructorDescription constructorDescription = new ConstructorDescription(componentClass);
			// add parameter descriptions of constructor
			for (Class<?> parameterType : constructor.getParameterTypes()) {
				addParameter(constructorDescription, parameterType);
			}
			// OK, add constructor description
			constructorDescription.postProcess();
			componentDescription.addConstructor(constructorDescription);
		}
	}

	private static void addParameter(AbstractInvocationDescription description, Class<?> parameterType)
			throws Exception {
		ParameterDescription parameterDescription = new ParameterDescription();
		parameterDescription.setType(parameterType);
		description.addParameter(parameterDescription);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rules
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Fills the given {@link ComponentDescription} with the data from the given
	 * {@link Component} model. The component corresponds to a single
	 * {@link wbp-component.xml} file, which has been read via JAXB.
	 */
	private static void process(ComponentDescription componentDescription, Component component, AstEditor editor)
			throws Exception {
		EditorState state = EditorState.get(editor);
		ILoadingContext context = EditorStateLoadingContext.get(state);
		acceptSafe(componentDescription, component.getToolkit(), new ToolkitRule());
		acceptSafe(componentDescription, component.getModel(), new ModelClassRule());
		// methods
		{
			Component.Methods methods = component.getMethods();
			if (methods != null) {
				for (Component.Methods.Method method : methods.getMethod()) {
					Class<?> componentClass = componentDescription.getComponentClass();
					MethodDescription methodDescription = new MethodDescription(componentClass);
					methodDescription.setName(method.getName());
					acceptSafe(methodDescription, method.getOrder(), MethodDescription::setOrderSpecification);
					acceptSafe(methodDescription, method.isExecutable(), MethodDescription::setExecutable);
					for (TagType tag : method.getTag()) {
						acceptSafe(methodDescription, tag, new MethodTagRule());
					}
					for (MethodParameter parameter : method.getParameter()) {
						addParametersRules(methodDescription, parameter, state);
					}
					methodDescription.postProcess();
					String signature = methodDescription.getSignature();
					Method javaMethod = ReflectionUtils.getMethodBySignature(componentClass, signature);
					Assert.isNotNull2(javaMethod, "No such method {0}.{1} during parsing {2}", componentClass.getName(),
							signature, componentDescription.getCurrentClass().getName());
					methodDescription.setReturnClass(javaMethod.getReturnType());
					componentDescription.addMethod(methodDescription);
				}
			}
		}
		// standard bean properties
		{
			acceptSafe(componentDescription, component.getStandardBeanProperties(), new StandardBeanPropertiesRule());
			for (PropertiesPreferred properties : component.getPropertiesPreferred()) {
				acceptSafe(componentDescription, properties, new StandardBeanPropertiesPreferredRule());
			}
			for (PropertiesNormal properties : component.getPropertiesNormal()) {
				acceptSafe(componentDescription, properties, new StandardBeanPropertiesNormalRule());
			}
			for (PropertiesAdvanced properties : component.getPropertiesAdvanced()) {
				acceptSafe(componentDescription, properties, new StandardBeanPropertiesAdvancedRule());
			}
			for (PropertiesHidden properties : component.getPropertiesHidden()) {
				acceptSafe(componentDescription, properties, new StandardBeanPropertiesHiddenRule());
			}
			for (PropertiesNoDefaultValue properties : component.getPropertiesNoDefaultValue()) {
				acceptSafe(componentDescription, properties, new StandardBeanPropertiesNoDefaultValueRule());
			}
			for (PropertyTag propertyTag : component.getPropertyTag()) {
				acceptSafe(componentDescription, propertyTag, new StandardBeanPropertyTagRule());
			}
			{
				for (Component.MethodSingleProperty singleProperty : component.getMethodSingleProperty()) {
					GenericPropertyDescription propertyDescription = getGenericPropertyDescription(componentDescription,
							singleProperty);
					addPropertyConfigurationRules(componentDescription, propertyDescription, singleProperty, state);
				}
			}
			for (MethodProperty methodProperty : component.getMethodProperty()) {
				acceptSafe(componentDescription, methodProperty, new MethodPropertyRule(editor.getJavaProject()));
			}
		}
		// public field properties
		{
			acceptSafe(componentDescription, component.getPublicFieldProperties(), new PublicFieldPropertiesRule());
		}
		// component order
		{
			acceptSafe(componentDescription, component.getOrder(), ComponentDescription::setOrder);
		}
		// creations
		{
			for (Creation creation : component.getCreation()) {
				CreationDescription creationDescription = getCreationDescription(componentDescription, creation,
						context);
				addCreationRules(creationDescription, creation);
				for (TagType tagType : creation.getTag()) {
					acceptSafe(creationDescription, tagType, new CreationTagRule());
				}
				TypeParametersType typeParameters = creation.getTypeParameters();
				if (typeParameters != null) {
					for (TypeParameterType typeParameter : typeParameters.getTypeParameter()) {
						acceptSafe(creationDescription, typeParameter, new CreationTypeParametersRule());
					}
				}
				componentDescription.addCreation(creationDescription);
			}
			Creation creationDefault = component.getCreationDefault();
			if (creationDefault != null) {
				CreationDescription creationDescription = getCreationDescription(componentDescription, creationDefault,
						context);
				addCreationRules(creationDescription, creationDefault);
				for (TagType tagType : creationDefault.getTag()) {
					acceptSafe(creationDescription, tagType, new CreationTagRule());
				}
				componentDescription.setCreationDefault(creationDescription);
			}
		}
		// morphing targets
		{
			MorphingType morphingTargets = component.getMorphTargets();
			if (morphingTargets != null) {
				MorphingType morphingType = component.getMorphTargets();
				acceptSafe(componentDescription, morphingType.getNoInherit(), new MorphingNoInheritRule());
				for (MorphingType.MorphTarget morphTarget : morphingType.getMorphTarget()) {
					acceptSafe(componentDescription, morphTarget, new MorphingTargetRule(state));
				}
			}
		}
		// description text
		{
			acceptSafe(componentDescription, component.getDescription(), ComponentDescription::setDescription);
		}
		// constructors
		{
			Constructors constructors = component.getConstructors();
			if (constructors != null) {
				for (Constructors.Constructor constructor : constructors.getConstructor()) {
					Class<?> componentClass = componentDescription.getComponentClass();
					ConstructorDescription constructorDescription = new ConstructorDescription(componentClass);
					for (MethodParameter parameter : constructor.getParameter()) {
						addParametersRules(constructorDescription, parameter, state);
					}
					constructorDescription.postProcess();
					// add constructor only if we are parsing final component class
					if (componentDescription.getCurrentClass() == componentDescription.getComponentClass()) {
						componentDescription.addConstructor(constructorDescription);
					}
				}
			}
		}
		// method order
		{
			MethodsOrderType methodsOrder = component.getMethodOrder();
			if (methodsOrder != null) {
				acceptSafe(componentDescription, methodsOrder.getDefault(), new MethodOrderDefaultRule());
				for (MethodsOrderType.Method method : methodsOrder.getMethod()) {
					acceptSafe(componentDescription, method, new MethodOrderMethodRule());
				}
				for (MethodsOrderType.Methods methods : methodsOrder.getMethods()) {
					acceptSafe(componentDescription, methods, new MethodOrderMethodsRule());
				}
			}
		}
		// exposed children
		{
			if (component.getExposingRules() != null) {
				ExposingRulesType exposingRules = component.getExposingRules();
				for (JAXBElement<ExposingRuleType> exposingRule : exposingRules.getExcludeOrInclude()) {
					acceptSafe(componentDescription, exposingRule, new ExposingRulesRule());
				}
			}
		}
		// methods-exclude, methods-include
		{
			Component.Methods methods = component.getMethods();
			if (methods != null) {
				for (Component.Methods.MethodsInclude methodsInclude : methods.getMethodsInclude()) {
					acceptSafe(componentDescription, methodsInclude.getSignature(), new MethodsOperationRule(true));
				}
				for (Component.Methods.MethodsExclude methodsExclude : methods.getMethodsExclude()) {
					acceptSafe(componentDescription, methodsExclude.getSignature(), new MethodsOperationRule(false));
				}
			}
		}
		// untyped parameters
		{
			Component.Parameters parameters = component.getParameters();
			if (parameters != null) {
				for (Component.Parameters.Parameter parameter : parameters.getParameter()) {
					componentDescription.addParameter(parameter.getName(), parameter.getValue());
				}
			}
		}
		addPropertiesRules(componentDescription, component, state);
		addConfigurablePropertiesRules(componentDescription, component);
	}

	/**
	 * Adds property configuration rules to the given
	 * {@link GenericPropertyDescription}.
	 */
	private static void addPropertiesRules(ComponentDescription componentDescription, Component component,
			EditorState state) throws Exception {
		for (PropertyConfiguration property : component.getProperty()) {
			String id = property.getId();
			GenericPropertyDescription propertyDescription = componentDescription.getProperty(id);
			addPropertyConfigurationRules(componentDescription, propertyDescription, property, state);
		}
	}

	/**
	 * Adds {@link Rule}'s for configuring {@link GenericPropertyDescription} on
	 * stack.
	 */
	private static void addPropertyConfigurationRules(ComponentDescription componentDescription,
			GenericPropertyDescription propertyDescription, PropertyConfigurationElements property, EditorState state)
			throws Exception {
		// category
		{
			acceptSafe(propertyDescription, property.getCategory(), new PropertyCategoryRule());
		}
		// editor
		{
			org.eclipse.wb.core.databinding.xsd.component.PropertyEditor editorModel = property.getEditor();
			if (editorModel != null) {
				String id = editorModel.getId();
				PropertyEditor editor = DescriptionPropertiesHelper.getConfigurableEditor(id);
				PropertyEditorDescription editorDescription = new PropertyEditorDescription(state, editor);
				addConfigurableObjectParametersRules(editorDescription, editorModel);
				// prepare editor
				editor = editorDescription.getConfiguredEditor();
				// set editor for current property
				propertyDescription.setEditor(editor);
			}
		}
		// defaultValue
		{
			ClassLoader classLoader = state.getEditorLoader();
			acceptSafe(propertyDescription, property.getDefaultValue(), new PropertyDefaultRule(classLoader));
		}
		// getter
		{
			acceptSafe(propertyDescription, property.getGetter(), new PropertyGetterRule(componentDescription));
		}
		// tag
		{
			acceptSafe(propertyDescription, property.getTag(), new PropertyTagRule());
		}
	}

	/**
	 * Adds {@link Rule}'s for adding {@link ConfigurablePropertyDescription}'s.
	 */
	private static void addConfigurablePropertiesRules(ComponentDescription componentDescription, Component component)
			throws Exception {
		for (ConfigurablePropertyType property : component.getAddProperty()) {
			ConfigurablePropertyDescription propertyDescription = getConfigurablePropertyDescription(
					componentDescription, property);
			addConfigurableObjectParametersRules(propertyDescription, property);
		}
	}

	/**
	 * Adds {@link Rule}'s for configuring {@link AbstractConfigurableDescription}.
	 */
	private static void addConfigurableObjectParametersRules(AbstractConfigurableDescription configurableDescription,
			org.eclipse.wb.core.databinding.xsd.component.ParameterBaseType configurable) throws Exception {
		for (ParameterBaseType.Parameter parameter : configurable.getParameter()) {
			acceptSafe(configurableDescription, parameter, new ConfigurableObjectParameterRule());
		}
		for (ParameterBaseType.ParameterList parameterList : configurable.getParameterList()) {
			acceptSafe(configurableDescription, parameterList, new ConfigurableObjectListParameterRule());
		}
	}

	/**
	 * Adds {@link Rule}'s for parsing {@link CreationDescription}'s.
	 */
	private static void addCreationRules(CreationDescription creationDescription, Creation creation) throws Exception {
		// description
		{
			acceptSafe(creationDescription, creation.getDescription(), CreationDescription::setDescription);
		}
		// source
		{
			acceptSafe(creationDescription, creation.getSource(), CreationDescription::setSource);
		}
		// invocation
		{
			for (Creation.Invocation invocation : creation.getInvocation()) {
				CreationInvocationDescription invocationDescription = new CreationInvocationDescription();
				invocationDescription.setSignature(invocation.getSignature());
				// arguments
				invocationDescription.setArguments(invocation.getContent());
				// add
				creationDescription.addInvocation(invocationDescription);
			}
		}
		// untyped parameters
		{
			for (Creation.Parameter parameter : creation.getParameter()) {
				creationDescription.addParameter(parameter.getName(), parameter.getContent());
			}
		}
	}

	/**
	 * Adds {@link Rule}'s for parsing {@link ParameterDescription}'s.
	 */
	static void addParametersRules(AbstractInvocationDescription methodDescription, MethodParameter parameter,
			EditorState state) throws Exception {
		ClassLoader classLoader = state.getEditorLoader();
		//
		ParameterDescription parameterDescription = new ParameterDescription();
		acceptSafe(parameterDescription, parameter.getType(), new SetClassPropertyRule(classLoader));
		acceptSafe(parameterDescription, parameter.getName(), ParameterDescription::setName);
		acceptSafe(parameterDescription, parameter.getDefaultSource(), ParameterDescription::setDefaultSource);
		acceptSafe(parameterDescription, parameter.isParent(), ParameterDescription::setParent);
		acceptSafe(parameterDescription, parameter.isParent2(), ParameterDescription::setParent2);
		acceptSafe(parameterDescription, parameter.isChild(), ParameterDescription::setChild);
		acceptSafe(parameterDescription, parameter.isChild2(), ParameterDescription::setChild2);
		acceptSafe(parameterDescription, parameter.getProperty(), ParameterDescription::setProperty);
		methodDescription.addParameter(parameterDescription);
		// editors
		{
			org.eclipse.wb.core.databinding.xsd.component.PropertyEditor editorModel = parameter.getEditor();
			if (editorModel != null) {
				String id = editorModel.getId();
				PropertyEditor editor = DescriptionPropertiesHelper.getConfigurableEditor(id);
				PropertyEditorDescription editorDescription = new PropertyEditorDescription(state, editor);
				addConfigurableObjectParametersRules(editorDescription, editorModel);
				// prepare editor
				editor = editorDescription.getConfiguredEditor();
				// set editor for current property
				parameterDescription.setEditor(editor);
			}
		}
		// tags
		for (TagType tag : parameter.getTag()) {
			acceptSafe(parameterDescription, tag, new ParameterTagRule());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean shouldCacheDescriptions_inPackage(ClassResourceInfo descriptionInfo, Class<?> componentClass)
			throws Exception {
		return hasMarkerFileForPackage(descriptionInfo, componentClass, ".wbp-cache-descriptions");
	}

	private static boolean shouldCachePresentation(ClassResourceInfo descriptionInfo, Class<?> componentClass)
			throws Exception {
		if (descriptionInfo.clazz == componentClass) {
			Bundle bundle = descriptionInfo.resource.getBundle();
			if (bundle != null) {
				return bundle.getEntry("wbp-meta/.wbp-cache-presentations") != null;
			}
		}
		return false;
	}

	/**
	 * Checks if package of given class has marker file with given name.
	 *
	 * @param descriptionInfo the first {@link ClassResourceInfo} for
	 *                        <code>*.wbp-component.xml</code>, in "to superclass"
	 *                        direction. We look into its {@link Bundle} for caching
	 *                        marker.
	 *
	 * @return <code>true</code> if package with given class has "cache enabled"
	 *         marker.
	 */
	private static boolean hasMarkerFileForPackage(ClassResourceInfo descriptionInfo, Class<?> componentClass,
			String markerFileName) throws Exception {
		ResourceInfo resourceInfo = descriptionInfo.resource;
		if (resourceInfo.getBundle() != null) {
			String packageName = CodeUtils.getPackage(componentClass.getName());
			String markerName = packageName.replace('.', '/') + "/" + markerFileName;
			return DescriptionHelper.getResourceInfo(null, resourceInfo.getBundle(), markerName) != null;
		}
		return false;
	}

	/**
	 * @return the instances of {@link IDescriptionProcessor}.
	 */
	public static List<IDescriptionProcessor> getDescriptionProcessors() {
		return ExternalFactoriesHelper.getElementsInstances(IDescriptionProcessor.class,
				"org.eclipse.wb.core.descriptionProcessors", "processor");
	}

	private static CreationDescription getCreationDescription(ComponentDescription componentDescription,
			Creation creation, ILoadingContext context) throws Exception {
		// prepare creation
		String id = creation.getId();
		String name = creation.getName();
		CreationDescription creationDescription = new CreationDescription(componentDescription, id, name);
		// set optional specific icon
		if (id != null) {
			Class<?> componentClass = componentDescription.getComponentClass();
			String suffix = "_" + id;
			creationDescription.setIcon(DescriptionHelper.getIcon(context, componentClass, suffix));
		}
		// OK, configured creation
		return creationDescription;
	}

	private static GenericPropertyDescription getGenericPropertyDescription(ComponentDescription componentDescription,
			MethodSingleProperty property) throws Exception {
		Class<?> componentClass = componentDescription.getComponentClass();
		// prepare method attributes
		String propertyTitle = property.getTitle();
		String methodSignature = property.getMethod();
		// prepare method
		Method method = ReflectionUtils.getMethodBySignature(componentClass, methodSignature);
		Assert.isTrue(method.getParameterTypes().length == 1, "Method with single parameter expected: %s", method);
		// add property
		return StandardBeanPropertiesRule.addSingleProperty(componentDescription, propertyTitle, method, null);
	}

	private static ConfigurablePropertyDescription getConfigurablePropertyDescription(
			ComponentDescription componentDescription, ConfigurablePropertyType property) throws Exception {
		String id = property.getId();
		String title = property.getTitle();
		// create property
		ConfigurablePropertyDescription propertyDescription = new ConfigurablePropertyDescription();
		propertyDescription.setId(id);
		propertyDescription.setTitle(title);
		// add property
		{
			componentDescription.addConfigurableProperty(propertyDescription);
		}
		return propertyDescription;
	}

	/**
	 * Null-safe invocation of {@link FailableBiConsumer#accept(Object, Object)}, in
	 * order to better handle optional model parameters. Does nothing if
	 * {@code model} is {@code null}.
	 */
	static <U, T> void acceptSafe(U description, T model, FailableBiConsumer<U, T, ?> consumer)
			throws Exception {
		if (model == null) {
			return;
		}
		consumer.accept(description, model);
	}

	@Deprecated
	@FunctionalInterface
	/**
	 * @deprecated Going to be removed by Commons Lang3 FailableBiConsumer
	 */
	public static interface FailableBiConsumer<T, U, E extends Exception> {
		void accept(T t, U u) throws E;
	}
}
