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

import java.io.Serializable;

/**
 * Stores four integers for top, left, bottom, and right measurements.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public final class Insets implements Serializable {
  private static final long serialVersionUID = 0L;
  //
  public static final Insets ZERO_INSETS = new Insets();
  /**
   * distance from top
   */
  public int top;
  /**
   * distance from left
   */
  public int left;
  /**
   * distance from bottom
   */
  public int bottom;
  /**
   * distance from right
   */
  public int right;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructs an Insets with all zeroes.
   */
  public Insets() {
  }

  /**
   * Constructs a new Insets with initial values the same as the provided Insets.
   */
  public Insets(Insets insets) {
    this(insets.top, insets.left, insets.bottom, insets.right);
  }

  /**
   * Constructs a new Insets with all the sides set to the specified value.
   */
  public Insets(int value) {
    this(value, value, value, value);
  }

  /**
   * Creates a new Insets with the specified top, left, bottom, and right values.
   */
  public Insets(int top, int left, int bottom, int right) {
    this.top = top;
    this.left = left;
    this.bottom = bottom;
    this.right = right;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the height for this Insets, equal to <code>top</code> + <code>bottom</code>.
   *
   * @see #getWidth()
   */
  public int getHeight() {
    return top + bottom;
  }

  /**
   * Returns the width for this Insets, equal to <code>left</code> + <code>right</code>.
   *
   * @see #getHeight()
   */
  public int getWidth() {
    return left + right;
  }

  /**
   * Returns true if all values are 0.
   */
  public boolean isEmpty() {
    return left == 0 && right == 0 && top == 0 && bottom == 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Modify operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds the values of the specified Insets to this Insets' values.
   */
  public Insets add(Insets insets) {
    top += insets.top;
    bottom += insets.bottom;
    left += insets.left;
    right += insets.right;
    return this;
  }

  /**
   * Transposes this object. Top and Left are exchanged. Bottom and Right are exchanged. Can be used
   * in orientation changes.
   */
  public Insets transpose() {
    int temp = top;
    top = left;
    left = temp;
    temp = right;
    right = bottom;
    bottom = temp;
    return this;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates an Insets representing the sum of this Insets with the specified Insets.
   */
  public Insets getAdded(Insets insets) {
    return new Insets(this).add(insets);
  }

  /**
   * Creates a new Insets with transposed values. Top and Left are transposed. Bottom and Right are
   * transposed.
   */
  public Insets getTransposed() {
    return new Insets(this).transpose();
  }

  /**
   * Returns negate of the Insets.
   */
  public Insets getNegated() {
    return new Insets(-top, -left, -bottom, -right);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + bottom;
    result = prime * result + left;
    result = prime * result + right;
    result = prime * result + top;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Insets other = (Insets) obj;
    if (bottom != other.bottom) {
      return false;
    }
    if (left != other.left) {
      return false;
    }
    if (right != other.right) {
      return false;
    }
    if (top != other.top) {
      return false;
    }
    return true;
  }

  /**
   * String representation.
   */
  @Override
  public String toString() {
    return "Insets(t=" + top + ", l=" + left + ", b=" + bottom + ", r=" + right + ")";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the minimal {@link Insets} (minimum for each side) from two given {@link Insets}.
   */
  public static Insets min(Insets insets_1, Insets insets_2) {
    return new Insets(Math.min(insets_1.top, insets_2.top),
        Math.min(insets_1.left, insets_2.left),
        Math.min(insets_1.bottom, insets_2.bottom),
        Math.min(insets_1.right, insets_2.right));
  }
}