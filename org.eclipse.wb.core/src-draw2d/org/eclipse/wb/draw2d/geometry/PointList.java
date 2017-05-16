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
 * Represents a List of Points. This class is used for building an <code>int[]</code>. The array is
 * internal, and is constructed and queried by the client using {@link Point Points}. SWT uses
 * integer arrays when painting polylines and polygons.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public final class PointList implements Translatable, Serializable {
  private static final long serialVersionUID = 0L;
  //
  private int[] m_points = new int[0];
  private Rectangle m_bounds;
  private int m_size;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructs an empty PointList.
   */
  public PointList() {
  }

  /**
   * Constructs a PointList with initial capacity <i>size</i>, but no points.
   */
  public PointList(int size) {
    m_points = new int[size * 2];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add/Remove/Set
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds Point <i>p</i> to this PointList.
   */
  public void addPoint(Point p) {
    addPoint(p.x, p.y);
  }

  /**
   * Adds the input point values to this PointList.
   *
   * @param x
   *          X value of a point to add
   * @param y
   *          Y value of a point to add
   */
  public void addPoint(int x, int y) {
    m_bounds = null;
    int arrayLength = m_points.length;
    int usedLength = m_size * 2;
    if (arrayLength == usedLength) {
      int old[] = m_points;
      m_points = new int[arrayLength + 2];
      System.arraycopy(old, 0, m_points, 0, arrayLength);
    }
    m_points[usedLength] = x;
    m_points[usedLength + 1] = y;
    m_size++;
  }

  /**
   * Inserts a given point at a specified index.
   *
   * @param p
   *          Point to be inserted.
   * @param index
   *          Position where the point is to be inserted.
   * @exception IndexOutOfBoundsException
   *              if the index is invalid
   * @see #setPoint(Point, int)
   */
  public void insertPoint(Point p, int index) {
    m_bounds = null;
    if (index > m_size || index < 0) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + m_size);
    }
    index *= 2;
    int length = m_points.length;
    int old[] = m_points;
    m_points = new int[length + 2];
    System.arraycopy(old, 0, m_points, 0, index);
    System.arraycopy(old, index, m_points, index + 2, length - index);
    m_points[index] = p.x;
    m_points[index + 1] = p.y;
    m_size++;
  }

  /**
   * Removes all the points stored by this list. Resets all the properties based on the point
   * information.
   */
  public void removeAllPoints() {
    m_bounds = null;
    m_size = 0;
  }

  /**
   * Removes the point at the specified index from the PointList, and returns it.
   *
   * @see #addPoint(Point)
   * @param index
   *          Index of the point to be removed.
   * @return The point which has been removed
   * @throws IndexOutOfBoundsException
   *           if the removal index is beyond the list capacity
   */
  public Point removePoint(int index) {
    m_bounds = null;
    if (index < 0 || index >= m_size) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + m_size);
    }
    index *= 2;
    Point pt = new Point(m_points[index], m_points[index + 1]);
    if (index != m_size * 2 - 2) {
      System.arraycopy(m_points, index + 2, m_points, index, m_size * 2 - index - 2);
    }
    m_size--;
    return pt;
  }

  /**
   * Sets the size of this PointList.
   */
  public void setSize(int newSize) {
    if (m_points.length > newSize * 2) {
      m_size = newSize;
      return;
    }
    int[] newArray = new int[newSize * 2];
    System.arraycopy(m_points, 0, newArray, 0, m_points.length);
    m_points = newArray;
    m_size = newSize;
  }

  /**
   * Returns the number of points in this PointList.
   */
  public int size() {
    return m_size;
  }

  /**
   * Returns the contents of this PointList as an integer array.
   */
  public int[] toIntArray() {
    if (m_points.length != m_size * 2) {
      int[] old = m_points;
      m_points = new int[m_size * 2];
      System.arraycopy(old, 0, m_points, 0, m_size * 2);
    }
    return m_points;
  }

  /**
   * Creates a copy of this PointList
   */
  public PointList getCopy() {
    PointList result = new PointList(m_size);
    System.arraycopy(m_points, 0, result.m_points, 0, m_size * 2);
    result.m_size = m_size;
    result.m_bounds = null;
    return result;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Point Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the first Point in the list.
   *
   * @throws IndexOutOfBoundsException
   *           if the list is empty
   */
  public Point getFirstPoint() {
    return getPoint(0);
  }

  /**
   * Returns the last point in the list.
   *
   * @throws IndexOutOfBoundsException
   *           if the list is empty
   */
  public Point getLastPoint() {
    return getPoint(m_size - 1);
  }

  /**
   * Returns the midpoint of the list of Points. The midpoint is the median of the List, unless
   * there are 2 medians (size is even), then the middle of the medians is returned.
   *
   * @throws IndexOutOfBoundsException
   *           if the list is empty
   */
  public Point getMidpoint() {
    if (m_size % 2 == 0) {
      return getPoint(m_size / 2 - 1).getTranslated(getPoint(m_size / 2)).scale(0.5f);
    }
    return getPoint(m_size / 2);
  }

  /**
   * Returns the Point in the list at the specified index.
   *
   * @throws IndexOutOfBoundsException
   *           If the specified index is out of range
   */
  public Point getPoint(int index) {
    if (index < 0 || index >= m_size) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + m_size);
    }
    index *= 2;
    return new Point(m_points[index], m_points[index + 1]);
  }

  /**
   * Copies the x and y values at given index into a specified Point. This method exists to avoid
   * the creation of a new <code>Point</code>.
   *
   * @see #getPoint(int)
   * @param p
   *          The Point which will be set with the &lt;x, y&gt; values
   * @param index
   *          The index being requested
   * @return The parameter <code>p</code> is returned for convenience
   */
  public Point getPoint(Point p, int index) {
    if (index < 0 || index >= m_size) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + m_size);
    }
    index *= 2;
    p.x = m_points[index];
    p.y = m_points[index + 1];
    return p;
  }

  /**
   * Overwrites a point at a given index in the list with the specified Point.
   *
   * @param pt
   *          Point which is to be stored at the index.
   * @param index
   *          Index where the given point is to be stored.
   */
  public void setPoint(Point pt, int index) {
    if (index < 0 || index >= m_size) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + m_size);
    }
    m_bounds = null;
    m_points[index * 2] = pt.x;
    m_points[index * 2 + 1] = pt.y;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the smallest Rectangle which contains all Points.
   */
  public Rectangle getBounds() {
    if (m_bounds != null) {
      return m_bounds;
    }
    m_bounds = new Rectangle();
    if (m_size > 0) {
      m_bounds.setLocation(m_points[0], m_points[1]);
      int coordinateCount = m_size * 2;
      for (int i = 0; i < coordinateCount; i += 2) {
        m_bounds.union(m_points[i], m_points[i + 1]);
      }
    }
    return m_bounds;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ITranslatable
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Moves the origin (0,0) of the coordinate system of all the points to the Point <i>pt</i>. This
   * updates the position of all the points in this PointList.
   */
  public final void translate(Point pt) {
    translate(pt.x, pt.y);
  }

  /**
   * Moves the origin (0,0) of the coordinate system of all the points to the Dimension
   * <i>dimension</i>. This updates the position of all the points in this PointList.
   */
  public final void translate(Dimension dimension) {
    translate(dimension.width, dimension.height);
  }

  /**
   * Moves the origin (0,0) of the coordinate system of all the points to the Insets <i>insets</i>.
   * This updates the position of all the points in this PointList.
   */
  public final void translate(Insets insets) {
    translate(insets.left, insets.top);
  }

  /**
   * Moves the origin (0,0) of the coordinate system of all the points to the Point (x,y). This
   * updates the position of all the points in this PointList.
   *
   * @param x
   *          Amount by which all the points will be shifted on the X axis.
   * @param y
   *          Amount by which all the points will be shifted on the Y axis.
   * @see #translate(Point)
   */
  public void translate(int x, int y) {
    if (x == 0 && y == 0) {
      return;
    }
    if (m_bounds != null) {
      m_bounds.translate(x, y);
    }
    for (int i = 0; i < m_size * 2; i += 2) {
      m_points[i] += x;
      m_points[i + 1] += y;
    }
  }
}