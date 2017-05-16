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
import org.eclipse.wb.draw2d.geometry.Point;

/**
 * This class use to find {@link Figure} under mouse.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class TargetFigureFindVisitor extends FigureVisitor {
  private final Point m_location;
  private Figure m_result;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TargetFigureFindVisitor(FigureCanvas canvas, int x, int y) {
    m_location = new Point(x, y);
    m_location.x += canvas.getHorizontalScrollModel().getSelection();
    m_location.y += canvas.getVerticalScrollModel().getSelection();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visitor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean visit(Figure figure) {
    boolean canAccept =
        m_result == null
            && figure.isVisible()
            && figure.containsPoint(m_location)
            && acceptVisit(figure);
    if (canAccept) {
      // found result, but we need to check child figure's
      m_location.translate(figure.getLocation().getNegated());
      m_location.translate(figure.getInsets().getNegated());
      return true;
    }
    return false;
  }

  @Override
  public void endVisit(Figure figure) {
    // OK, this location last child figure under mouse
    if (m_result == null) {
      if (acceptResult(figure)) {
        m_result = figure;
      } else {
        // if figure was visited, but NOT accepted, we should restore location
        m_location.translate(figure.getLocation());
        m_location.translate(figure.getInsets());
      }
    }
  }

  /**
   * Returns <code>true</code> if given {@link Figure} can involve to search.<br>
   * By default return <code>true</code>.
   */
  protected boolean acceptVisit(Figure figure) {
    return true;
  }

  /**
   * Returns <code>true</code> if given {@link Figure} is result.<br>
   * By default return <code>true</code>.
   */
  protected boolean acceptResult(Figure figure) {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return mouse target {@link Figure}.
   */
  public Figure getTargetFigure() {
    return m_result;
  }
}