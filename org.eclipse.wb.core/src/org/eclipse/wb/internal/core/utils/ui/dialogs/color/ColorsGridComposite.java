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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import org.apache.commons.lang.ArrayUtils;

/**
 * Control for displaying grid of {@link ColorInfo}.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public class ColorsGridComposite extends Canvas {
  private int m_cellWidth = 25;
  private int m_cellHeight = 25;
  private boolean m_showNames;
  private int m_maxNameWidth = Integer.MAX_VALUE;
  private int m_colorWidth;
  private ColorInfo[] m_colors;
  private int m_columns = 16;
  private int m_rows;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColorsGridComposite(Composite parent, int style) {
    super(parent, SWT.NO_MERGE_PAINTS | SWT.NO_BACKGROUND);
    setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        onPaint(e);
      }
    });
    addMouseListeners();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_captured;
  private ColorInfo m_currentColorUnderMouse;

  private void addMouseListeners() {
    addMouseMoveListener(new MouseMoveListener() {
      public void mouseMove(MouseEvent e) {
        if (!m_captured) {
          ColorInfo colorUnderMouse = getColorUnderMouse(e);
          setNewColorUnderMouse(colorUnderMouse);
        }
      }
    });
    addMouseTrackListener(new MouseTrackAdapter() {
      @Override
      public void mouseExit(MouseEvent e) {
        setNewColorUnderMouse(null);
      }
    });
    addMouseListener(new MouseAdapter() {
      private ColorInfo m_color;

      @Override
      public void mouseDown(MouseEvent e) {
        capture(true);
        m_color = getColorUnderMouse(e);
      }

      @Override
      public void mouseUp(MouseEvent e) {
        capture(false);
        // send notification that color was clicked
        if (getClientArea().contains(e.x, e.y)) {
          Event event = new Event();
          event.data = m_color;
          notifyListeners(SWT.Selection, event);
        }
      }

      private void capture(boolean capture) {
        setCapture(capture);
        m_captured = capture;
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColorInfo[] getColors() {
    return m_colors;
  }

  public void setColors(ColorInfo[] colors) {
    m_colors = colors;
    updateGrid();
  }

  public void setColumns(int columns) {
    m_columns = columns;
    updateGrid();
  }

  public void setCellWidth(int cellWidth) {
    m_cellWidth = cellWidth;
    updateGrid();
  }

  public void setCellHeight(int cellHeight) {
    m_cellHeight = cellHeight;
    updateGrid();
  }

  public void showNames(int colorWidth) {
    m_showNames = true;
    m_colorWidth = colorWidth;
  }

  public void setMaxNameWidth(int maxNameWidth) {
    m_maxNameWidth = maxNameWidth;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Paint
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Point computeSize(int wHint, int hHint, boolean changed) {
    if (m_showNames) {
      // prepare max width of name
      int maxNameWidth = 0;
      {
        GC gc = new GC(this);
        try {
          for (int i = 0; i < m_colors.length; i++) {
            ColorInfo colorInfo = m_colors[i];
            maxNameWidth = Math.max(maxNameWidth, gc.stringExtent(colorInfo.m_name).x);
          }
          maxNameWidth = Math.min(maxNameWidth, m_maxNameWidth);
        } finally {
          gc.dispose();
        }
      }
      // recalculate cell width
      m_cellWidth = m_colorWidth + maxNameWidth + 5;
    }
    return new Point(m_cellWidth * m_columns, m_cellHeight * m_rows);
  }

  private void onPaint(PaintEvent e) {
    GC paintGC = e.gc;
    Rectangle paintRect = new Rectangle(e.x, e.y, e.width, e.height);
    //
    Image backImage = new Image(getDisplay(), e.width, e.height);
    GC gc = new GC(backImage);
    //
    gc.setBackground(getBackground());
    gc.fillRectangle(0, 0, e.width, e.height);
    //
    try {
      int border = 3;
      for (int index = 0; index < m_colors.length; index++) {
        ColorInfo colorInfo = m_colors[index];
        Rectangle cellRect = getCellRectForIndex(index);
        if (!paintRect.intersects(cellRect)) {
          continue;
        }
        int x = cellRect.x;
        int y = cellRect.y;
        // translate coordinates to back image
        x -= e.x;
        y -= e.y;
        // draw cell for color
        if (m_showNames) {
          if (colorInfo.m_rgb != null) {
            drawColorCell(gc, x + border, y + border, m_colorWidth - 2 * border, m_cellHeight
                - 2
                * border, colorInfo);
          }
          {
            Point extent = gc.textExtent(colorInfo.m_name);
            int x0 = x + m_colorWidth;
            int y0 = y + (m_cellHeight - extent.y) / 2;
            gc.setForeground(getForeground());
            gc.drawText(colorInfo.m_name, x0, y0, true);
          }
        } else {
          drawColorCell(gc, x + border, y + border, m_cellWidth - 2 * border, m_cellHeight
              - 2
              * border, colorInfo);
        }
        // draw border around color under cursor
        if (colorInfo == m_currentColorUnderMouse) {
          gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
          gc.drawRectangle(x, y, cellRect.width - 1, cellRect.height - 1);
        }
      }
      // draw back image
      paintGC.drawImage(backImage, e.x, e.y);
    } finally {
      gc.dispose();
      backImage.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Draws 3d color cell for given {@link ColorInfo}.
   */
  public static void drawColorCell(GC gc, int x, int y, int w, int h, ColorInfo colorInfo) {
    Display display = Display.getCurrent();
    // fill rectangle with color
    {
      Color oldBackground = gc.getBackground();
      Color color = new Color(display, colorInfo.m_rgb);
      try {
        gc.setBackground(color);
        gc.fillRectangle(x, y, w, h);
      } finally {
        color.dispose();
        gc.setBackground(oldBackground);
      }
    }
    // draw 3D border
    {
      gc.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
      gc.drawLine(x, y, x + w - 1, y);
      gc.drawLine(x, y, x, y + h - 1);
    }
  }

  /**
   * @return {@link ColorInfo} under mouse cursor.
   */
  private ColorInfo getColorUnderMouse(MouseEvent e) {
    int column = e.x / m_cellWidth;
    int row = e.y / m_cellHeight;
    int index = row * m_columns + column;
    return index < m_colors.length ? m_colors[index] : null;
  }

  /**
   * Sets new color under mouse and forces redraw for old and new cells.
   */
  private void setNewColorUnderMouse(ColorInfo colorUnderMouse) {
    if (m_currentColorUnderMouse != colorUnderMouse) {
      // send notification
      {
        Event event = new Event();
        event.data = colorUnderMouse;
        notifyListeners(SWT.DefaultSelection, event);
      }
      // redraw cells
      {
        ColorInfo oldColorUnderMouse = m_currentColorUnderMouse;
        m_currentColorUnderMouse = colorUnderMouse;
        redrawColorCell(oldColorUnderMouse);
        redrawColorCell(m_currentColorUnderMouse);
      }
    }
  }

  /**
   * Marks cell for given color as "dirty" as needed to redraw.
   */
  private void redrawColorCell(ColorInfo colorInfo) {
    Rectangle cell = getCellRectForColor(colorInfo);
    if (cell != null) {
      redraw(cell.x, cell.y, cell.width, cell.height, false);
    }
  }

  /**
   * @return the rectangle for given color or <code>null</code> if <code>null</code> color given
   */
  private Rectangle getCellRectForColor(ColorInfo colorInfo) {
    if (colorInfo == null) {
      return null;
    }
    int index = ArrayUtils.indexOf(m_colors, colorInfo);
    return getCellRectForIndex(index);
  }

  /**
   * @return the rectangle for given color index
   */
  private Rectangle getCellRectForIndex(int index) {
    int column = index % m_columns;
    int row = index / m_columns;
    Rectangle cellRect =
        new Rectangle(m_cellWidth * column, m_cellHeight * row, m_cellWidth, m_cellHeight);
    return cellRect;
  }

  /**
   * Updates grid after any accessor.
   */
  private void updateGrid() {
    if (m_colors != null && m_columns != 0) {
      m_rows = m_colors.length / m_columns + (m_colors.length % m_columns != 0 ? 1 : 0);
      redraw();
    }
  }
}
