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
import org.eclipse.wb.internal.swing.gef.policy.component.box.StrutDirectRigidEditPolicy;
import org.eclipse.wb.internal.swing.gef.policy.component.box.StrutSelectionRigidEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import javax.swing.Box;

/**
 * The {@link EditPart} for {@link Box#createRigidArea(java.awt.Dimension)}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.part
 */
public final class BoxRigidAreaEditPart extends BoxEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BoxRigidAreaEditPart(ComponentInfo component) {
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
    installEditPolicy(EditPolicy.SELECTION_ROLE, new StrutSelectionRigidEditPolicy(m_component));
    installEditPolicy(new StrutDirectRigidEditPolicy(m_component));
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
        BoxStrutHorizontalEditPart.draw(graphics, r);
        BoxStrutVerticalEditPart.draw(graphics, r);
      }
    };
  }
}
