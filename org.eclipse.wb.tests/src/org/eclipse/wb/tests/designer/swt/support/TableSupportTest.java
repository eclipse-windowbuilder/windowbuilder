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
import org.eclipse.wb.internal.swt.support.TableSupport;

/**
 * Tests for {@link TableSupport}.
 *
 * @author lobas_av
 */
public class TableSupportTest extends AbstractSupportTest {
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
        "    Table table = new Table(this, SWT.BORDER);",
        "    TableColumn column1 = new TableColumn(table, SWT.NONE);",
        "    column1.setText(\"Column1\");",
        "    column1.setWidth(100);",
        "    TableColumn column2 = new TableColumn(table, SWT.NONE);",
        "    column2.setText(\"Column2\");",
        "    column2.setWidth(200);",
        "    TableItem item = new TableItem(table, SWT.NONE);",
        "    item.setText(0, \"item00\");",
        "    item.setText(1, \"item11\");",
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
  public void test_classes() throws Exception {
    assertSame(getTableClass(), TableSupport.getTableClass());
    assertSame(getTableItemClass(), TableSupport.getTableItemClass());
    assertSame(getTableColumnClass(), TableSupport.getTableColumnClass());
  }

  public void test_getHeaderHeight() throws Exception {
    Object table = getTable();
    int headerHeight = (Integer) ReflectionUtils.invokeMethod(table, "getHeaderHeight()");
    assertSame(headerHeight, TableSupport.getHeaderHeight(table));
  }

  public void test_getItemHeight() throws Exception {
    Object table = getTable();
    int itemHeight = (Integer) ReflectionUtils.invokeMethod(table, "getItemHeight()");
    assertSame(itemHeight, TableSupport.getItemHeight(table));
  }

  public void test_column() throws Exception {
    Object[] columns = TableSupport.getColumns(getTable());
    assertNotNull(columns);
    assertEquals(2, columns.length);
    assertNotNull(columns[0]);
    assertNotNull(columns[1]);
    assertSame(getTableColumnClass(), columns[0].getClass());
    assertSame(getTableColumnClass(), columns[1].getClass());
    assertEquals(
        ReflectionUtils.invokeMethod(columns[0], "getWidth()"),
        TableSupport.getColumnWidth(columns[0]));
    assertEquals(
        ReflectionUtils.invokeMethod(columns[1], "getWidth()"),
        TableSupport.getColumnWidth(columns[1]));
    assertEquals("Column1", ReflectionUtils.invokeMethod(columns[0], "getText()"));
    assertEquals("Column2", ReflectionUtils.invokeMethod(columns[1], "getText()"));
  }

  public void test_items() throws Exception {
    Object[] items = TableSupport.getItems(getTable());
    assertNotNull(items);
    assertEquals(1, items.length);
    assertNotNull(items[0]);
    assertSame(getTableItemClass(), items[0].getClass());
    assertEquals("item00", ReflectionUtils.invokeMethod(items[0], "getText(int)", 0));
    assertEquals("item11", ReflectionUtils.invokeMethod(items[0], "getText(int)", 1));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Class<?> getTableClass() throws Exception {
    return m_lastLoader.loadClass("org.eclipse.swt.widgets.Table");
  }

  private Class<?> getTableItemClass() throws Exception {
    return m_lastLoader.loadClass("org.eclipse.swt.widgets.TableItem");
  }

  private Class<?> getTableColumnClass() throws Exception {
    return m_lastLoader.loadClass("org.eclipse.swt.widgets.TableColumn");
  }

  private Object getTable() {
    return m_shell.getChildrenControls().get(0).getObject();
  }
}