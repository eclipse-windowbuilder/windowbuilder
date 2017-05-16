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
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.Statement;

import javax.swing.JButton;

/**
 * Specific sub-class of {@link EmptyVariableSupport} that adds new {@link JavaInfo} using just
 * creation source as {@link ExpressionStatement}. For example for {@link JButton}:
 *
 * <pre>
 * 	new JButton("My button");
 * </pre>
 *
 * Note that there are no assignment to variable, just pure {@link Expression}.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class EmptyPureVariableSupport extends EmptyVariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EmptyPureVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
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
    Expression initializer = ((ExpressionStatement) statement).getExpression();
    add_setInitializer(initializer);
  }
}
