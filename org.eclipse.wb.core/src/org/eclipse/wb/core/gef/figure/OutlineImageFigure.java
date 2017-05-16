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
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * Figure which draws associated {@link Image} and outlines it with border.
 *
 * @author mitin_aa
 * @coverage core.gef.policy
 */
public final class OutlineImageFigure extends Figure {
  private final Image m_image;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public OutlineImageFigure() {
    this(null);
  }

  public OutlineImageFigure(Image image) {
    this(image, IColorConstants.orange);
  }

  public OutlineImageFigure(Image image, Color borderColor) {
    m_image = image;
    setForeground(borderColor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Paint
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void paintClientArea(Graphics graphics) {
    if (m_image != null) {
      graphics.drawImage(m_image, 0, 0);
    }
    graphics.drawRectangle(getClientArea().getResized(-1, -1));
  }
}