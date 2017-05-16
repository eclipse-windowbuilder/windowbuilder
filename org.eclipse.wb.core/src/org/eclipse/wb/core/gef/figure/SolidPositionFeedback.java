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
package org.eclipse.wb.core.gef.figure;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.graphics.Color;

/**
 * Feedback with opaque {@link Figure}.
 *
 * @author scheglov_ke
 * @coverage core.gef.figure
 */
public final class SolidPositionFeedback extends AbstractPositionFeedback {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SolidPositionFeedback(Layer layer, Rectangle bounds, String hint) {
    super(layer, bounds, hint);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure methods
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    Figure figure = new Figure();
    figure.setOpaque(true);
    figure.setBorder(new LineBorder(getBorderColor()));
    return figure;
  }

  @Override
  public void update(boolean contains) {
    if (contains) {
      m_figure.setBackground(getActiveColor());
    } else {
      m_figure.setBackground(getInactiveColor());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Colors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the border {@link Color}.
   */
  private Color getBorderColor() {
    return IColorConstants.darkGreen;
    //return IColorConstants.orange;
  }

  /**
   * @return the inactivate {@link Color}.
   */
  private Color getInactiveColor() {
    //return SWTResourceManager.getColor(0x64, 0x95, 0xED);
    return IColorConstants.lightGreen;
  }

  /**
   * @return the activate {@link Color}.
   */
  private Color getActiveColor() {
    //return SWTResourceManager.getColor(0x1E, 0xB0, 0xFF);
    return IColorConstants.yellow;
  }
}