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
package org.eclipse.wb.tests.designer.swt.support;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.support.RectangleSupport;

import org.eclipse.draw2d.geometry.Rectangle;

import java.lang.reflect.InvocationTargetException;

/**
 * Test for {@link RectangleSupport}.
 *
 * @author lobas_av
 */
public class RectangleSupportTest extends AbstractSupportTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_newPoint() throws Exception {
    Object rectangle = RectangleSupport.newRectangle(1, 2, 3, 4);
    assertNotNull(rectangle);
    assertSame(getRectangleClass(), rectangle.getClass());
    assertEquals(1, ReflectionUtils.getFieldInt(rectangle, "x"));
    assertEquals(2, ReflectionUtils.getFieldInt(rectangle, "y"));
    assertEquals(3, ReflectionUtils.getFieldInt(rectangle, "width"));
    assertEquals(4, ReflectionUtils.getFieldInt(rectangle, "height"));
  }

  public void test_getPoint() throws Exception {
    Object rectangle = createRectangle(1, 2, 3, 4);
    Rectangle testRectangle = RectangleSupport.getRectangle(rectangle);
    assertNotNull(testRectangle);
    assertEquals(1, testRectangle.x);
    assertEquals(2, testRectangle.y);
    assertEquals(3, testRectangle.width);
    assertEquals(4, testRectangle.height);
  }

  public void test_toString() throws Exception {
    Object rectangle = createRectangle(1, 2, 3, 4);
    assertEquals("(1, 2, 3, 4)", RectangleSupport.toString(rectangle));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Object createRectangle(int x, int y, int width, int height)
      throws InstantiationException, IllegalAccessException, InvocationTargetException, Exception {
    return ReflectionUtils.getConstructorBySignature(getRectangleClass(), "<init>(int,int,int,int)").newInstance(
        x,
        y,
        width,
        height);
  }

  private Class<?> getRectangleClass() throws ClassNotFoundException {
    return m_lastLoader.loadClass("org.eclipse.swt.graphics.Rectangle");
  }
}