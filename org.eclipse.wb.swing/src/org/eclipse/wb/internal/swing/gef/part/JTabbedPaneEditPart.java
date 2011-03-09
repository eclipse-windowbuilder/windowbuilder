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

import com.google.common.collect.Lists;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.swing.gef.policy.component.JTabbedPaneLayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.policy.component.JTabbedPaneTabLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.JTabbedPaneInfo;

import java.util.List;

/**
 * The {@link EditPart} for {@link JTabbedPaneInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.part
 */
public final class JTabbedPaneEditPart extends ComponentEditPart {
  private final JTabbedPaneInfo m_component;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JTabbedPaneEditPart(JTabbedPaneInfo component) {
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
    installEditPolicy(new JTabbedPaneTabLayoutEditPolicy(m_component));
    installEditPolicy(EditPolicy.LAYOUT_ROLE, new JTabbedPaneLayoutEditPolicy(m_component));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Object> getModelChildren() {
    List<Object> children = Lists.newArrayList();
    children.addAll(super.getModelChildren());
    children.addAll(m_component.getTabs());
    return children;
  }
}
