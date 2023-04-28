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
package org.eclipse.wb.internal.xwt.support;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.swt.support.ContainerSupport;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;

/**
 * Utilities for SWT {@link Control} coordinates.
 *
 * @author scheglov_ke
 * @coverage XWT.support
 */
public final class CoordinateUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // SWT -> draw2d
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the draw2d {@link Rectangle} for given SWT {@link org.eclipse.swt.graphics.Rectangle}.
   */
  public static Rectangle getRectangle(org.eclipse.swt.graphics.Rectangle r) {
    return new Rectangle(r.x, r.y, r.width, r.height);
  }

  /**
   * @return the draw2d {@link Point} for given SWT {@link org.eclipse.swt.graphics.Point}.
   */
  public static Point getPoint(org.eclipse.swt.graphics.Point point) {
    return new Point(point.x, point.y);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the draw2d result of {@link Control#getBounds()}.
   */
  public static Rectangle getBounds(Control control) throws Exception {
    org.eclipse.swt.graphics.Rectangle bounds = control.getBounds();
    return getRectangle(bounds);
  }

  /**
   * @return the draw2d result of {@link Control#toDisplay(int, int)}.
   */
  public static Point toDisplay(Control control, int x, int y) throws Exception {
    org.eclipse.swt.graphics.Point location = control.toDisplay(x, y);
    return getPoint(location);
  }

  /**
   * @return the draw2d result of <code>Control.computeSize(SWT.DEFAULT, SWT.DEFAULT)</code>.
   */
  public static Dimension getPreferredSize(Control control) throws Exception {
    org.eclipse.swt.graphics.Point size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    Point pointSize = getPoint(size);
    return new Dimension(pointSize.x, pointSize.y);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Client area
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the size of given {@link Composite} to set so that its client area will have given
   *         size.
   */
  public static Dimension computeTrimSize(Composite composite, int width, int height)
      throws Exception {
    org.eclipse.swt.graphics.Rectangle trim = composite.computeTrim(0, 0, width, height);
    return new Dimension(trim.width, trim.height);
  }

  /**
   * @return the draw2d result of {@link Composite#getClientArea()}.
   */
  public static Rectangle getClientArea(Scrollable composite) {
    org.eclipse.swt.graphics.Rectangle rectangle = composite.getClientArea();
    return getRectangle(rectangle);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Complex
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the location of given control in display coordinates.
   */
  public static Point getDisplayLocation(Control control) throws Exception {
    Rectangle bounds = getBounds(control);
    int x = bounds.x;
    int y = bounds.y;
    return getDisplayLocation(control, x, y);
  }

  /**
   * @return the given location (in parent of given <code>control</code>) in display coordinates.
   */
  public static Point getDisplayLocation(Control control, int x, int y) throws Exception {
    if (!(control instanceof Shell)) {
      Composite parent = control.getParent();
      if (parent != null) {
        Point location = toDisplay(parent, x, y);
        x = location.x;
        y = location.y;
      }
    }
    return new Point(x, y);
  }

  /**
   * @return {@link Insets} for given {@link Composite}. i.e. shift of client area from sides of
   *         bounds.
   */
  public static Insets getClientAreaInsets(Composite composite) throws Exception {
    // prepare top/left
    int top;
    int left;
    {
      Point displayLocation = getDisplayLocation(composite);
      Point clientAreaLocation = toDisplay(composite, 0, 0);
      top = Math.abs(clientAreaLocation.y - displayLocation.y);
      left = Math.abs(clientAreaLocation.x - displayLocation.x);
    }
    // if client area (0,0) is not shifted from top-left corner of bounds, then no insets at all
    if (top == 0 && left == 0) {
      return Insets.ZERO_INSETS;
    }
    // prepare bottom/right
    Rectangle bounds = getBounds(composite);
    Rectangle clientArea = getClientArea(composite);
    int bottom = bounds.height - top - clientArea.height;
    int right = bounds.width - left - clientArea.width;
    // final insets
    return new Insets(top, left, bottom, right);
  }

  /**
   * Returns the {@link Insets} that can be used to crop bounds of this {@link Composite} to produce
   * a rectangle which describes the area of this {@link Composite} which is capable of displaying
   * data (that is, not covered by the "trimmings").
   * <p>
   * Note, that this method is different from {@link #getClientAreaInsets()}. For example in
   * {@link Group} point <code>(0,0)</code> is point on group border, but
   * {@link Group#getClientArea()} returns size of border on sides. But still, if we <b>want</b> to
   * place child {@link Control} exactly in top-left point of {@link Group}, we should use
   * <code>(0,0)</code>. However if we want to place {@link Control} in <b>top-left of preferred
   * location</b>, then {@link #getClientAreaInsets2()} should be used.
   *
   * @return the {@link Insets} for "displaying data" part of given {@link Composite}.
   */
  public static Insets getClientAreaInsets2(Composite composite) throws Exception {
    // if client area (0,0) is shifted from top-left corner of bounds, then no need it additional insets
    {
      Insets trimInsets = getClientAreaInsets(composite);
      if (trimInsets.top != 0 || trimInsets.left != 0) {
        return Insets.ZERO_INSETS;
      }
    }
    // prepare bounds/clientArea
    Rectangle bounds = ControlSupport.getBounds(composite);
    Rectangle clientArea = ContainerSupport.getClientArea(composite);
    // prepare insets
    int top = clientArea.y;
    int left = clientArea.x;
    int bottom = bounds.height - top - clientArea.height;
    int right = bounds.width - left - clientArea.width;
    return new Insets(top, left, bottom, right);
  }
}