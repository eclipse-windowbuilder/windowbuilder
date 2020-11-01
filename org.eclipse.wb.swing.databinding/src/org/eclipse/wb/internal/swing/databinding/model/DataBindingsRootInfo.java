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
package org.eclipse.wb.internal.swing.databinding.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.parser.ISubParser;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.Messages;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeansObserveTypeContainer;
import org.eclipse.wb.internal.swing.databinding.model.beans.ElPropertyObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.AutoBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.BindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JComboBoxBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JListBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JTableBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.UpdateStrategyInfo;
import org.eclipse.wb.internal.swing.databinding.model.components.ComponentsObserveTypeContainer;
import org.eclipse.wb.internal.swing.databinding.model.components.JavaInfoReferenceProvider;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.BeanPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.ElPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.ObjectPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.apache.commons.lang.StringEscapeUtils;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manage binding source code (add invocation initDataBindings(), configure classpath and
 * etc.) for compilation unit.
 *
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public final class DataBindingsRootInfo implements ISubParser {
  public static final String INIT_DATA_BINDINGS_METHOD_NAME = "initDataBindings";
  public static final String[] ACCESS_VALUES = {"public ", "protected ", "private ", ""};
  private static final String BINDINGS_CREATE_AUTO_BINDING =
      "org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy,";
  private static final String SWING_BINDINGS_CREATE_JLIST_BINDING =
      "org.jdesktop.swingbinding.SwingBindings.createJListBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy,";
  private static final String SWING_BINDINGS_CREATE_JTABLE_BINDING =
      "org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy,";
  private static final String SWING_BINDINGS_CREATE_JCOMBO_BOX_BINDING =
      "org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy,";
  private static final String CONSTRUCTOR_SIGNATURE =
      "<init>(org.eclipse.wb.internal.swing.databinding.model.bindings.UpdateStrategyInfo,org.eclipse.wb.internal.swing.databinding.model.ObserveInfo,org.eclipse.wb.internal.swing.databinding.model.ObserveInfo,org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo,org.eclipse.wb.internal.swing.databinding.model.ObserveInfo,org.eclipse.wb.internal.swing.databinding.model.ObserveInfo,org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo)";
  private static Constructor<JListBindingInfo> m_constructorJList;
  private static Constructor<JTableBindingInfo> m_constructorJTable;
  private static Constructor<JComboBoxBindingInfo> m_constructorJComboBox;
  private final DatabindingsProvider m_provider;
  private final List<BindingInfo> m_bindings = new ArrayList<>();
  private MethodDeclaration m_initDataBindings;
  private boolean m_addToGroup;
  private boolean m_addInitializeContext;
  private boolean m_addPostInitializeContext;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DataBindingsRootInfo(DatabindingsProvider provider) {
    m_provider = provider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<BindingInfo> getBindings() {
    return m_bindings;
  }

  public MethodDeclaration getInitDataBindings() {
    return m_initDataBindings;
  }

  public void setInitDataBindings(MethodDeclaration initDataBindings) {
    m_initDataBindings = initDataBindings;
    IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(m_initDataBindings);
    ITypeBinding returnType = methodBinding == null ? null : methodBinding.getReturnType();
    m_addToGroup = returnType != null
        && "org.jdesktop.beansbinding.BindingGroup".equals(
            AstNodeUtils.getFullyQualifiedName(returnType, false));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      ClassInstanceCreation creation,
      Expression[] arguments,
      IModelResolver resolver,
      IDatabindingsProvider provider) throws Exception {
    ITypeBinding binding = AstNodeUtils.getTypeBinding(creation);
    if (binding == null) {
      return null;
    }
    // binding converter/validator
    if (AstNodeUtils.isSuccessorOf(binding, "org.jdesktop.beansbinding.Converter")
        || AstNodeUtils.isSuccessorOf(binding, "org.jdesktop.beansbinding.Validator")) {
      String parameters = null;
      if (arguments.length > 0) {
        String source = editor.getSource(creation);
        parameters = source.substring(source.indexOf('('));
      }
      return new TypeObjectInfo(GenericUtils.getCreationType(editor, creation), parameters);
    }
    return null;
  }

  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver) throws Exception {
    if (m_constructorJList == null) {
      // prepare constructors for swing bindings
      m_constructorJList =
          ReflectionUtils.getConstructorBySignature(JListBindingInfo.class, CONSTRUCTOR_SIGNATURE);
      m_constructorJTable =
          ReflectionUtils.getConstructorBySignature(JTableBindingInfo.class, CONSTRUCTOR_SIGNATURE);
      m_constructorJComboBox = ReflectionUtils.getConstructorBySignature(
          JComboBoxBindingInfo.class,
          CONSTRUCTOR_SIGNATURE);
    }
    if (signature.startsWith(BINDINGS_CREATE_AUTO_BINDING)) {
      // Bindings.createAutoBinding(AutoBinding.UpdateStrategy, ...)
      switch (arguments.length) {
        case 4 :
          return createAutoBindingForObjectToProperty(
              editor,
              signature,
              invocation,
              arguments,
              resolver);
        case 5 :
          if (signature.endsWith(",java.lang.String)")) {
            return createAutoBindingForObjectToProperty(
                editor,
                signature,
                invocation,
                arguments,
                resolver);
          }
        case 6 :
          return createAutoBindingForProperties(editor, signature, invocation, arguments, resolver);
      }
    } else if (signature.startsWith(SWING_BINDINGS_CREATE_JLIST_BINDING)) {
      // SwingBindings.createJListBinding(AutoBinding.UpdateStrategy, ...)
      if (signature.endsWith(",java.util.List,javax.swing.JList)")
          || signature.endsWith(",java.util.List,javax.swing.JList,java.lang.String)")) {
        return createBindingForListToJComponent(
            editor,
            signature,
            invocation,
            arguments,
            resolver,
            m_constructorJList);
      }
      if (signature.endsWith(",org.jdesktop.beansbinding.Property,javax.swing.JList)")
          || signature.endsWith(
              ",org.jdesktop.beansbinding.Property,javax.swing.JList,java.lang.String)")) {
        return createBindingForListPropertyToJComponent(
            editor,
            signature,
            invocation,
            arguments,
            resolver,
            m_constructorJList);
      }
    } else if (signature.startsWith(SWING_BINDINGS_CREATE_JTABLE_BINDING)) {
      // SwingBindings.createJTableBinding(AutoBinding.UpdateStrategy, ...)
      if (signature.endsWith(",java.util.List,javax.swing.JTable)")
          || signature.endsWith(",java.util.List,javax.swing.JTable,java.lang.String)")) {
        return createBindingForListToJComponent(
            editor,
            signature,
            invocation,
            arguments,
            resolver,
            m_constructorJTable);
      }
      if (signature.endsWith(",org.jdesktop.beansbinding.Property,javax.swing.JTable)")
          || signature.endsWith(
              ",org.jdesktop.beansbinding.Property,javax.swing.JTable,java.lang.String)")) {
        return createBindingForListPropertyToJComponent(
            editor,
            signature,
            invocation,
            arguments,
            resolver,
            m_constructorJTable);
      }
    } else if (signature.startsWith(SWING_BINDINGS_CREATE_JCOMBO_BOX_BINDING)) {
      // SwingBindings.createJComboBoxBinding(AutoBinding.UpdateStrategy, ...)
      if (signature.endsWith(",java.util.List,javax.swing.JComboBox)")
          || signature.endsWith(",java.util.List,javax.swing.JComboBox,java.lang.String)")) {
        return createBindingForListToJComponent(
            editor,
            signature,
            invocation,
            arguments,
            resolver,
            m_constructorJComboBox);
      }
      if (signature.endsWith(",org.jdesktop.beansbinding.Property,javax.swing.JComboBox)")
          || signature.endsWith(
              ",org.jdesktop.beansbinding.Property,javax.swing.JComboBox,java.lang.String)")) {
        return createBindingForListPropertyToJComponent(
            editor,
            signature,
            invocation,
            arguments,
            resolver,
            m_constructorJComboBox);
      }
    } else if (signature.endsWith("initializeBindings()")) {
      m_addInitializeContext = true;
    } else if (signature.endsWith("postInitializeBindings()")) {
      m_addPostInitializeContext = true;
    }
    return null;
  }

  private BindingInfo addBinding(AstEditor editor,
      String signature,
      Expression[] arguments,
      BindingInfo binding) throws Exception {
    if (signature.endsWith(",java.lang.String)")) {
      String name = CoreUtils.evaluate(String.class, editor, arguments[arguments.length - 1]);
      binding.setName(StringEscapeUtils.unescapeJava(name));
    }
    binding.preCreate();
    m_bindings.add(binding);
    return binding;
  }

  /**
   * Bindings.createAutoBinding(UpdateStrategy, SS, TS, Property<TS, TV>, [String])
   */
  private BindingInfo createAutoBindingForObjectToProperty(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver) throws Exception {
    // strategy
    UpdateStrategyInfo strategyInfo = createStrategy(arguments[0]);
    // model object
    ObserveInfo model = getObserveInfo(arguments[1]);
    if (model == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.DataBindingsRootInfo_errModelArgumentNotFound,
              arguments[1]),
          new Throwable());
      return null;
    }
    // model properties
    IGenericType[] types = GenericUtils.getReturnTypeArguments(editor, invocation, 3);
    PropertyInfo modelAstProperty = new ObjectPropertyInfo(types[0]);
    ObserveInfo modelProperty = modelAstProperty.getObserveProperty(model);
    Assert.isNotNull(modelProperty);
    assertEquals(modelProperty, modelAstProperty);
    // target object
    ObserveInfo target = getObserveInfo(arguments[2]);
    if (target == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.DataBindingsRootInfo_errTargetArgumentNotFound,
              arguments[2]),
          new Throwable());
      return null;
    }
    // target AST property
    PropertyInfo targetAstProperty = (PropertyInfo) resolver.getModel(arguments[3]);
    if (targetAstProperty == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.DataBindingsRootInfo_errTargetPropertyArgumentNotFound,
              arguments[3]),
          new Throwable());
      return null;
    }
    // target property
    ObserveInfo targetProperty = targetAstProperty.getObserveProperty(target);
    if (targetProperty == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.DataBindingsRootInfo_errTargetProperty2NotFound,
              arguments[2],
              arguments[3]),
          new Throwable());
      targetProperty = createDefaultProperty(targetAstProperty);
    } else {
      assertObserves(types, 1, target, targetAstProperty, targetProperty);
    }
    // binding
    return addBinding(
        editor,
        signature,
        arguments,
        new AutoBindingInfo(strategyInfo,
            target,
            targetProperty,
            targetAstProperty,
            model,
            modelProperty,
            modelAstProperty));
  }

  /**
   * Bindings.createAutoBinding(UpdateStrategy, SS, Property<SS, SV>, TS, Property<TS, TV>,
   * [String])
   */
  private BindingInfo createAutoBindingForProperties(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver) throws Exception {
    // strategy
    UpdateStrategyInfo strategyInfo = createStrategy(arguments[0]);
    // model object
    ObserveInfo model = getObserveInfo(arguments[1]);
    if (model == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.DataBindingsRootInfo_errModelArgumentNotFound,
              arguments[1]),
          new Throwable());
      return null;
    }
    // model AST property
    PropertyInfo modelAstProperty = (PropertyInfo) resolver.getModel(arguments[2]);
    if (modelAstProperty == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.DataBindingsRootInfo_errModelPropertyArgumentNotFound,
              arguments[2]),
          new Throwable());
      return null;
    }
    // model property
    ObserveInfo modelProperty = modelAstProperty.getObserveProperty(model);
    IGenericType[] types = GenericUtils.getReturnTypeArguments(editor, invocation, 4);
    if (modelProperty == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.DataBindingsRootInfo_errModelProperty2NotFound,
              arguments[1],
              arguments[2]),
          new Throwable());
      modelProperty = createDefaultProperty(modelAstProperty);
    } else {
      assertObserves(types, 0, model, modelAstProperty, modelProperty);
    }
    // target object
    ObserveInfo target = getObserveInfo(arguments[3]);
    if (target == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.DataBindingsRootInfo_errTargetArgumentNotFound,
              arguments[3]),
          new Throwable());
      return null;
    }
    // target AST property
    PropertyInfo targetAstProperty = (PropertyInfo) resolver.getModel(arguments[4]);
    if (targetAstProperty == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(Messages.DataBindingsRootInfo_errArgumentNotFound, arguments[4]),
          new Throwable());
      return null;
    }
    // target property
    ObserveInfo targetProperty = targetAstProperty.getObserveProperty(target);
    if (targetProperty == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.DataBindingsRootInfo_errTargetProperty2NotFound,
              arguments[3],
              arguments[4]),
          new Throwable());
      targetProperty = createDefaultProperty(targetAstProperty);
    } else {
      assertObserves(types, 2, target, targetAstProperty, targetProperty);
    }
    // binding
    return addBinding(
        editor,
        signature,
        arguments,
        new AutoBindingInfo(strategyInfo,
            target,
            targetProperty,
            targetAstProperty,
            model,
            modelProperty,
            modelAstProperty));
  }

  /**
   * SwingBindings.createJListBinding(UpdateStrategy, List<E>, JList, [String])<br>
   * SwingBindings.createJTableBinding(UpdateStrategy, List<E>, JTable, [String])<br>
   * SwingBindings.createJComboBoxBinding(UpdateStrategy, List<E>, JComboBox, [String])
   */
  private BindingInfo createBindingForListToJComponent(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver,
      Constructor<? extends BindingInfo> constructor) throws Exception {
    // strategy
    UpdateStrategyInfo strategyInfo = createStrategy(arguments[0]);
    // model object
    ObserveInfo model = getBeanObserveInfo(arguments[1]);
    if (model == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.DataBindingsRootInfo_errModelArgumentNotFound,
              arguments[1]),
          new Throwable());
      return null;
    }
    // model properties
    PropertyInfo modelAstProperty = new ObjectPropertyInfo(model.getObjectType());
    ObserveInfo modelProperty = modelAstProperty.getObserveProperty(model);
    Assert.isNotNull(modelProperty);
    assertEquals(modelProperty, modelAstProperty);
    // target object
    ObserveInfo target = getComponentObserveInfo(arguments[2]);
    if (target == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.DataBindingsRootInfo_errTargetArgumentNotFound,
              arguments[2]),
          new Throwable());
      return null;
    }
    // target properties
    PropertyInfo targetAstProperty = new ObjectPropertyInfo(target.getObjectType());
    ObserveInfo targetProperty = targetAstProperty.getObserveProperty(target);
    Assert.isNotNull(targetProperty);
    assertEquals(targetProperty, targetAstProperty);
    // binding
    return addBinding(
        editor,
        signature,
        arguments,
        constructor.newInstance(
            strategyInfo,
            target,
            targetProperty,
            targetAstProperty,
            model,
            modelProperty,
            modelAstProperty));
  }

  /**
   * SwingBindings.createJListBinding(UpdateStrategy, SS, Property<SS, List<E>>, JList, [String])
   * <br>
   * SwingBindings.createJTableBinding(UpdateStrategy, SS, Property<SS, List<E>>, JTable,
   * [String])<br>
   * SwingBindings.createJComboBoxBinding(UpdateStrategy, SS, Property<SS, List<E>>, JComboBox,
   * [String])
   */
  private BindingInfo createBindingForListPropertyToJComponent(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver,
      Constructor<? extends BindingInfo> constructor) throws Exception {
    // strategy
    UpdateStrategyInfo strategyInfo = createStrategy(arguments[0]);
    // model object
    ObserveInfo model = getObserveInfo(arguments[1]);
    if (model == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.DataBindingsRootInfo_errModelArgumentNotFound,
              arguments[1]),
          new Throwable());
      return null;
    }
    // model AST property
    IGenericType[] types = GenericUtils.getReturnTypeArguments(editor, invocation, 2);
    GenericUtils.assertEquals(model.getObjectType(), types[1]);
    PropertyInfo modelAstProperty = (PropertyInfo) resolver.getModel(arguments[2]);
    if (modelAstProperty == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.DataBindingsRootInfo_errModelPropertyArgumentNotFound,
              arguments[2]),
          new Throwable());
      return null;
    }
    // model property
    ObserveInfo modelProperty = modelAstProperty.getObserveProperty(model);
    if (modelProperty == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.DataBindingsRootInfo_errModelProperty2NotFound,
              arguments[1],
              arguments[2]),
          new Throwable());
      modelProperty = createDefaultProperty(modelAstProperty);
    }
    // target object
    ObserveInfo target = getComponentObserveInfo(arguments[3]);
    if (target == null) {
      AbstractParser.addError(
          editor,
          MessageFormat.format(
              Messages.DataBindingsRootInfo_errTargetArgumentNotFound,
              arguments[3]),
          new Throwable());
      return null;
    }
    // target properties
    PropertyInfo targetAstProperty = new ObjectPropertyInfo(target.getObjectType());
    ObserveInfo targetProperty = targetAstProperty.getObserveProperty(target);
    Assert.isNotNull(targetProperty);
    assertEquals(targetProperty, targetAstProperty);
    //
    Assert.isTrue(
        DatabindingsProvider.isSwingBinding(
            model,
            modelProperty) != DatabindingsProvider.isSwingBinding(target, targetProperty));
    // binding
    return addBinding(
        editor,
        signature,
        arguments,
        constructor.newInstance(
            strategyInfo,
            target,
            targetProperty,
            targetAstProperty,
            model,
            modelProperty,
            modelAstProperty));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static UpdateStrategyInfo createStrategy(Expression expression) {
    expression = AstNodeUtils.getActualVariableExpression(expression);
    return new UpdateStrategyInfo(expression.toString());
  }

  private ObserveInfo getObserveInfo(Expression expression) throws Exception {
    ObserveInfo observeInfo = getComponentObserveInfo(expression);
    if (observeInfo != null) {
      return observeInfo;
    }
    return getBeanObserveInfo(expression);
  }

  private ObserveInfo getBeanObserveInfo(Expression expression) throws Exception {
    BeansObserveTypeContainer beansContainer =
        (BeansObserveTypeContainer) m_provider.getContainer(ObserveType.BEANS);
    return beansContainer.resolve(expression);
  }

  private ObserveInfo getComponentObserveInfo(Expression expression) throws Exception {
    ComponentsObserveTypeContainer componentsContainer =
        (ComponentsObserveTypeContainer) m_provider.getContainer(ObserveType.WIDGETS);
    return componentsContainer.resolve(expression);
  }

  private static void assertEquals(ObserveInfo property, PropertyInfo astProperty) {
    if (property instanceof ElPropertyObserveInfo) {
      Assert.instanceOf(ElPropertyInfo.class, astProperty);
    } else {
      GenericUtils.assertEquals(property.getObjectType(), astProperty.getValueType());
    }
  }

  private static void assertObserves(IGenericType[] types,
      int index,
      ObserveInfo observe,
      PropertyInfo astProperty,
      ObserveInfo property) {
    GenericUtils.assertEquals(observe.getObjectType(), astProperty.getSourceObjectType());
    GenericUtils.assertEquals(types[index], astProperty.getSourceObjectType());
    GenericUtils.assertEquals(types[index + 1], astProperty.getValueType());
    if (property.getObjectType() != null) {
      assertEquals(property, astProperty);
    }
  }

  private static ObserveInfo createDefaultProperty(PropertyInfo astProperty) {
    String text = "";
    if (astProperty instanceof BeanPropertyInfo) {
      BeanPropertyInfo beanProperty = (BeanPropertyInfo) astProperty;
      text = beanProperty.getPath();
    }
    return new UndefineObserveInfo(text, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void preCommit() throws Exception {
    JavaInfoReferenceProvider.LAZY_DETECTED = true;
  }

  /**
   * Save model changes to source code.
   */
  public boolean commit() throws Exception {
    JavaInfo javaInfoRoot = m_provider.getJavaInfoRoot();
    AstEditor editor = javaInfoRoot.getEditor();
    IJavaProject javaProject = editor.getJavaProject();
    CodeGenerationSupport generationSupport =
        new CodeGenerationSupport(CoreUtils.useGenerics(javaProject),
            new ContainerAstObjectInfo(m_bindings));
    //
    boolean reparse = DataBindingsCodeUtils.ensureDBLibraries(javaProject);
    // prepare source code
    List<String> methodLines = new ArrayList<>();
    //
    if (m_addInitializeContext) {
      methodLines.add("initializeBindings();");
      methodLines.add("//");
    }
    //
    int size = m_bindings.size();
    for (int i = 0; i < size; i++) {
      BindingInfo binding = m_bindings.get(i);
      if (i > 0 && binding.addSourceCodeSeparator()) {
        methodLines.add("//");
      }
      binding.addSourceCode(methodLines, generationSupport);
    }
    // handle group
    if (m_addToGroup) {
      String groupVariable = generationSupport.generateLocalName("bindingGroup");
      //
      methodLines.add("//");
      methodLines.add(
          "org.jdesktop.beansbinding.BindingGroup "
              + groupVariable
              + " = new org.jdesktop.beansbinding.BindingGroup();");
      methodLines.add("//");
      //
      for (BindingInfo binding : m_bindings) {
        if (!binding.isManaged()) {
          methodLines.add(groupVariable + ".addBinding(" + binding.getVariableIdentifier() + ");");
        }
      }
      //
      if (m_addPostInitializeContext) {
        methodLines.add("//");
        methodLines.add("postInitializeBindings();");
        methodLines.add("//");
      }
      //
      methodLines.add("return " + groupVariable + ";");
    } else if (m_addPostInitializeContext) {
      methodLines.add("//");
      methodLines.add("postInitializeBindings();");
    }
    //
    if (m_initDataBindings != null) {
      editor.removeBodyDeclaration(m_initDataBindings);
    }
    // create or replace initDataBindings() method
    MethodDeclaration lastInfoMethod =
        DataBindingsCodeUtils.getLastInfoDeclaration(m_initDataBindings, javaInfoRoot);
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(javaInfoRoot);
    BodyDeclarationTarget target = new BodyDeclarationTarget(typeDeclaration, null, false);
    m_initDataBindings = editor.addMethodDeclaration(
        createMethodHeader(m_addToGroup, lastInfoMethod),
        methodLines,
        target);
    // check call initDataBindings() after creation all components
    DataBindingsCodeUtils.ensureInvokeInitDataBindings(editor, lastInfoMethod);
    //
    return reparse;
  }

  public void postCommit() throws Exception {
    JavaInfoReferenceProvider.LAZY_DETECTED = false;
  }

  private static String createMethodHeader(boolean addToGroup, MethodDeclaration lastInfoMethod)
      throws Exception {
    String returnType = addToGroup ? "org.jdesktop.beansbinding.BindingGroup " : "void ";
    int access = 1; // XXX
    // check static
    if (Modifier.isStatic(lastInfoMethod.getModifiers())) {
      return ACCESS_VALUES[access]
          + "static "
          + returnType
          + ""
          + INIT_DATA_BINDINGS_METHOD_NAME
          + "()";
    }
    // normal
    return ACCESS_VALUES[access] + returnType + INIT_DATA_BINDINGS_METHOD_NAME + "()";
  }
}