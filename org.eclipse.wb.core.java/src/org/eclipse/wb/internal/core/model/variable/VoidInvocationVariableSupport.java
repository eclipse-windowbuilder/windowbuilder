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
package org.eclipse.wb.internal.core.model.variable;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import javax.swing.JToolBar;

/**
 * Implementation of {@link VariableSupport} for component without variable that created using
 * invocation of some method of other {@link JavaInfo}. For example {@link JToolBar#addSeparator()}.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class VoidInvocationVariableSupport extends AbstractNoNameVariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VoidInvocationVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "void";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTitle() throws Exception {
    MethodInvocation invocation = getInvocation();
    return AstNodeUtils.getMethodSignature(invocation);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getReferenceExpression(NodeTarget target) throws Exception {
    throw new IllegalStateException();
  }

  @Override
  public String getAccessExpression(NodeTarget target) throws Exception {
    throw new IllegalStateException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public StatementTarget getStatementTarget() throws Exception {
    throw new IllegalStateException();
  }

  @Override
  public void ensureInstanceReadyAt(StatementTarget target) throws Exception {
    m_javaInfo.getEditor().moveStatement(getStatement(), target);
  }

  @Override
  public StatementTarget getAssociationTarget(StatementTarget target) throws Exception {
    return target;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getVariableStatementSource(StatementTarget associationTarget) throws Exception {
    NodeTarget creationTarget = new NodeTarget(associationTarget);
    return m_javaInfo.getCreationSupport().add_getSource(creationTarget) + ";";
  }

  @Override
  public void add_setVariableStatement(Statement statement) throws Exception {
    ExpressionStatement expressionStatement = (ExpressionStatement) statement;
    m_javaInfo.getCreationSupport().add_setSourceExpression(expressionStatement.getExpression());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the creation {@link MethodInvocation}.
   */
  private MethodInvocation getInvocation() throws Exception {
    return (MethodInvocation) m_javaInfo.getCreationSupport().getNode();
  }

  /**
   * @return the {@link Statement} for creation {@link MethodInvocation}.
   */
  private Statement getStatement() throws Exception {
    MethodInvocation invocation = getInvocation();
    return AstNodeUtils.getEnclosingStatement(invocation);
  }
}
