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

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.support.PointSupport;

import java.lang.reflect.InvocationTargetException;

/**
 * Test for {@link PointSupport}.
 * 
 * @author lobas_av
 */
public class PointSupportTest extends AbstractSupportTest {
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
  public void test_getLabelClass() throws Exception {
    assertSame(
        m_lastLoader.loadClass("org.eclipse.swt.graphics.Point"),
        PointSupport.getPointClass());
  }

  public void test_newPoint() throws Exception {
    Object point = PointSupport.newPoint(1, 2);
    assertNotNull(point);
    assertSame(getPointClass(), point.getClass());
    assertEquals(1, ReflectionUtils.getFieldInt(point, "x"));
    assertEquals(2, ReflectionUtils.getFieldInt(point, "y"));
  }

  public void test_getPoint() throws Exception {
    Object point = createPoint(1, 2);
    Point testPoint = PointSupport.getPoint(point);
    assertNotNull(testPoint);
    assertEquals(1, testPoint.x);
    assertEquals(2, testPoint.y);
  }

  public void test_getPointCopy() throws Exception {
    Object point = createPoint(1, 2);
    Object pointCopy = PointSupport.getPointCopy(point);
    assertNotSame(point, pointCopy);
    assertEquals(point, pointCopy);
  }

  public void test_toString() throws Exception {
    Object point = createPoint(1, 2);
    assertEquals("(1, 2)", PointSupport.toString(point));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Object createPoint(int x, int y) throws InstantiationException, IllegalAccessException,
      InvocationTargetException, Exception {
    return ReflectionUtils.getConstructorBySignature(getPointClass(), "<init>(int,int)").newInstance(
        x,
        y);
  }

  private Class<?> getPointClass() throws ClassNotFoundException {
    return m_lastLoader.loadClass("org.eclipse.swt.graphics.Point");
  }
}