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

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.PointList;
import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.SWT;

/**
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class Polyline extends Figure {
  private boolean m_xorMode;
  private int m_lineStyle = SWT.LINE_SOLID;
  private int m_lineWidth = 1;
  private PointList m_points = new PointList();
  private Rectangle m_pointsBounds;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add/Remove/Get Point's
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds the passed point to the {@link Polyline}.
   */
  public void addPoint(Point point) {
    m_points.addPoint(point);
    resetState();
  }

  /**
   * Inserts a given point at a specified index in the {@link Polyline}.
   */
  public void insertPoint(Point point, int index) {
    m_points.insertPoint(point, index);
    resetState();
  }

  /**
   * Removes a point from the {@link Polyline}.
   */
  public void removePoint(int index) {
    m_points.removePoint(index);
    resetState();
  }

  /**
   * Erases the {@link Polyline} and removes all of its {@link Point Points}.
   */
  public void removeAllPoints() {
    m_points.removeAllPoints();
    resetState();
  }

  /**
   * Returns the points in this {@link Polyline} <B>by reference</B>. If the returned list is
   * modified, this {@link Polyline} must be informed by calling {@link #setPoints(PointList)}.
   * Failure to do so will result in layout and paint problems.
   */
  public PointList getPoints() {
    return m_points;
  }

  /**
   * Sets the list of points to be used by this polyline connection. Removes any previously existing
   * points.
   */
  public void setPoints(PointList points) {
    m_points = points;
    resetState();
  }

  /**
   * Returns the first point in the {@link Polyline}.
   */
  public Point getStart() {
    return m_points.getFirstPoint();
  }

  /**
   * Sets the start point of the {@link Polyline}.
   */
  public void setStart(Point point) {
    if (m_points.size() == 0) {
      addPoint(point);
    } else {
      setPoint(point, 0);
    }
  }

  /**
   * Sets the end point of the {@link Polyline}.
   */
  public void setEnd(Point point) {
    int size = m_points.size();
    if (size < 2) {
      addPoint(point);
    } else {
      setPoint(point, size - 1);
    }
  }

  /**
   * Returns the last point in the {@link Polyline}.
   */
  public Point getEnd() {
    return m_points.getLastPoint();
  }

  /**
   * Sets the points at both extremes of the {@link Polyline}.
   */
  public void setEndpoints(Point start, Point end) {
    setStart(start);
    setEnd(end);
  }

  /**
   * Sets the point at <code>index</code> to the {@link Point} <code>pt</code>. Calling this method
   * results in a recalculation of the polyline's bounding box. If you're going to set multiple
   * Points, use {@link #setPoints(PointList)}.
   */
  public void setPoint(Point point, int index) {
    m_points.setPoint(point, index);
    resetState();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Rectangle getBounds() {
    Rectangle bounds = super.getBounds();
    if (m_pointsBounds != m_points.getBounds()) {
      m_pointsBounds = m_points.getBounds();
      bounds.setBounds(m_pointsBounds);
      bounds.expand(m_lineWidth / 2, m_lineWidth / 2);
    }
    return bounds;
  }

  /**
   * For this figure bounds is calculate value.
   */
  @Override
  public void setBounds(Rectangle bounds) {
  }

  /**
   * Returns <code>true</code> if the point <code>(x, y)</code> is contained within this
   * {@link Figure}'s bounds. For {@link Polyline} check containts every polyline sections.
   */
  @Override
  public boolean containsPoint(int x, int y) {
    TEMP_BOUNDS.setBounds(getBounds());
    int tolerance = m_lineWidth / 2 + 2;
    TEMP_BOUNDS.expand(tolerance, tolerance);
    if (!TEMP_BOUNDS.contains(x, y)) {
      return false;
    }
    //
    int points[] = m_points.toIntArray();
    for (int index = 0; index < points.length - 3; index += 2) {
      if (lineContainsPoint(
          points[index],
          points[index + 1],
          points[index + 2],
          points[index + 3],
          x,
          y,
          tolerance)) {
        return true;
      }
    }
    return false;
  }

  private static final Rectangle TEMP_BOUNDS = new Rectangle();

  //
  private static final boolean lineContainsPoint(int x1,
      int y1,
      int x2,
      int y2,
      int px,
      int py,
      int tolerance) {
    TEMP_BOUNDS.setBounds(x1, y1, 0, 0);
    TEMP_BOUNDS.union(x2, y2);
    TEMP_BOUNDS.expand(tolerance, tolerance);
    //
    if (!TEMP_BOUNDS.contains(px, py)) {
      return false;
    }
    int result = 0;
    // calculates the length squared of the cross product of two vectors, v1 & v2.
    if (x1 != x2 && y1 != y2) {
      int v1x = x2 - x1;
      int v1y = y2 - y1;
      int v2x = px - x1;
      int v2y = py - y1;
      int numerator = v2x * v1y - v1x * v2y;
      int denominator = v1x * v1x + v1y * v1y;
      result = (int) ((long) numerator * numerator / denominator);
    }
    // if it is the same point, and it passes the bounding box test,
    // the result is always true.
    return result <= tolerance * tolerance;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * For this figure opaque is missing.
   */
  @Override
  public void setOpaque(boolean opaque) {
  }

  @Override
  protected void paintClientArea(Graphics graphics) {
    Rectangle bounds = super.getBounds();
    graphics.translate(-bounds.x, -bounds.y);
    graphics.setXORMode(m_xorMode);
    graphics.setLineStyle(m_lineStyle);
    graphics.setLineWidth(m_lineWidth);
    graphics.drawPolyline(m_points);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the line style.
   */
  public int getLineStyle() {
    return m_lineStyle;
  }

  /**
   * Sets the style of line.
   */
  public void setLineStyle(int lineStyle) {
    if (m_lineStyle != lineStyle) {
      m_lineStyle = lineStyle;
      repaint();
    }
  }

  /**
   * Returns the line width.
   */
  public int getLineWidth() {
    return m_lineWidth;
  }

  /**
   * Sets the line width.
   */
  public void setLineWidth(int lineWidth) {
    if (m_lineWidth != lineWidth) {
      m_lineWidth = lineWidth;
      m_pointsBounds = null;
      resetState();
    }
  }

  /**
   * Gets whether XOR based outline should be used for this {@link Polyline}.
   */
  public boolean isXorMode() {
    return m_xorMode;
  }

  /**
   * Sets whether XOR based outline should be used for this {@link Polyline}.
   */
  public void setXorMode(boolean xorMode) {
    if (m_xorMode != xorMode) {
      m_xorMode = xorMode;
      repaint();
    }
  }
}