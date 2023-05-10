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

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.draw2d.geometry.Point;

import java.lang.reflect.Constructor;

/**
 * Stub class for using SWT {@link org.eclipse.swt.graphics.Point} in another {@link ClassLoader}.
 *
 * @author lobas_av
 * @coverage swt.support
 */
public class PointSupport extends AbstractSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Point
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link org.eclipse.swt.graphics.Point} class loaded from active editor
   *         {@link ClassLoader}.
   */
  public static Class<?> getPointClass() {
    return loadClass("org.eclipse.swt.graphics.Point");
  }

  /**
   * Create object, invoke <code>new Point(x, y)</code>.
   */
  public static Object newPoint(int x, int y) throws Exception {
    Constructor<?> constructor =
        ReflectionUtils.getConstructorBySignature(getPointClass(), "<init>(int,int)");
    return constructor.newInstance(x, y);
  }

  /**
   * Converts toolkit <code>Point</code> into draw2d {@link Point}.
   */
  public static Point getPoint(Object point) {
    return new Point(getX(point), getY(point));
  }

  /**
   * @return the copy of toolkit <code>Point</code>.
   */
  public static Object getPointCopy(Object point) throws Exception {
    return newPoint(getX(point), getY(point));
  }

  /**
   * Create string presentation of point: <code>(x, y)</code>.
   */
  public static String toString(Object point) throws Exception {
    return "(" + getX(point) + ", " + getY(point) + ")";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>x</code> field for SWT {@link org.eclipse.swt.graphics.Point} object.
   */
  private static int getX(Object point) {
    return ReflectionUtils.getFieldInt(point, "x");
  }

  /**
   * @return <code>y</code> field for SWT {@link org.eclipse.swt.graphics.Point} object.
   */
  private static int getY(Object point) {
    return ReflectionUtils.getFieldInt(point, "y");
  }
}