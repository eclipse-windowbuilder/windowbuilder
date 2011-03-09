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
package org.eclipse.wb.internal.rcp.swing2swt.gef;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.ControlPositionLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.swing2swt.layout.BorderLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link BorderLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage rcp.swing2swt.gef
 */
public final class BorderLayoutEditPolicy extends ControlPositionLayoutEditPolicy<String> {
  private final BorderLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BorderLayoutEditPolicy(BorderLayoutInfo layout) {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addFeedbacks() throws Exception {
    addFeedback(0, 0, 1, 0.25, new Insets(0, 0, 1, 0), "North", "NORTH");
    addFeedback(0, 0.75, 1, 1, new Insets(1, 0, 0, 0), "South", "SOUTH");
    addFeedback(0, 0.25, 0.25, 0.75, new Insets(1, 0, 1, 1), "West", "WEST");
    addFeedback(0.75, 0.25, 1, 0.75, new Insets(1, 1, 1, 0), "East", "EAST");
    addFeedback(0.25, 0.25, 0.75, 0.75, new Insets(1, 1, 1, 1), "Center", "CENTER");
  }

  private void addFeedback(double px1,
      double py1,
      double px2,
      double py2,
      Insets insets,
      String hint,
      String region) throws Exception {
    if (m_layout.getControl(region) == null) {
      super.addFeedback(px1, py1, px2, py2, insets, hint, region);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation of commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(ControlInfo component, String data) throws Exception {
    m_layout.command_CREATE(component, data);
  }

  @Override
  protected void command_MOVE(ControlInfo component, String data) throws Exception {
    m_layout.command_MOVE(component, data);
  }
}
