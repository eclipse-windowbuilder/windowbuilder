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

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Stub class for using SWT {@link org.eclipse.swt.widgets.Control} in another {@link ClassLoader}.
 *
 * @author lobas_av
 * @coverage swt.support
 */
public class ControlSupport extends AbstractSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Classes
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link org.eclipse.swt.widgets.Widget} {@link Class} loaded from active editor
   *         {@link ClassLoader}.
   */
  public static Class<?> getWidgetClass() {
    return loadClass("org.eclipse.swt.widgets.Widget");
  }

  /**
   * @return {@link org.eclipse.swt.widgets.Control} {@link Class} loaded from active editor
   *         {@link ClassLoader}.
   */
  public static Class<?> getControlClass() {
    return loadClass("org.eclipse.swt.widgets.Control");
  }

  /**
   * @return <code>true</code> if given {@link Class} is successor of
   *         {@link org.eclipse.swt.widgets.Control}.
   */
  public static boolean isControlClass(Class<?> clazz) {
    return ReflectionUtils.isSuccessorOf(clazz, "org.eclipse.swt.widgets.Control");
  }

  /**
   * @return <code>true</code> if given {@link Object} is {@link org.eclipse.swt.widgets.Control}.
   */
  public static boolean isControl(Object o) {
    if (o != null) {
      return isControlClass(o.getClass());
    } else {
      return false;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Invoke method <code>int Widget.getStyle()</code> for widget.
   */
  public static int getStyle(final Object widget) {
    return (Integer) ReflectionUtils.invokeMethodEx(widget, "getStyle()");
  }

  /**
   * @return <code>true</code> if given widget contains given style.
   */
  public static boolean isStyle(Object widget, int style) {
    return (getStyle(widget) & style) != 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Invoke method <code>Rectangle Control.getBounds()</code> for control.
   */
  public static Rectangle getBounds(Object control) throws Exception {
    Object bounds = ReflectionUtils.invokeMethod(control, "getBounds()");
    return RectangleSupport.getRectangle(bounds);
  }

  /**
   * Invoke method <code>Point Control.toDisplay(int, int)</code> for control.
   */
  public static Point toDisplay(Object control, int x, int y) throws Exception {
    Object location = ReflectionUtils.invokeMethod(control, "toDisplay(int,int)", x, y);
    return PointSupport.getPoint(location);
  }

  /**
   * Invoke method <code>Control.setSize(int, int)</code> for control.
   */
  public static void setSize(Object control, int width, int height) throws Exception {
    ReflectionUtils.invokeMethod(control, "setSize(int,int)", width, height);
  }

  /**
   * Invoke method <code>Control.setLocation(int, int)</code> for control.
   */
  public static void setLocation(Object control, int x, int y) throws Exception {
    ReflectionUtils.invokeMethod(control, "setLocation(int,int)", x, y);
  }

  /**
   * Invoke method <code>Control.computeSize(SWT.DEFAULT, SWT.DEFAULT)</code> for control.
   */
  public static Object computeSize_DEFAULT(Object control) throws Exception {
    return ReflectionUtils.invokeMethod(
        control,
        "computeSize(int,int)",
        SwtSupport.DEFAULT,
        SwtSupport.DEFAULT);
  }

  /**
   * Invoke method <code>Control.computeSize(SWT.DEFAULT, SWT.DEFAULT)</code> for control.
   */
  public static Dimension getPreferredSize(Object control) throws Exception {
    Object size =
        ReflectionUtils.invokeMethod(
            control,
            "computeSize(int,int)",
            SwtSupport.DEFAULT,
            SwtSupport.DEFAULT);
    Point pointSize = PointSupport.getPoint(size);
    return new Dimension(pointSize.x, pointSize.y);
  }

  /**
   * Invoke method <code>Control.getBorderWidth()</code> for given control.
   */
  public static int getBorderWidth(Object control) {
    return (Integer) ReflectionUtils.invokeMethodEx(control, "getBorderWidth()");
  }

  /**
   * Invoke method <code>Control.pack()</code> for control.
   */
  public static void pack(Object control) throws Exception {
    ReflectionUtils.invokeMethod(control, "pack()");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutData
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Invoke method <code>Control.getLayoutData()</code> for control.
   */
  public static Object getLayoutData(Object control) {
    return ReflectionUtils.invokeMethodEx(control, "getLayoutData()");
  }

  /**
   * Invoke method <code>Control.setLayoutData()</code> for control.
   */
  public static void setLayoutData(Object control, Object layoutData) {
    ReflectionUtils.invokeMethodEx(control, "setLayoutData(java.lang.Object)", layoutData);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Data
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Invoke method <code>Control.getData(String)</code> for control.
   */
  public static Object getData(Object control, String key) throws Exception {
    return ReflectionUtils.invokeMethod(control, "getData(java.lang.String)", key);
  }

  /**
   * Invoke method <code>Control.setData(String, Object)</code> for control.
   */
  public static void setData(Object control, String key, Object value) throws Exception {
    ReflectionUtils.invokeMethod(control, "setData(java.lang.String,java.lang.Object)", key, value);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parent
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Invoke method <code>Control Control.getParent()</code> for control.
   */
  public static Object getParent(Object control) {
    return ReflectionUtils.invokeMethodEx(control, "getParent()");
  }

  /**
   * Invoke method <code>Control Control.getShell()</code> for control.
   */
  public static Object getShell(Object control) {
    return ReflectionUtils.invokeMethodEx(control, "getShell()");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dispose
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Invoke method <code>Widget.dispose()</code> for widget if it not disposed.
   */
  public static void dispose(Object widget) {
    if (widget == null) {
      return;
    }
    if (!isDisposed(widget)) {
      ReflectionUtils.invokeMethodEx(widget, "dispose()");
    }
  }

  /**
   * @return <code>true</code> if given <code>Widget</code> is disposed.
   */
  public static boolean isDisposed(Object widget) {
    return (Boolean) ReflectionUtils.invokeMethodEx(widget, "isDisposed()");
  }
}