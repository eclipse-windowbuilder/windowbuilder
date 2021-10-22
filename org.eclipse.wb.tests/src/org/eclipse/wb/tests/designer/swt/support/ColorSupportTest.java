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
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;
import org.eclipse.wb.internal.swt.support.ColorSupport;

import org.eclipse.swt.graphics.Color;

/**
 * Test for {@link ColorSupport}.
 *
 * @author lobas_av
 */
public class ColorSupportTest extends AbstractSupportTest {
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
  public void test_getCopy() throws Exception {
    Object color = createColor(1, 2, 3);
    Object copyColor = ColorSupport.getCopy(color);
    try {
      // check create
      assertNotNull(copyColor);
      // check copy
      assertNotSame(color, copyColor);
      assertSame(color.getClass(), copyColor.getClass());
      // check RGB
      assertEquals(1, ReflectionUtils.invokeMethod(color, "getRed()"));
      assertEquals(2, ReflectionUtils.invokeMethod(color, "getGreen()"));
      assertEquals(3, ReflectionUtils.invokeMethod(color, "getBlue()"));
      // check state
      assertFalse((Boolean) ReflectionUtils.invokeMethod(copyColor, "isDisposed()"));
    } finally {
      ColorSupport.dispose(color);
      ColorSupport.dispose(copyColor);
    }
  }

  public void test_getColor() throws Exception {
    Object eSWTColor = createColor(1, 2, 3);
    Color SWTColor = ColorSupport.getColor(eSWTColor);
    try {
      // check create
      assertNotNull(SWTColor);
      // check RGB
      assertEquals(1, SWTColor.getRed());
      assertEquals(2, SWTColor.getGreen());
      assertEquals(3, SWTColor.getBlue());
      // check state
      assertFalse(SWTColor.isDisposed());
    } finally {
      ColorSupport.dispose(eSWTColor);
      ColorSupport.dispose(SWTColor);
    }
  }

  public void test_toString() throws Exception {
    Object color = createColor(1, 2, 3);
    try {
      assertEquals("1,2,3", ColorSupport.toString(color));
    } finally {
      ColorSupport.dispose(color);
    }
  }

  public void test_isDisposed() throws Exception {
    Object color = createColor(1, 2, 3);
    // initial state
    assertFalse(ColorSupport.isDisposed(color));
    // do dispose
    ReflectionUtils.invokeMethod(color, "dispose()");
    assertTrue(ColorSupport.isDisposed(color));
  }

  public void test_dispose() throws Exception {
    Object color = createColor(1, 2, 3);
    // check state
    assertFalse((Boolean) ReflectionUtils.invokeMethod(color, "isDisposed()"));
    // dispose
    ColorSupport.dispose(color);
    // check new state
    assertTrue((Boolean) ReflectionUtils.invokeMethod(color, "isDisposed()"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ColorInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_createInfo_color() throws Exception {
    Object color = createColor(1, 2, 3);
    try {
      ColorInfo info = ColorSupport.createInfo(color);
      // check create
      assertNotNull(info);
      // check color name
      assertNull(info.m_name);
      // check special data
      assertNull(info.getData());
      // check RGB
      assertNotNull(info.m_rgb);
      assertEquals(1, info.m_rgb.red);
      assertEquals(2, info.m_rgb.green);
      assertEquals(3, info.m_rgb.blue);
    } finally {
      ColorSupport.dispose(color);
    }
  }

  public void test_createInfo_field() throws Exception {
    Class<?> SWTClass = m_lastLoader.loadClass("org.eclipse.swt.SWT");
    ColorInfo info = ColorSupport.createInfo(ReflectionUtils.getFieldByName(SWTClass, "COLOR_RED"));
    // check create
    assertNotNull(info);
    // check color name
    assertEquals("COLOR_RED", info.m_name);
    // check special data
    assertEquals("org.eclipse.swt.SWT.COLOR_RED", info.getData());
    // check RGB
    assertNotNull(info.m_rgb);
    assertEquals(255, info.m_rgb.red);
    assertEquals(0, info.m_rgb.green);
    assertEquals(0, info.m_rgb.blue);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Object createColor(int red, int green, int blue) throws Exception {
    Class<?> ColorClass = m_lastLoader.loadClass("org.eclipse.swt.graphics.Color");
    return ReflectionUtils.getConstructorBySignature(
        ColorClass,
        "<init>(org.eclipse.swt.graphics.Device,int,int,int)").newInstance(null, red, green, blue);
  }
}