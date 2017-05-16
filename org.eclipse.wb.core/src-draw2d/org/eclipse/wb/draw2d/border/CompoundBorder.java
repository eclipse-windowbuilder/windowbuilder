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

import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.geometry.Insets;

/**
 * {@link CompoundBorder} allows for the nesting of two borders. The nested borders are referred to
 * as the <i>inner</i> and <i>outer</i> borders.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class CompoundBorder extends Border {
  private final Border m_outer;
  private final Border m_inner;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructs a default {@link CompoundBorder} with no borders under it.
   */
  public CompoundBorder() {
    this(null, null);
  }

  /**
   * Constructs a {@link CompoundBorder} with the two borders specified as input.
   */
  public CompoundBorder(Border outer, Border inner) {
    super(null);
    m_outer = outer;
    m_inner = inner;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Assess
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the inner border of this {@link CompoundBorder}.
   */
  public Border getInnerBorder() {
    return m_inner;
  }

  /**
   * Returns the outer border of this {@link CompoundBorder}.
   */
  public Border getOuterBorder() {
    return m_outer;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Border
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Get border insets.
   */
  @Override
  public Insets getInsets() {
    Insets insets = new Insets();
    if (m_inner != null) {
      insets.add(m_inner.getInsets());
    }
    if (m_outer != null) {
      insets.add(m_outer.getInsets());
    }
    return insets;
  }

  @Override
  protected void paint(int ownerWidth, int ownerHeight, Graphics graphics) {
    if (m_outer != null) {
      // paint outer
      {
        graphics.pushState();
        m_outer.paint(ownerWidth, ownerHeight, graphics);
        graphics.popState();
      }
      // update bounds for inner
      {
        Insets insets = m_outer.getInsets();
        graphics.translate(insets.left, insets.top);
        ownerWidth -= insets.getWidth();
        ownerHeight -= insets.getHeight();
      }
    }
    if (m_inner != null) {
      m_inner.paint(ownerWidth, ownerHeight, graphics);
    }
  }
}