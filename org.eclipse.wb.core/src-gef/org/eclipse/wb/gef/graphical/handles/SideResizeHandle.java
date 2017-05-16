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
package org.eclipse.wb.gef.graphical.handles;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.ICursorConstants;
import org.eclipse.wb.draw2d.ILocator;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

/**
 * Resize {@link Handle} located on left/top/right/bottom sides of owner {@link GraphicalEditPart}.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class SideResizeHandle extends Handle {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SideResizeHandle(GraphicalEditPart owner, int side, int width, boolean center) {
    super(owner, new ResizeHandleLocator(owner.getFigure(), side, width, center));
    if (side == IPositionConstants.LEFT || side == IPositionConstants.RIGHT) {
      setCursor(ICursorConstants.SIZEE);
    } else {
      setCursor(ICursorConstants.SIZEN);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Locator
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class ResizeHandleLocator implements ILocator {
    private final Figure m_reference;
    private final int m_side;
    private final int m_width;
    private final boolean m_center;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ResizeHandleLocator(Figure reference, int side, int width, boolean center) {
      m_reference = reference;
      m_side = side;
      m_width = width;
      m_center = center;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ILocator
    //
    ////////////////////////////////////////////////////////////////////////////
    public void relocate(Figure target) {
      Rectangle bounds = m_reference.getBounds().getCopy();
      FigureUtils.translateFigureToFigure(m_reference, target, bounds);
      //
      int locationOffset = m_center ? m_width / 2 : m_width;
      if (m_side == IPositionConstants.LEFT) {
        bounds.x -= locationOffset;
        bounds.width = m_width;
      } else if (m_side == IPositionConstants.RIGHT) {
        bounds.x = bounds.right() - locationOffset;
        bounds.width = m_width;
      } else if (m_side == IPositionConstants.TOP) {
        bounds.y -= locationOffset;
        bounds.height = m_width;
      } else {
        bounds.y = bounds.bottom() - locationOffset;
        bounds.height = m_width;
      }
      target.setBounds(bounds);
    }
  }
}