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
 * The {@link EditPart} for {@link Box#createGlue()}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.part
 */
public final class BoxGlueEditPart extends BoxEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BoxGlueEditPart(ComponentInfo component) {
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
        int x = (r.width - SPRING_SIZE) / 2;
        int y = (r.height - SPRING_SIZE) / 2;
        BoxGlueHorizontalEditPart.draw(graphics, new Rectangle(0, y, r.width, SPRING_SIZE));
        BoxGlueVerticalEditPart.draw(graphics, new Rectangle(x, 0, SPRING_SIZE, r.height));
      }
    };
  }
}
