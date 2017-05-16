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
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.draw2d.SemiTransparentFigure;

import org.eclipse.swt.graphics.Color;

/**
 * Feedback with "ghost" effect.
 *
 * @author scheglov_ke
 * @coverage core.gef.figure
 */
public final class GhostPositionFeedback extends AbstractPositionFeedback {
  private static final Color m_fillColor = new Color(null, 0, 255, 0);
  private static final Color m_activeColor = new Color(null, 255, 255, 0);
  private static final Color m_borderColor = new Color(null, 0, 192, 0);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GhostPositionFeedback(Layer layer, Rectangle bounds, String hint) {
    super(layer, bounds, hint);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure methods
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    Figure figure = new SemiTransparentFigure(50);
    figure.setBackground(m_fillColor);
    figure.setBorder(new LineBorder(m_borderColor));
    return figure;
  }

  @Override
  public void update(boolean contains) {
    if (contains) {
      m_figure.setBackground(m_activeColor);
    } else {
      m_figure.setBackground(m_fillColor);
    }
  }
}