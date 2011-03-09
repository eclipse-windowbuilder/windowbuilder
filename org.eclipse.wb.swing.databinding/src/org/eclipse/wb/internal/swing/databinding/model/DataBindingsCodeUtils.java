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
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

/**
 * Project and code utils.
 * 
 * @author lobas_av
 * @author sablin_aa
 * @coverage bindings.swing.model
 */
public final class DataBindingsCodeUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link IJavaProject} has JSR-295 libraries.
   */
  public static boolean hasDBLibraries(IJavaProject javaProject) {
    return ProjectUtils.hasType(javaProject, "org.jdesktop.beansbinding.AutoBinding");
  }

  // FIXME remove this method after approving check using hasDBLibraries()
  public static boolean ensureDBLibraries(IJavaProject javaProject) throws Exception {
    /*if (!hasDBLibraries(javaProject)) {
    	ProjectUtils.addJar(
    		javaProject,
    		Activator.getDefault().getBundle(),
    		"beansbinding-1.2.1.jar",
    		"beansbinding-1.2.1-src.zip");
    	return true;
    }*/
    return false;
  }

  /**
   * @return {@link MethodDeclaration} for last {@link JavaInfo} into hierarchy.
   */
  public static MethodDeclaration getLastInfoDeclaration(MethodDeclaration initDataBindings,
      JavaInfo rootJavaInfo) throws Exception {
    LastComponentVisitor visitor = new LastComponentVisitor(initDataBindings, rootJavaInfo);
    MethodDeclaration method = AstNodeUtils.getEnclosingMethod(visitor.getLastNode());
    Assert.isNotNull(method);
    return method;
  }

  /**
   * Check add invocation <code>initDataBindings()</code> to method {@link MethodDeclaration}
   * <code>lastInfoMethod</code>.
   */
  public static void ensureInvokeInitDataBindings(AstEditor editor, MethodDeclaration lastInfoMethod)
      throws Exception {
    // find call initDataBindings()
    InitDataBindingsVisitor visitor = new InitDataBindingsVisitor();
    //
    editor.getAstUnit().accept(visitor);
    if (visitor.isInvoke()) {
      return;
    }
    //
    visitor.reset();
    lastInfoMethod.accept(visitor);
    // prepare invoke source
    String initDBInvokeSource = DataBindingsRootInfo.INIT_DATA_BINDINGS_METHOD_NAME + "();";
    // add initDataBindings()
    Statement windowVisibleStatement = visitor.getWindowVisibleStatement();
    List<Statement> statements = DomGenerics.statements(lastInfoMethod.getBody());
    StatementTarget methodTarget;
    //
    if (windowVisibleStatement != null) {
      methodTarget = new StatementTarget(windowVisibleStatement, true);
    } else if (statements.isEmpty()) {
      methodTarget = new StatementTarget(lastInfoMethod, true);
    } else {
      Statement lastStatement = statements.get(statements.size() - 1);
      methodTarget = new StatementTarget(lastStatement, lastStatement instanceof ReturnStatement);
    }
    //
    editor.addStatement(initDBInvokeSource, methodTarget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visitors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This visitor do find last {@link ComponentInfo} into hierarchy.
   */
  private static class LastComponentVisitor extends ObjectInfoVisitor {
    private final MethodDeclaration m_initDataBindings;
    private ASTNode m_lastNode;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public LastComponentVisitor(MethodDeclaration initDataBindings, JavaInfo rootJavaInfo)
        throws Exception {
      m_initDataBindings = initDataBindings;
      m_lastNode = rootJavaInfo.getCreationSupport().getNode();
      rootJavaInfo.accept0(this);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public ASTNode getLastNode() {
      return m_lastNode;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ObjectInfoVisitor
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean visit(ObjectInfo objectInfo) throws Exception {
      if (objectInfo instanceof ComponentInfo) {
        ComponentInfo componentInfo = (ComponentInfo) objectInfo;
        ASTNode node = componentInfo.getAssociation().getStatement();
        if (node == null) {
          if (componentInfo.getVariableSupport() instanceof LazyVariableSupport) {
            // TODO
          } else {
            node = componentInfo.getCreationSupport().getNode();
          }
        }
        if (node != null && AstNodeUtils.getEnclosingMethod(node) != m_initDataBindings) {
          m_lastNode = node;
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
    private boolean m_checkDoubleInvokeInitDB;
    private Statement m_windowVisibleStatement;

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
          if (m_checkDoubleInvokeInitDB) {
            Assert.isTrue(!m_invoke, "Double invoke initDataBindings()");
          }
          m_invoke = true;
        } else if ("setVisible".equals(methodName)
            && AstNodeUtils.isSuccessorOf(node.getExpression(), "java.awt.Window")) {
          Assert.isNull(m_windowVisibleStatement, "Double invoke %window%.setVisible()");
          m_windowVisibleStatement = AstNodeUtils.getEnclosingStatement(node);
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

    public Statement getWindowVisibleStatement() {
      return m_windowVisibleStatement;
    }

    public void reset() {
      m_invoke = false;
      m_checkDoubleInvokeInitDB = true;
      m_windowVisibleStatement = null;
    }
  }
}