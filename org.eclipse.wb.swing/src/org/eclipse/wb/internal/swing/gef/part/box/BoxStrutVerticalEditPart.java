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
package org.eclipse.wb.internal.swing.gef.part.box;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.swing.gef.policy.component.box.StrutDirectVerticalEditPolicy;
import org.eclipse.wb.internal.swing.gef.policy.component.box.StrutSelectionVerticalEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import javax.swing.Box;

/**
 * The {@link EditPart} for {@link Box#createVerticalStrut(int)}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.part
 */
public final class BoxStrutVerticalEditPart extends BoxEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BoxStrutVerticalEditPart(ComponentInfo component) {
    super(component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(EditPolicy.SELECTION_ROLE, new StrutSelectionVerticalEditPolicy(m_component));
    installEditPolicy(new StrutDirectVerticalEditPolicy(m_component));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    return new Figure() {
      @Override
      protected void paintClientArea(Graphics graphics) {
        Rectangle r = getClientArea();
        draw(graphics, r);
      }
    };
  }

  /**
   * Draws vertical strut in given {@link Rectangle}.
   */
  static void draw(Graphics graphics, Rectangle r) {
    int x = r.getCenter().x;
    // draw strut
    {
      graphics.setForegroundColor(COLOR_SPRING);
      graphics.drawLine(x - 1, r.top(), x - 1, r.bottom());
      graphics.drawLine(x + 1, r.top(), x + 1, r.bottom());
    }
    // draw borders
    {
      graphics.setForegroundColor(COLOR_BORDER);
      int x1 = x - SPRING_SIZE / 2;
      int x2 = x + SPRING_SIZE / 2;
      graphics.drawLine(x1, r.top() + 0, x2, r.top() + 0);
      graphics.drawLine(x1, r.top() + 1, x2, r.top() + 1);
      graphics.drawLine(x1, r.bottom() - 1, x2, r.bottom() - 1);
      graphics.drawLine(x1, r.bottom() - 2, x2, r.bottom() - 2);
    }
  }
}
