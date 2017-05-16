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
 * Stores an integer width and height. This class provides various methods for manipulating this
 * Dimension or creating new derived Objects.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public final class Dimension implements Serializable {
  private static final long serialVersionUID = 0L;
  /**
   * The width.
   */
  public int width;
  /**
   * The height.
   */
  public int height;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructs a Dimension of zero width and height.
   */
  public Dimension() {
  }

  /**
   * Constructs a Dimension with the supplied width and height values.
   */
  public Dimension(int width, int height) {
    this.width = width;
    this.height = height;
  }

  /**
   * Constructs a Dimension with the width and height of the passed Dimension.
   */
  public Dimension(Dimension dimension) {
    width = dimension.width;
    height = dimension.height;
  }

  /**
   * Constructs a Dimension where the width and height are the x and y distances of the input point
   * from the origin.
   */
  public Dimension(org.eclipse.swt.graphics.Point point) {
    width = point.x;
    height = point.y;
  }

  /**
   * Constructs a Dimension with the width and height of the Image supplied as input.
   */
  public Dimension(org.eclipse.swt.graphics.Image image) {
    org.eclipse.swt.graphics.Rectangle bounds = image.getBounds();
    width = bounds.width;
    height = bounds.height;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Copies the width and height values of the input Dimension to this Dimension.
   */
  public void setSize(Dimension dimension) {
    width = dimension.width;
    height = dimension.height;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns <code>true</code> if the input Dimension fits into this Dimension. A Dimension of the
   * same size is considered to "fit".
   */
  public boolean contains(Dimension dimension) {
    return width >= dimension.width && height >= dimension.height;
  }

  /**
   * Returns <code>true</code> if this Dimension properly contains the one specified. Proper
   * containment is defined as containment using "<", instead of "<=".
   */
  public boolean containsProper(Dimension dimension) {
    return width > dimension.width && height > dimension.height;
  }

  /**
   * Returns the area of this Dimension.
   */
  public int getArea() {
    return width * height;
  }

  /**
   * Returns <code>true</code> if this Dimension's width and height are equal to the given width and
   * height.
   */
  public boolean equals(int _width, int _height) {
    return width == _width && height == _height;
  }

  /**
   * Returns <code>true</code> if the Dimension has width or height greater than 0.
   */
  public boolean isEmpty() {
    return width <= 0 || height <= 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Modify operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Expands the size of this Dimension by the specified amount.
   */
  public Dimension expand(Dimension dimension) {
    width += dimension.width;
    height += dimension.height;
    return this;
  }

  /**
   * Expands the size of this Dimension by the specified amount.
   */
  public Dimension expand(Point point) {
    width += point.x;
    height += point.y;
    return this;
  }

  /**
   * Expands the size of this Dimension by the specified width and height.
   */
  public Dimension expand(int _width, int _height) {
    width += _width;
    height += _height;
    return this;
  }

  /**
   * This Dimension is intersected with the one specified. Intersection is performed by taking the
   * min() of the values from each dimension.
   *
   * @param dimension
   *          the Dimension used to perform the min()
   */
  public Dimension intersect(Dimension dimension) {
    width = Math.min(dimension.width, width);
    height = Math.min(dimension.height, height);
    return this;
  }

  /**
   * Negates the width and height of this Dimension.
   */
  public Dimension negate() {
    width = -width;
    height = -height;
    return this;
  }

  /**
   * Scales the width and height of this Dimension by the amount supplied, and returns this for
   * convenience.
   *
   * @param amount
   *          value by which this Dimension's width and height are to be scaled
   */
  public Dimension scale(double amount) {
    return scale(amount, amount);
  }

  /**
   * Scales the width of this Dimension by <i>width</i> and scales the height of this Dimension by
   * <i>height</i>. Returns this for convenience.
   */
  public Dimension scale(double _width, double _height) {
    width = (int) Math.floor(width * _width);
    height = (int) Math.floor(height * _height);
    return this;
  }

  /**
   * Reduces the width of this Dimension by <i>width</i>, and reduces the height of this Dimension
   * by <i>height</i>. Returns this for convenience.
   */
  public Dimension shrink(int w, int h) {
    return expand(-w, -h);
  }

  /**
   * Swaps the width and height of this Dimension, and returns this for convenience. Can be useful
   * in orientation changes.
   */
  public Dimension transpose() {
    int temp = width;
    width = height;
    height = temp;
    return this;
  }

  /**
   * Sets the width of this Dimension to the greater of this Dimension's width and
   * <i>dimension</i>.width. Likewise for this Dimension's height.
   */
  public Dimension union(Dimension dimension) {
    return union(dimension.width, dimension.height);
  }

  /**
   * Sets the width of this Dimension to the greater of this Dimension's width and <i>width</i>.
   * Likewise for this Dimension's height.
   */
  public Dimension union(int _width, int _height) {
    width = Math.max(width, _width);
    height = Math.max(height, _height);
    return this;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates and returns a copy of this Dimension.
   */
  public Dimension getCopy() {
    return new Dimension(this);
  }

  /**
   * Creates and returns a new Dimension representing the difference between this Dimension and the
   * one specified.
   */
  public Dimension getDifference(Dimension dimension) {
    return new Dimension(width - dimension.width, height - dimension.height);
  }

  /**
   * Creates and returns a Dimension representing the sum of this Dimension and the one specified.
   */
  public Dimension getExpanded(Dimension dimension) {
    return new Dimension(width + dimension.width, height + dimension.height);
  }

  /**
   * Creates and returns a new Dimension representing the sum of this Dimension and the one
   * specified.
   */
  public Dimension getExpanded(int _width, int _height) {
    return new Dimension(width + _width, height + _height);
  }

  /**
   * Creates and returns a new Dimension representing the intersection of this Dimension and the one
   * specified.
   */
  public Dimension getIntersected(Dimension dimension) {
    return new Dimension(this).intersect(dimension);
  }

  /**
   * Creates and returns a new Dimension with negated values.
   */
  public Dimension getNegated() {
    return new Dimension(-width, -height);
  }

  /**
   * Creates a new Dimension with its width and height scaled by the specified value.
   */
  public Dimension getScaled(double amount) {
    return new Dimension(this).scale(amount);
  }

  /**
   * Creates a new Dimension with its height and width swapped. Useful in orientation change
   * calculations.
   */
  public Dimension getTransposed() {
    return new Dimension(this).transpose();
  }

  /**
   * Creates a new Dimension representing the union of this Dimension with the one specified. Union
   * is defined as the max() of the values from each Dimension.
   */
  public Dimension getUnioned(Dimension dimension) {
    return new Dimension(this).union(dimension);
  }

  /**
   * Creates a new Dimension representing the union of this Dimension with the given input.
   */
  public Dimension getUnioned(int _width, int _height) {
    return new Dimension(this).union(_width, _height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns whether the input Object is equivalent to this Dimension. <code>true</code> if the
   * Object is a Dimension and its width and height are equal to this Dimension's width and height,
   * <code>false</code> otherwise.
   */
  @Override
  public boolean equals(Object object) {
    if (object instanceof Dimension) {
      Dimension dimension = (Dimension) object;
      return dimension.width == width && dimension.height == height;
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = result * 31 + width;
    result = result * 31 + height;
    return result;
  }

  /**
   * String representation.
   */
  @Override
  public String toString() {
    return "Dimension(" + width + ", " + height + ")";
  }
}