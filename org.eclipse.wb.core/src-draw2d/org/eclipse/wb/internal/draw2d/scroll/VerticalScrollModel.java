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
package org.eclipse.wb.internal.draw2d.scroll;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

/**
 * VerticalScrollModel represents model for support vertical scrolling.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class VerticalScrollModel extends ScrollModel {
  private final Canvas m_canvas;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VerticalScrollModel(Canvas canvas) {
    super(canvas.getVerticalBar());
    m_canvas = canvas;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ScrollModel
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Scroll to top part of window area.
   */
  @Override
  protected void handlePositiveScrolling(int delta, int newSelection) {
    Rectangle clientArea = m_canvas.getClientArea();
    m_canvas.scroll(0, 0, 0, delta, clientArea.width, clientArea.height - delta, true);
    m_selection = newSelection;
    m_canvas.redraw(0, clientArea.height - delta, clientArea.width, delta, true);
  }

  /**
   * Scroll to bottom part of window area.
   */
  @Override
  protected void handleNegativeScrolling(int delta, int newSelection) {
    Rectangle clientArea = m_canvas.getClientArea();
    m_canvas.scroll(0, delta, 0, 0, clientArea.width, clientArea.height - delta, true);
    m_selection = newSelection;
    m_canvas.redraw(0, 0, clientArea.width, delta, true);
  }
}