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
import org.eclipse.wb.internal.swing.gef.policy.component.box.GlueSelectionEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import javax.swing.Box;

/**
 * The {@link EditPart} for {@link Box#createHorizontalGlue()}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.part
 */
public final class BoxGlueHorizontalEditPart extends BoxEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BoxGlueHorizontalEditPart(ComponentInfo component) {
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
    installEditPolicy(EditPolicy.SELECTION_ROLE, new GlueSelectionEditPolicy());
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
   * Draws horizontal spring in given {@link Rectangle}.
   */
  static void draw(Graphics graphics, Rectangle r) {
    graphics.pushState();
    try {
      graphics.translate(r.getLocation());
      // draw spring
      {
        graphics.setForegroundColor(COLOR_SPRING);
        int x = 0;
        while (x < r.width) {
          graphics.drawLine(x, 3, x + 2, 3 + 5);
          x += 2;
          graphics.drawLine(x, 3 + 5, x + 2, 3);
          x += 2;
        }
      }
      // draw borders
      {
        graphics.setForegroundColor(COLOR_BORDER);
        graphics.drawLine(0, 0, 0, r.height);
        graphics.drawLine(1, 0, 1, r.height);
        graphics.drawLine(r.width - 1, 0, r.width - 1, r.height);
        graphics.drawLine(r.width - 2, 0, r.width - 2, r.height);
      }
    } finally {
      graphics.popState();
    }
  }
}
