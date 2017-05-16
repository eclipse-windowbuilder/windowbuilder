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

import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Implementation of {@link Association} for {@link ClassInstanceCreation}.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public abstract class ConstructorAssociation extends Association {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  ConstructorAssociation() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the underlying {@link ClassInstanceCreation}.
   */
  public abstract ClassInstanceCreation getCreation();

  @Override
  public final Statement getStatement() {
    return AstNodeUtils.getEnclosingStatement(getCreation());
  }

  @Override
  public final String getSource() {
    return m_editor.getSource(getCreation());
  }
}
