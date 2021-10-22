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

import org.eclipse.swt.widgets.Display;

/**
 * Stub class for using SWT {@link Display} in another {@link ClassLoader}.
 *
 * @author lobas_av
 * @coverage swt.support
 */
public class DisplaySupport extends AbstractSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Display
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link org.eclipse.swt.widgets.Display} {@link Class} loaded from active editor
   *         {@link ClassLoader}.
   */
  public static Class<?> getDisplayClass() {
    return loadClass("org.eclipse.swt.widgets.Display");
  }

  /**
   * Invoke method <code>Display.getDefault()</code>.
   */
  public static Object getDefault() throws Exception {
    return ReflectionUtils.invokeMethod(getDisplayClass(), "getDefault()");
  }

  /**
   * Invoke method <code>Display.getCurrent()</code>.
   */
  public static Object getCurrent() throws Exception {
    return ReflectionUtils.invokeMethod(getDisplayClass(), "getCurrent()");
  }

  /**
   * Invoke method <code>Display.getSystemColor()</code> for given color.
   */
  public static Object getSystemColor(Object id) throws Exception {
    return ReflectionUtils.invokeMethod(getCurrent(), "getSystemColor(int)", id);
  }

  /**
   * Invoke method <code>Display.getSystemFont()</code> for default display.
   */
  public static Object getSystemFont() throws Exception {
    return ReflectionUtils.invokeMethod(getDefault(), "getSystemFont()");
  }
}