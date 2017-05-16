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
package org.eclipse.wb.draw2d.events;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.draw2d.FigureCanvas;

/**
 * Instances of this class are sent whenever mouse related actions occur. This includes mouse
 * buttons being pressed and released, the mouse pointer being moved and the mouse pointer crossing
 * widget boundaries.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public final class MouseEvent {
  /**
   * the button that was pressed or released; 1 for the first button, 2 for the second button, and 3
   * for the third button, etc.
   */
  public final int button;
  /**
   * the state of the keyboard modifier keys at the time the event was generated
   */
  public final int stateMask;
  /**
   * the widget-relative, x coordinate of the pointer at the time the mouse button was pressed or
   * released
   */
  public final int x;
  /**
   * the widget-relative, y coordinate of the pointer at the time the mouse button was pressed or
   * released
   */
  public final int y;
  /**
   * the {@link Figure} that issued the event
   */
  public final Figure source;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MouseEvent(FigureCanvas canvas, org.eclipse.swt.events.MouseEvent event, Figure source) {
    button = event.button;
    stateMask = event.stateMask;
    this.source = source;
    //
    Rectangle bounds = source.getBounds();
    Point location = new Point(event.x - bounds.x, event.y - bounds.y);
    location.x += canvas.getHorizontalScrollModel().getSelection();
    location.y += canvas.getVerticalScrollModel().getSelection();
    FigureUtils.translateAbsoluteToFigure(source, location);
    //
    x = location.x;
    y = location.y;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Consume
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_consumed;

  /**
   * Marks this event as consumed so that it doesn't get passed on to other listeners.
   */
  public void consume() {
    m_consumed = true;
  }

  /**
   * Return whether this event has been consumed.
   */
  public boolean isConsumed() {
    return m_consumed;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer("MouseEvent{source=");
    buffer.append(source);
    buffer.append(" button=");
    buffer.append(button);
    buffer.append(" stateMask=");
    buffer.append(stateMask);
    buffer.append(" x=");
    buffer.append(x);
    buffer.append(" y=");
    buffer.append(y);
    buffer.append('}');
    return buffer.toString();
  }
}