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

import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.lang.reflect.Constructor;

/**
 * Stub class for using SWT {@link org.eclipse.swt.widgets.Menu} in another {@link ClassLoader}.
 *
 * @author mitin_aa
 * @coverage swt.support
 */
public class MenuSupport extends AbstractSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Classes
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link org.eclipse.swt.widgets.Menu} {@link Class} loaded from active editor
   *         {@link ClassLoader}.
   */
  public static Class<?> getMenuClass() {
    return loadClass("org.eclipse.swt.widgets.Menu");
  }

  /**
   * @return <code>true</code> if given {@link Class} is successor of
   *         {@link org.eclipse.swt.widgets.Menu}.
   */
  public static boolean isMenuClass(Class<?> clazz) {
    return ReflectionUtils.isSuccessorOf(clazz, "org.eclipse.swt.widgets.Menu");
  }

  /**
   * @return {@link org.eclipse.swt.widgets.MenuItem} {@link Class} loaded from active editor
   *         {@link ClassLoader}.
   */
  public static Class<?> getMenuItemClass() {
    return loadClass("org.eclipse.swt.widgets.MenuItem");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given object is successor of {@link org.eclipse.swt.widgets.Menu}.
   */
  public static boolean isMenu(Object o) {
    return isMenuClass(o.getClass());
  }

  /**
   * @param object
   *          A menu item instance passed as object instance.
   * @return {@link org.eclipse.swt.widgets.Menu} instance for given menu item with SWT.CASCADE
   *         style set. It may return null if menu item is not cascaded or has no menu associated
   *         with it.
   */
  public static Object getMenu(Object object) throws Exception {
    return ReflectionUtils.invokeMethod(object, "getMenu()");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Items
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param object
   *          A menu instance passed as object instance.
   * @return child items of given {@link org.eclipse.swt.widgets.Menu} instance.
   */
  public static Object[] getItems(Object object) throws Exception {
    return (Object[]) ReflectionUtils.invokeMethod(object, "getItems()");
  }

  /**
   *
   * Adds a placeholder item with string "(add items here)" into empty menu.
   *
   * @param menuObject
   *          A menu object in which placeholder should be placed.
   */
  public static void addPlaceholder(Object menuObject) throws Exception {
    Constructor<?> menuItemCtor =
        ReflectionUtils.getConstructorBySignature(
            getMenuItemClass(),
            "<init>(org.eclipse.swt.widgets.Menu,int)");
    Object placeholderItem = menuItemCtor.newInstance(menuObject, 0);
    ReflectionUtils.invokeMethod(
        placeholderItem,
        "setText(java.lang.String)",
        IMenuInfo.NO_ITEMS_TEXT);
  }
}
