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
import org.eclipse.wb.internal.swing.gef.policy.component.box.StrutDirectHorizontalEditPolicy;
import org.eclipse.wb.internal.swing.gef.policy.component.box.StrutSelectionHorizontalEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import javax.swing.Box;

/**
 * The {@link EditPart} for {@link Box#createHorizontalStrut(int)}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.part
 */
public final class BoxStrutHorizontalEditPart extends BoxEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BoxStrutHorizontalEditPart(ComponentInfo component) {
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
    installEditPolicy(
        EditPolicy.SELECTION_ROLE,
        new StrutSelectionHorizontalEditPolicy(m_component));
    installEditPolicy(new StrutDirectHorizontalEditPolicy(m_component));
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
   * Draws horizontal strut in given {@link Rectangle}.
   */
  static void draw(Graphics graphics, Rectangle r) {
    int y = r.getCenter().y;
    // draw strut
    {
      graphics.setForegroundColor(COLOR_SPRING);
      graphics.drawLine(r.left(), y - 1, r.right(), y - 1);
      graphics.drawLine(r.left(), y + 1, r.right(), y + 1);
    }
    // draw borders
    {
      graphics.setForegroundColor(COLOR_BORDER);
      int y1 = y - SPRING_SIZE / 2;
      int y2 = y + SPRING_SIZE / 2;
      graphics.drawLine(r.left() + 0, y1, r.left() + 0, y2);
      graphics.drawLine(r.left() + 1, y1, r.left() + 1, y2);
      graphics.drawLine(r.right() - 1, y1, r.right() - 1, y2);
      graphics.drawLine(r.right() - 2, y1, r.right() - 2, y2);
    }
  }
}
