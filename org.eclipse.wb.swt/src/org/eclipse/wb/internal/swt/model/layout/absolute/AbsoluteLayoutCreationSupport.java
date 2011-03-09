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
package org.eclipse.wb.internal.swt.model.layout.absolute;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NullLiteral;

/**
 * Implementation of {@link CreationSupport} for {@link AbsoluteLayoutInfo} specified as
 * {@link NullLiteral} in "setLayout(null)"
 * 
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class AbsoluteLayoutCreationSupport extends CreationSupport {
  private Expression m_expression;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
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
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    m_javaInfo.addBroadcastListener(new JavaInfoSetObjectAfter() {
      public void invoke(JavaInfo target, Object object) throws Exception {
        if (target == m_javaInfo.getParentJava()) {
          m_javaInfo.setObject(null);
        }
      }
    });
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    return m_expression == node;
  }

  @Override
  public ASTNode getNode() {
    return m_expression;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getSource(NodeTarget target) throws Exception {
    return "null";
  }

  @Override
  public void add_setSourceExpression(Expression expression) throws Exception {
    Assert.instanceOf(NullLiteral.class, expression);
    m_expression = expression;
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
}