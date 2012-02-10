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
package org.eclipse.wb.internal.rcp.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.eval.ExecutionFlowUtils2;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.core.model.association.ConstructorParentAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.MethodParameterCreationSupport;
import org.eclipse.wb.internal.core.model.creation.OpaqueCreationSupport;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.model.description.AbstractInvocationDescription;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.variable.MethodParameterVariableSupport;
import org.eclipse.wb.internal.core.model.variable.ThisVariableSupport;
import org.eclipse.wb.internal.core.parser.IJavaInfoParseResolver;
import org.eclipse.wb.internal.core.parser.IParseFactory;
import org.eclipse.wb.internal.core.parser.ParseRootContext;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.BundleClassLoader;
import org.eclipse.wb.internal.core.utils.reflect.CompositeClassLoader;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.rcp.IExceptionConstants;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.rcp.model.e4.E4PartInfo;
import org.eclipse.wb.internal.rcp.model.forms.SectionPartInfo;
import org.eclipse.wb.internal.rcp.model.rcp.ActionFactoryCreationSupport;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.FolderViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutAddViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutCreateFolderInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutCreationSupport;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.FastViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.PerspectiveShortcutInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.ViewShortcutInfo;
import org.eclipse.wb.internal.rcp.model.widgets.DialogInfo;
import org.eclipse.wb.internal.rcp.preferences.IPreferenceConstants;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.ui.PlatformUI;

import java.util.List;

/**
 * {@link IParseFactory} for RCP.
 * 
 * @author scheglov_ke
 * @coverage rcp.parser
 */
public final class ParseFactory extends org.eclipse.wb.internal.swt.parser.ParseFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IParseFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final String[] KNOWN_TYPES = {
      "org.eclipse.swt.widgets.Widget",
      "org.eclipse.swt.widgets.Dialog",
      "org.eclipse.jface.window.Window",
      "org.eclipse.jface.dialogs.DialogPage",
      "org.eclipse.ui.part.WorkbenchPart",
      "org.eclipse.ui.part.Page",
      "org.eclipse.ui.IPerspectiveFactory",
      "org.eclipse.ui.application.ActionBarAdvisor",
      "org.eclipse.ui.forms.SectionPart",
      "org.eclipse.ui.forms.IDetailsPage",
      "org.eclipse.ui.forms.MasterDetailsBlock",
      "org.eclipse.ui.splash.AbstractSplashHandler",};
  public static final String[] NOT_EDITING_TYPES = {
      "org.eclipse.swt.widgets.TabItem",
      "org.eclipse.swt.custom.CTabItem",};

  @Override
  public ParseRootContext getRootContext(AstEditor editor,
      TypeDeclaration typeDeclaration,
      ITypeBinding typeBinding) throws Exception {
    // check for "Chinese problem" - using SWT without configuring project for SWT
    if (editor.getSource().contains("org.eclipse.swt.")
        && editor.getJavaProject().findType("org.eclipse.swt.SWT") == null) {
      throw new DesignerException(IExceptionConstants.NOT_CONFIGURED_FOR_SWT);
    }
    // check for RCP project
    if (!is_RCPContext(editor)) {
      return null;
    }
    // special unsupported classes
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.wizard.IWizard")) {
      throw new DesignerException(IExceptionConstants.NO_DESIGN_WIZARD);
    }
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.ui.part.MultiPageEditorPart")) {
      throw new DesignerException(IExceptionConstants.NO_DESIGN_MP_EDITOR);
    }
    if (AstNodeUtils.isSuccessorOf(typeBinding, NOT_EDITING_TYPES)) {
      throw new DesignerException(IExceptionConstants.NO_DESIGN_WIDGET);
    }
    // check for RCP type
    if (!isRCPObject(typeDeclaration)) {
      return null;
    }
    // prepare class loader
    initializeClassLoader(editor);
    // check for @wbp.parser.entryPoint
    {
      MethodDeclaration method = ExecutionFlowUtils.getExecutionFlow_entryPoint(typeDeclaration);
      if (method != null) {
        List<MethodDeclaration> rootMethods = Lists.newArrayList(method);
        return new ParseRootContext(null, new ExecutionFlowDescription(rootMethods));
      }
    }
    // check for org.eclipse.ui.IPerspectiveFactory
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.ui.IPerspectiveFactory")) {
      ClassLoader classLoader = EditorState.get(editor).getEditorLoader();
      MethodDeclaration createMethod =
          AstNodeUtils.getMethodBySignature(
              typeDeclaration,
              "createInitialLayout(org.eclipse.ui.IPageLayout)");
      SingleVariableDeclaration pageLayoutParameter = DomGenerics.parameters(createMethod).get(0);
      Class<?> pageLayoutClass = classLoader.loadClass("org.eclipse.ui.IPageLayout");
      //
      CreationSupport creationSupport = new PageLayoutCreationSupport(pageLayoutParameter);
      JavaInfo javaInfo = JavaInfoUtils.createJavaInfo(editor, pageLayoutClass, creationSupport);
      if (javaInfo != null) {
        ExecutionFlowUtils2.ensurePermanentValue(pageLayoutParameter.getName()).setModel(javaInfo);
        javaInfo.setVariableSupport(new MethodParameterVariableSupport(javaInfo,
            pageLayoutParameter));
        // prepare root context
        List<MethodDeclaration> rootMethods = ImmutableList.of(createMethod);
        return new ParseRootContext(javaInfo, new ExecutionFlowDescription(rootMethods));
      }
    }
    // support for known RCP super-types
    if (AstNodeUtils.isSuccessorOf(typeBinding, KNOWN_TYPES)) {
      ITypeBinding typeBinding_super = typeBinding.getSuperclass();
      // prepare Class of "super"
      Class<?> superClass;
      {
        superClass = getSuperClass(editor, typeBinding_super);
        if (superClass == Object.class) {
          if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.ui.forms.IDetailsPage")) {
            ClassLoader classLoader = EditorState.get(editor).getEditorLoader();
            superClass = classLoader.loadClass("org.eclipse.ui.forms.IDetailsPage");
          }
        }
        if (superClass == Dialog.class) {
          ClassLoader classLoader = EditorState.get(editor).getEditorLoader();
          superClass = DialogInfo.getNotAbstractDialog(classLoader);
        }
      }
      // prepare creation
      MethodDeclaration constructor = getConstructor(editor, typeDeclaration);
      ThisCreationSupport creationSupport = new ThisCreationSupport(constructor);
      // try to create JavaInfo
      JavaInfo javaInfo = JavaInfoUtils.createJavaInfo(editor, superClass, creationSupport);
      if (javaInfo != null) {
        javaInfo.setVariableSupport(new ThisVariableSupport(javaInfo, constructor));
        // prepare root context
        List<MethodDeclaration> rootMethods = Lists.newArrayList();
        rootMethods.add(constructor);
        if (javaInfo instanceof DialogInfo) {
          DialogInfo.contributeExecutionFlow(typeDeclaration, rootMethods);
        }
        return new ParseRootContext(javaInfo, new ExecutionFlowDescription(rootMethods));
      }
    }
    // E4 part
    for (MethodDeclaration method : typeDeclaration.getMethods()) {
      IAnnotationBinding[] annotations = method.resolveBinding().getAnnotations();
      for (IAnnotationBinding annotation : annotations) {
        String annotationName = annotation.getAnnotationType().getQualifiedName();
        if ("javax.annotation.PostConstruct".equals(annotationName)) {
          for (SingleVariableDeclaration parameter : DomGenerics.parameters(method)) {
            if (AstNodeUtils.isSuccessorOf(
                parameter.getType().resolveBinding(),
                "org.eclipse.swt.widgets.Composite")) {
              // prepare ComponentDescription
              ComponentDescription componentDescription =
                  ComponentDescriptionHelper.getDescription(
                      editor,
                      "org.eclipse.swt.widgets.Composite");
              componentDescription.setToolkit(RcpToolkitDescription.INSTANCE);
              // prepare JavaInfo
              JavaInfo javaInfo =
                  new E4PartInfo(editor,
                      componentDescription,
                      new MethodParameterCreationSupport(parameter));
              javaInfo.setVariableSupport(new MethodParameterVariableSupport(javaInfo, parameter));
              // register JavaInfo
              ObjectInfoUtils.setNewId(javaInfo);
              javaInfo.bindToExpression(parameter.getName());
              // prepare root context
              List<MethodDeclaration> rootMethods = Lists.newArrayList(method);
              return new ParseRootContext(javaInfo, new ExecutionFlowDescription(rootMethods));
            }
          }
        }
      }
    }
    // no root found
    return null;
  }

  @Override
  public JavaInfo create(final AstEditor editor,
      ClassInstanceCreation creation,
      final IMethodBinding methodBinding,
      ITypeBinding typeBinding,
      Expression arguments[],
      JavaInfo argumentInfos[]) throws Exception {
    if (!hasRCP(editor)) {
      return null;
    }
    if (creation.getAnonymousClassDeclaration() != null) {
      typeBinding = typeBinding.getSuperclass();
    }
    // Forms API: SectionPart (wrapper)
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.ui.forms.SectionPart")) {
      final Class<?> creationClass = getClass(editor, typeBinding);
      SectionPartInfo javaInfo =
          (SectionPartInfo) JavaInfoUtils.createJavaInfo(
              editor,
              creationClass,
              new ConstructorCreationSupport(creation));
      AbstractInvocationDescription methodDescription =
          new RunnableObjectEx<AbstractInvocationDescription>() {
            public AbstractInvocationDescription runObject() throws Exception {
              ComponentDescription description =
                  ComponentDescriptionHelper.getDescription(editor, creationClass);
              return description.getConstructor(methodBinding);
            }
          }.runObject();
      javaInfo.getWrapper().configureWrapper(methodDescription, argumentInfos);
      javaInfo.setAssociation(new ConstructorParentAssociation());
      return javaInfo;
    }
    return super.create(editor, creation, methodBinding, typeBinding, arguments, argumentInfos);
  }

  @Override
  public JavaInfo create(AstEditor editor,
      MethodInvocation invocation,
      IMethodBinding methodBinding,
      Expression[] arguments,
      JavaInfo expressionInfo,
      JavaInfo[] argumentInfos,
      IJavaInfoParseResolver javaInfoResolver) throws Exception {
    if (!hasRCP(editor)) {
      return null;
    }
    // check "super"
    {
      JavaInfo javaInfo =
          super.create(
              editor,
              invocation,
              methodBinding,
              arguments,
              expressionInfo,
              argumentInfos,
              javaInfoResolver);
      if (javaInfo != null) {
        return javaInfo;
      }
    }
    // support for org.eclipse.ui.IPerspectiveFactory
    if (expressionInfo instanceof PageLayoutInfo) {
      PageLayoutInfo pageLayout = (PageLayoutInfo) expressionInfo;
      String signature = AstNodeUtils.getMethodSignature(methodBinding);
      if (signature.equals("addView(java.lang.String,int,float,java.lang.String)")
          || signature.equals("addPlaceholder(java.lang.String,int,float,java.lang.String)")
          || signature.equals("addStandaloneView(java.lang.String,boolean,int,float,java.lang.String)")
          || signature.equals("addStandaloneViewPlaceholder(java.lang.String,int,float,java.lang.String,boolean)")) {
        return new PageLayoutAddViewInfo(pageLayout, invocation);
      }
      if (signature.equals("createFolder(java.lang.String,int,float,java.lang.String)")
          || signature.equals("createPlaceholderFolder(java.lang.String,int,float,java.lang.String)")) {
        return new PageLayoutCreateFolderInfo(pageLayout, invocation);
      }
      if (signature.equals("addFastView(java.lang.String)")
          || signature.equals("addFastView(java.lang.String,float)")) {
        return new FastViewInfo(pageLayout, pageLayout.getFastViewContainer(), invocation);
      }
      if (signature.equals("addShowViewShortcut(java.lang.String)")) {
        return new ViewShortcutInfo(pageLayout, pageLayout.getViewShortcutContainer(), invocation);
      }
      if (signature.equals("addPerspectiveShortcut(java.lang.String)")) {
        return new PerspectiveShortcutInfo(pageLayout,
            pageLayout.getPerspectiveShortcutContainer(),
            invocation);
      }
      return null;
    } else if (expressionInfo instanceof PageLayoutCreateFolderInfo) {
      PageLayoutCreateFolderInfo folder = (PageLayoutCreateFolderInfo) expressionInfo;
      String signature = AstNodeUtils.getMethodSignature(methodBinding);
      if (signature.equals("addView(java.lang.String)")
          || signature.equals("addPlaceholder(java.lang.String)")) {
        return new FolderViewInfo(folder, invocation);
      }
    }
    // Action from org.eclipse.ui.actions.ActionFactory
    if (invocation.getExpression() instanceof QualifiedName) {
      QualifiedName qualifiedName = (QualifiedName) invocation.getExpression();
      if (AstNodeUtils.getFullyQualifiedName(qualifiedName.getQualifier(), false).equals(
          "org.eclipse.ui.actions.ActionFactory")) {
        CreationSupport creationSupport =
            new ActionFactoryCreationSupport(invocation, qualifiedName.getName().getIdentifier());
        JavaInfo component =
            JavaInfoUtils.createJavaInfo(
                editor,
                getClass(editor, methodBinding.getReturnType()),
                creationSupport);
        return component;
      }
    }
    // special support for IManagedForm.getToolkit()
    if (invocation.getLocationInParent() == ConstructorInvocation.ARGUMENTS_PROPERTY
        && AstNodeUtils.isMethodInvocation(
            invocation,
            "org.eclipse.ui.forms.IManagedForm",
            "getToolkit()")) {
      CreationSupport creationSupport = new OpaqueCreationSupport(invocation);
      InstanceFactoryInfo toolkit =
          InstanceFactoryInfo.createFactory(
              editor,
              getClass(editor, methodBinding.getReturnType()),
              creationSupport);
      EditorState.get(editor).getTmp_Components().add(toolkit);
      return toolkit;
    }
    // no JavaInfo for MethodInvocation
    return null;
  }

  @Override
  public boolean isToolkitObject(AstEditor editor, ITypeBinding typeBinding) throws Exception {
    return is_RCPContext(editor) && isSWTObject(typeBinding);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_HAS_RCP = ParseFactory.class.getName();

  /**
   * @return <code>true</code> if given {@link AstEditor} can use RCP.
   */
  private static boolean hasRCP(AstEditor editor) {
    Boolean result = (Boolean) editor.getGlobalValue(KEY_HAS_RCP);
    if (result == null) {
      IJavaProject javaProject = editor.getJavaProject();
      try {
        result = javaProject.findType("org.eclipse.swt.custom.SashForm") != null;
      } catch (Throwable e) {
        result = false;
      }
      editor.putGlobalValue(KEY_HAS_RCP, result);
    }
    return result.booleanValue();
  }

  /**
   * @return <code>true</code> if {@link IJavaProject} of given {@link AstEditor} is RCP project.
   */
  private static boolean is_RCPContext(AstEditor editor) throws Exception {
    return editor.getJavaProject().findType("org.eclipse.swt.custom.SashForm") != null;
  }

  /**
   * @return <code>true</code> if given {@link TypeDeclaration} contains RCP objects creations.
   */
  private static boolean isRCPObject(TypeDeclaration typeDeclaration) throws Exception {
    final boolean[] isSwt = new boolean[1];
    // check top level type
    {
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
      if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.widgets.Dialog")
          || AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.preference.PreferencePage")
          || AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.window.Window")
          || AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.ui.IPerspectiveFactory")
          || AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.ui.application.ActionBarAdvisor")) {
        return true;
      }
    }
    // check for Widget's creations
    typeDeclaration.accept(new ASTVisitor() {
      @Override
      public void postVisit(ASTNode node) {
        if (!isSwt[0] && node instanceof Expression) {
          Expression expression = (Expression) node;
          ITypeBinding expressionBinding = AstNodeUtils.getTypeBinding(expression);
          if (AstNodeUtils.isSuccessorOf(expressionBinding, "org.eclipse.swt.widgets.Widget")) {
            isSwt[0] = true;
          }
        }
      }
    });
    return isSwt[0];
  }

  /**
   * @return <code>true</code> if given type binding is RCP object.
   */
  private static boolean isSWTObject(ITypeBinding typeBinding) throws Exception {
    if (typeBinding == null) {
      return false;
    }
    // SWT
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.widgets.Widget")) {
      return true;
    }
    // JFace
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.viewers.ColumnLayoutData")
        || AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.resource.ResourceRegistry")
        || AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.action.IAction")
        || AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.action.IContributionItem")
        || AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.action.IContributionManager")
        || AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.preference.FieldEditor")
        || AstNodeUtils.isSuccessorOf(
            typeBinding,
            "org.eclipse.jface.fieldassist.ControlDecoration")) {
      return true;
    }
    // Layout
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.widgets.Layout")) {
      return true;
    }
    // standard SWT LayoutData's
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.layout.RowData")
        || AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.layout.GridData")
        || AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.layout.FormData")
        || AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.swt.layout.FormAttachment")) {
      return true;
    }
    // Forms API LayoutData's
    if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.ui.forms.widgets.ColumnLayoutData")
        || AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.ui.forms.widgets.TableWrapData")) {
      return true;
    }
    //
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getToolkitId() {
    return IPreferenceConstants.TOOLKIT_ID;
  }

  /**
   * <p>
   * Note, that we include <code>org.eclipse.ui</code> bundle into parent {@link ClassLoader}, so
   * user code will able to talk with existing Eclipse Core/UI classes, such as {@link PlatformUI}
   * and OSGi.
   * <p>
   * We always include <code>com.ibm.icu</code> bundle to support Forms API, because it imports
   * <code>com.ibm.icu</code> classes using <code>import-package</code>, not using direct
   * dependency.
   */
  @Override
  protected void initializeClassLoader_parent(AstEditor editor,
      CompositeClassLoader parentClassLoader) throws Exception {
    parentClassLoader.add(new BundleClassLoader("com.ibm.icu"), ImmutableList.of("com.ibm.icu."));
    parentClassLoader.add(new BundleClassLoader("org.eclipse.ui"), null);
    parentClassLoader.add(new BundleClassLoader("org.eclipse.ui.forms"), null);
    parentClassLoader.add(new BundleClassLoader("org.eclipse.jdt.ui"), null);
    super.initializeClassLoader_parent(editor, parentClassLoader);
  }
}