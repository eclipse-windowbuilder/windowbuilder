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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;

/**
 * A figure that can display text.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class Label extends Figure {
  private String m_text = "";
  private Dimension m_preferredSize;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Construct an empty {@link Label}.
   */
  public Label() {
  }

  /**
   * Construct a {@link Label} with passed String as its text.
   */
  public Label(String text) {
    setText(text);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Label
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the desirable size for this label's text.
   */
  public Dimension getPreferredSize() {
    if (m_preferredSize == null) {
      m_preferredSize = FigureUtils.calculateTextSize(m_text, getFont());
      Insets insets = getInsets();
      m_preferredSize.expand(insets.getWidth(), insets.getHeight());
    }
    return m_preferredSize;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the text of the label.
   */
  public String getText() {
    return m_text;
  }

  /**
   * Sets the label's text.
   */
  public void setText(String text) {
    if (text == null) {
      text = "";
    }
    if (!m_text.equals(text)) {
      m_text = text;
      resetState();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void repaint(boolean reset, int x, int y, int width, int height) {
    if (reset) {
      m_preferredSize = null;
    }
    super.repaint(reset, x, y, width, height);
  }

  @Override
  protected void paintClientArea(Graphics graphics) {
    graphics.drawText(m_text, 0, 0);
  }
}