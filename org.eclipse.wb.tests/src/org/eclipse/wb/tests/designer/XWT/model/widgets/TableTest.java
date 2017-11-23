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
package org.eclipse.wb.tests.designer.XWT.model.widgets;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.TableColumnInfo;
import org.eclipse.wb.internal.xwt.model.widgets.TableInfo;
import org.eclipse.wb.internal.xwt.model.widgets.TableItemInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Tests for {@link TableInfo}, {@link TableColumnInfo} and {@link TableItemInfo}.
 * 
 * @author scheglov_ke
 */
public class TableTest extends XwtModelTest {
  private static final int COLUMN_HEIGHT = 24;

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
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Table wbp:name='table'>",
        "    <TableColumn width='100'/>",
        "    <TableItem wbp:name='item_1'/>",
        "    <TableItem wbp:name='item_2'/>",
        "  </Table>",
        "</Shell>");
    refresh();
    TableInfo table = getObjectByName("table");
    // prepare items
    List<TableItemInfo> items = table.getItems();
    assertThat(items).hasSize(2);
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
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Table wbp:name='table'>",
        "    <TableItem wbp:name='item'/>",
        "  </Table>",
        "</Shell>");
    refresh();
    TableInfo table = getObjectByName("table");
    TableItemInfo item = getObjectByName("item");
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
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Table wbp:name='table'/>",
        "</Shell>");
    refresh();
    TableInfo table = getObjectByName("table");
    // no items initially
    assertThat(table.getItems()).isEmpty();
    // add new TableItem
    TableItemInfo newItem = createObject("org.eclipse.swt.widgets.TableItem");
    flowContainer_CREATE(table, newItem, null);
    // check result
    assertThat(table.getItems()).containsExactly(newItem);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Table wbp:name='table'>",
        "    <TableItem text='New TableItem'/>",
        "  </Table>",
        "</Shell>");
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
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Table wbp:name='table' headerVisible='true'>",
        "    <TableColumn width='50'/>",
        "    <TableColumn width='100'/>",
        "  </Table>",
        "</Shell>");
    refresh();
    TableInfo table = getObjectByName("table");
    int aiLeft = table.getClientAreaInsets().left;
    int aiTop = table.getClientAreaInsets().top;
    // prepare columns
    List<TableColumnInfo> columns = table.getColumns();
    assertThat(columns).hasSize(2);
    // check bounds
    {
      TableColumnInfo column_1 = columns.get(0);
      assertEquals(new Rectangle(0, 0, 50, COLUMN_HEIGHT), column_1.getModelBounds());
      assertEquals(new Rectangle(aiLeft, aiTop, 50, COLUMN_HEIGHT), column_1.getBounds());
    }
    {
      TableColumnInfo column_2 = columns.get(1);
      assertEquals(new Rectangle(50, 0, 100, COLUMN_HEIGHT), column_2.getModelBounds());
      assertEquals(new Rectangle(aiLeft + 50, aiTop, 100, COLUMN_HEIGHT), column_2.getBounds());
    }
  }

  public void test_TableColumn_setWidth() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Table headerVisible='true'>",
        "    <TableColumn wbp:name='column' width='100'/>",
        "  </Table>",
        "</Shell>");
    refresh();
    TableColumnInfo column = getObjectByName("column");
    Property widthProperty = column.getPropertyByTitle("width");
    // check initial width
    assertEquals(100, widthProperty.getValue());
    // set new width
    column.setWidth(120);
    assertEquals(120, widthProperty.getValue());
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Table headerVisible='true'>",
        "    <TableColumn wbp:name='column' width='120'/>",
        "  </Table>",
        "</Shell>");
  }

  public void test_add_TableColumn() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Table wbp:name='table'/>",
        "</Shell>");
    TableInfo table = getObjectByName("table");
    // no columns initially
    assertTrue(table.getColumns().isEmpty());
    // add new TableColumn
    TableColumnInfo newColumn = createObject("org.eclipse.swt.widgets.TableColumn");
    flowContainer_CREATE(table, newColumn, null);
    // check result
    assertThat(table.getColumns()).containsExactly(newColumn);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Table wbp:name='table'>",
        "    <TableColumn text='New Column' width='100'/>",
        "  </Table>",
        "</Shell>");
  }

  public void test_move_TableColumn() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Table wbp:name='table'>",
        "    <TableColumn text='columns 1'/>",
        "    <TableColumn text='columns 2'/>",
        "  </Table>",
        "</Shell>");
    TableInfo table = getObjectByName("table");
    // prepare columns
    List<TableColumnInfo> columns = table.getColumns();
    assertThat(columns).hasSize(2);
    TableColumnInfo column_1 = columns.get(0);
    TableColumnInfo column_2 = columns.get(1);
    // move column
    flowContainer_MOVE(table, column_2, column_1);
    // check result
    assertThat(table.getColumns()).containsExactly(column_2, column_1);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Table wbp:name='table'>",
        "    <TableColumn text='columns 2'/>",
        "    <TableColumn text='columns 1'/>",
        "  </Table>",
        "</Shell>");
  }

  public void test_reparent_TableColumn() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Table wbp:name='table_1'>",
        "    <TableColumn wbp:name='column_1'/>",
        "  </Table>",
        "  <Table wbp:name='table_2'>",
        "    <TableColumn wbp:name='column_2'/>",
        "  </Table>",
        "</Shell>");
    TableInfo table_2 = getObjectByName("table_2");
    TableColumnInfo column_1 = getObjectByName("column_1");
    TableColumnInfo column_2 = getObjectByName("column_2");
    // move column_1 before column_2
    flowContainer_MOVE(table_2, column_1, column_2);
    // check result
    assertThat(table_2.getColumns()).containsExactly(column_1, column_2);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Table wbp:name='table_1'/>",
        "  <Table wbp:name='table_2'>",
        "    <TableColumn wbp:name='column_1'/>",
        "    <TableColumn wbp:name='column_2'/>",
        "  </Table>",
        "</Shell>");
  }

  public void test_clipboard() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Table wbp:name='table'>",
            "    <TableColumn text='columns 1'/>",
            "    <TableColumn text='columns 2'/>",
            "  </Table>",
            "</Shell>");
    refresh();
    TableInfo table = getObjectByName("table");
    // do copy/paste
    {
      XmlObjectMemento memento = XmlObjectMemento.createMemento(table);
      TableInfo newTable = (TableInfo) memento.create(shell);
      shell.getLayout().command_CREATE(newTable, null);
      XmlObjectMemento.apply(newTable);
    }
    assertXML(
        "<Shell>",
        "  <Table wbp:name='table'>",
        "    <TableColumn text='columns 1'/>",
        "    <TableColumn text='columns 2'/>",
        "  </Table>",
        "  <Table>",
        "    <TableColumn text='columns 1'/>",
        "    <TableColumn text='columns 2'/>",
        "  </Table>",
        "</Shell>");
  }
}