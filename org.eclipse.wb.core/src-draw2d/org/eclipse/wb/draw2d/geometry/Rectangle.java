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
 * Represents a Rectangle(x, y, width, height). This class provides various methods for manipulating
 * this Rectangle or creating new derived geometrical Objects.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public final class Rectangle implements Translatable, Serializable {
  private static final long serialVersionUID = 0L;
  /**
   * the X value
   */
  public int x;
  /**
   * the Y value
   */
  public int y;
  /**
   * the width
   */
  public int width;
  /**
   * the height
   */
  public int height;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public Rectangle() {
  }

  /**
   * Constructs a Rectangle given a location and size.
   */
  public Rectangle(Point location, Dimension size) {
    this(location.x, location.y, size.width, size.height);
  }

  /**
   * Constructs a copy of the provided Rectangle.
   */
  public Rectangle(Rectangle rectangle) {
    this(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
  }

  /**
   * Constructs a copy of the provided SWT {@link org.eclipse.swt.graphics.Rectangle}.
   */
  public Rectangle(org.eclipse.swt.graphics.Rectangle rectangle) {
    this(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
  }

  /**
   * Constructs a Rectangle with the provided values.
   */
  public Rectangle(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  /**
   * Constructs the smallest Rectangle that contains the specified Points.
   *
   * @param point1
   *          Upper left hand corner
   * @param point2
   *          Lower right hand corner
   */
  public Rectangle(Point point1, Point point2) {
    setLocation(point1);
    union(point2);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns a new Rectangle which has the exact same parameters as this Rectangle.
   */
  public Rectangle getCopy() {
    return new Rectangle(this);
  }

  /**
   * Creates a new SWT {@link org.eclipse.swt.graphics.Rectangle Rectangle} from this Rectangle.
   */
  public org.eclipse.swt.graphics.Rectangle getSwtRectangle() {
    return new org.eclipse.swt.graphics.Rectangle(x, y, width, height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the upper left hand corner of the rectangle.
   */
  public Point getLocation() {
    return new Point(x, y);
  }

  /**
   * Retuns the dimensions of this Rectangle.
   */
  public Dimension getSize() {
    return new Dimension(width, height);
  }

  /**
   * Sets the parameters of this Rectangle from the Rectangle passed in and returns this for
   * convenience.
   */
  public Rectangle setBounds(Rectangle rectangle) {
    x = rectangle.x;
    y = rectangle.y;
    width = rectangle.width;
    height = rectangle.height;
    return this;
  }

  /**
   * Sets the parameters of this Rectangle given as input and returns this for convenience.
   */
  public Rectangle setBounds(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    return this;
  }

  /**
   * Sets the location of this Rectangle to the point given as input and returns this for
   * convenience.
   */
  public Rectangle setLocation(Point point) {
    x = point.x;
    y = point.y;
    return this;
  }

  /**
   * Sets the location of this Rectangle to the coordinates given as input and returns this for
   * convenience.
   */
  public Rectangle setLocation(int x, int y) {
    this.x = x;
    this.y = y;
    return this;
  }

  /**
   * Sets the width and height of this Rectangle to the width and height of the given Dimension and
   * returns this for convenience.
   */
  public Rectangle setSize(Dimension dimension) {
    width = dimension.width;
    height = dimension.height;
    return this;
  }

  /**
   * Sets the width of this Rectangle to <i>w</i> and the height of this Rectangle to <i>h</i> and
   * returns this for convenience.
   */
  public Rectangle setSize(int width, int height) {
    this.width = width;
    this.height = height;
    return this;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Advanced operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Moves x-coordinate on specified value, keeping right side.
   */
  public void moveX(int deltaX) {
    x += deltaX;
    width -= deltaX;
  }

  /**
   * Moves y-coordinate on specified value, keeping bottom side.
   */
  public void moveY(int deltaY) {
    y += deltaY;
    height -= deltaY;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates that <code>x</code>, but keeps {@link #right()}.
   */
  public void setX(int x) {
    width += this.x - x;
    this.x = x;
  }

  /**
   * Returns the x-coordinate of the left side of this Rectangle.
   */
  public int left() {
    return x;
  }

  /**
   * Returns the x-coordinate of the right side of this Rectangle.
   */
  public int right() {
    return x + width;
  }

  /**
   * Updates the width to have specified {@link #right()}.
   */
  public void setRight(int right) {
    width = right - x;
  }

  /**
   * Updates that <code>y</code>, but keeps {@link #bottom()}.
   */
  public void setY(int y) {
    height += this.y - y;
    this.y = y;
  }

  /**
   * Returns the y-coordinate of the top side of this Rectangle.
   */
  public int top() {
    return y;
  }

  /**
   * Returns the y-coordinate of the bottom of this Rectangle.
   */
  public int bottom() {
    return y + height;
  }

  /**
   * Updates the width to have specified {@link #bottom()}.
   */
  public void setBottom(int bottom) {
    height = bottom - y;
  }

  /**
   * Returns whether the given point is within the boundaries of this Rectangle. The boundaries are
   * inclusive of the top and left edges, but exclusive of the bottom and right edges.
   */
  public boolean contains(Point point) {
    return contains(point.x, point.y);
  }

  /**
   * Returns whether the given coordinates are within the boundaries of this Rectangle. The
   * boundaries are inclusive of the top and left edges, but exclusive of the bottom and right
   * edges.
   */
  public boolean contains(int _x, int _y) {
    return _x >= x && _y >= y && _x < x + width && _y < y + height;
  }

  /**
   * Returns <code>true</code> if the input Rectangle intersects this Rectangle.
   */
  public boolean intersects(Rectangle rect) {
    return rect.x < x + width
        && rect.y < y + height
        && rect.x + rect.width > x
        && rect.y + rect.height > y;
  }

  /**
   * Returns <code>true</code> if this Rectangle's width or height is less than or equal to 0.
   */
  public boolean isEmpty() {
    return width <= 0 || height <= 0;
  }

  /**
   * Returns <code>true</code> if the input Rectangle touches this Rectangle.
   */
  public boolean touches(Rectangle rectangle) {
    return rectangle.x <= x + width
        && rectangle.y <= y + height
        && rectangle.x + rectangle.width >= x
        && rectangle.y + rectangle.height >= y;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Modify operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Crops this rectangle by the amount specified in <code>insets</code>.
   */
  public Rectangle crop(Insets insets) {
    if (insets == null) {
      return this;
    }
    x += insets.left;
    y += insets.top;
    width -= insets.getWidth();
    height -= insets.getHeight();
    return this;
  }

  /**
   * Expands the horizontal and vertical sides of this Rectangle with the values provided as input,
   * and returns this for convenience. The location of its center is kept constant.
   */
  public Rectangle expand(int hIncrement, int vIncrement) {
    return shrink(-hIncrement, -vIncrement);
  }

  /**
   * Expands the horizontal and vertical sides of this Rectangle by the width and height of the
   * given Insets, and returns this for convenience.
   */
  public Rectangle expand(Insets insets) {
    x -= insets.left;
    y -= insets.top;
    height += insets.getHeight();
    width += insets.getWidth();
    return this;
  }

  /**
   * Sets the size of this Rectangle to the intersection region with the Rectangle supplied as
   * input, and returns this for convenience. The location and dimensions are set to zero if there
   * is no intersection with the input Rectangle.
   */
  public Rectangle intersect(Rectangle rect) {
    int x1 = Math.max(x, rect.x);
    int x2 = Math.min(x + width, rect.x + rect.width);
    int y1 = Math.max(y, rect.y);
    int y2 = Math.min(y + height, rect.y + rect.height);
    //
    if (x2 - x1 < 0 || y2 - y1 < 0) {
      x = y = width = height = 0; // No intersection
    } else {
      x = x1;
      y = y1;
      width = x2 - x1;
      height = y2 - y1;
    }
    return this;
  }

  /**
   * Resizes this Rectangle by the Dimension provided as input and returns this for convenience.
   * This Rectange's width will become this.width + sizeDelta.width. Likewise for height.
   */
  public Rectangle resize(Dimension sizeDelta) {
    width += sizeDelta.width;
    height += sizeDelta.height;
    return this;
  }

  /**
   * Resizes this Rectangle by the values supplied as input and returns this for convenience. This
   * Rectangle's width will become this.width + dw. This Rectangle's height will become this.height
   * + dh.
   */
  public Rectangle resize(int dw, int dh) {
    width += dw;
    height += dh;
    return this;
  }

  /**
   * Scales the location and size of this Rectangle by the given scale and returns this for
   * convenience.
   */
  public final Rectangle scale(double scaleFactor) {
    return scale(scaleFactor, scaleFactor);
  }

  /**
   * Scales the location and size of this Rectangle by the given scales and returns this for
   * convenience.
   */
  public Rectangle scale(double scaleX, double scaleY) {
    int oldX = x;
    int oldY = y;
    x = (int) Math.floor(x * scaleX);
    y = (int) Math.floor(y * scaleY);
    width = (int) Math.ceil((oldX + width) * scaleX) - x;
    height = (int) Math.ceil((oldY + height) * scaleY) - y;
    return this;
  }

  /**
   * Shrinks the sides of this Rectangle by the horizontal and vertical values provided as input,
   * and returns this Rectangle for convenience. The center of this Rectangle is kept constant.
   */
  public Rectangle shrink(int h, int v) {
    x += h;
    width -= h + h;
    y += v;
    height -= v + v;
    return this;
  }

  /**
   * Switches the x and y values, as well as the width and height of this Rectangle. Useful for
   * orientation changes.
   */
  public Rectangle transpose() {
    int temp = x;
    x = y;
    y = temp;
    temp = width;
    width = height;
    height = temp;
    return this;
  }

  /**
   * Unions this Rectangle's width and height with the specified Dimension.
   */
  public Rectangle union(Dimension dimension) {
    width = Math.max(width, dimension.width);
    height = Math.max(height, dimension.height);
    return this;
  }

  /**
   * Updates this Rectangle's bounds to the minimum size which can hold both this Rectangle and the
   * coordinate (x,y).
   */
  public Rectangle union(int x1, int y1) {
    if (x1 < x) {
      width += x - x1;
      x = x1;
    } else {
      int right = x + width;
      if (x1 >= right) {
        right = x1 + 1;
        width = right - x;
      }
    }
    if (y1 < y) {
      height += y - y1;
      y = y1;
    } else {
      int bottom = y + height;
      if (y1 >= bottom) {
        bottom = y1 + 1;
        height = bottom - y;
      }
    }
    return this;
  }

  /**
   * Updates this Rectangle's bounds to the minimum size which can hold both this Rectangle and the
   * given Point.
   */
  public final void union(Point point) {
    union(point.x, point.y);
  }

  /**
   * Updates this Rectangle's dimensions to the minimum size which can hold both this Rectangle and
   * the given Rectangle.
   */
  public final Rectangle union(Rectangle rectangle) {
    if (rectangle == null) {
      return this;
    }
    return union(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
  }

  /**
   * Updates this Rectangle's dimensions to the minimum size which can hold both this Rectangle and
   * the rectangle (x, y, w, h).
   */
  public Rectangle union(int _x, int _y, int _w, int _h) {
    int right = Math.max(x + width, _x + _w);
    int bottom = Math.max(y + height, _y + _h);
    x = Math.min(x, _x);
    y = Math.min(y, _y);
    width = right - x;
    height = bottom - y;
    return this;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ITranslatable
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Moves this Rectangle horizontally by the x value of the given Point and vertically by the y
   * value of the given Point.
   */
  public void translate(Point point) {
    x += point.x;
    y += point.y;
  }

  /**
   * Moves this {@link Rectangle} horizontally by the <code>.width</code> value of the given
   * {@link Dimension} and vertically by the <code>.height</code> value of the given
   * {@link Dimension}.
   */
  public void translate(Dimension dimension) {
    x += dimension.width;
    y += dimension.height;
  }

  /**
   * Moves this {@link Rectangle} horizontally by the <code>.left</code> value of the given
   * {@link Insets} and vertically by the <code>.top</code> value of the given {@link Insets}.
   */
  public void translate(Insets insets) {
    x += insets.left;
    y += insets.top;
  }

  /**
   * Moves this Rectangle horizontally by dx and vertically by dy.
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
   * Returns a new Point representing the middle point of the bottom side of this Rectangle.
   */
  public Point getBottom() {
    return new Point(x + width / 2, bottom());
  }

  /**
   * Returns a new Point representing the bottom left point of this Rectangle.
   */
  public Point getBottomLeft() {
    return new Point(x, y + height);
  }

  /**
   * Returns a new Point representing the bottom right point of this Rectangle.
   */
  public Point getBottomRight() {
    return new Point(x + width, y + height);
  }

  /**
   * Returns a new point representing the center of this Rectangle.
   */
  public Point getCenter() {
    return new Point(x + width / 2, y + height / 2);
  }

  /**
   * Returns a new Rectangle with the specified insets cropped.
   */
  public Rectangle getCropped(Insets insets) {
    Rectangle rectangle = new Rectangle(this);
    rectangle.crop(insets);
    return rectangle;
  }

  /**
   * Returns a new incremented Rectangle, where the sides are expanded by the horizonatal and
   * vertical values provided. The center of the Rectangle is maintained constant.
   */
  public Rectangle getExpanded(int hIncrement, int vIncrement) {
    return new Rectangle(this).expand(hIncrement, vIncrement);
  }

  /**
   * Creates and returns a new Rectangle with the bounds of <code>this</code> Rectangle, expanded by
   * the given Insets.
   */
  public Rectangle getExpanded(Insets insets) {
    return new Rectangle(this).expand(insets);
  }

  /**
   * Returns a new Rectangle which has the intersection of this Rectangle and the rectangle provided
   * as input. Returns an empty Rectangle if there is no intersection.
   */
  public Rectangle getIntersection(Rectangle rectangle) {
    int x1 = Math.max(x, rectangle.x);
    int x2 = Math.min(x + width, rectangle.x + rectangle.width);
    int y1 = Math.max(y, rectangle.y);
    int y2 = Math.min(y + height, rectangle.y + rectangle.height);
    //
    if (x2 - x1 < 0 || y2 - y1 < 0) {
      return new Rectangle(); // No intersection
    }
    return new Rectangle(x1, y1, x2 - x1, y2 - y1);
  }

  /**
   * Returns a new Point representing the middle point of the left hand side of this Rectangle.
   */
  public Point getLeft() {
    return new Point(x, y + height / 2);
  }

  /**
   * Returns a new Rectangle which is equivalent to this Rectangle with its dimensions modified by
   * the passed width <i>w</i> and height <i>h</i>.
   */
  public Rectangle getResized(int w, int h) {
    return new Rectangle(this).resize(w, h);
  }

  /**
   * Returns a new Rectangle which is equivalent to this Rectangle with its dimensions modified by
   * the passed Dimension <i>d</i>.
   */
  public Rectangle getResized(Dimension dimension) {
    return new Rectangle(this).resize(dimension);
  }

  /**
   * Returns a new Point which represents the middle point of the right hand side of this Rectangle.
   */
  public Point getRight() {
    return new Point(right(), y + height / 2);
  }

  /**
   * Returns a new Point which represents the middle point of the top side of this Rectangle.
   */
  public Point getTop() {
    return new Point(x + width / 2, y);
  }

  /**
   * Returns a new Point which represents the top left hand corner of this Rectangle.
   */
  public Point getTopLeft() {
    return new Point(x, y);
  }

  /**
   * Returns a new Point which represents the top right hand corner of this Rectangle.
   */
  public Point getTopRight() {
    return new Point(x + width, y);
  }

  /**
   * Returns a new Rectangle which is shifted along each axis by the passed values.
   */
  public Rectangle getTranslated(int dx, int dy) {
    Rectangle rectangle = new Rectangle(this);
    rectangle.translate(dx, dy);
    return rectangle;
  }

  /**
   * Returns a new Rectangle which is shifted by the position of the given Point.
   */
  public Rectangle getTranslated(Point point) {
    Rectangle rectangle = new Rectangle(this);
    rectangle.translate(point);
    return rectangle;
  }

  /**
   * Returns a new rectangle whose width and height have been interchanged, as well as its x and y
   * values. This can be useful in orientation changes.
   */
  public Rectangle getTransposed() {
    Rectangle rectangle = new Rectangle(this);
    rectangle.transpose();
    return rectangle;
  }

  /**
   * Returns a new Rectangle which contains both this Rectangle and the Rectangle supplied as input.
   */
  public Rectangle getUnion(Rectangle rectangle) {
    if (rectangle == null || rectangle.isEmpty()) {
      return new Rectangle(this);
    }
    Rectangle union = new Rectangle(Math.min(x, rectangle.x), Math.min(y, rectangle.y), 0, 0);
    union.width = Math.max(x + width, rectangle.x + rectangle.width) - union.x;
    union.height = Math.max(y + height, rectangle.y + rectangle.height) - union.y;
    return union;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Transform to interval
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Interval} of projection on X or Y axis, depending on
   *         <code>isHorizonal</code> param.
   */
  public Interval getInterval(boolean isHorizontal) {
    return isHorizontal ? new Interval(x, width) : new Interval(y, height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns whether the input object is equal to this Rectangle or not. Rectangles are equivalent
   * if their x, y, height, and width values are the same.
   */
  @Override
  public boolean equals(Object object) {
    if (object == this) {
      return true;
    } else if (object instanceof Rectangle) {
      Rectangle rectangle = (Rectangle) object;
      return x == rectangle.x
          && y == rectangle.y
          && width == rectangle.width
          && height == rectangle.height;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return x + y + width + height;
  }

  /**
   * Returns the description of this Rectangle.
   */
  @Override
  public String toString() {
    return "Rectangle(" + x + ", " + y + ", " + width + ", " + height + ")";
  }
}