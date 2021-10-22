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

import org.eclipse.swt.widgets.Table;

/**
 * Stub class for using SWT {@link org.eclipse.swt.widget.Table}'s in another {@link ClassLoader}.
 *
 * @author lobas_av
 * @author mitin_aa
 * @coverage swt.support
 */
public class TableSupport extends AbstractSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Class
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link org.eclipse.swt.widgets.Table} {@link Class} loaded from active editor
   *         {@link ClassLoader}.
   */
  public static Class<?> getTableClass() {
    return loadClass("org.eclipse.swt.widgets.Table");
  }

  /**
   * @return {@link org.eclipse.swt.widgets.TableColumn} {@link Class} loaded from active editor
   *         {@link ClassLoader}.
   */
  public static Class<?> getTableColumnClass() {
    return loadClass("org.eclipse.swt.widgets.TableColumn");
  }

  /**
   * @return {@link org.eclipse.swt.widgets.TableItem} {@link Class} loaded from active editor
   *         {@link ClassLoader}.
   */
  public static Class<?> getTableItemClass() {
    return loadClass("org.eclipse.swt.widgets.TableItem");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Table
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Invoke method <code>Table.getHeaderHeight()</code> for given table.
   */
  public static int getHeaderHeight(Object table) throws Exception {
    return (Integer) ReflectionUtils.invokeMethod(table, "getHeaderHeight()");
  }

  /**
   * Invoke method <code>Table.getItemHeight()</code> for given table.
   */
  public static int getItemHeight(Object table) throws Exception {
    return (Integer) ReflectionUtils.invokeMethod(table, "getItemHeight()");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TableColumn
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Invoke method <code>TableColumn.getWidth()</code> for given table column.
   */
  public static int getColumnWidth(Object column) throws Exception {
    return (Integer) ReflectionUtils.invokeMethod(column, "getWidth()");
  }

  /**
   * @return child columns of given {@link Table} instance.
   */
  public static Object[] getColumns(Object object) throws Exception {
    return (Object[]) ReflectionUtils.invokeMethod(object, "getColumns()");
  }

  /**
   * @return child items of given {@link Table} instance.
   */
  public static Object[] getItems(Object object) throws Exception {
    return (Object[]) ReflectionUtils.invokeMethod(object, "getItems()");
  }
}