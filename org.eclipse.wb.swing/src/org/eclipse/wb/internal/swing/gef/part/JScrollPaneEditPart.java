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
package org.eclipse.wb.internal.swing.gef.part;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.swing.gef.policy.component.JScrollPaneLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.JScrollPaneInfo;

/**
 * The {@link EditPart} for {@link JScrollPaneInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.part
 */
public final class JScrollPaneEditPart extends ComponentEditPart {
  private final JScrollPaneInfo m_component;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JScrollPaneEditPart(JScrollPaneInfo component) {
    super(component);
    m_component = component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(EditPolicy.LAYOUT_ROLE, new JScrollPaneLayoutEditPolicy(m_component));
  }
}
