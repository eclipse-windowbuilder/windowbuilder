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
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.ExposedPropertyCreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.ExposedPropertyVariableSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;
import org.eclipse.wb.internal.swt.model.widgets.TableColumnInfo;
import org.eclipse.wb.internal.swt.model.widgets.TableInfo;
import org.eclipse.wb.internal.swt.model.widgets.TableItemInfo;
import org.eclipse.wb.internal.swt.model.widgets.WidgetInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import java.util.List;

/**
 * Tests for {@link TableInfo}, {@link TableColumnInfo} and {@link TableItemInfo}.
 *
 * @author lobas_av
 * @author scheglov_ke
 * @author mitin_aa
 */
public class TableTest extends RcpModelTest {
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
  // Items
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for parsing {@link TableItem} and bounds of {@link TableItemInfo}.
   */
  public void test_TableItem() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    Table table = new Table(this, SWT.BORDER);",
            "    {",
            "      TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
            "      tableColumn.setWidth(100);",
            "    }",
            "    TableItem item_1 = new TableItem(table, SWT.NONE);",
            "    TableItem item_2 = new TableItem(table, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    // prepare items
    List<TableItemInfo> items = table.getItems();
    assertEquals(2, items.size());
    TableItemInfo item_1 = items.get(0);
    TableItemInfo item_2 = items.get(1);
    // check bounds
    Insets tableInsets = table.getClientAreaInsets();
    {
      // "model" bounds
      Rectangle modelBounds = item_1.getModelBounds();
      assertNotNull(modelBounds);
      assertEquals(0, modelBounds.x);
      assertEquals(0, modelBounds.y);
      assertEquals(table.getClientArea().width, modelBounds.width);
      // "shot" bounds
      Rectangle bounds = item_1.getBounds();
      assertEquals(tableInsets.left, bounds.x);
      assertEquals(tableInsets.top, bounds.y);
      assertEquals(modelBounds.width, bounds.width);
      assertEquals(modelBounds.height, bounds.height);
    }
    {
      Rectangle modelBounds = item_2.getModelBounds();
      assertEquals(0, modelBounds.x);
      assertEquals(item_1.getModelBounds().bottom(), modelBounds.y);
    }
  }

  /**
   * If no {@link TableColumn}, then {@link TabItem} should have width of {@link Table}.
   */
  public void test_TableItem_whenNoColumns() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    Table table = new Table(this, SWT.BORDER);",
            "    TableItem item = new TableItem(table, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    TableInfo table = getJavaInfoByName("table");
    TableItemInfo item = getJavaInfoByName("item");
    // check bounds
    Insets tableInsets = table.getClientAreaInsets();
    {
      // "model" bounds
      Rectangle modelBounds = item.getModelBounds();
      assertNotNull(modelBounds);
      assertEquals(0, modelBounds.x);
      assertEquals(0, modelBounds.y);
      assertEquals(table.getClientArea().width, modelBounds.width);
      // "shot" bounds
      Rectangle bounds = item.getBounds();
      assertEquals(tableInsets.left, bounds.x);
      assertEquals(tableInsets.top, bounds.y);
      assertEquals(modelBounds.width, bounds.width);
      assertEquals(modelBounds.height, bounds.height);
    }
  }

  public void test_add_TableItem() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    Table table = new Table(this, SWT.BORDER);",
            "  }",
            "}");
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    // no items initially
    assertTrue(table.getItems().isEmpty());
    // add new TableItem
    TableItemInfo newItem = createJavaInfo("org.eclipse.swt.widgets.TableItem");
    flowContainer_CREATE(table, newItem, null);
    // check result
    List<TableItemInfo> items = table.getItems();
    assertEquals(1, items.size());
    assertTrue(items.contains(newItem));
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    Table table = new Table(this, SWT.BORDER);",
        "    {",
        "      TableItem tableItem = new TableItem(table, SWT.NONE);",
        "      tableItem.setText('New TableItem');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Columns
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for parsing {@link TableColumn} and bounds of {@link TableColumnInfo}.
   */
  public void test_TableColumn() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    Table table = new Table(this, SWT.BORDER);",
            "    table.setHeaderVisible(true);",
            "    {",
            "      TableColumn tableColumn_1 = new TableColumn(table, SWT.NONE);",
            "      tableColumn_1.setWidth(50);",
            "    }",
            "    {",
            "      TableColumn tableColumn_2 = new TableColumn(table, SWT.NONE);",
            "      tableColumn_2.setWidth(100);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    // prepare columns
    List<TableColumnInfo> columns = table.getColumns();
    assertEquals(2, columns.size());
    TableColumnInfo column_1 = columns.get(0);
    TableColumnInfo column_2 = columns.get(1);
    // check bounds
    Insets tableInsets = table.getClientAreaInsets();
    {
      // "model" bounds
      Rectangle modelBounds = column_1.getModelBounds();
      assertNotNull(modelBounds);
      assertEquals(0, modelBounds.x);
      assertEquals(0, modelBounds.y);
      assertEquals(50, modelBounds.width);
      assertTrue(modelBounds.height > 15 && modelBounds.height < 50);
      // "shot" bounds
      Rectangle bounds = column_1.getBounds();
      assertEquals(tableInsets.left, bounds.x);
      assertEquals(tableInsets.top, bounds.y);
      assertEquals(modelBounds.width, bounds.width);
      assertEquals(modelBounds.height, bounds.height);
    }
    {
      // "model" bounds
      Rectangle modelBounds = column_2.getModelBounds();
      assertNotNull(modelBounds);
      assertEquals(50, modelBounds.x);
      assertEquals(0, modelBounds.y);
    }
  }

  public void test_TableColumn_setWidth() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Table table = new Table(this, SWT.BORDER);",
            "    {",
            "      TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
            "      tableColumn.setWidth(100);",
            "    }",
            "  }",
            "}");
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    //
    TableColumnInfo column = table.getColumns().get(0);
    Property widthProperty = column.getPropertyByTitle("width");
    // check initial width
    assertEquals(100, widthProperty.getValue());
    // set new width
    column.setWidth(120);
    assertEquals(120, widthProperty.getValue());
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    Table table = new Table(this, SWT.BORDER);",
        "    {",
        "      TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
        "      tableColumn.setWidth(120);",
        "    }",
        "  }",
        "}");
  }

  public void test_add_TableColumn() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Table table = new Table(this, SWT.BORDER);",
            "  }",
            "}");
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    // no columns initially
    assertTrue(table.getColumns().isEmpty());
    // add new TableColumn
    TableColumnInfo newColumn = createJavaInfo("org.eclipse.swt.widgets.TableColumn");
    flowContainer_CREATE(table, newColumn, null);
    // check result
    List<TableColumnInfo> columns = table.getColumns();
    assertEquals(1, columns.size());
    assertTrue(columns.contains(newColumn));
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    Table table = new Table(this, SWT.BORDER);",
        "    {",
        "      TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
        "      tableColumn.setWidth(100);",
        "      tableColumn.setText('New Column');",
        "    }",
        "  }",
        "}");
  }

  public void test_move_TableColumn() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Table table = new Table(this, SWT.BORDER);",
            "    {",
            "      TableColumn tableColumn_1 = new TableColumn(table, SWT.NONE);",
            "      tableColumn_1.setText('Column 1');",
            "    }",
            "    {",
            "      TableColumn tableColumn_2 = new TableColumn(table, SWT.NONE);",
            "      tableColumn_2.setText('Column 2');",
            "    }",
            "  }",
            "}");
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    // prepare columns
    List<TableColumnInfo> columns = table.getColumns();
    assertEquals(2, columns.size());
    TableColumnInfo column_1 = columns.get(0);
    TableColumnInfo column_2 = columns.get(1);
    // move column
    flowContainer_MOVE(table, column_2, column_1);
    // check result
    assertSame(column_2, table.getColumns().get(0));
    assertSame(column_1, table.getColumns().get(1));
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    Table table = new Table(this, SWT.BORDER);",
        "    {",
        "      TableColumn tableColumn_2 = new TableColumn(table, SWT.NONE);",
        "      tableColumn_2.setText('Column 2');",
        "    }",
        "    {",
        "      TableColumn tableColumn_1 = new TableColumn(table, SWT.NONE);",
        "      tableColumn_1.setText('Column 1');",
        "    }",
        "  }",
        "}");
  }

  public void test_reparent_TableColumn() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    //",
            "    Table table_1 = new Table(this, SWT.BORDER);",
            "    {",
            "      TableColumn tableColumn_1 = new TableColumn(table_1, SWT.NONE);",
            "      tableColumn_1.setText('Column 1');",
            "    }",
            "    //",
            "    Table table_2 = new Table(this, SWT.BORDER);",
            "    {",
            "      TableColumn tableColumn_2 = new TableColumn(table_2, SWT.NONE);",
            "      tableColumn_2.setText('Column 2');",
            "    }",
            "  }",
            "}");
    // prepare table_1
    TableInfo table_1 = (TableInfo) shell.getChildrenControls().get(0);
    TableColumnInfo column_1 = table_1.getColumns().get(0);
    // prepare table_2
    TableInfo table_2 = (TableInfo) shell.getChildrenControls().get(1);
    TableColumnInfo column_2 = table_2.getColumns().get(0);
    // move column_1 before column_2
    flowContainer_MOVE(table_2, column_1, column_2);
    // check result
    {
      List<TableColumnInfo> columns = table_2.getColumns();
      assertEquals(2, columns.size());
      assertSame(column_1, columns.get(0));
      assertSame(column_2, columns.get(1));
    }
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    //",
        "    Table table_1 = new Table(this, SWT.BORDER);",
        "    //",
        "    Table table_2 = new Table(this, SWT.BORDER);",
        "    {",
        "      TableColumn tableColumn_1 = new TableColumn(table_2, SWT.NONE);",
        "      tableColumn_1.setText('Column 1');",
        "    }",
        "    {",
        "      TableColumn tableColumn_2 = new TableColumn(table_2, SWT.NONE);",
        "      tableColumn_2.setText('Column 2');",
        "    }",
        "  }",
        "}");
  }

  public void test_clipboard() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Table table = new Table(this, SWT.BORDER);",
            "      {",
            "        TableColumn tableColumn_1 = new TableColumn(table, SWT.NONE);",
            "        tableColumn_1.setText('Column 1');",
            "      }",
            "      {",
            "        TableColumn tableColumn_2 = new TableColumn(table, SWT.NONE);",
            "        tableColumn_2.setText('Column 2');",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableInfo table = getJavaInfoByName("table");
    // do copy/paste
    {
      JavaInfoMemento memento = JavaInfoMemento.createMemento(table);
      TableInfo newTable = (TableInfo) memento.create(shell);
      shell.getLayout().command_CREATE(newTable, null);
      JavaInfoMemento.apply(newTable);
    }
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Table table = new Table(this, SWT.BORDER);",
        "      {",
        "        TableColumn tableColumn_1 = new TableColumn(table, SWT.NONE);",
        "        tableColumn_1.setText('Column 1');",
        "      }",
        "      {",
        "        TableColumn tableColumn_2 = new TableColumn(table, SWT.NONE);",
        "        tableColumn_2.setText('Column 2');",
        "      }",
        "    }",
        "    {",
        "      Table table = new Table(this, SWT.BORDER);",
        "      {",
        "        TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
        "        tableColumn.setText('Column 1');",
        "      }",
        "      {",
        "        TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
        "        tableColumn.setText('Column 2');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exposed
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_column_exposed() throws Exception {
    setFileContentSrc(
        "test/ExposedComposite.java",
        getTestSource(
            "public class ExposedComposite extends Composite {",
            "  private TableColumn m_tableColumn;",
            "  private Table m_table;",
            "  //",
            "  public ExposedComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout(2, false));",
            "    m_table = new Table(this, SWT.BORDER);",
            "    {",
            "      m_tableColumn = new TableColumn(m_table, SWT.NONE);",
            "      m_tableColumn.setWidth(100);",
            "      m_tableColumn.setText('New Column');",
            "    }",
            "  }",
            "  public TableColumn getColumn() {",
            "    return m_tableColumn;",
            "  }",
            "  public Table getTable() {",
            "    return m_table;",
            "  }",
            "}"));
    waitForAutoBuild();
    CompositeInfo mainComposite =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ExposedComposite composite = new ExposedComposite(this, SWT.NONE);",
            "  }",
            "}");
    assertEquals(1, mainComposite.getChildrenControls().size());
    //
    CompositeInfo composite = (CompositeInfo) mainComposite.getChildrenControls().get(0);
    assertEquals(1, composite.getChildren(WidgetInfo.class).size());
    //
    TableInfo exposedTable = composite.getChildren(TableInfo.class).get(0);
    assertNotNull(exposedTable);
    WidgetInfo exposedColumn = exposedTable.getChildren(WidgetInfo.class).get(0);
    assertNotNull(exposedColumn);
    assertInstanceOf(ExposedPropertyCreationSupport.class, exposedColumn.getCreationSupport());
    assertInstanceOf(ExposedPropertyVariableSupport.class, exposedColumn.getVariableSupport());
  }

  public void test_item_exposed() throws Exception {
    setFileContentSrc(
        "test/ExposedComposite.java",
        getTestSource(
            "public class ExposedComposite extends Composite {",
            "  private TableItem m_tableItem;",
            "  private Table m_table;",
            "  //",
            "  public ExposedComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout(2, false));",
            "    m_table = new Table(this, SWT.BORDER);",
            "    {",
            "      TableColumn tableColumn = new TableColumn(m_table, SWT.NONE);",
            "      tableColumn.setWidth(100);",
            "      tableColumn.setText('New Column');",
            "    }",
            "    {",
            "      m_tableItem = new TableItem(m_table, SWT.NONE);",
            "      m_tableItem.setText('New Item');",
            "    }",
            "  }",
            "  public TableItem getItem() {",
            "    return m_tableItem;",
            "  }",
            "  public Table getTable() {",
            "    return m_table;",
            "  }",
            "}"));
    waitForAutoBuild();
    CompositeInfo mainComposite =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ExposedComposite composite = new ExposedComposite(this, SWT.NONE);",
            "  }",
            "}");
    assertEquals(1, mainComposite.getChildrenControls().size());
    //
    CompositeInfo composite = (CompositeInfo) mainComposite.getChildrenControls().get(0);
    assertEquals(1, composite.getChildren(WidgetInfo.class).size());
    //
    TableInfo exposedTable = composite.getChildren(TableInfo.class).get(0);
    assertNotNull(exposedTable);
    WidgetInfo exposedItem = exposedTable.getChildren(WidgetInfo.class).get(0);
    assertNotNull(exposedItem);
    assertInstanceOf(ItemInfo.class, exposedItem);
    assertInstanceOf(ExposedPropertyCreationSupport.class, exposedItem.getCreationSupport());
    assertInstanceOf(ExposedPropertyVariableSupport.class, exposedItem.getVariableSupport());
  }
}