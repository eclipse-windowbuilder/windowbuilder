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
package org.eclipse.wb.internal.core.model.creation;

import org.eclipse.wb.internal.core.utils.ast.NodeTarget;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

/**
 * {@link CreationSupport} for creating objects using some {@link Expression}, without any special
 * support for type of this {@link Expression}.
 * <p>
 * {@link OpaqueCreationSupport} is very generic, so itself can not know if it is possible to
 * perform some operations. {@link ICreationSupportPermissions} is used to access operations
 * enablement/implementation.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public class OpaqueCreationSupport extends CreationSupport {
  private ICreationSupportPermissions m_permissions = ICreationSupportPermissions.FALSE;
  private String m_source;
  private Expression m_expression;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public OpaqueCreationSupport(String source) {
    m_source = source;
  }

  public OpaqueCreationSupport(Expression expression) {
    m_expression = expression;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "opaque";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link ICreationSupportPermissions} to delegate operations to.
   */
  public void setPermissions(ICreationSupportPermissions permissions) {
    m_permissions = permissions;
  }

  @Override
  public ASTNode getNode() {
    return m_expression;
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    return node == m_expression;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canDelete() {
    return m_permissions.canDelete(m_javaInfo);
  }

  @Override
  public void delete() throws Exception {
    m_permissions.delete(m_javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Permissions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canReorder() {
    return m_permissions.canReorder(m_javaInfo);
  }

  @Override
  public boolean canReparent() {
    return m_permissions.canReparent(m_javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getSource(NodeTarget target) {
    return m_source;
  }

  @Override
  public void add_setSourceExpression(Expression expression) throws Exception {
    m_expression = expression;
    m_javaInfo.bindToExpression(expression);
  }
}
