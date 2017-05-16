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
package org.eclipse.wb.internal.core.model.property.event;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.GenericTypeResolver;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AnonymousTypeDeclaration;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.AstParser;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.FocusListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link Property} for single {@link ListenerInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.events
 */
final class ListenerMethodProperty extends AbstractEventProperty
    implements
      IPreferenceConstants,
      IListenerMethodProperty {
  private final IPreferenceStore m_preferences;
  private final ListenerInfo m_listener;
  private final ListenerMethodInfo m_method;
  private final ListenerMethodProperty[] m_siblings;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ListenerMethodProperty(JavaInfo javaInfo,
      ListenerInfo listener,
      ListenerMethodInfo method,
      ListenerMethodProperty[] siblings) {
    super(javaInfo, getTitle(listener, method), ListenerMethodPropertyEditor.INSTANCE);
    m_preferences = javaInfo.getDescription().getToolkit().getPreferences();
    m_listener = listener;
    m_method = method;
    m_siblings = siblings;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Title
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the reduced title of {@link ListenerMethodInfo}, for example use "gained" instead of
   *         "focusGained" for {@link FocusListener}.
   */
  private static String getTitle(ListenerInfo listener, ListenerMethodInfo method) {
    String listenerTitle = listener.getName();
    String methodTitle = method.getName();
    if (methodTitle.startsWith(listenerTitle) && !methodTitle.equals(listenerTitle)) {
      return StringUtils.uncapitalize(methodTitle.substring(listenerTitle.length()));
    }
    return methodTitle;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IProperty
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isModified() throws Exception {
    MethodDeclaration listenerMethod = findListenerMethod();
    if (listenerMethod != null) {
      if (hasStubRouting(listenerMethod)) {
        return findStubMethod_orNull(listenerMethod) != null;
      }
      return true;
    }
    // no listener method
    return false;
  }

  @Override
  public void setValue(Object value) throws Exception {
    Assert.isTrue(value == UNKNOWN_VALUE, "Unsupported value |%s|.", value);
    ExecutionUtils.run(m_javaInfo, new RunnableEx() {
      public void run() throws Exception {
        removeListenerMethod();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ListenerInfo}.
   */
  public ListenerInfo getListener() {
    return m_listener;
  }

  /**
   * @return the {@link ListenerMethodInfo}.
   */
  public ListenerMethodInfo getMethod() {
    return m_method;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Remove support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Removes stub/handler for this listener method.
   */
  private void removeListenerMethod() throws Exception {
    // prepare listener type
    TypeDeclaration listenerType = findListenerType();
    if (listenerType == null) {
      return;
    }
    // prepare listener method
    MethodDeclaration listenerMethod = findListenerMethod();
    if (listenerMethod == null) {
      return;
    }
    // if "listenerType" is "this" type, then we implement interface, so remove only stub
    if (listenerType == JavaInfoUtils.getTypeDeclaration(m_javaInfo)) {
      removeStubMethod(listenerMethod);
      return;
    }
    // check that listener has adapter and adapter is used
    if (m_listener.hasAdapter()
        && AstNodeUtils.isSuccessorOf(
            AstNodeUtils.getTypeBinding(listenerType),
            m_listener.getAdapter())) {
      // ask confirmation about method remove
      if (!MessageDialog.openConfirm(
          DesignerPlugin.getShell(),
          ModelMessages.ListenerMethodProperty_deleteMethodTitle,
          MessageFormat.format(
              ModelMessages.ListenerMethodProperty_deleteMethodMessage,
              m_method.getName()))) {
        return;
      }
      // remove method
      removeListenerMethod(listenerType, listenerMethod);
    } else {
      // ask confirmation about listener remove
      if (!MessageDialog.openConfirm(
          DesignerPlugin.getShell(),
          ModelMessages.ListenerMethodProperty_deleteListenerTitle,
          MessageFormat.format(
              ModelMessages.ListenerMethodProperty_deleteListenerMessage,
              m_listener.getName()))) {
        return;
      }
      // remove listener
      removeListener();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listener type support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TypeDeclaration} that handles this event listener.
   */
  TypeDeclaration findListenerType() {
    Expression argument = getListenerExpression();
    if (argument != null) {
      // check for "this"
      if (argument instanceof ThisExpression) {
        return AstNodeUtils.getEnclosingType(argument);
      }
      // check for listener creation
      if (argument instanceof ClassInstanceCreation) {
        ClassInstanceCreation creation = (ClassInstanceCreation) argument;
        // check for anonymous class
        if (creation.getAnonymousClassDeclaration() != null) {
          return AnonymousTypeDeclaration.create(creation.getAnonymousClassDeclaration());
        }
        // find inner type
        return AstNodeUtils.getTypeDeclaration(creation);
      }
    }
    // no listener found
    return null;
  }

  /**
   * @return the {@link Expression} that represents listener reference, may be creation
   *         <code>new FocusListener()</code>, may be <code>this</code> reference, etc. May return
   *         <code>null</code> if no listener yet.
   */
  private Expression getListenerExpression() {
    Expression expression = getListenerExpression0();
    if (expression != null) {
      ExecutionFlowDescription flow = JavaInfoUtils.getState(m_javaInfo).getFlowDescription();
      expression = ExecutionFlowUtils.getFinalExpression(flow, expression);
    }
    return expression;
  }

  /**
   * @return the {@link Expression} used as direct argument for <code>addXListener()</code> or
   *         constructor.
   */
  private Expression getListenerExpression0() {
    String addListenerMethodSignature = m_listener.getMethodSignature();
    // try to find listener adding
    MethodInvocation invocation = m_javaInfo.getMethodInvocation(addListenerMethodSignature);
    if (invocation != null) {
      return (Expression) invocation.arguments().get(0);
    }
    // try to find listener in constructor
    if (m_javaInfo.getCreationSupport() instanceof ConstructorCreationSupport) {
      ConstructorCreationSupport creationSupport =
          (ConstructorCreationSupport) m_javaInfo.getCreationSupport();
      for (ParameterDescription parameter : creationSupport.getDescription().getParameters()) {
        String listenerMethodTag = parameter.getTag("events: add listener method");
        if (addListenerMethodSignature.equals(listenerMethodTag)) {
          return DomGenerics.arguments(creationSupport.getCreation()).get(parameter.getIndex());
        }
      }
    }
    // no listener found
    return null;
  }

  /**
   * Removes the listener {@link TypeDeclaration} with all its methods and stubs (if enabled).
   */
  void removeListener() throws Exception {
    // prepare listener TypeDeclaration now, when we have so reference on it via addXXXListener()
    TypeDeclaration listenerType = findListenerType();
    // delete inner or "simple"
    if (listenerType != null && listenerType.getParent() instanceof TypeDeclaration) {
      removeListener_inner(listenerType);
    } else {
      // remove stubs
      if (m_preferences.getBoolean(P_DELETE_STUB)) {
        removeListenerStubs();
      }
      // remove addXXXListener()
      m_javaInfo.removeMethodInvocations(m_listener.getMethodSignature());
    }
    // refresh
    ExecutionUtils.refresh(m_javaInfo);
  }

  /**
   * Implementation of {@link #removeListener()} for inner type.
   */
  private void removeListener_inner(TypeDeclaration listenerType) throws Exception {
    List<ClassInstanceCreation> listenerCreations =
        AstNodeUtils.getClassInstanceCreations(listenerType);
    boolean removeAllListenerArtifacts = true;
    if (listenerCreations.size() > 1) {
      if (m_javaInfo.isDeleting()) {
        removeAllListenerArtifacts = false;
      } else {
        String message =
            MessageFormat.format(
                ModelMessages.ListenerMethodProperty_deleteAllListenerUsagesMessage,
                m_listener.getName());
        if (!MessageDialog.openQuestion(
            DesignerPlugin.getShell(),
            ModelMessages.ListenerMethodProperty_deleteAllListenerUsagesTitle,
            message)) {
          removeAllListenerArtifacts = false;
        }
      }
    }
    // remove stubs
    if (m_preferences.getBoolean(P_DELETE_STUB) && removeAllListenerArtifacts) {
      removeListenerStubs();
    }
    // remove addXXXListener()
    m_javaInfo.removeMethodInvocations(m_listener.getMethodSignature());
    // remove also "inner" type
    if (removeAllListenerArtifacts) {
      for (ClassInstanceCreation classInstanceCreation : listenerCreations) {
        m_javaInfo.getEditor().removeEnclosingStatement(classInstanceCreation);
      }
      m_javaInfo.getEditor().removeBodyDeclaration(listenerType);
    }
  }

  /**
   * Removes stubs for this {@link ListenerInfo}.
   */
  private void removeListenerStubs() throws Exception {
    for (ListenerMethodProperty property : m_siblings) {
      MethodDeclaration listenerMethod = property.findListenerMethod();
      if (listenerMethod != null) {
        property.removeStubMethod(listenerMethod);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Inner class
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link BodyDeclarationTarget} for adding new inner class listener.
   */
  private BodyDeclarationTarget getListenerInnerClassTarget() {
    int position = m_preferences.getInt(P_INNER_POSITION);
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(m_javaInfo);
    List<BodyDeclaration> declarations = DomGenerics.bodyDeclarations(typeDeclaration);
    // first declaration
    if (position == V_INNER_FIRST) {
      return new BodyDeclarationTarget(typeDeclaration, true);
    }
    // last declaration
    if (position == V_INNER_LAST) {
      return new BodyDeclarationTarget(typeDeclaration, false);
    }
    // before first listener, or first declaration
    if (position == V_INNER_BEFORE) {
      // try to find first existing inner listener class
      for (BodyDeclaration declaration : declarations) {
        if (declaration instanceof TypeDeclaration) {
          ITypeBinding binding = AstNodeUtils.getTypeBinding((TypeDeclaration) declaration);
          if (AstNodeUtils.isSuccessorOf(binding, "java.util.EventListener")) {
            return new BodyDeclarationTarget(declaration, true);
          }
        }
      }
      // by default as first declaration
      return new BodyDeclarationTarget(typeDeclaration, true);
    }
    // after last listener, or last declaration
    if (position == V_INNER_AFTER) {
      // by default as last declaration
      BodyDeclarationTarget target = new BodyDeclarationTarget(typeDeclaration, false);
      // try to find last existing inner class listener
      for (BodyDeclaration declaration : declarations) {
        if (declaration instanceof TypeDeclaration) {
          ITypeBinding binding = AstNodeUtils.getTypeBinding((TypeDeclaration) declaration);
          if (AstNodeUtils.isSuccessorOf(binding, "java.util.EventListener")) {
            target = new BodyDeclarationTarget(declaration, false);
          }
        }
      }
      //
      return target;
    }
    //
    throw new IllegalArgumentException("Unknown position for inner class: " + position);
  }

  /**
   * @return the new inner {@link TypeDeclaration} for event listener.
   */
  private TypeDeclaration addListenerInnerClassDeclaration() throws Exception {
    // prepare target
    BodyDeclarationTarget target = getListenerInnerClassTarget();
    // prepare header
    String headerSource;
    if (m_listener.hasAdapter()) {
      headerSource = " extends " + m_listener.getAdapter().getCanonicalName();
    } else {
      Class<?> listenerType = m_listener.getInterface();
      if (listenerType.isInterface()) {
        headerSource = " implements " + getListenerTypeNameSource();
      } else {
        headerSource = " extends " + getListenerTypeNameSource();
      }
    }
    // add inner class
    List<String> lines =
        ImmutableList.of("private class " + createInnerClassName() + headerSource + " {", "}");
    return m_javaInfo.getEditor().addTypeDeclaration(lines, target);
  }

  /**
   * @return the unique name of inner {@link TypeDeclaration} for event listener.
   */
  private String createInnerClassName() {
    Map<String, String> valueMap = Maps.newTreeMap();
    {
      String componentName = getComponentName(m_javaInfo);
      valueMap.put("component_name", componentName);
      valueMap.put("Component_name", StringUtils.capitalize(componentName));
      //
      String componentType =
          CodeUtils.getShortClass(m_javaInfo.getDescription().getComponentClass().getName());
      valueMap.put("component_className", componentType);
      valueMap.put("Component_className", StringUtils.capitalize(componentType));
      //
      String listenerMethodName =
          CodeUtils.getShortClass(m_listener.getInterface().getCanonicalName());
      valueMap.put("listener_className", listenerMethodName);
      valueMap.put("Listener_className", StringUtils.capitalize(listenerMethodName));
      //
      String listenerName = m_listener.getSimpleName();
      valueMap.put("listener_name", listenerName);
      valueMap.put("Listener_name", StringUtils.capitalize(listenerName));
    }
    // generate base name
    String template = m_preferences.getString(P_INNER_NAME_TEMPLATE);
    String baseName = StrSubstitutor.replace(template, valueMap);
    // generate unique name
    return m_javaInfo.getEditor().getUniqueTypeName(baseName);
  }

  /**
   * @return the name of host {@link JavaInfo}.
   */
  private static String getComponentName(JavaInfo javaInfo) {
    return javaInfo.getVariableSupport().getComponentName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listener method support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the listener method. If there are no such method, creates it.
   */
  private MethodDeclaration ensureListenerMethod() throws Exception {
    MethodDeclaration listenerMethod = findListenerMethod();
    if (listenerMethod == null) {
      // ensure listener type
      TypeDeclaration listenerType = findListenerType();
      if (listenerType == null) {
        boolean implementInterfaceMethods = !m_listener.hasAdapter();
        int eventCodeType = m_preferences.getInt(P_CODE_TYPE);
        if (eventCodeType == V_CODE_ANONYMOUS) {
          // prepare listener source
          String source = "new " + getListenerTypeNameSource() + "() {\n}";
          // add listener and get added listener type
          m_javaInfo.addMethodInvocation(m_listener.getMethodSignature(), source);
          listenerType = findListenerType();
        } else if (eventCodeType == V_CODE_INNER_CLASS) {
          // add listener inner class
          listenerType = addListenerInnerClassDeclaration();
          // use listener
          String source = "new " + listenerType.getName().getIdentifier() + "()";
          m_javaInfo.addMethodInvocation(m_listener.getMethodSignature(), source);
        } else if (eventCodeType == V_CODE_INTERFACE) {
          // prepare listener type
          listenerType = JavaInfoUtils.getTypeDeclaration(m_javaInfo);
          implementInterfaceMethods =
              m_javaInfo.getEditor().ensureInterfaceImplementation(
                  listenerType,
                  m_listener.getInterface().getCanonicalName());
          // add listener
          m_javaInfo.addMethodInvocation(m_listener.getMethodSignature(), "this");
        }
        // implement all methods, if there are no adapter
        if (implementInterfaceMethods) {
          List<ListenerMethodInfo> interfaceMethods = m_listener.getMethods();
          for (ListenerMethodInfo interfaceMethodInfo : interfaceMethods) {
            if (interfaceMethodInfo.isAbstract()) {
              addListenerMethod(listenerType, interfaceMethodInfo);
            }
          }
        }
      }
      // ensure listener method
      {
        listenerMethod = findListenerMethod();
        if (listenerMethod == null) {
          listenerMethod = addListenerMethod(listenerType, m_method);
        }
      }
    }
    // return listener method
    return listenerMethod;
  }

  /**
   * @return the name of listener type, including generic arguments.
   */
  private String getListenerTypeNameSource() {
    // simple case - no generics
    {
      Class<?> listenerType = m_listener.getListenerType();
      if (listenerType.getTypeParameters().length == 0) {
        return listenerType.getCanonicalName();
      }
    }
    // listener with generics
    Type listenerType = m_listener.getMethod().getGenericParameterTypes()[0];
    GenericTypeResolver resolver_2 = m_listener.getResolver();
    return GenericsUtils.getTypeName(resolver_2, listenerType);
  }

  /**
   * Adds listener method for given {@link ListenerMethodInfo} in given {@link TypeDeclaration}.
   */
  private MethodDeclaration addListenerMethod(TypeDeclaration typeDeclaration,
      ListenerMethodInfo methodInfo) throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    // prepare annotations
    List<String> annotations = Lists.newArrayList();
    if (shouldAppendOverride(typeDeclaration, methodInfo)) {
      annotations.add("@Override");
    }
    // prepare parameter names
    String[] parameterNames = null;
    {
      String listenerTypeName = m_listener.getListenerType().getCanonicalName();
      IType listenerType = editor.getJavaProject().findType(listenerTypeName);
      IMethod listenerMethod = CodeUtils.findMethod(listenerType, methodInfo.getSignature());
      parameterNames = listenerMethod.getParameterNames();
    }
    // prepare header code
    String headerCode;
    {
      // prepare parameters
      String parametersCode = "";
      {
        String[] parameterTypes = methodInfo.getActualParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
          String parameterType = parameterTypes[i];
          // comma
          if (parametersCode.length() != 0) {
            parametersCode += ", ";
          }
          // append type
          if (m_preferences.getBoolean(P_FINAL_PARAMETERS)) {
            parametersCode += "final ";
          }
          parametersCode += parameterType;
          parametersCode += " ";
          // append name
          parametersCode += parameterNames[i];
        }
      }
      // prepare full header code
      headerCode =
          "public "
              + methodInfo.getMethod().getReturnType().getName()
              + " "
              + methodInfo.getName()
              + "("
              + parametersCode
              + ")";
    }
    // prepare body
    List<String> bodyLines = getListenerMethodBody(methodInfo);
    // add method
    BodyDeclarationTarget target = new BodyDeclarationTarget(typeDeclaration, false);
    return editor.addMethodDeclaration(annotations, headerCode, bodyLines, target);
  }

  /**
   * In Java5 we should append "@Override" annotation when override adapter methods.
   */
  private boolean shouldAppendOverride(TypeDeclaration type, ListenerMethodInfo method)
      throws Exception {
    IJavaProject javaProject = m_javaInfo.getEditor().getJavaProject();
    ListenerInfo listener = method.getListener();
    if (ProjectUtils.isJDK15(javaProject) && listener.hasAdapter()) {
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(type);
      ITypeBinding superTypeBinding = typeBinding.getSuperclass();
      return AstNodeUtils.isSuccessorOf(superTypeBinding, listener.getAdapter());
    }
    return false;
  }

  /**
   * Sometimes we need to tweak it, for example in GWT for TabPanel and
   * TabListener.onBeforeTabSelected we should return <code>true</code>, not <code>false</code> as
   * we do in general case.
   *
   * @param methodInfo
   *
   * @return body of listener method
   */
  private static List<String> getListenerMethodBody(ListenerMethodInfo methodInfo) {
    Class<?> returnType = methodInfo.getMethod().getReturnType();
    if (returnType == Void.TYPE) {
      return ImmutableList.of();
    } else {
      String defaultValue = AstParser.getDefaultValue(returnType.getName());
      return ImmutableList.of("return " + defaultValue + ";");
    }
  }

  /**
   * Removes listener method, can be used only if listener has adapter.
   */
  private void removeListenerMethod(TypeDeclaration typeDeclaration,
      MethodDeclaration listenerMethod) throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    // remove stub, called from listener method
    removeStubMethod(listenerMethod);
    // remove listener method itself
    editor.removeBodyDeclaration(listenerMethod);
    // remove listener if we don't have methods
    if (typeDeclaration.bodyDeclarations().isEmpty()) {
      removeListener();
    }
  }

  /**
   * Removes stub method for given listener method.
   * <p>
   * When we use {@link IPreferenceConstants#V_CODE_INTERFACE} code style, we can not remove
   * listener method itself, instead we should remove stub method and its invocation from listener
   * method.
   */
  private void removeStubMethod(MethodDeclaration listenerMethod) throws Exception {
    MethodDeclaration stubMethod = findStubMethod_orNull(listenerMethod);
    if (stubMethod != null) {
      AstEditor editor = m_javaInfo.getEditor();
      // remove stub invocations
      boolean canDeleteStub = true;
      for (MethodInvocation invocation : AstNodeUtils.getMethodInvocations(stubMethod)) {
        Statement statement = AstNodeUtils.getEnclosingStatement(invocation);
        if (AstNodeUtils.getEnclosingMethod(statement) == listenerMethod) {
          if (statement.getParent() instanceof IfStatement) {
            statement = (Statement) statement.getParent();
          } else if (statement.getParent() instanceof Block
              && statement.getParent().getParent() instanceof IfStatement) {
            statement = (Statement) statement.getParent().getParent();
          }
          editor.removeStatement(statement);
        } else {
          canDeleteStub = false;
        }
      }
      // remove stub method
      if (canDeleteStub) {
        editor.removeBodyDeclaration(stubMethod);
      }
    }
  }

  /**
   * @return the listener method in event listener.
   */
  private MethodDeclaration findListenerMethod() {
    TypeDeclaration listenerType = findListenerType();
    if (listenerType != null) {
      return AstNodeUtils.getMethodBySignature(listenerType, m_method.getSignatureAST());
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Stub method support
  //
  // Stub method is method that really handles event. We can generate anonymous
  // classes that just calls stub methods from its methods.
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Opens stub method (or listener method if there are no stub method).
   */
  public void openStubMethod() throws Exception {
    // prepare method as listener method or stub method
    MethodDeclaration method = findStubMethod();
    if (method == null) {
      method = ExecutionUtils.runObject(m_javaInfo, new RunnableObjectEx<MethodDeclaration>() {
        public MethodDeclaration runObject() throws Exception {
          return ensureStubMethod();
        }
      });
    }
    // open method
    JavaInfoUtils.scheduleOpenNode(m_javaInfo, method);
  }

  /**
   * @return the stub method (if enabled), or listener method. If there are no such method, creates
   *         it.
   */
  private MethodDeclaration ensureStubMethod() throws Exception {
    MethodDeclaration listenerMethod = ensureListenerMethod();
    // create stub, if needed
    if (m_preferences.getBoolean(P_CREATE_STUB)) {
      MethodDeclaration stubMethod = findStubMethod_orNull(listenerMethod);
      if (stubMethod == null) {
        stubMethod = addStubMethod(m_method, listenerMethod);
      }
      return stubMethod;
    }
    // no need to stub, so return listener method
    return listenerMethod;
  }

  /**
   * Adds the stub method and its invocation from given listener method.
   */
  private MethodDeclaration addStubMethod(ListenerMethodInfo methodInfo,
      MethodDeclaration listenerMethod) throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    // don't use stub for inner listener
    if (listenerMethod.getParent().getParent() instanceof TypeDeclaration) {
      return listenerMethod;
    }
    // prepare stub name/signature
    String stubMethodName = getStubMethodName(methodInfo);
    String signature;
    {
      signature = methodInfo.getSignature();
      signature = StringUtils.replace(signature, methodInfo.getName(), stubMethodName);
    }
    // add stub method declaration
    MethodDeclaration stubMethod;
    {
      TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(m_javaInfo);
      // try to find existing stub method
      stubMethod = AstNodeUtils.getMethodBySignature(typeDeclaration, signature);
      // add stub method declaration
      if (stubMethod == null) {
        // prepare modifiers
        String modifiers;
        {
          boolean isStaticContext;
          {
            ExecutionFlowDescription flowDescription = EditorState.get(editor).getFlowDescription();
            isStaticContext = flowDescription.isStatic();
          }
          if (isStaticContext) {
            modifiers = "protected static ";
          } else {
            modifiers = "protected ";
          }
        }
        // prepare header
        String header =
            modifiers
                + "void "
                + stubMethodName
                + "("
                + editor.getParametersSource(listenerMethod)
                + ")";
        // add stub method
        stubMethod =
            editor.addMethodDeclaration(
                header,
                ImmutableList.<String>of(),
                new BodyDeclarationTarget(typeDeclaration, false));
      }
    }
    // add invocation for stub method from listener method
    {
      String lineInvoke =
          stubMethodName
              + "("
              + StringUtils.join(editor.getParameterNames(listenerMethod), ", ")
              + ");";
      // prepare source for stub invocation Statement
      List<String> lines;
      if (m_preferences.getInt(P_CODE_TYPE) == V_CODE_INTERFACE) {
        lines = getConditionalStubInvocationSource(listenerMethod, editor, lineInvoke);
      } else {
        lines = ImmutableList.of(lineInvoke);
      }
      // add Statement that invokes stub
      if (lines != null) {
        StatementTarget target = new StatementTarget(listenerMethod, true);
        TemplateUtils.addStatement(m_javaInfo, target, lines);
      }
    }
    // OK, we've added stub method
    return stubMethod;
  }

  /**
   * <pre>
	 *   if (event.getSource() == m_myComponent) {
	 *     do_myComponent_keyPressed(event);
	 *   }
	 * </pre>
   *
   * @return lines for conditional stub invocation.
   */
  private List<String> getConditionalStubInvocationSource(MethodDeclaration listenerMethod,
      final AstEditor editor,
      String lineInvoke) throws Exception {
    List<SingleVariableDeclaration> parameters = DomGenerics.parameters(listenerMethod);
    for (final SingleVariableDeclaration parameter : parameters) {
      // try to load ComponentDescription for listener method parameter
      ComponentDescription eventClassDescription =
          ExecutionUtils.runObjectIgnore(new RunnableObjectEx<ComponentDescription>() {
            public ComponentDescription runObject() throws Exception {
              ClassLoader editorLoader = EditorState.get(editor).getEditorLoader();
              String parameterTypeName =
                  AstNodeUtils.getFullyQualifiedName(parameter.getType(), true);
              Class<?> parameterType =
                  ReflectionUtils.getClassByName(editorLoader, parameterTypeName);
              return ComponentDescriptionHelper.getDescription(editor, parameterType);
            }
          },
              null);
      // invoke stub only if we can check for our component
      if (eventClassDescription != null) {
        String componentAccess =
            eventClassDescription.getParameter("eventsProperty.componentAccess");
        if (componentAccess != null) {
          String lineIf =
              TemplateUtils.format(
                  "if ({0}{1} == {2}) '{'",
                  parameter.getName().getIdentifier(),
                  componentAccess,
                  m_javaInfo);
          return ImmutableList.of(lineIf, "\t" + lineInvoke, "}");
        }
      }
    }
    // no parameter with "component" access
    return null;
  }

  /**
   * @return the name of the stub method.
   */
  private String getStubMethodName(ListenerMethodInfo methodInfo) {
    Map<String, String> valueMap = Maps.newTreeMap();
    {
      String componentName = getComponentName(m_javaInfo);
      valueMap.put("component_name", componentName);
      valueMap.put("Component_name", StringUtils.capitalize(componentName));
      //
      String componentType =
          CodeUtils.getShortClass(m_javaInfo.getDescription().getComponentClass().getName());
      valueMap.put("component_class_name", componentType);
      valueMap.put("Component_class_name", StringUtils.capitalize(componentType));
      //
      String methodName = methodInfo.getName();
      valueMap.put("event_name", methodName);
      valueMap.put("Event_name", StringUtils.capitalize(methodName));
    }
    //
    String template = m_preferences.getString(P_STUB_NAME_TEMPLATE);
    return StrSubstitutor.replace(template, valueMap);
  }

  /**
   * @return the stub method.
   */
  MethodDeclaration findStubMethod() {
    MethodDeclaration listenerMethod = findListenerMethod();
    return findStubMethod(listenerMethod);
  }

  /**
   * @return the stub method for given listener method.
   */
  private MethodDeclaration findStubMethod(MethodDeclaration listenerMethod) {
    // try to find stub method
    MethodDeclaration stubMethod = findStubMethod_orNull(listenerMethod);
    if (stubMethod != null) {
      return stubMethod;
    }
    // use listener method, if no stub
    return listenerMethod;
  }

  /**
   * @return the stub method for given listener method, or <code>null</code> if listener method has
   *         no stub.
   */
  private MethodDeclaration findStubMethod_orNull(MethodDeclaration listenerMethod) {
    if (listenerMethod == null) {
      return null;
    }
    // direct stub method
    {
      MethodDeclaration stubMethod = findStubMethod(listenerMethod.getBody());
      if (stubMethod != null) {
        return stubMethod;
      }
    }
    // conditional stub method (for "this" listener)
    List<Statement> statements = DomGenerics.statements(listenerMethod.getBody());
    for (Statement statement : statements) {
      if (statement instanceof IfStatement) {
        IfStatement ifStatement = (IfStatement) statement;
        if (ifStatement.getExpression() instanceof InfixExpression) {
          InfixExpression condition = (InfixExpression) ifStatement.getExpression();
          if (condition.getOperator() == InfixExpression.Operator.EQUALS
              && m_javaInfo.isRepresentedBy(condition.getRightOperand())) {
            Statement thenStatement = ifStatement.getThenStatement();
            MethodDeclaration stubMethod = findStubMethod(thenStatement);
            if (stubMethod != null) {
              return stubMethod;
            }
            break;
          }
        }
      }
    }
    // no stub
    return null;
  }

  /**
   * @param listenerStatement
   *          the {@link Statement} that invokes stub method, may be {@link Block} with single other
   *          {@link Statement}.
   *
   * @return the stub {@link MethodDeclaration}, invoked by given {@link Statement}, or
   *         <code>null</code> if no stub invocation found.
   */
  private MethodDeclaration findStubMethod(Statement listenerStatement) {
    List<Statement> statements;
    if (listenerStatement instanceof Block) {
      statements = DomGenerics.statements((Block) listenerStatement);
    } else {
      statements = ImmutableList.of(listenerStatement);
    }
    // analyze statements
    if (statements.size() == 1) {
      Statement statement = statements.get(0);
      if (statement instanceof ExpressionStatement) {
        ExpressionStatement expressionStatement = (ExpressionStatement) statement;
        if (expressionStatement.getExpression() instanceof MethodInvocation) {
          MethodInvocation invocation = (MethodInvocation) expressionStatement.getExpression();
          TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(m_javaInfo);
          String methodSignature = AstNodeUtils.getMethodSignature(invocation);
          return AstNodeUtils.getMethodBySignature(typeDeclaration, methodSignature);
        }
      }
    }
    // no stub
    return null;
  }

  /**
   * @return <code>true</code> if given listener method has {@link IfStatement} that routes
   *         execution flow into some stub method.
   */
  private static boolean hasStubRouting(MethodDeclaration listenerMethod) {
    List<Statement> statements = DomGenerics.statements(listenerMethod.getBody());
    if (!statements.isEmpty() && statements.get(0) instanceof IfStatement) {
      IfStatement ifStatement = (IfStatement) statements.get(0);
      if (ifStatement.getExpression() instanceof InfixExpression) {
        InfixExpression condition = (InfixExpression) ifStatement.getExpression();
        if (condition.getOperator() == InfixExpression.Operator.EQUALS) {
          return true;
        }
      }
    }
    return false;
  }
}
