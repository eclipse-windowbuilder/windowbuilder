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
package org.eclipse.wb.internal.core.gef.part.nonvisual;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.draw2d.Label;

import org.eclipse.swt.graphics.Image;

/**
 * A figure that can display text and image.
 *
 * @author lobas_av
 * @coverage core.gef.nonvisual
 */
public class BeanFigure extends Figure {
  private final Image m_image;
  private final Label m_label = new Label();
  private final Point m_imageLocation = new Point();
  private final Dimension m_imageSize;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeanFigure(Image image) {
    m_image = image;
    m_imageSize = new Dimension(m_image);
    add(m_label);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void update(String text, Point location) {
    if (m_label.getText().equals(text)) {
      setLocation(location);
    } else {
      // configure text
      m_label.setText(text);
      Dimension textSize = m_label.getPreferredSize();
      // calculate total width
      int width = Math.max(m_imageSize.width, textSize.width);
      // set all bounds
      setBounds(new Rectangle(location.x, location.y, width, m_imageSize.height + textSize.height));
      m_imageLocation.x = width / 2 - m_imageSize.width / 2;
      m_label.setBounds(new Rectangle(width / 2 - textSize.width / 2,
          m_imageSize.height,
          textSize.width,
          textSize.height));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void paintClientArea(Graphics graphics) {
    graphics.drawImage(m_image, m_imageLocation);
  }
}