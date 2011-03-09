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
package org.eclipse.wb.internal.swing.model.layout.absolute;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NullLiteral;

/**
 * Implementation of {@link CreationSupport} for {@link AbsoluteLayoutInfo} specified as
 * {@link NullLiteral} in "setLayout(null)".
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class AbsoluteLayoutCreationSupport extends CreationSupport {
  private Expression m_expression;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsoluteLayoutCreationSupport() {
  }

  public AbsoluteLayoutCreationSupport(Expression expression) {
    Assert.isNotNull(expression);
    m_expression = expression;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return m_javaInfo.getEditor().getSource(m_expression);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isJavaInfo(ASTNode node) {
    return false;
  }

  @Override
  public ASTNode getNode() {
    return m_expression;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canDelete() {
    return true;
  }

  @Override
  public void delete() throws Exception {
    JavaInfo parent = m_javaInfo.getParentJava();
    parent.removeChild(m_javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getSource(NodeTarget target) throws Exception {
    return "null";
  }

  @Override
  public void add_setSourceExpression(Expression expression) throws Exception {
    Assert.isNotNull(expression);
    m_expression = expression;
    // set object (to initialize JavaInfo)
    m_javaInfo.setObject(null);
  }
}
