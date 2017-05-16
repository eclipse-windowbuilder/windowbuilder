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
package org.eclipse.wb.core.gefTree.part;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.gefTree.policy.dblclick.DoubleClickLayoutEditPolicy;

/**
 * {@link TreeEditPart} for {@link JavaInfo}.
 *
 * @author mitin_aa
 * @coverage core.gefTree
 */
public class JavaEditPart extends ObjectEditPart {
  private final JavaInfo m_javaInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaEditPart(JavaInfo javaInfo) {
    super(javaInfo);
    m_javaInfo = javaInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    DoubleClickLayoutEditPolicy.install(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link JavaInfo} model for this {@link JavaInfoEditPart}.
   */
  public final JavaInfo getJavaInfo() {
    return m_javaInfo;
  }
}
