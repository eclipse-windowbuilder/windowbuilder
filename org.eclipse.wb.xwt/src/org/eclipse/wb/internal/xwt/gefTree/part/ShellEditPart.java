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
package org.eclipse.wb.internal.xwt.gefTree.part;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.xwt.gefTree.policy.MenuBarDropLayoutEditPolicy;
import org.eclipse.wb.internal.xwt.model.widgets.ShellInfo;

/**
 * {@link EditPart} for {@link ShellInfo}.
 *
 * @author scheglov_ke
 * @coverage XWT.gefTree.part
 */
public class ShellEditPart extends CompositeEditPart {
  private final ShellInfo m_shell;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ShellEditPart(ShellInfo shell) {
    super(shell);
    m_shell = shell;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(new MenuBarDropLayoutEditPolicy(m_shell));
  }
}
