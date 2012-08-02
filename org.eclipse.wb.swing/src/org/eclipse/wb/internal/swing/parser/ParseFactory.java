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
package org.eclipse.wb.internal.swing.parser;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.EditorStateLoadingContext;
import org.eclipse.wb.internal.core.model.description.helpers.ILoadingContext;
import org.eclipse.wb.internal.core.model.variable.ThisVariableSupport;
import org.eclipse.wb.internal.core.parser.AbstractParseFactory;
import org.eclipse.wb.internal.core.parser.IJavaInfoParseResolver;
import org.eclipse.wb.internal.core.parser.IParseFactory;
import org.eclipse.wb.internal.core.parser.ParseRootContext;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.swing.IExceptionConstants;
import org.eclipse.wb.internal.swing.model.bean.ActionAnonymousCreationSupport;
import org.eclipse.wb.internal.swing.model.bean.ActionInnerCreationSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JPopupMenuAssociation;
import org.eclipse.wb.internal.swing.model.component.menu.JPopupMenuInfo;
import org.eclipse.wb.internal.swing.preferences.IPreferenceConstants;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;

/**
 * {@link IParseFactory} for Swing.
 * 
 * @author scheglov_ke
 * @coverage swing.parser
 */
public class ParseFactory extends AbstractParseFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IParseFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ParseRootContext getRootContext(AstEditor editor,
      TypeDeclaration typeDeclaration,
      ITypeBinding typeBinding) throws Exception {
    // special unsupported classes
    if (AstNodeUtils.isSuccessorOf(typeBinding, "javax.swing.Action")) {
      throw new DesignerException(IExceptionConstants.NO_DESIGN_ACTION);
    }
    // check for Swing/AWT type
    {
      // check if there are java.awt.Component successors
      final boolean[] isSwing = new boolean[1];
      typeDeclaration.accept(new ASTVisitor() {
        @Override
        public void postVisit(ASTNode node) {
          if (!isSwing[0] && node instanceof Expression) {
            Expression expression = (Expression) node;
            ITypeBinding expressionBinding = AstNodeUtils.getTypeBinding(expression);
            if (AstNodeUtils.isSuccessorOf(expressionBinding, Component.class)) {
              isSwing[0] = true;
            }
          }
        }
      });
      // not Swing, ignore
      if (!isSwing[0]) {
        return null;
      }
    }
    // rewrite
    new SwingRewriteProcessor(editor, typeDeclaration).rewrite();
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
    // support for java.awt.Component
    if (AstNodeUtils.isSuccessorOf(typeBinding, Component.class)
        || AstNodeUtils.isSuccessorOf(typeBinding, "org.jdesktop.application.View")) {
      ITypeBinding typeBinding_super = typeBinding.getSuperclass();
      // prepare class of component
      Class<?> superClass = getSuperClass(editor, typeBinding_super);
      if (superClass == javax.swing.JComponent.class) {
        superClass = java.awt.Container.class;
      }
      // prepare creation
      MethodDeclaration constructor = getConstructor(editor, typeDeclaration);
      ThisCreationSupport creationSupport = new ThisCreationSupport(constructor);
      // create JavaInfo
      JavaInfo javaInfo = JavaInfoUtils.createJavaInfo(editor, superClass, creationSupport);
      if (javaInfo != null) {
        javaInfo.setVariableSupport(new ThisVariableSupport(javaInfo, constructor));
        // prepare root context
        List<MethodDeclaration> rootMethods = Lists.newArrayList();
        rootMethods.add(constructor);
        addRootMethods(rootMethods, superClass, typeDeclaration);
        return new ParseRootContext(javaInfo, new ExecutionFlowDescription(rootMethods));
      }
    }
    // no root found
    return null;
  }

  /**
   * Includes additional {@link MethodDeclaration} into execution flow.
   */
  private static void addRootMethods(List<MethodDeclaration> rootMethods,
      Class<?> superClass,
      TypeDeclaration typeDeclaration) {
    if (ReflectionUtils.isSuccessorOf(superClass, "java.applet.Applet")) {
      MethodDeclaration initMethod = AstNodeUtils.getMethodBySignature(typeDeclaration, "init()");
      if (initMethod != null) {
        rootMethods.add(initMethod);
      }
    }
    // add Action methods
    for (MethodDeclaration method : typeDeclaration.getMethods()) {
      IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(method);
      if (methodBinding != null) {
        if (AstNodeUtils.isSuccessorOf(methodBinding.getReturnType(), "javax.swing.Action")) {
          rootMethods.add(method);
        }
      }
    }
  }

  @Override
  public JavaInfo create(AstEditor editor,
      ClassInstanceCreation creation,
      IMethodBinding methodBinding,
      ITypeBinding typeBinding,
      Expression arguments[],
      JavaInfo argumentInfos[]) throws Exception {
    AnonymousClassDeclaration anonymousClassDeclaration = creation.getAnonymousClassDeclaration();
    // tweak anonymous type binding
    if (anonymousClassDeclaration != null) {
      typeBinding = typeBinding.getSuperclass();
    }
    // special support for javax.swing.AbstractAction 
    if (AstNodeUtils.isSuccessorOf(creation, "javax.swing.AbstractAction")) {
      // ... as named class
      {
        ITypeBinding declaringClassBinding = AstNodeUtils.getGenericDeclaringClass(typeBinding);
        if (declaringClassBinding != null) {
          // ... as inner class
          ITypeBinding topTypeBinding = AstNodeUtils.getEnclosingTypeTop(creation).resolveBinding();
          if (declaringClassBinding == topTypeBinding) {
            CreationSupport creationSupport = new ActionInnerCreationSupport(creation);
            return JavaInfoUtils.createJavaInfo(editor, AbstractAction.class, creationSupport);
          }
          // ... as external class
          {
            CreationSupport creationSupport = new ConstructorCreationSupport(creation);
            return JavaInfoUtils.createJavaInfo(editor, AbstractAction.class, creationSupport);
          }
        }
      }
      // ... as anonymous class
      if (anonymousClassDeclaration != null) {
        Class<?> clazz = getClass(editor, typeBinding);
        CreationSupport creationSupport = new ActionAnonymousCreationSupport(creation);
        return JavaInfoUtils.createJavaInfo(editor, clazz, creationSupport);
      }
    }
    // check "super"
    {
      JavaInfo javaInfo =
          super.create(editor, creation, methodBinding, typeBinding, arguments, argumentInfos);
      if (javaInfo != null) {
        return javaInfo;
      }
    }
    // Swing object
    if (isSwingObject(editor, typeBinding)) {
      // prepare class of component
      Class<?> componentClass = getClass(editor, typeBinding);
      if (componentClass == null) {
        return null;
      }
      // create JavaInfo
      CreationSupport creationSupport = new ConstructorCreationSupport(creation);
      return JavaInfoUtils.createJavaInfo(editor, componentClass, creationSupport);
    }
    // unknown class
    return null;
  }

  @Override
  public JavaInfo create(AstEditor editor,
      MethodInvocation invocation,
      IMethodBinding methodBinding,
      Expression arguments[],
      JavaInfo expressionInfo,
      JavaInfo argumentInfos[],
      IJavaInfoParseResolver javaInfoResolver) throws Exception {
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
    // javax.swing.JPopupMenu is associated using addPopup() invocation
    if (argumentInfos.length == 2
        && argumentInfos[0] instanceof ComponentInfo
        && argumentInfos[1] instanceof JPopupMenuInfo
        && AstNodeUtils.getMethodSignature(methodBinding).equals(
            "addPopup(java.awt.Component,javax.swing.JPopupMenu)")) {
      ComponentInfo component = (ComponentInfo) argumentInfos[0];
      JPopupMenuInfo popup = (JPopupMenuInfo) argumentInfos[1];
      popup.setAssociation(new JPopupMenuAssociation(invocation));
      component.addChild(popup);
    }
    // no JavaInfo for MethodInvocation
    return null;
  }

  @Override
  public boolean isToolkitObject(AstEditor editor, ITypeBinding typeBinding) throws Exception {
    return isSwingObject(editor, typeBinding);
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

  @Override
  protected void initializeClassLoader(AstEditor editor) throws Exception {
    ensureNotHeadless();
    super.initializeClassLoader(editor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link ITypeBinding} is Swing/AWT object.
   */
  private static boolean isSwingObject(AstEditor editor, ITypeBinding typeBinding) throws Exception {
    if (typeBinding == null) {
      return false;
    }
    if (AstNodeUtils.isSuccessorOf(
        typeBinding,
        "java.awt.Component",
        "java.awt.LayoutManager",
        "javax.swing.Action",
        "javax.swing.ButtonGroup")) {
      return true;
    }
    //
    String qualifiedName = AstNodeUtils.getFullyQualifiedName(typeBinding, true);
    if ("java.awt.GridBagConstraints".equals(qualifiedName)
        || "javax.swing.AbstractAction".equals(qualifiedName)) {
      return true;
    }
    // try to find forced toolkit
    {
      EditorState editorState = EditorState.get(editor);
      ILoadingContext loadingContext = EditorStateLoadingContext.get(editorState);
      if (DescriptionHelper.hasForcedToolkitForComponent(
          loadingContext,
          IPreferenceConstants.TOOLKIT_ID,
          qualifiedName)) {
        return true;
      }
    }
    //
    return false;
  }

  /**
   * FlexBuilder from Adobe configures Swing to work in "headless" mode. We should check and
   * configure it back into valid state.
   */
  private static void ensureNotHeadless() {
    try {
      if (GraphicsEnvironment.isHeadless()) {
        // set 'headless' to false
        Properties systemProps = System.getProperties();
        systemProps.put("java.awt.headless", "false");
        System.setProperties(systemProps);
        // clear cached values
        Toolkit.getDefaultToolkit();
        ReflectionUtils.setField(Toolkit.class, "toolkit", null);
        ReflectionUtils.setField(GraphicsEnvironment.class, "headless", null);
        ReflectionUtils.setField(GraphicsEnvironment.class, "localEnv", null);
        // force re-initialize
        GraphicsEnvironment.isHeadless();
        GraphicsEnvironment.getLocalGraphicsEnvironment();
        Toolkit.getDefaultToolkit();
      }
    } catch (Throwable e) {
      // ignore
    }
  }
}
