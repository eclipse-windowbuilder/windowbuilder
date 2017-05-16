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
package org.eclipse.wb.internal.core.gef.part;

import org.eclipse.wb.core.gef.policy.selection.LineSelectionEditPolicy;
import org.eclipse.wb.core.model.IWrapper;
import org.eclipse.wb.core.model.IWrapperInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

import org.eclipse.swt.widgets.Display;

/**
 * {@link EditPart} for wrapper {@link IWrapperInfo}.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public class AbstractWrapperEditPart extends GraphicalEditPart {
  private final IWrapper m_wrapper;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractWrapperEditPart(IWrapper wrapper) {
    setModel(wrapper.getWrapperInfo());
    m_wrapper = wrapper;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(EditPolicy.SELECTION_ROLE, new LineSelectionEditPolicy(IColorConstants.black));
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
        graphics.drawImage(m_wrapper.getWrapperInfo().getDescription().getIcon(), 0, 0);
      }
    };
  }

  @Override
  protected void refreshVisuals() {
    Display.getCurrent().asyncExec(new Runnable() {
      public void run() {
        refreshVisuals0();
      }
    });
  }

  /**
   * {@link EditPart} refreshes children and then visuals. So, we should wait for parent visuals
   * refresh.
   */
  private void refreshVisuals0() {
    org.eclipse.swt.graphics.Rectangle imageBounds =
        m_wrapper.getWrapperInfo().getDescription().getIcon().getBounds();
    int width = imageBounds.width;
    int height = imageBounds.height;
    Rectangle parentClientArea = ((GraphicalEditPart) getParent()).getFigure().getClientArea();
    Point location = parentClientArea.getBottomRight().getTranslated(-width, -height);
    location.translate(-3, -3);
    Rectangle bounds = new Rectangle(location.x, location.y, width, height);
    // no animation
    getFigure().setBounds(bounds);
  }
}