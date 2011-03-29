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
package org.eclipse.wb.internal.rcp.databinding.model;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils;
import org.eclipse.wb.internal.swt.model.widgets.WidgetInfo;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.pde.core.plugin.PluginRegistry;

import java.util.List;

/**
 * Project and code utils.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model
 */
public final class DataBindingsCodeUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public static String getPojoObservablesClass() {
    return "org.eclipse.core.databinding.beans.PojoObservables";
  }

  /**
   * Check add to project classpath or plugin require bundles JFace bindings libraries.
   */
  public static boolean ensureDBLibraries(IJavaProject javaProject) throws Exception {
    // calculate required plugins
    boolean addDatabindingCore =
        !ProjectUtils.hasType(javaProject, "org.eclipse.core.databinding.Binding");
    boolean addDatabindingBeans =
        !ProjectUtils.hasType(javaProject, "org.eclipse.core.databinding.beans.BeansObservables");
    boolean addDatabindingObservable =
        PluginRegistry.findModel("org.eclipse.core.databinding.observable") != null
            && !ProjectUtils.hasType(
                javaProject,
                "org.eclipse.core.databinding.observable.Observables");
    boolean addDatabindingProperty =
        PluginRegistry.findModel("org.eclipse.core.databinding.property") != null
            && !ProjectUtils.hasType(javaProject, "org.eclipse.core.databinding.property.IProperty");
    boolean addDatabindingJFace =
        !ProjectUtils.hasType(javaProject, "org.eclipse.jface.databinding.swt.SWTObservables");
    boolean addEquinoxCommon =
        !ProjectUtils.hasType(javaProject, "org.eclipse.core.runtime.Status");
    boolean addComIbmIcu = !ProjectUtils.hasType(javaProject, "com.ibm.icu.text.NumberFormat");
    // check required plugins
    if (addDatabindingCore
        || addDatabindingBeans
        || addDatabindingObservable
        || addDatabindingProperty
        || addDatabindingJFace) {
      IProject project = javaProject.getProject();
      // check 'java project' or 'plugin project'
      if (project.hasNature("org.eclipse.pde.PluginNature")) {
        // collect plugin imports
        List<String> pluginIds = Lists.newArrayList();
        if (addDatabindingCore) {
          pluginIds.add("org.eclipse.core.databinding");
        }
        if (addDatabindingBeans) {
          pluginIds.add("org.eclipse.core.databinding.beans");
        }
        if (addDatabindingObservable) {
          pluginIds.add("org.eclipse.core.databinding.observable");
        }
        if (addDatabindingProperty) {
          pluginIds.add("org.eclipse.core.databinding.property");
        }
        if (addDatabindingJFace) {
          pluginIds.add("org.eclipse.jface.databinding");
        }
        if (addEquinoxCommon) {
          pluginIds.add("org.eclipse.equinox.common");
        }
        if (addComIbmIcu) {
          pluginIds.add("com.ibm.icu");
        }
        // add to plugin imports
        if (!pluginIds.isEmpty()) {
          PdeUtils.get(project).addPluginImport(pluginIds);
        }
      } else {
        // add to project .classpath
        if (addDatabindingCore) {
          ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.core.databinding");
        }
        if (addDatabindingBeans) {
          ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.core.databinding.beans");
        }
        if (addDatabindingObservable) {
          ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.core.databinding.observable");
        }
        if (addDatabindingProperty) {
          ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.core.databinding.property");
        }
        if (addDatabindingJFace) {
          ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.jface.databinding");
        }
        if (addEquinoxCommon) {
          ProjectUtils.addPluginLibraries(javaProject, "org.eclipse.equinox.common");
        }
        if (addComIbmIcu) {
          ProjectUtils.addPluginLibraries(javaProject, "com.ibm.icu");
        }
      }
      ProjectUtils.waitForAutoBuild();
      return true;
    }
    return false;
  }

  /**
   * Check add to client {@link IJavaProject} Designer support code for TreeViewer.
   */
  public static void ensureDesignerResources(IJavaProject javaProject) throws Exception {
    String[] classes =
        {
            "Utils",
            "IdentityWrapper",
            "ListenerSupport",
            "TreeObservableLabelProvider",
            "TreeBeanAdvisor",
            "BeansObservableFactory",
            "BeansListObservableFactory",
            "BeansSetObservableFactory"};
    for (int i = 0; i < classes.length; i++) {
      ProjectUtils.ensureResourceType(
          javaProject,
          Activator.getDefault().getBundle(),
          "org.eclipse.wb.rcp.databinding." + classes[i]);
    }
  }

  /**
   * @return {@link MethodDeclaration} for last {@link JavaInfo} into hierarchy.
   */
  public static MethodDeclaration getLastInfoDeclaration(MethodDeclaration initDataBindings,
      JavaInfo rootJavaInfo) throws Exception {
    LastWidgetVisitor visitor = new LastWidgetVisitor(initDataBindings, rootJavaInfo);
    MethodDeclaration method = JavaInfoUtils.getMethodDeclaration(visitor.getLastInfo());
    Assert.isNotNull(method);
    return method;
  }

  /**
   * Check add invocation <code>initDataBindings()</code> to method {@link MethodDeclaration}
   * <code>lastInfoMethod</code>.
   */
  public static void ensureInvokeInitDataBindings(JavaInfo rootJavaInfo,
      AstEditor editor,
      TypeDeclaration typeDeclaration,
      MethodDeclaration lastInfoMethod) throws Exception {
    IPreferenceStore store = Activator.getStore();
    if (!store.getBoolean(IPreferenceConstants.ADD_INVOKE_INITDB_TO_GUI)) {
      return;
    }
    if (rootJavaInfo != null
        && !store.getBoolean(IPreferenceConstants.ADD_INVOKE_INITDB_TO_COMPOSITE_CONSTRUCTOR)
        && JavaInfoUtils.getMethodDeclaration(rootJavaInfo).isConstructor()
        && isComposite(rootJavaInfo)) {
      return;
    }
    // find call initDataBindings()
    InitDataBindingsVisitor visitor = new InitDataBindingsVisitor();
    lastInfoMethod.accept(visitor);
    //
    if (visitor.isInvoke()) {
      return;
    }
    // prepare invoke source
    String initDBInvokeSource = prepareDBInvokeSource(editor, typeDeclaration, lastInfoMethod);
    // add initDataBindings()
    Statement shellOpenStatement = visitor.getShellOpenStatement();
    List<Statement> statements = DomGenerics.statements(lastInfoMethod.getBody());
    StatementTarget methodTarget;
    //
    if (shellOpenStatement != null) {
      methodTarget = new StatementTarget(shellOpenStatement, true);
    } else if (statements.isEmpty()) {
      methodTarget = new StatementTarget(lastInfoMethod, true);
    } else {
      Statement lastStatement = statements.get(statements.size() - 1);
      methodTarget = new StatementTarget(lastStatement, lastStatement instanceof ReturnStatement);
    }
    //
    editor.addStatement(initDBInvokeSource, methodTarget);
  }

  private static boolean isComposite(JavaInfo javaInfo) throws Exception {
    Class<?> componentClass = javaInfo.getDescription().getComponentClass();
    if (org.eclipse.swt.widgets.Composite.class.isAssignableFrom(componentClass)) {
      return !org.eclipse.swt.widgets.Decorations.class.isAssignableFrom(componentClass);
    }
    return false;
  }

  private static String prepareDBInvokeSource(AstEditor editor,
      TypeDeclaration typeDeclaration,
      MethodDeclaration lastInfoMethod) throws Exception {
    if (Modifier.isStatic(lastInfoMethod.getModifiers())
        || !Activator.getStore().getBoolean(IPreferenceConstants.ADD_INITDB_TO_FIELD)) {
      return DataBindingsRootInfo.INIT_DATA_BINDINGS_METHOD_NAME + "();";
    }
    // check create context field
    for (FieldDeclaration field : typeDeclaration.getFields()) {
      Type type = field.getType();
      if (type == null || AstNodeUtils.getTypeBinding(type) == null) {
        continue;
      }
      //
      String fieldType = AstNodeUtils.getFullyQualifiedName(type, false);
      //
      if ("org.eclipse.core.databinding.DataBindingContext".equals(fieldType)) {
        Assert.equals(1, field.fragments().size());
        VariableDeclarationFragment fragment = DomGenerics.fragments(field).get(0);
        return fragment.getName().getIdentifier()
            + " = "
            + DataBindingsRootInfo.INIT_DATA_BINDINGS_METHOD_NAME
            + "();";
      }
    }
    // create context field
    BodyDeclarationTarget fieldTarget = new BodyDeclarationTarget(typeDeclaration, null, true);
    editor.addFieldDeclaration(
        "private org.eclipse.core.databinding.DataBindingContext m_bindingContext;",
        fieldTarget);
    return "m_bindingContext = " + DataBindingsRootInfo.INIT_DATA_BINDINGS_METHOD_NAME + "();";
  }

  /**
   * Check move source code from <code><b>public static void main(String[])</b></code> to
   * <code>Realm.runWithDefault()</code>.
   */
  public static void ensureEnclosingRealmOfMain(AstEditor editor) throws Exception {
    TypeDeclaration type = DomGenerics.types(editor.getAstUnit()).get(0);
    MethodDeclaration mainMethod =
        AstNodeUtils.getMethodBySignature(type, "main(java.lang.String[])");
    //
    if (mainMethod == null) {
      return;
    }
    // check Realm enclosing
    RealmMethodVisitor visitor = new RealmMethodVisitor();
    mainMethod.accept(visitor);
    if (visitor.isEnclosing()) {
      return;
    }
    //
    List<Statement> oldStatements =
        Lists.newArrayList(DomGenerics.statements(mainMethod.getBody()));
    //
    Statement displayStatement =
        editor.addStatement(
            "org.eclipse.swt.widgets.Display display = org.eclipse.swt.widgets.Display.getDefault();",
            new StatementTarget(mainMethod, true));
    //
    List<String> lines = Lists.newArrayList();
    lines.add("org.eclipse.core.databinding.observable.Realm.runWithDefault(org.eclipse.jface.databinding.swt.SWTObservables.getRealm(display), new java.lang.Runnable() {");
    lines.add("\tpublic void run() {");
    lines.add("\t}");
    lines.add("});");
    //
    Statement realmStatement =
        editor.addStatement(lines, new StatementTarget(displayStatement, false));
    //
    final Block[] runBody = new Block[1];
    realmStatement.accept(new ASTVisitor() {
      @Override
      public boolean visit(MethodDeclaration node) {
        runBody[0] = node.getBody();
        return false;
      }
    });
    //
    Assert.isNotNull(runBody[0]);
    StatementTarget moveTarget = new StatementTarget(runBody[0], false);
    //
    for (Statement statement : oldStatements) {
      editor.moveStatement(statement, moveTarget);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visitors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This visitor do find last {@link WidgetInfo} into hierarchy.
   */
  private static class LastWidgetVisitor extends ObjectInfoVisitor {
    private final MethodDeclaration m_initDataBindings;
    private JavaInfo m_lastInfo;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public LastWidgetVisitor(MethodDeclaration initDataBindings, JavaInfo rootJavaInfo)
        throws Exception {
      m_initDataBindings = initDataBindings;
      m_lastInfo = rootJavaInfo;
      rootJavaInfo.accept0(this);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public JavaInfo getLastInfo() {
      return m_lastInfo;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ObjectInfoVisitor
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean visit(ObjectInfo objectInfo) throws Exception {
      if (objectInfo instanceof WidgetInfo) {
        WidgetInfo widgetInfo = (WidgetInfo) objectInfo;
        if (JavaInfoUtils.getMethodDeclaration(widgetInfo) != m_initDataBindings) {
          m_lastInfo = widgetInfo;
        }
      }
      return true;
    }
  }
  /**
   * This visitor do find invocation <code>initDataBindings()</code> into AST.
   */
  private static class InitDataBindingsVisitor extends ASTVisitor {
    private boolean m_invoke;
    private Statement m_shellOpenStatement;

    ////////////////////////////////////////////////////////////////////////////
    //
    // ASTVisitor
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean visit(MethodInvocation node) {
      String methodName = node.getName().getIdentifier();
      if (node.arguments().isEmpty()) {
        if ("initDataBindings".equals(methodName)) {
          Assert.isTrue(!m_invoke, "Double invoke initDataBindings()");
          m_invoke = true;
        } else if ("open".equals(methodName)
            && AstNodeUtils.isSuccessorOf(node.getExpression(), "org.eclipse.swt.widgets.Shell")) {
          Assert.isNull(m_shellOpenStatement, "Double invoke %shell%.open()");
          m_shellOpenStatement = AstNodeUtils.getEnclosingStatement(node);
        }
      }
      return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public boolean isInvoke() {
      return m_invoke;
    }

    public Statement getShellOpenStatement() {
      return m_shellOpenStatement;
    }
  }
  /**
   * This visitor do find invocation <code>Realm.runWithDefault()</code> into AST.
   */
  private static class RealmMethodVisitor extends ASTVisitor {
    private boolean m_enclosing;

    ////////////////////////////////////////////////////////////////////////////
    //
    // ASTVisitor
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean visit(MethodInvocation invocation) {
      if (!m_enclosing) {
        m_enclosing =
            "org.eclipse.core.databinding.observable.Realm.runWithDefault(org.eclipse.core.databinding.observable.Realm,java.lang.Runnable)".equals(CoreUtils.getMethodSignature(invocation));
      }
      return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public boolean isEnclosing() {
      return m_enclosing;
    }
  }
}