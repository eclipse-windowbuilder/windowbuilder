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

import org.eclipse.wb.core.gefTree.policy.layout.LayoutPolicyUtils;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.xwt.gefTree.policy.DropLayoutEditPolicy;
import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;

/**
 * {@link EditPart} for {@link CompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage XWT.gefTree.part
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
    // support for dropping Layout_Info's
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
      if (layout != m_currentLayout) {
        LayoutEditPolicy policy = LayoutPolicyUtils.createLayoutEditPolicy(this, layout);
        if (policy != null) {
          m_currentLayout = layout;
          installEditPolicy(EditPolicy.LAYOUT_ROLE, policy);
        }
      }
    }
  }
}
