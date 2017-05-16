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
package org.eclipse.wb.internal.core.utils.ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

/**
 * {@link StatementTarget} contains information about location for placing {@link Statement}.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public final class StatementTarget {
  private final Block m_block;
  private Statement m_statement;
  private final boolean m_before;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StatementTarget(MethodDeclaration method, boolean beginning) {
    this(method.getBody(), beginning);
  }

  public StatementTarget(Block block, boolean beginning) {
    this(block, null, beginning);
  }

  public StatementTarget(Statement statement, boolean before) {
    this(null, statement, before);
  }

  public StatementTarget(ASTNode node, boolean before) {
    this(null, AstNodeUtils.getEnclosingStatement(node), before);
  }

  public StatementTarget(Block block, Statement statement, boolean before) {
    m_block = block;
    m_statement = statement;
    m_before = before;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    if (m_statement != null) {
      return (m_before ? "before " : "after ") + m_statement;
    } else {
      return (m_before ? "begin " : "end ") + m_block;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the position, associated with this target, i.e. position of {@link Statement} or
   *         begin/end of {@link Block}.
   */
  public int getPosition() {
    if (m_statement != null) {
      return m_before
          ? AstNodeUtils.getSourceBegin(m_statement)
          : AstNodeUtils.getSourceEnd(m_statement);
    } else {
      return m_before
          ? AstNodeUtils.getSourceBegin(m_block)
          : AstNodeUtils.getSourceEnd(m_block) - 1;
    }
  }

  /**
   * @return any target {@link ASTNode} - {@link Statement} or {@link Block}.
   */
  public ASTNode getNode() {
    return m_statement != null ? m_statement : m_block;
  }

  /**
   * @return the {@link Block} that should contain new statement, or <code>null</code> if
   *         {@link #getStatement()} returns not <code>null</code>.
   */
  public Block getBlock() {
    return m_block;
  }

  /**
   * @return the target {@link Statement} for new statement, or <code>null</code> if
   *         {@link #getBlock()} returns not <code>null</code>.
   */
  public Statement getStatement() {
    return m_statement;
  }

  /**
   * Sets the target {@link Statement}.
   * <p>
   * I know that would be good to keep {@link StatementTarget} immutable. However when
   * <code>EmptyVariable</code> ensures local variable for code/statement like this
   * <code>new Button(parent);</code> and target statement is that "expression" statement, we
   * replace it with "variable declaration" statement, so it <b>have</b> to be replaced.
   */
  public void setStatement(Statement statement) {
    m_statement = statement;
  }

  /**
   * @return <code>true</code> if new statement should be added before target statement or in
   *         beginning of {@link Block}.
   */
  public boolean isBefore() {
    return m_before;
  }

  /**
   * @return negation of {@link #isBefore()}.
   */
  public boolean isAfter() {
    return !m_before;
  }
}
