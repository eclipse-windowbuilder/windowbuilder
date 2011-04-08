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

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.GefMessages;
import org.eclipse.wb.internal.swing.gef.policy.ComponentPositionLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.JScrollPaneInfo;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link JScrollPaneInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class JScrollPaneLayoutEditPolicy extends ComponentPositionLayoutEditPolicy<String> {
  private final JScrollPaneInfo m_component;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JScrollPaneLayoutEditPolicy(JScrollPaneInfo component) {
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
    if (m_component.isEmptyPosition("getColumnHeader")) {
      addFeedback(
          0.0,
          0.0,
          1.0,
          0.2,
          new Insets(0, 0, 2, 0),
          GefMessages.JScrollPaneLayoutEditPolicy_columnHeader,
          "setColumnHeaderView");
    }
    if (m_component.isEmptyPosition("getRowHeader")) {
      addFeedback(
          0.0,
          0.2,
          0.2,
          1.0,
          new Insets(0, 0, 0, 2),
          GefMessages.JScrollPaneLayoutEditPolicy_rowHeader,
          "setRowHeaderView");
    }
    if (m_component.isEmptyPosition("getViewport")) {
      addFeedback(
          0.2,
          0.2,
          1.0,
          1.0,
          new Insets(0, 0, 0, 0),
          GefMessages.JScrollPaneLayoutEditPolicy_viewport,
          "setViewportView");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation of commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(ComponentInfo component, String data) throws Exception {
    m_component.command_CREATE(component, data);
  }

  @Override
  protected void command_MOVE(ComponentInfo component, String data) throws Exception {
    m_component.command_MOVE(component, data);
  }

  @Override
  protected void command_ADD(ComponentInfo component, String data) throws Exception {
    m_component.command_ADD(component, data);
  }
}
