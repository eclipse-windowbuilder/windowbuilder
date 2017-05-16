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
package org.eclipse.wb.core.gef.part;

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.model.description.IComponentDescription;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.eclipse.swt.graphics.Image;

/**
 * {@link EditPart} that displays icon for its component.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public abstract class ComponentIconEditPart extends GraphicalEditPart {
  private final Object m_component;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentIconEditPart(Object component) {
    setModel(component);
    m_component = component;
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
        Image image = getIcon();
        graphics.drawImage(image, 0, 0);
      }
    };
  }

  @Override
  protected void refreshVisuals() {
    org.eclipse.swt.graphics.Rectangle iconBounds = getIcon().getBounds();
    Rectangle bounds = getFigureBounds(iconBounds.width, iconBounds.height);
    getFigure().setBounds(bounds);
  }

  private Image getIcon() {
    IComponentDescription description =
        GlobalState.getDescriptionHelper().getDescription(m_component);
    return description.getIcon();
  }

  /**
   * @return the bounds of {@link Figure} based on size of icon.
   */
  protected abstract Rectangle getFigureBounds(int width, int height);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    installEditPolicy(EditPolicy.SELECTION_ROLE, new NonResizableSelectionEditPolicy());
  }
}
