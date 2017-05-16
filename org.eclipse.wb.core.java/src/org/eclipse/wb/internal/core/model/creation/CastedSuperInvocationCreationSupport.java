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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

/**
 * Implementation of {@link CreationSupport} for {@link CastExpression} that has
 * {@link SuperMethodInvocation} .<br>
 * Such pattern is used in JFace dialogs.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public final class CastedSuperInvocationCreationSupport extends CreationSupport {
  private final CastExpression m_castExpression;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CastedSuperInvocationCreationSupport(CastExpression castExpression) {
    m_castExpression = castExpression;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "casted-superInvocation: " + m_castExpression;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ASTNode getNode() {
    return m_castExpression;
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    return node == m_castExpression;
  }
}
