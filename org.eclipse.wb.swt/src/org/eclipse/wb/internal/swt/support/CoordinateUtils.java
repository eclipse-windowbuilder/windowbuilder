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
package org.eclipse.wb.internal.swt.support;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * Utilities for SWT widget coordinates.
 *
 * @author lobas_av
 * @coverage swt.support
 */
public final class CoordinateUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private CoordinateUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the location of given control in display coordinates.
   */
  public static Point getDisplayLocation(Object composite) throws Exception {
    Rectangle bounds = ControlSupport.getBounds(composite);
    int x = bounds.x;
    int y = bounds.y;
    return getDisplayLocation(composite, x, y);
  }

  /**
   * @return the given location (in parent of given <code>composite</code>) in display coordinates.
   */
  public static Point getDisplayLocation(Object composite, int x, int y) throws Exception {
    if (!ContainerSupport.isShell(composite)) {
      Object parent = ControlSupport.getParent(composite);
      if (parent != null) {
        Point location = ControlSupport.toDisplay(parent, x, y);
        x = location.x;
        y = location.y;
      }
    }
    return new Point(x, y);
  }

  /**
   * @return the bounds of <code>child</code> relative to <code>parent</code>.
   */
  public static Rectangle getBounds(Object parent, Object child) throws Exception {
    Rectangle bounds = ControlSupport.getBounds(child);
    Point childLocation = CoordinateUtils.getDisplayLocation(child);
    Point parentLocation = CoordinateUtils.getDisplayLocation(parent);
    bounds.x = childLocation.x - parentLocation.x;
    bounds.y = childLocation.y - parentLocation.y;
    return bounds;
  }

  /**
   * @return {@link Insets} for given composite. Practically we need here only shift of
   *         <code>(0,0)</code> point of client area relative to the top-left corner of bounds.
   */
  public static Insets getClientAreaInsets(Object composite) throws Exception {
    // prepare top/left
    int top;
    int left;
    {
      Point displayLocation = getDisplayLocation(composite);
      Point clientAreaLocation = ControlSupport.toDisplay(composite, 0, 0);
      // tweak for right-to-left
      {
        boolean isRTL = ContainerSupport.isRTL(composite);
        boolean isParentRTL = ContainerSupport.isRTL(ControlSupport.getParent(composite));
        if (isRTL && !isParentRTL) {
          Rectangle clientArea = ContainerSupport.getClientArea(composite);
          clientAreaLocation.x -= clientArea.width;
        }
      }
      //
      top = Math.abs(clientAreaLocation.y - displayLocation.y);
      left = Math.abs(clientAreaLocation.x - displayLocation.x);
    }
    // if client area (0,0) is not shifted from top-left corner of bounds, then no insets at all
    if (top == 0 && left == 0) {
      return Insets.ZERO_INSETS;
    }
    // prepare bottom/right
    Rectangle bounds = ControlSupport.getBounds(composite);
    Rectangle clientArea = ContainerSupport.getClientArea(composite);
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
  public static Insets getClientAreaInsets2(Object composite) throws Exception {
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