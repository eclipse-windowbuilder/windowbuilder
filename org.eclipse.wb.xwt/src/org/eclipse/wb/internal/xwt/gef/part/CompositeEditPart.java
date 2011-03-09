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
package org.eclipse.wb.internal.xwt.gef.part;

import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.IRefreshableEditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.xwt.gef.policy.DropLayoutEditPolicy;
import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;

/**
 * {@link GraphicalEditPart} for {@link CompositeInfo}.
 * 
 * @author scheglov_ke
 * @coverage XML.gef
 */
public class CompositeEditPart extends ControlEditPart {
  private final CompositeInfo m_composite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompositeEditPart(CompositeInfo composite) {
    super(composite);
    m_composite = composite;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  private LayoutInfo m_currentLayout;

  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    // support for dropping LayoutInfo's
    if (m_composite.hasLayout()) {
      installEditPolicy(new DropLayoutEditPolicy(m_composite));
    }
  }

  @Override
  protected void refreshEditPolicies() {
    super.refreshEditPolicies();
    // support for dropping components
    if (m_composite.hasLayout()) {
      LayoutInfo layout = m_composite.getLayout();
      if (m_currentLayout != layout) {
        m_currentLayout = layout;
        LayoutEditPolicy policy = LayoutPolicyUtils.createLayoutEditPolicy(this, layout);
        installEditPolicy(EditPolicy.LAYOUT_ROLE, policy);
      } else {
        EditPolicy policy = getEditPolicy(EditPolicy.LAYOUT_ROLE);
        if (policy instanceof IRefreshableEditPolicy) {
          ((IRefreshableEditPolicy) policy).refreshEditPolicy();
        }
      }
    }
  }
}