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
package org.eclipse.wb.internal.swing.gef.policy.component;

import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.GefMessages;
import org.eclipse.wb.internal.swing.gef.policy.ComponentPositionLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.JSplitPaneInfo;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link JSplitPaneInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class JSplitPaneLayoutEditPolicy extends ComponentPositionLayoutEditPolicy<Boolean> {
  private final JSplitPaneInfo m_component;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JSplitPaneLayoutEditPolicy(JSplitPaneInfo component) {
    super(component);
    m_component = component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addFeedbacks() throws Exception {
    if (m_component.isEmptyPosition(true)) {
      addFeedback(
          m_component.getPositionRectangle(true),
          GefMessages.JSplitPaneLayoutEditPolicy_leftTop,
          Boolean.TRUE);
    }
    if (m_component.isEmptyPosition(false)) {
      addFeedback(
          m_component.getPositionRectangle(false),
          GefMessages.JSplitPaneLayoutEditPolicy_rightBottom,
          Boolean.FALSE);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation of commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(ComponentInfo component, Boolean data) throws Exception {
    m_component.command_CREATE(component, data);
  }

  @Override
  protected void command_MOVE(ComponentInfo component, Boolean data) throws Exception {
    m_component.command_MOVE(component, data);
  }

  @Override
  protected void command_ADD(ComponentInfo component, Boolean data) throws Exception {
    m_component.command_ADD(component, data);
  }
}
