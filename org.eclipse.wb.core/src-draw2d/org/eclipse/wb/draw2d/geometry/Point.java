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
 * Represents a point (x, y) in 2-dimensional space. This class provides various methods for
 * manipulating this Point or creating new derived geometrical Objects.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public final class Point implements Translatable, Serializable {
  private static final long serialVersionUID = 0L;
  /**
   * x value
   */
  public int x;
  /**
   * y value
   */
  public int y;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructs a Point at location (0,0).
   */
  public Point() {
  }

  /**
   * Constructs a Point at the specified x and y locations.
   */
  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Constructs a Point at the specified x and y locations.
   */
  public Point(double x, double y) {
    this.x = (int) x;
    this.y = (int) y;
  }

  /**
   * Constructs a Point which is at the same location as the specified
   * {@link org.eclipse.swt.graphics.Point Point}.
   */
  public Point(Point copy) {
    x = copy.x;
    y = copy.y;
  }

  /**
   * Constructs a Point which is at the same location as the specified Point.
   */
  public Point(org.eclipse.swt.graphics.Point copy) {
    x = copy.x;
    y = copy.y;
  }

  /**
   * Constructs a {@link Point} which is at the same location as the specified {@link Dimension}.
   */
  public Point(Dimension copy) {
    x = copy.width;
    y = copy.height;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return a copy of this Point
   */
  public Point getCopy() {
    return new Point(x, y);
  }

  /**
   * Creates a new SWT {@link org.eclipse.swt.graphics.Point Point} from this Point.
   */
  public org.eclipse.swt.graphics.Point getSwtPoint() {
    return new org.eclipse.swt.graphics.Point(x, y);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the location of this Point to the provided x and y locations.
   *
   * @return <code>this</code> for convenience
   */
  public Point setLocation(int x, int y) {
    this.x = x;
    this.y = y;
    return this;
  }

  /**
   * Sets the location of this Point to the specified Point.
   */
  public Point setLocation(Point point) {
    x = point.x;
    y = point.y;
    return this;
  }

  /**
   * Sets the location of this Point to the specified Point.
   */
  public Point setLocation(org.eclipse.swt.graphics.Point point) {
    x = point.x;
    y = point.y;
    return this;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Calculates the difference in between this Point and the one specified.
   */
  public Dimension getDifference(Point point) {
    return new Dimension(x - point.x, y - point.y);
  }

  /**
   * Calculates the distance from this Point to the one specified.
   */
  public double getDistance(Point point) {
    return Math.sqrt(getDistance2(point));
  }

  /**
   * Calculates the distance squared between this Point and the one specified.
   */
  public int getDistance2(Point point) {
    int dx = point.x - x;
    int dy = point.y - y;
    return dx * dx + dy * dy;
  }

  /**
   * Calculates the orthogonal distance to the specified point. The orthogonal distance is the sum
   * of the horizontal and vertical differences.
   */
  public int getDistanceOrthogonal(Point point) {
    return Math.abs(y - point.y) + Math.abs(x - point.x);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Modify operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Negates the x and y values of this Point.
   */
  public Point negate() {
    x = -x;
    y = -y;
    return this;
  }

  /**
   * Scales this Point by the specified amount.
   */
  public Point scale(double amount) {
    return scale(amount, amount);
  }

  /**
   * Scales this Point by the specified values.
   */
  public Point scale(double xAmount, double yAmount) {
    x = (int) Math.floor(x * xAmount);
    y = (int) Math.floor(y * yAmount);
    return this;
  }

  /**
   * Transposes this object. X and Y values are exchanged.
   */
  public Point transpose() {
    int temp = x;
    x = y;
    y = temp;
    return this;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ITranslatable
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Shifts the location of this Point by the location of the input Point along each of the axes.
   */
  public void translate(Point point) {
    translate(point.x, point.y);
  }

  /**
   * Shifts this {@link Point} by the values of the {@link Dimension} along each axis.
   */
  public void translate(Dimension dimension) {
    translate(dimension.width, dimension.height);
  }

  /**
   * Shifts this {@link Point} by the values of the {@link Insets} along each axis.
   */
  public void translate(Insets insets) {
    translate(insets.left, insets.top);
  }

  /**
   * Shifts this Point by the values supplied along each axes.
   */
  public void translate(int dx, int dy) {
    x += dx;
    y += dy;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates a Point with negated x and y values.
   */
  public Point getNegated() {
    return getCopy().negate();
  }

  /**
   * Creates a new Point from this Point by scaling by the specified amount.
   */
  public Point getScaled(double amount) {
    return getCopy().scale(amount);
  }

  /**
   * Creates a new Point which is translated by the values of the input Dimension.
   */
  public Point getTranslated(Dimension delta) {
    Point copy = getCopy();
    copy.translate(delta);
    return copy;
  }

  /**
   * Creates a new Point which is translated by the specified x and y values
   */
  public Point getTranslated(int _x, int _y) {
    Point copy = getCopy();
    copy.translate(_x, _y);
    return copy;
  }

  /**
   * Creates a new Point which is translated by the values of the provided Point.
   */
  public Point getTranslated(Point point) {
    Point copy = getCopy();
    copy.translate(point);
    return copy;
  }

  /**
   * Creates a new Point with the transposed values of this Point. Can be useful in orientation
   * change calculations.
   */
  public Point getTransposed() {
    return getCopy().transpose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Static Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates a new Point representing the MAX of two provided Points.
   */
  public static Point max(Point point1, Point point2) {
    Point point = new Rectangle(point1, point2).getBottomRight();
    point.translate(-1, -1);
    return point;
  }

  /**
   * Creates a new Point representing the MIN of two provided Points.
   */
  public static Point min(Point point1, Point point2) {
    return new Rectangle(point1, point2).getTopLeft();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for equality.
   */
  @Override
  public boolean equals(Object object) {
    if (object instanceof Point) {
      Point point = (Point) object;
      return point.x == x && point.y == y;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return x ^ y << 10;
  }

  /**
   * String representation.
   */
  @Override
  public String toString() {
    return "Point(" + x + ", " + y + ")";
  }
}