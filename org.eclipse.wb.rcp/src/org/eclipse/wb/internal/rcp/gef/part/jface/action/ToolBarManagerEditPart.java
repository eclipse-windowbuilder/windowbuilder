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
package org.eclipse.wb.internal.rcp.gef.part.jface.action;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.rcp.gef.policy.jface.action.ToolBarManagerLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.jface.action.ToolBarManagerInfo;

/**
 * {@link EditPart} for {@link ToolBarManagerInfo}.
 * 
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class ToolBarManagerEditPart extends AbstractComponentEditPart {
  private final ToolBarManagerInfo m_manager;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ToolBarManagerEditPart(ToolBarManagerInfo manager) {
    super(manager);
    m_manager = manager;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refreshEditPolicies() {
    super.refreshEditPolicies();
    installEditPolicy(new ToolBarManagerLayoutEditPolicy(m_manager));
  }
}
