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
package org.eclipse.wb.core.model.association;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Implementation of {@link Association} for {@link JavaInfo} children that are created with host
 * {@link JavaInfo} and associated with it just by fact of creation. It does not have explicit
 * presentation in source code, but we still need it just to have some {@link Association} for such
 * components.
 * <p>
 * Examples: exposed components, implicit layouts, etc.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class ImplicitObjectAssociation extends Association {
  private final JavaInfo m_hostJavaInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImplicitObjectAssociation(JavaInfo hostJavaInfo) {
    m_hostJavaInfo = hostJavaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Statement getStatement() {
    // implicit components are associated by the fact of creation, so use creation Statement
    ASTNode creationNode = m_hostJavaInfo.getCreationSupport().getNode();
    return AstNodeUtils.getEnclosingStatement(creationNode);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean remove() throws Exception {
    // even after delete, implicit object stays associated with its parent
    return false;
  }
}
