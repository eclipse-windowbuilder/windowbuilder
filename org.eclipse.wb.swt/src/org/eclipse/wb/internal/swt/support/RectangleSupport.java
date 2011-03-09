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

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.lang.reflect.Constructor;

/**
 * Stub class for using SWT {@link org.eclipse.swt.graphics.Rectangle} in another
 * {@link ClassLoader}.
 * 
 * @author lobas_av
 * @coverage swt.support
 */
public class RectangleSupport extends AbstractSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Rectangle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create new rectangle, invoke <code>new Rectangle(x, y, width, height)</code>
   */
  public static Object newRectangle(int x, int y, int width, int height) throws Exception {
    Constructor<?> constructor =
        ReflectionUtils.getConstructorBySignature(
            loadClass("org.eclipse.swt.graphics.Rectangle"),
            "<init>(int,int,int,int)");
    return constructor.newInstance(x, y, width, height);
  }

  /**
   * Convert rectangle.
   */
  public static Rectangle getRectangle(Object rectangle) throws Exception {
    return new Rectangle(getX(rectangle),
        getY(rectangle),
        getWidth(rectangle),
        getHeight(rectangle));
  }

  /**
   * Create string presentation of rectangle: <code>(x, y, width, height)</code>.
   */
  public static String toString(Object rectangle) throws Exception {
    return "("
        + getX(rectangle)
        + ", "
        + getY(rectangle)
        + ", "
        + getWidth(rectangle)
        + ", "
        + getHeight(rectangle)
        + ")";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>x</code> field for SWT {@link org.eclipse.swt.graphics.Rectangle} object.
   */
  private static int getX(Object rectangle) throws Exception {
    return ReflectionUtils.getFieldInt(rectangle, "x");
  }

  /**
   * @return <code>y</code> field for SWT {@link org.eclipse.swt.graphics.Rectangle} object.
   */
  private static int getY(Object rectangle) throws Exception {
    return ReflectionUtils.getFieldInt(rectangle, "y");
  }

  /**
   * @return <code>width</code> field for SWT {@link org.eclipse.swt.graphics.Rectangle} object.
   */
  private static int getWidth(Object rectangle) throws Exception {
    return ReflectionUtils.getFieldInt(rectangle, "width");
  }

  /**
   * @return <code>height</code> field for SWT {@link org.eclipse.swt.graphics.Rectangle} object.
   */
  private static int getHeight(Object rectangle) throws Exception {
    return ReflectionUtils.getFieldInt(rectangle, "height");
  }
}