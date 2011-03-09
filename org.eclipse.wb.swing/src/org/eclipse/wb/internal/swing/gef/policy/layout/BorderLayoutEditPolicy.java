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
package org.eclipse.wb.internal.swing.gef.policy.layout;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.ComponentsLayoutRequestValidator;
import org.eclipse.wb.internal.swing.gef.policy.ComponentPositionLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.BorderLayoutInfo;

import java.awt.BorderLayout;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link BorderLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class BorderLayoutEditPolicy extends ComponentPositionLayoutEditPolicy<String> {
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
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return ComponentsLayoutRequestValidator.INSTANCE_EXT;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addFeedbacks() throws Exception {
    addFeedback(0, 0, 1, 0.25, new Insets(0, 0, 1, 0), "North", BorderLayout.NORTH, "NORTH");
    addFeedback(0, 0.75, 1, 1, new Insets(1, 0, 0, 0), "South", BorderLayout.SOUTH, "SOUTH");
    addFeedback(0, 0.25, 0.25, 0.75, new Insets(1, 0, 1, 1), "West", BorderLayout.WEST, "WEST");
    addFeedback(0.75, 0.25, 1, 0.75, new Insets(1, 1, 1, 0), "East", BorderLayout.EAST, "EAST");
    addFeedback(
        0.25,
        0.25,
        0.75,
        0.75,
        new Insets(1, 1, 1, 1),
        "Center",
        BorderLayout.CENTER,
        "CENTER");
  }

  /**
   * Adds feedback for given constraints.
   */
  private void addFeedback(double px1,
      double py1,
      double px2,
      double py2,
      Insets insets,
      String hint,
      String region,
      String regionField) throws Exception {
    if (m_layout.getComponent(region) == null) {
      addFeedback(px1, py1, px2, py2, insets, hint, region);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation of commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(ComponentInfo component, String data) throws Exception {
    m_layout.command_CREATE(component, data);
  }

  @Override
  protected void command_MOVE(ComponentInfo component, String data) throws Exception {
    m_layout.command_MOVE(component, data);
  }
}
