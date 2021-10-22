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
package org.eclipse.wb.internal.rcp.gefTree.part.rcp.perspective.shortcuts;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.rcp.gefTree.policy.rcp.perspective.shortcuts.ViewShortcutContainerLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.ViewShortcutContainerInfo;

/**
 * {@link EditPart} for {@link ViewShortcutContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gefTree.part
 */
public final class ViewShortcutContainerEditPart extends AbstractShortcutContainerEditPart {
  private final ViewShortcutContainerInfo m_container;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewShortcutContainerEditPart(ViewShortcutContainerInfo container) {
    super(container);
    m_container = container;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(new ViewShortcutContainerLayoutEditPolicy(m_container));
  }
}
