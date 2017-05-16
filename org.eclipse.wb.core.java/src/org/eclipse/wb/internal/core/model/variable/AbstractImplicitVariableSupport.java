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
import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Implementation of {@link VariableSupport} for some implicit variable for child on other
 * {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public abstract class AbstractImplicitVariableSupport extends AbstractNoNameVariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractImplicitVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean hasExpression(NodeTarget target) {
    // we materialize using LocalUniqueVariableSupport, so will have expression
    return true;
  }

  @Override
  public final String getReferenceExpression(NodeTarget target) throws Exception {
    VariableSupport localVariable = ensureLocalVariable();
    updateTarget_ifWasAfterParentStatement(target);
    return localVariable.getReferenceExpression(target);
  }

  @Override
  public final String getAccessExpression(NodeTarget target) throws Exception {
    return getReferenceExpression(target) + ".";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final StatementTarget getStatementTarget() throws Exception {
    return ensureLocalVariable().getStatementTarget();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Materializing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the parent {@link JavaInfo} for this {@link JavaInfo} child.
   */
  protected abstract JavaInfo getParent();

  /**
   * @return the materialized {@link VariableSupport}.
   */
  private VariableSupport ensureLocalVariable() throws Exception {
    JavaInfo parent = getParent();
    // generate local variable
    VariableSupport variableSupport = new LocalUniqueVariableSupport(m_javaInfo);
    m_javaInfo.setVariableSupport(variableSupport);
    // add statement
    StatementTarget target = parent.getVariableSupport().getStatementTarget();
    ImplicitObjectAssociation association = new ImplicitObjectAssociation(parent);
    PureFlatStatementGenerator.INSTANCE.add(m_javaInfo, target, association);
    // return variable
    return variableSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils for updating NodeTarget
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If parent Statement was used as target, update target to use new "local variable" Statement.
   */
  private void updateTarget_ifWasAfterParentStatement(NodeTarget target) throws Exception {
    StatementTarget statementTarget = target.getStatementTarget();
    if (statementTarget != null) {
      Statement oldStatement = updateTarget_getParentStatement();
      if (statementTarget.isAfter() && statementTarget.getStatement() == oldStatement) {
        Statement newStatement = updateTarget_getNewVariableStatement();
        statementTarget.setStatement(newStatement);
      }
    }
  }

  private Statement updateTarget_getParentStatement() throws Exception {
    return getParent().getVariableSupport().getStatementTarget().getStatement();
  }

  private Statement updateTarget_getNewVariableStatement() {
    Expression variableExpression =
        ((LocalUniqueVariableSupport) m_javaInfo.getVariableSupport()).m_variable;
    return AstNodeUtils.getEnclosingStatement(variableExpression);
  }
}
