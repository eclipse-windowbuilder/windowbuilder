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
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.draw2d.scroll.HorizontalScrollModel;
import org.eclipse.wb.internal.draw2d.scroll.ScrollModel;
import org.eclipse.wb.internal.draw2d.scroll.VerticalScrollModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * A Canvas that contains {@link Figure Figures}.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class FigureCanvas extends Canvas {
  private RootFigure m_rootFigure;
  private final Dimension m_rootPreferredSize = new Dimension();
  private ScrollModel m_horizontalModel;
  private ScrollModel m_verticalModel;
  private Image m_bufferedImage;
  private boolean m_drawCached;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FigureCanvas(Composite parent, int style) {
    super(parent, style | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE);
    // initialize scroll begin state
    initScrolling();
    // add all listeners
    hookControlEvents();
    // create root figure
    createRootFigure();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FigureCanvas
  //
  ////////////////////////////////////////////////////////////////////////////
  private void initScrolling() {
    m_horizontalModel = new HorizontalScrollModel(this);
    m_verticalModel = new VerticalScrollModel(this);
  }

  private void createRootFigure() {
    m_rootFigure = new RootFigure(this, new RefreshManager(this));
    m_rootFigure.setBackground(getBackground());
    m_rootFigure.setForeground(getForeground());
    m_rootFigure.setFont(getFont());
    setDefaultEventManager();
  }

  protected void setDefaultEventManager() {
    m_rootFigure.setEventManager(new EventManager(this));
  }

  private void disposeBufferedImage() {
    if (m_bufferedImage != null) {
      m_bufferedImage.dispose();
      m_bufferedImage = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the {@link ScrollModel} what support the horizontal scrolling.
   */
  public ScrollModel getHorizontalScrollModel() {
    return m_horizontalModel;
  }

  /**
   * Returns the {@link ScrollModel} what support the vertical scrolling.
   */
  public ScrollModel getVerticalScrollModel() {
    return m_verticalModel;
  }

  /**
   * Returns figures container.
   */
  public RootFigure getRootFigure() {
    return m_rootFigure;
  }

  /**
   * Sets draw cached mode.
   */
  public void setDrawCached(boolean value) {
    m_drawCached = value;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Control
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Point computeSize(int wHint, int hHint, boolean changed) {
    Dimension size = m_rootFigure.getPreferredSize().getUnioned(wHint, hHint);
    return new Point(size.width, size.height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle events
  //
  ////////////////////////////////////////////////////////////////////////////
  private void hookControlEvents() {
    addListener(SWT.Dispose, new Listener() {
      public void handleEvent(Event event) {
        disposeBufferedImage();
      }
    });
    addListener(SWT.Resize, new Listener() {
      public void handleEvent(Event event) {
        handleResize();
      }
    });
    addListener(SWT.Paint, new Listener() {
      public void handleEvent(Event event) {
        handlePaint(event.gc, event.x, event.y, event.width, event.height);
      }
    });
  }

  private void handleResize() {
    disposeBufferedImage();
    configureScrollingAndRedraw();
  }

  private void handlePaint(GC paintGC, int x, int y, int width, int height) {
    // check draw cached mode
    if (m_drawCached) {
      if (m_bufferedImage == null) {
        paintGC.fillRectangle(x, y, width, height);
      } else {
        paintGC.drawImage(m_bufferedImage, 0, 0);
      }
      return;
    }
    // check double buffered image
    if (m_bufferedImage == null) {
      Point size = getSize();
      m_bufferedImage = new Image(null, size.x, size.y);
    }
    // prepare double buffered Graphics
    GC bufferedGC = new GC(m_bufferedImage);
    try {
      bufferedGC.setClipping(x, y, width, height);
      bufferedGC.setBackground(paintGC.getBackground());
      bufferedGC.setForeground(paintGC.getForeground());
      bufferedGC.setFont(paintGC.getFont());
      bufferedGC.setLineStyle(paintGC.getLineStyle());
      bufferedGC.setLineWidth(paintGC.getLineWidth());
      bufferedGC.setXORMode(paintGC.getXORMode());
      // draw content
      Graphics graphics = new Graphics(bufferedGC);
      graphics.translate(-m_horizontalModel.getSelection(), -m_verticalModel.getSelection());
      m_rootFigure.paint(graphics);
    } finally {
      bufferedGC.dispose();
    }
    // flush painting
    paintGC.drawImage(m_bufferedImage, 0, 0);
  }

  private void configureScrollingAndRedraw() {
    // cache figures preferred size
    m_rootPreferredSize.setSize(m_rootFigure.getPreferredSize());
    // get client area
    org.eclipse.swt.graphics.Rectangle clientArea = getClientArea();
    // set new figures bounds (thereof or resize window or resize/relocate figure)
    m_rootFigure.setBounds(new Rectangle(clientArea).setLocation(0, 0));
    // configure horizontal and vertical scroll bar's
    m_horizontalModel.configure(clientArea.width, m_rootPreferredSize.width);
    m_verticalModel.configure(clientArea.height, m_rootPreferredSize.height);
    // set repaint
    redraw();
  }

  /**
   * Check bounds and reconfigure scroll bar's if needed and repaint client area.
   */
  public void handleRefresh(int x, int y, int width, int height) {
    if (m_rootPreferredSize.equals(m_rootFigure.getPreferredSize())) {
      // calculate paint area
      Point size = getSize();
      Rectangle paintArea = new Rectangle(0, 0, size.x, size.y);
      paintArea.intersect(new Rectangle(x, y, width, height));
      // set repaint
      redraw(paintArea.x, paintArea.y, paintArea.width, paintArea.height, true);
    } else {
      configureScrollingAndRedraw();
    }
  }
}