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
package org.eclipse.wb.internal.core.utils.ui.dialogs.color;

import org.eclipse.wb.internal.core.utils.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * Control for displaying {@link ColorInfo}.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public final class ColorPreviewCanvas extends Canvas {
  private ColorInfo m_color;
  private final boolean m_showShortText;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColorPreviewCanvas(Composite parent, int style, boolean showShortText) {
    super(parent, style);
    m_showShortText = showShortText;
    addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        onPaint(e.gc);
      }
    });
  }

  public ColorPreviewCanvas(Composite parent, int style) {
    this(parent, style, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setColor(ColorInfo color) {
    m_color = color;
    redraw();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Painting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Point computeSize(int wHint, int hHint, boolean changed) {
    int width = wHint;
    int height = 70;
    return new Point(width, height);
  }

  private void onPaint(GC gc) {
    Rectangle r = getClientArea();
    if (m_color != null) {
      int y = 0;
      // draw title
      {
        String title;
        if (m_showShortText) {
          title = m_color.getName();
        } else {
          title = m_color.getTitle();
        }
        int height = gc.stringExtent(title).y;
        drawCenteredText(gc, title, 0, 0, r.width, height);
        y += height;
      }
      // draw description if present
      if (m_color.m_description != null) {
        String text = m_color.m_description;
        int height = gc.stringExtent(text).y;
        drawCenteredText(gc, text, 0, y, r.width, height);
        y += height;
      }
      // draw color
      if (m_color.m_rgb != null) {
        Color color = new Color(getDisplay(), m_color.m_rgb);
        try {
          // as background
          {
            Color oldBackground = gc.getBackground();
            gc.setBackground(color);
            gc.fillRectangle(0, y, r.width / 2, r.height - y);
            gc.setBackground(oldBackground);
          }
          // as foreground
          {
            Font font = new Font(getDisplay(), "Arial", 16, SWT.NORMAL);
            try {
              gc.setFont(font);
              gc.setForeground(color);
              drawCenteredText(
                  gc,
                  Messages.ColorPreviewCanvas_sampleText,
                  r.width / 2,
                  y,
                  r.width / 2,
                  r.height - y);
            } finally {
              font.dispose();
            }
          }
        } finally {
          color.dispose();
        }
      }
    }
  }

  /**
   * Draws given text at center of given rectangle.
   *
   * @return the extent of given text
   */
  private static void drawCenteredText(GC gc, String text, int x, int y, int w, int h) {
    Point extent = gc.textExtent(text);
    gc.drawText(text, x + (w - extent.x) / 2, y + (h - extent.y) / 2, true);
  }
}
