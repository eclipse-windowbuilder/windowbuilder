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
package org.eclipse.wb.internal.core.model.description.helpers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.description.CreationInvocationDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.resource.ResourceInfo;
import org.eclipse.wb.internal.core.model.description.rules.ObjectCreateRule;
import org.eclipse.wb.internal.core.model.description.rules.SetListedPropertiesRule;
import org.eclipse.wb.internal.core.model.description.rules.StandardBeanPropertiesRule;
import org.eclipse.wb.internal.core.utils.IOUtils2;
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
import org.eclipse.wb.internal.core.utils.ui.ImageDisposer;
import org.eclipse.wb.internal.core.utils.xml.parser.QAttribute;
import org.eclipse.wb.internal.core.utils.xml.parser.QHandlerAdapter;
import org.eclipse.wb.internal.core.utils.xml.parser.QParser;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.impl.NoOpLog;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Helper for accessing descriptions of factories - {@link FactoryMethodDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public class FactoryDescriptionHelper {
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
  private static final ClassMap<Map<String, FactoryMethodDescription>[]> m_descriptionMaps =
      ClassMap.create();

  /**
   * Returns factory methods of given {@link Class} and its super classes.
   *
   * @return the {@link Map} signature -> {@link FactoryMethodDescription}.
   */
  @SuppressWarnings("unchecked")
  public static Map<String, FactoryMethodDescription> getDescriptionsMap(AstEditor editor,
      Class<?> factoryClass,
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
      signaturesMap = Maps.newTreeMap();
      signaturesMaps[index] = signaturesMap;
      // this factory class methods
      {
        Map<String, FactoryMethodDescription> map =
            getDescriptionsMap0(editor, factoryClass, forStatic);
        signaturesMap.putAll(map);
      }
      // super factory class methods (cached)
      {
        Class<?> superFactoryClass = factoryClass.getSuperclass();
        if (superFactoryClass != null) {
          Map<String, FactoryMethodDescription> map =
              getDescriptionsMap(editor, superFactoryClass, forStatic);
          signaturesMap.putAll(map);
        }
      }
    }
    // done
    return signaturesMap;
  }

  /**
   * Returns factory methods declared in given {@link Class} (but not in super classes).
   *
   * @return the {@link Map} signature -> {@link FactoryMethodDescription}.
   */
  private static Map<String, FactoryMethodDescription> getDescriptionsMap0(AstEditor editor,
      Class<?> factoryClass,
      boolean forStatic) throws Exception {
    try {
      return getDescriptionsMap0Ex(editor, factoryClass, forStatic);
    } catch (Throwable e) {
      EditorState.get(editor).addWarning(
          new EditorWarning("Can not get factory methods for " + factoryClass, e));
      return Maps.newTreeMap();
    }
  }

  /**
   * Implementation of {@link #getDescriptionsMap0(AstEditor, Class, boolean)} that can throw
   * exceptions.
   */
  private static Map<String, FactoryMethodDescription> getDescriptionsMap0Ex(AstEditor editor,
      Class<?> factoryClass,
      boolean forStatic) throws Exception {
    EditorState state = EditorState.get(editor);
    ILoadingContext context = EditorStateLoadingContext.get(state);
    // try to find cached map
    {
      Map<String, FactoryMethodDescription> signaturesMap =
          state.getFactorySignatures(factoryClass, forStatic);
      if (signaturesMap != null) {
        return signaturesMap;
      }
    }
    //
    String factoryClassName = factoryClass.getName();
    IType factoryType = editor.getJavaProject().findType(factoryClassName);
    if (factoryType == null) {
      return Maps.newTreeMap();
    }
    Boolean allMethodsAreFactories = null;
    List<FactoryMethodDescription> descriptions = Lists.newArrayList();
    // read descriptions from XML
    {
      String descriptionName = factoryClassName.replace('.', '/') + ".wbp-factory.xml";
      ResourceInfo resourceInfo =
          DescriptionHelper.getResourceInfo(context, factoryClass, descriptionName);
      if (resourceInfo != null) {
        Map<Integer, FactoryMethodDescription> textualDescriptions = Maps.newHashMap();
        Digester digester = prepareDigester(factoryClass, state, textualDescriptions);
        digester.push(allMethodsAreFactories);
        digester.push(descriptions);
        allMethodsAreFactories = (Boolean) digester.parse(resourceInfo.getURL());
        readTextualDescriptions(resourceInfo, textualDescriptions);
      }
    }
    // prepare map: signature -> description
    Map<String, FactoryMethodDescription> signaturesMap = Maps.newTreeMap();
    for (FactoryMethodDescription description : descriptions) {
      signaturesMap.put(description.getSignature(), description);
    }
    // factory flag for not-wbp methods
    if (allMethodsAreFactories == null) {
      allMethodsAreFactories = hasFactorySuffix(factoryType) || hasFactoryTag(factoryType);
    }
    // if no methods from XML, may be no methods at all
    if (!allMethodsAreFactories.booleanValue()
        && descriptions.isEmpty()
        && !hasFactoryTagSource(factoryType)) {
      return Maps.newTreeMap();
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
      Method method =
          ReflectionUtils.getMethodBySignature(factoryClass, description.getSignature());
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
      Image icon;
      {
        String signature = entry.getKey();
        String signatureUnix = StringUtils.replaceChars(signature, "(,)", "___");
        String iconPath = factoryClassName.replace('.', '/') + "." + signatureUnix;
        icon = DescriptionHelper.getIconImage(context, iconPath);
        description.setIcon(icon);
      }
      // schedule disposing
      {
        String name = description.getDeclaringClass().getName() + "." + description.getSignature();
        ImageDisposer.add(description, name, icon);
      }
    }
    // remember descriptions in cache
    state.putFactorySignatures(factoryClass, forStatic, signaturesMap);
    return signaturesMap;
  }

  /**
   * @return <code>true</code> if given {@link Method} modifiers are valid to consider this
   *         {@link Method} as possible factory methods. Usually we require "public" visibility.
   *         However for "local" factory methods we allow any visibility.
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
  public static FactoryMethodDescription getDescription(AstEditor editor,
      Class<?> factoryClass,
      String methodSignature,
      boolean forStatic) throws Exception {
    Map<String, FactoryMethodDescription> signaturesMap =
        getDescriptionsMap(editor, factoryClass, forStatic);
    return signaturesMap.get(methodSignature);
  }

  /**
   * Finds {@link ICompilationUnit}'s with factories.
   *
   * @param thePackage
   *          the {@link IPackageFragment} to find factories in.
   *
   * @return the {@link ICompilationUnit}'s with factories.
   */
  public static List<ICompilationUnit> getFactoryUnits(AstEditor editor, IPackageFragment thePackage)
      throws Exception {
    List<ICompilationUnit> factoryUnits = Lists.newArrayList();
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
      public Boolean runObject() throws Exception {
        ClassLoader classLoader = EditorState.get(editor).getEditorLoader();
        Class<?> clazz = classLoader.loadClass(typeName);
        return !getDescriptionsMap(editor, clazz, true).isEmpty()
            || !getDescriptionsMap(editor, clazz, false).isEmpty();
      }
    },
        false);
  }

  /**
   * @return <code>true</code> if given {@link MethodInvocation} is invocation of factory.
   */
  public static boolean isFactoryInvocation(final AstEditor editor,
      final MethodInvocation invocation) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
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
  private static Digester prepareDigester(Class<?> factoryClass,
      EditorState state,
      final Map<Integer, FactoryMethodDescription> textualDescriptions) {
    Digester digester = new Digester() {
      private static final String DESCRIPTION_PATTERN = "factory/method/description";
      private int m_descriptionIndex;

      @Override
      public void endElement(String namespaceURI, String localName, String qName)
          throws SAXException {
        // description with HTML support
        if (DESCRIPTION_PATTERN.equals(match)) {
          FactoryMethodDescription methodDescription = (FactoryMethodDescription) peek();
          textualDescriptions.put(m_descriptionIndex, methodDescription);
          m_descriptionIndex++;
        }
        // continue
        super.endElement(namespaceURI, localName, qName);
      }
    };
    digester.setLogger(new NoOpLog());
    addRules(digester, state, factoryClass);
    return digester;
  }

  /**
   * Adds {@link Rule}'s for factory description parsing.
   */
  private static void addRules(Digester digester, EditorState state, final Class<?> declaringClass) {
    // allMethodsAreFactories flag
    {
      String pattern = "factory/allMethodsAreFactories";
      digester.addRule(pattern, new Rule() {
        @Override
        public void body(String namespace, String name, String text) throws Exception {
          Object list = getDigester().pop();
          Boolean allMethodsAreFactories = (Boolean) getDigester().pop();
          if ("true".equalsIgnoreCase(text)) {
            allMethodsAreFactories = Boolean.TRUE;
          }
          if ("false".equalsIgnoreCase(text)) {
            allMethodsAreFactories = Boolean.FALSE;
          }
          getDigester().push(allMethodsAreFactories);
          getDigester().push(list);
        }
      });
    }
    // methods
    {
      String pattern = "factory/method";
      digester.addRule(pattern, new Rule() {
        @Override
        public void begin(String namespace, String name, Attributes attributes) throws Exception {
          FactoryMethodDescription factoryMethodDescription =
              new FactoryMethodDescription(declaringClass);
          Boolean allMethodsAreFactories = (Boolean) getDigester().peek(1);
          factoryMethodDescription.setFactory(allMethodsAreFactories != null
              ? allMethodsAreFactories.booleanValue()
              : true);
          digester.push(factoryMethodDescription);
        }

        @Override
        public void end(String namespace, String name) throws Exception {
          digester.pop();
        }
      });
      digester.addSetProperties(pattern);
      digester.addSetNext(pattern, "add");
      digester.addCallMethod(pattern, "postProcess");
      ComponentDescriptionHelper.addParametersRules(digester, pattern + "/parameter", state);
    }
    // invocation
    {
      String pattern = "factory/method/invocation";
      digester.addRule(pattern, new ObjectCreateRule(CreationInvocationDescription.class));
      digester.addRule(pattern, new SetListedPropertiesRule(new String[]{"signature"}));
      // arguments
      digester.addCallMethod(pattern, "setArguments", 1);
      digester.addCallParam(pattern, 0);
      // add
      digester.addSetNext(pattern, "addInvocation");
    }
    // name text
    {
      String pattern = "factory/method/name";
      digester.addCallMethod(pattern, "setPresentationName", 1);
      digester.addCallParam(pattern, 0);
    }
    // untyped parameters
    {
      String pattern = "factory/method/parameters/parameter";
      digester.addCallMethod(pattern, "addParameter", 2);
      digester.addCallParam(pattern, 0, "name");
      digester.addCallParam(pattern, 1);
    }
  }

  private static void readTextualDescriptions(ResourceInfo resourceInfo,
      final Map<Integer, FactoryMethodDescription> descriptionIndexToMethod) throws Exception {
    final String xmlText = IOUtils2.readString(resourceInfo.getURL().openStream());
    QParser.parse(new StringReader(xmlText), new QHandlerAdapter() {
      private int m_index = 0;
      private int m_descriptionStart;

      @Override
      public void startElement(int offset,
          int length,
          String tag,
          Map<String, String> attributes,
          List<QAttribute> attrList,
          boolean closed) throws Exception {
        if ("description".equals(tag)) {
          m_descriptionStart = offset + length;
        }
      }

      @Override
      public void endElement(int offset, int endOffset, String tag) throws Exception {
        if ("description".equals(tag)) {
          FactoryMethodDescription methodDescription = descriptionIndexToMethod.get(m_index);
          if (methodDescription != null) {
            methodDescription.setDescription(xmlText.substring(m_descriptionStart, offset));
          }
          m_index++;
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Description from JavaDoc
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String WBP_FACTORY_TAG = "@wbp.factory";
  private static final String WBP_FACTORY_PARAMETER_SOURCE_TAG = "@wbp.factory.parameter.source ";
  private static final String WBP_FACTORY_PARAMETER_PROPERTY_TAG =
      "@wbp.factory.parameter.property ";
  private static final String WBP_FACTORY_PARAMETERS_NO_BINDING =
      "@wbp.factory.parameters.noBinding";

  /**
   * @return <code>true</code> if given {@link MethodDeclaration} is marked as factory method.
   */
  public static boolean isFactoryMethod(MethodDeclaration methodDeclaration) {
    return AstNodeUtils.hasJavaDocTag(methodDeclaration, WBP_FACTORY_TAG);
  }

  /**
   * @return <code>true</code> if {@link IType} has name that ends with "Factory", so we consider
   *         all its methods as factories.
   */
  private static boolean hasFactorySuffix(IType type) throws Exception {
    return type.getElementName().endsWith("Factory");
  }

  /**
   * @return <code>true</code> if given {@link IType} has global, class level, factory tag, so all
   *         methods are marked as factories.
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
   * @return <code>true</code> if given {@link IType} has factory tag at all, global or for some
   *         specific method.
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
   * Updates {@link FactoryMethodDescription} using JavaDoc's in source of factory class.
   */
  private static void updateDescriptionsJavaDoc0(AstEditor editor,
      FactoryMethodDescription description) throws Exception {
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
   * Updates {@link FactoryMethodDescription} using JavaDoc's in source of factory class.
   */
  private static void updateDescriptionsJavaDoc(AstEditor editor,
      FactoryMethodDescription description) throws Exception {
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
          String parameterString =
              javaDocLine.substring(WBP_FACTORY_PARAMETER_SOURCE_TAG.length()).trim();
          // prepare parameter
          ParameterDescription parameter =
              getJavaDocParameter(description, parameterNames, parameterString);
          // set default source
          {
            String parameterSource = StringUtilities.removeFirstWord(parameterString);
            parameter.setDefaultSource(parameterSource);
          }
        }
        // parameter property
        if (javaDocLine.startsWith(WBP_FACTORY_PARAMETER_PROPERTY_TAG)) {
          String parameterString =
              javaDocLine.substring(WBP_FACTORY_PARAMETER_PROPERTY_TAG.length()).trim();
          // prepare parameter
          ParameterDescription parameter =
              getJavaDocParameter(description, parameterNames, parameterString);
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
      String[] parameterNames,
      String parameterString) {
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
   * @return the id of standard bean property (created using {@link StandardBeanPropertiesRule})
   *         with given title or <code>null</code> if no such property exists.
   */
  private static String getStandardPropertyId(List<PropertyDescriptor> propertyDescriptors,
      String title) {
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
