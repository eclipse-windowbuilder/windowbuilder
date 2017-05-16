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
package org.eclipse.wb.draw2d.geometry;

/**
 * Conditionally transposes geometrical objects based on an "enabled" flag. When enabled, the method
 * t(Object) will transpose the passed geometrical object. Otherwise, the object will be returned
 * without modification
 *
 * @author lobas_av
 * @author mitin_aa
 * @coverage gef.draw2d
 */
public class Transposer {
  private boolean m_enabled;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public Transposer() {
    super();
  }

  public Transposer(boolean enabled) {
    super();
    m_enabled = enabled;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // State
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Disables transposing of inputs.
   */
  public void disable() {
    m_enabled = false;
  }

  /**
   * Enables transposing of inputs.
   */
  public void enable() {
    m_enabled = true;
  }

  /**
   * Returns <code>true</code> if this {@link Transposer} is enabled.
   */
  public boolean isEnabled() {
    return m_enabled;
  }

  /**
   * Sets the enabled state of this {@link Transposer}.
   */
  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns a new transposed {@link Dimension} of the input {@link Dimension}.
   */
  public Dimension t(Dimension dimension) {
    return m_enabled ? dimension.getTransposed() : dimension;
  }

  /**
   * Returns a new transposed {@link Insets} of the input {@link Insets}.
   */
  public Insets t(Insets insets) {
    return m_enabled ? insets.getTransposed() : insets;
  }

  /**
   * Returns a new transposed {@link Point} of the input {@link Point}.
   */
  public Point t(Point point) {
    return m_enabled ? point.getTransposed() : point;
  }

  /**
   * Returns a new transposed {@link Rectangle} of the input {@link Rectangle}.
   */
  public Rectangle t(Rectangle rectangle) {
    return m_enabled ? rectangle.getTransposed() : rectangle;
  }
}