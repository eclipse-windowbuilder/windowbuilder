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
package org.eclipse.wb.draw2d.border;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;

/**
 * A {@link Border} is a graphical decoration that is painted just inside the outer edge of a
 * {@link Figure}.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public abstract class Border {
  private final Insets m_insets;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructor {@link Border} with border insets.
   */
  public Border(Insets insets) {
    m_insets = insets;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Border
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Get border insets.
   */
  public Insets getInsets() {
    return m_insets;
  }

  /**
   * Paint border for <code>owner</code> {@link Figure}.
   */
  public final void paint(Figure owner, Graphics graphics) {
    Rectangle bounds = owner.getBounds();
    paint(bounds.width, bounds.height, graphics);
  }

  /**
   * Paint border for {@link Figure}. Coordinate (0, 0) correspond with {@link Figure} (0, 0) and
   * <code>onwerWidth</code>, <code>ownerHeight</code> correspond with {@link Figure}
   * <code>width</code>, <code>height</code>.
   */
  protected abstract void paint(int ownerWidth, int ownerHeight, Graphics graphics);
}