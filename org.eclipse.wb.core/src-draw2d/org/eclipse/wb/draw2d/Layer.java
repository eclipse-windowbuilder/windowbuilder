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
package org.eclipse.wb.draw2d;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.draw2d.IRootFigure;

/**
 * A transparent figure simple figure's container using into {@link IRootFigure}.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class Layer extends Figure {
  private final String m_name;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Layer(String name) {
    m_name = name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Direct set bounds from {@link IRootFigure} without notification.
   */
  @Override
  public void setBounds(Rectangle bounds) {
    getBounds().setBounds(bounds);
  }

  /**
   * If children not contains given point <code>(x, y)</code> then {@link Layer} just as not
   * contains it.
   */
  @Override
  public boolean containsPoint(int x, int y) {
    for (Figure childFigure : getChildren()) {
      if (childFigure.containsPoint(x, y)) {
        return true;
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return identification name.
   */
  public String getName() {
    return m_name;
  }

  /**
   * For this figure opaque is missing.
   */
  @Override
  public void setOpaque(boolean opaque) {
  }
}