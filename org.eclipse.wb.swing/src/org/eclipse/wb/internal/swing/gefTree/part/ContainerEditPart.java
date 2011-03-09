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
package org.eclipse.wb.internal.swing.gefTree.part;

import org.eclipse.wb.core.gefTree.policy.layout.LayoutPolicyUtils;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.gefTree.policy.DefaultLayoutEditPolicy;
import org.eclipse.wb.internal.swing.gefTree.policy.DropLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

/**
 * {@link EditPart} for {@link ContainerInfo} for GEF Tree.
 * 
 * @author mitin_aa
 * @coverage swing.gefTree.part
 */
public final class ContainerEditPart extends ComponentEditPart {
  private final ContainerInfo m_container;
  private LayoutInfo m_currentLayout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ContainerEditPart(ContainerInfo model) {
    super(model);
    m_container = model;
  }

  @Override
  protected void refreshEditPolicies() {
    super.refreshEditPolicies();
    // support for dropping LayoutInfo's
    if (m_container.canSetLayout()) {
      installEditPolicy(new DropLayoutEditPolicy(m_container));
    }
    // support for dropping components
    if (m_container.hasLayout()) {
      LayoutInfo layout = m_container.getLayout();
      if (layout != m_currentLayout) {
        m_currentLayout = layout;
        LayoutEditPolicy policy = LayoutPolicyUtils.createLayoutEditPolicy(this, layout);
        if (policy == null) {
          policy = new DefaultLayoutEditPolicy();
        }
        installEditPolicy(EditPolicy.LAYOUT_ROLE, policy);
      }
    }
  }
}
