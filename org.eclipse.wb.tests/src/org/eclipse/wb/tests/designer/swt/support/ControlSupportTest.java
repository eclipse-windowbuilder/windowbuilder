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
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.support.ContainerSupport;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

/**
 * Tests for {@link ControlSupport}.
 *
 * @author lobas_av
 */
public class ControlSupportTest extends AbstractSupportTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String[] getTestSource() {
    return new String[]{
        "public class Test extends Shell {",
        "  public Test() {",
        "    Button button = new Button(this, SWT.CHECK);",
        "    button.setBounds(10, 20, 50, 30);",
        "  }",
        "}"};
  }

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
  public void test_getWidgetClass() throws Exception {
    Class<?> WidgetClass = m_lastLoader.loadClass("org.eclipse.swt.widgets.Widget");
    assertSame(WidgetClass, ControlSupport.getWidgetClass());
  }

  public void test_getControlClass() throws Exception {
    Class<?> ControlClass = m_lastLoader.loadClass("org.eclipse.swt.widgets.Control");
    assertSame(ControlClass, ControlSupport.getControlClass());
  }

  /**
   * Test for {@link ControlSupport#isControlClass(Class)}.
   */
  public void test_isControlClass() throws Exception {
    // Object is not Control
    assertFalse(ControlSupport.isControlClass(Object.class));
    // Control is Control ;-)
    assertTrue(ControlSupport.isControlClass(ControlSupport.getControlClass()));
    // Shell is Control
    assertTrue(ControlSupport.isControlClass(ContainerSupport.getShellClass()));
  }

  public void test_isControl() throws Exception {
    // "null" is not Control
    assertFalse(ControlSupport.isControl(null));
    // Object is not Control
    assertFalse(ControlSupport.isControl(new Object()));
    // Shell is Control
    Object shell = ContainerSupport.createShell();
    try {
      assertTrue(ControlSupport.isControl(shell));
    } finally {
      ControlSupport.dispose(shell);
    }
  }

  public void test_getStyle() throws Exception {
    Object button = getButton();
    assertEquals(
        ReflectionUtils.invokeMethod(button, "getStyle()"),
        ControlSupport.getStyle(button));
  }

  public void test_isStyle() throws Exception {
    Object button = getButton();
    assertTrue(ControlSupport.isStyle(button, SWT.CHECK));
    assertFalse(ControlSupport.isStyle(button, SWT.RADIO));
  }

  public void test_getBounds() throws Exception {
    Object button = getButton();
    Rectangle bounds = ControlSupport.getBounds(button);
    assertEquals(new Rectangle(10, 20, 50, 30), bounds);
  }

  public void test_toDisplay() throws Exception {
    Object button = getButton();
    Object toDisplayPoint = ReflectionUtils.invokeMethod(button, "toDisplay(int,int)", 5, 5);
    Point toDisplayPointTest = ControlSupport.toDisplay(button, 5, 5);
    assertNotNull(toDisplayPointTest);
    assertEquals(ReflectionUtils.getFieldInt(toDisplayPoint, "x"), toDisplayPointTest.x);
    assertEquals(ReflectionUtils.getFieldInt(toDisplayPoint, "y"), toDisplayPointTest.y);
  }

  public void test_setSize() throws Exception {
    Object button = getButton();
    ControlSupport.setSize(button, 55, 25);
    Object size = ReflectionUtils.invokeMethod(button, "getSize()");
    assertEquals(55, ReflectionUtils.getFieldInt(size, "x"));
    assertEquals(25, ReflectionUtils.getFieldInt(size, "y"));
  }

  public void test_setLocation() throws Exception {
    Object button = getButton();
    ControlSupport.setLocation(button, 20, 10);
    Object location = ReflectionUtils.invokeMethod(button, "getLocation()");
    assertEquals(20, ReflectionUtils.getFieldInt(location, "x"));
    assertEquals(10, ReflectionUtils.getFieldInt(location, "y"));
  }

  public void test_getPreferredSize() throws Exception {
    Object button = getButton();
    Object preferredSize = ControlSupport.computeSize_DEFAULT(button);
    Dimension preferredSizeTest = ControlSupport.getPreferredSize(button);
    assertNotNull(preferredSizeTest);
    assertEquals(ReflectionUtils.getFieldInt(preferredSize, "x"), preferredSizeTest.width);
    assertEquals(ReflectionUtils.getFieldInt(preferredSize, "y"), preferredSizeTest.height);
  }

  public void test_pack() throws Exception {
    Object shell = m_shell.getObject();
    ReflectionUtils.invokeMethod(shell, "setSize(int,int)", 300, 200);
    ReflectionUtils.invokeMethod(shell, "pack()");
    Object size = ReflectionUtils.invokeMethod(shell, "getSize()");
    ReflectionUtils.invokeMethod(shell, "setSize(int,int)", 300, 200);
    ControlSupport.pack(shell);
    Object testSize = ReflectionUtils.invokeMethod(shell, "getSize()");
    assertEquals(ReflectionUtils.getFieldInt(size, "x"), ReflectionUtils.getFieldInt(size, "x"));
    assertEquals(
        ReflectionUtils.getFieldInt(testSize, "y"),
        ReflectionUtils.getFieldInt(testSize, "y"));
  }

  public void test_data() throws Exception {
    Object button = getButton();
    assertNull(ControlSupport.getData(button, "key"));
    ControlSupport.setData(button, "key", this);
    assertSame(this, ControlSupport.getData(button, "key"));
    ControlSupport.setData(button, "key", null);
    assertNull(ControlSupport.getData(button, "key"));
  }

  public void test_getParent() throws Exception {
    Object shell = m_shell.getObject();
    Object button = getButton();
    assertSame(shell, ControlSupport.getParent(button));
    assertNull(ControlSupport.getParent(shell));
  }

  public void test_getShell() throws Exception {
    Object shell = m_shell.getObject();
    Object button = getButton();
    assertSame(shell, ControlSupport.getShell(button));
  }

  public void test_dispose_ignoreNull() throws Exception {
    ControlSupport.dispose(null);
  }

  public void test_dispose() throws Exception {
    Object button = getButton();
    // initially not disposed
    assertFalse(ControlSupport.isDisposed(button));
    // do dispose
    ControlSupport.dispose(button);
    assertTrue(ControlSupport.isDisposed(button));
    // second dispose() ignored
    ControlSupport.dispose(button);
  }

  /**
   * Test for {@link ControlSupport#getLayoutData(Object)}
   */
  public void test_getLayoutData() throws Exception {
    disposeLastModel();
    CompositeInfo shellInfo =
        (CompositeInfo) parseSource(
            "test",
            "Test2.java",
            getTestSource(
                "public class Test2 extends Shell {",
                "  public Test2() {",
                "    setLayout(new RowLayout());",
                "    Button button = new Button(this, SWT.NONE);",
                "    button.setLayoutData(new RowData(100, 50));",
                "  }",
                "}"));
    shellInfo.refresh();
    ControlInfo buttonInfo = shellInfo.getChildrenControls().get(0);
    Object button = buttonInfo.getObject();
    // call getLayoutData()
    Object layoutData = ControlSupport.getLayoutData(button);
    assertNotNull(layoutData);
    assertEquals("org.eclipse.swt.layout.RowData", layoutData.getClass().getName());
  }

  private Object getButton() {
    return m_shell.getChildrenControls().get(0).getObject();
  }
}