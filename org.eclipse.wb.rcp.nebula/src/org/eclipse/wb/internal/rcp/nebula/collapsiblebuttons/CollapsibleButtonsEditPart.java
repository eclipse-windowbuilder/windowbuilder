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
package org.eclipse.wb.internal.rcp.nebula.collapsiblebuttons;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.policies.TerminatorLayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.part.CompositeEditPart;

/**
 * {@link EditPart} for {@link CollapsibleButtonsInfo}.
 * 
 * @author sablin_aa
 * @coverage nebula.gef
 */
public final class CollapsibleButtonsEditPart extends CompositeEditPart {
  private final CollapsibleButtonsInfo m_collButtons;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CollapsibleButtonsEditPart(CollapsibleButtonsInfo collButtons) {
    super(collButtons);
    m_collButtons = collButtons;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refreshEditPolicies() {
    super.refreshEditPolicies();
    installEditPolicy(new CollapsibleButtonsLayoutEditPolicy(m_collButtons));
    installEditPolicy(new TerminatorLayoutEditPolicy());
  }
}
