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

import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.AstVisitorEx;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.apache.commons.lang.StringUtils;

/**
 * Changes known unsupported patterns into supported.
 * 
 * @author scheglov_ke
 * @coverage swing.parser
 */
public final class SwingRewriteProcessor {
  private final AstEditor editor;
  private final TypeDeclaration typeDeclaration;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SwingRewriteProcessor(AstEditor editor, TypeDeclaration typeDeclaration) {
    this.editor = editor;
    this.typeDeclaration = typeDeclaration;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rewrite
  //
  ////////////////////////////////////////////////////////////////////////////
  public void rewrite() {
    rewrite_JScrollPane();
    rewrite_SuperMethodInvocation();
    rewrite_RootPaneContainer();
  }

  private void rewrite_JScrollPane() {
    typeDeclaration.accept(new AstVisitorEx() {
      @Override
      public void endVisitEx(MethodInvocation node) throws Exception {
        if (AstNodeUtils.isMethodInvocation(node, "javax.swing.JScrollPane", "getViewport()")) {
          if (node.getParent() instanceof MethodInvocation) {
            MethodInvocation addInvocation = (MethodInvocation) node.getParent();
            if (addInvocation.getName().getIdentifier().equals("add")
                && addInvocation.getParent() instanceof ExpressionStatement) {
              Statement statement = (Statement) addInvocation.getParent();
              String scrollSource = editor.getSource(node.getExpression());
              Expression componentExpression = DomGenerics.arguments(addInvocation).get(0);
              String componentSource = editor.getSource(componentExpression);
              StatementTarget target = new StatementTarget(statement, true);
              editor.addStatement(
                  scrollSource + ".setViewportView(" + componentSource + ");",
                  target);
              editor.removeEnclosingStatement(statement);
            }
          }
        }
      }
    });
  }

  private void rewrite_SuperMethodInvocation() {
    typeDeclaration.accept(new AstVisitorEx() {
      @Override
      public void endVisitEx(SuperMethodInvocation node) throws Exception {
        if (AstNodeUtils.isMethodInvocation(
            node,
            "java.awt.Container",
            "setLayout(java.awt.LayoutManager)")) {
          String superSource = editor.getSource(node);
          String source = StringUtils.removeStart(superSource, "super.");
          editor.replaceExpression(node, source);
        }
      }
    });
  }

  private void rewrite_RootPaneContainer() {
    typeDeclaration.accept(new AstVisitorEx() {
      @Override
      public void endVisitEx(MethodInvocation node) throws Exception {
        if (isInterestingMethod(node)) {
          Expression expression = node.getExpression();
          String newExpression;
          if (expression == null || expression instanceof ThisExpression) {
            newExpression = "getContentPane()";
          } else {
            newExpression = editor.getSource(expression) + ".getContentPane()";
          }
          editor.replaceInvocationExpression(node, newExpression);
        }
      }

      private boolean isInterestingMethod(MethodInvocation node) {
        // expression
        {
          Expression expression = node.getExpression();
          if (expression == null) {
            if (AstNodeUtils.getEnclosingType(node) != typeDeclaration) {
              return false;
            }
            if (!isRootPaneContainer(typeDeclaration)) {
              return false;
            }
          } else {
            if (!isRootPaneContainer(expression)) {
              return false;
            }
          }
        }
        // signature
        String methodName = node.getName().getIdentifier();
        if (methodName.equals("setLayout")) {
          String signature = AstNodeUtils.getMethodSignature(node);
          if (signature.equals("setLayout(java.awt.LayoutManager)")) {
            return true;
          }
        }
        if (methodName.equals("add")) {
          return true;
        }
        return false;
      }
    });
  }

  private static boolean isRootPaneContainer(Object target) {
    ITypeBinding typeBinding;
    if (target instanceof TypeDeclaration) {
      typeBinding = AstNodeUtils.getTypeBinding((TypeDeclaration) target);
    } else {
      assert target instanceof Expression;
      typeBinding = AstNodeUtils.getTypeBinding((Expression) target);
    }
    return AstNodeUtils.isSuccessorOf(typeBinding, "javax.swing.RootPaneContainer");
  }
}
