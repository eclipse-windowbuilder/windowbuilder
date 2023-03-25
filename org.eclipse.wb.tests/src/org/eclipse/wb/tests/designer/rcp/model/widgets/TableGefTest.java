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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.internal.rcp.model.widgets.TableInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

/**
 * Test for {@link TableInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class TableGefTest extends RcpGefTest {
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
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_CREATE_column() throws Exception {
    openJavaInfo(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableColumn column = new TableColumn(this, SWT.NONE);",
        "      column.setText('column');",
        "      column.setWidth(150);",
        "    }",
        "  }",
        "}");
    JavaInfo column = getJavaInfoByName("column");
    //
    loadCreationTool("org.eclipse.swt.widgets.TableColumn");
    canvas.moveTo(column, 0.1, 0.5);
    canvas.assertFeedbacks(canvas.getLinePredicate(column, IPositionConstants.LEFT));
    canvas.click();
    assertEditor(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableColumn tableColumn = new TableColumn(this, SWT.NONE);",
        "      tableColumn.setWidth(100);",
        "      tableColumn.setText('New Column');",
        "    }",
        "    {",
        "      TableColumn column = new TableColumn(this, SWT.NONE);",
        "      column.setText('column');",
        "      column.setWidth(150);",
        "    }",
        "  }",
        "}");
  }

  // Test get stuck during the Linux build...
  public void DISABLE_test_canvas_MOVE_column() throws Exception {
    openJavaInfo(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableColumn column_1 = new TableColumn(this, SWT.NONE);",
        "      column_1.setText('column_1');",
        "      column_1.setWidth(150);",
        "    }",
        "    {",
        "      TableColumn column_2 = new TableColumn(this, SWT.NONE);",
        "      column_2.setText('column_2');",
        "      column_2.setWidth(100);",
        "    }",
        "  }",
        "}");
    JavaInfo column_1 = getJavaInfoByName("column_1");
    JavaInfo column_2 = getJavaInfoByName("column_2");
    //
    canvas.beginMove(column_2);
    canvas.dragTo(column_1, 0.1, 0.5).endDrag();
    assertEditor(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableColumn column_2 = new TableColumn(this, SWT.NONE);",
        "      column_2.setText('column_2');",
        "      column_2.setWidth(100);",
        "    }",
        "    {",
        "      TableColumn column_1 = new TableColumn(this, SWT.NONE);",
        "      column_1.setText('column_1');",
        "      column_1.setWidth(150);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_RESIZE_column() throws Exception {
    openJavaInfo(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableColumn column = new TableColumn(this, SWT.NONE);",
        "      column.setText('column');",
        "      column.setWidth(150);",
        "    }",
        "  }",
        "}");
    JavaInfo column = getJavaInfoByName("column");
    //
    canvas.target(column).outX(1).inY(0.5);
    canvas.beginDrag().dragOn(-50, 0).endDrag();
    assertEditor(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableColumn column = new TableColumn(this, SWT.NONE);",
        "      column.setText('column');",
        "      column.setWidth(100);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_CREATE_item() throws Exception {
    openJavaInfo(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableItem item = new TableItem(this, SWT.NONE);",
        "      item.setText('item');",
        "    }",
        "  }",
        "}");
    JavaInfo item = getJavaInfoByName("item");
    //
    loadCreationTool("org.eclipse.swt.widgets.TableItem");
    canvas.moveTo(item, 0.5, 0.1);
    canvas.assertFeedbacks(canvas.getLinePredicate(item, IPositionConstants.TOP));
    canvas.click();
    assertEditor(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableItem tableItem = new TableItem(this, SWT.NONE);",
        "      tableItem.setText('New TableItem');",
        "    }",
        "    {",
        "      TableItem item = new TableItem(this, SWT.NONE);",
        "      item.setText('item');",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_MOVE_item() throws Exception {
    openJavaInfo(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableItem item_1 = new TableItem(this, SWT.NONE);",
        "      item_1.setText('item_1');",
        "    }",
        "    {",
        "      TableItem item_2 = new TableItem(this, SWT.NONE);",
        "      item_2.setText('item_2');",
        "    }",
        "  }",
        "}");
    JavaInfo item_1 = getJavaInfoByName("item_1");
    JavaInfo item_2 = getJavaInfoByName("item_2");
    //
    canvas.beginMove(item_2);
    canvas.dragTo(item_1, 0.5, 0.1);
    canvas.assertFeedbacks(canvas.getLinePredicate(item_1, IPositionConstants.TOP));
    canvas.endDrag();
    assertEditor(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableItem item_2 = new TableItem(this, SWT.NONE);",
        "      item_2.setText('item_2');",
        "    }",
        "    {",
        "      TableItem item_1 = new TableItem(this, SWT.NONE);",
        "      item_1.setText('item_1');",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_CREATE_cursor() throws Exception {
    JavaInfo table =
        openJavaInfo(
            "public class Test extends Table {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    setHeaderVisible(true);",
            "  }",
            "}");
    // do create
    loadCreationTool("org.eclipse.swt.custom.TableCursor");
    canvas.moveTo(table);
    canvas.assertFeedbacks(canvas.getTargetPredicate(table));
    canvas.assertCommandNotNull();
    canvas.click();
    assertEditor(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableCursor tableCursor = new TableCursor(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
    // only one TableCursor
    loadCreationTool("org.eclipse.swt.custom.TableCursor");
    canvas.moveTo(table);
    canvas.assertCommandNull();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE_column() throws Exception {
    openJavaInfo(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableColumn column = new TableColumn(this, SWT.NONE);",
        "      column.setText('column');",
        "      column.setWidth(150);",
        "    }",
        "  }",
        "}");
    JavaInfo column = getJavaInfoByName("column");
    //
    loadCreationTool("org.eclipse.swt.widgets.TableColumn");
    tree.moveBefore(column).click();
    assertEditor(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableColumn tableColumn = new TableColumn(this, SWT.NONE);",
        "      tableColumn.setWidth(100);",
        "      tableColumn.setText('New Column');",
        "    }",
        "    {",
        "      TableColumn column = new TableColumn(this, SWT.NONE);",
        "      column.setText('column');",
        "      column.setWidth(150);",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_MOVE_column() throws Exception {
    openJavaInfo(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableColumn column_1 = new TableColumn(this, SWT.NONE);",
        "      column_1.setText('column_1');",
        "      column_1.setWidth(150);",
        "    }",
        "    {",
        "      TableColumn column_2 = new TableColumn(this, SWT.NONE);",
        "      column_2.setText('column_2');",
        "      column_2.setWidth(100);",
        "    }",
        "  }",
        "}");
    JavaInfo column_1 = getJavaInfoByName("column_1");
    JavaInfo column_2 = getJavaInfoByName("column_2");
    //
    tree.startDrag(column_2).dragBefore(column_1).endDrag();
    assertEditor(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableColumn column_2 = new TableColumn(this, SWT.NONE);",
        "      column_2.setText('column_2');",
        "      column_2.setWidth(100);",
        "    }",
        "    {",
        "      TableColumn column_1 = new TableColumn(this, SWT.NONE);",
        "      column_1.setText('column_1');",
        "      column_1.setWidth(150);",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_CREATE_item() throws Exception {
    openJavaInfo(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableItem item = new TableItem(this, SWT.NONE);",
        "      item.setText('item');",
        "    }",
        "  }",
        "}");
    JavaInfo column = getJavaInfoByName("item");
    //
    loadCreationTool("org.eclipse.swt.widgets.TableItem");
    tree.moveBefore(column).click();
    assertEditor(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableItem tableItem = new TableItem(this, SWT.NONE);",
        "      tableItem.setText('New TableItem');",
        "    }",
        "    {",
        "      TableItem item = new TableItem(this, SWT.NONE);",
        "      item.setText('item');",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_MOVE_item() throws Exception {
    openJavaInfo(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableItem item_1 = new TableItem(this, SWT.NONE);",
        "      item_1.setText('item_1');",
        "    }",
        "    {",
        "      TableItem item_2 = new TableItem(this, SWT.NONE);",
        "      item_2.setText('item_2');",
        "    }",
        "  }",
        "}");
    JavaInfo item_1 = getJavaInfoByName("item_1");
    JavaInfo item_2 = getJavaInfoByName("item_2");
    //
    tree.startDrag(item_2).dragBefore(item_1).endDrag();
    assertEditor(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableItem item_2 = new TableItem(this, SWT.NONE);",
        "      item_2.setText('item_2');",
        "    }",
        "    {",
        "      TableItem item_1 = new TableItem(this, SWT.NONE);",
        "      item_1.setText('item_1');",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_CREATE_cursor() throws Exception {
    JavaInfo table =
        openJavaInfo(
            "public class Test extends Table {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    setHeaderVisible(true);",
            "  }",
            "}");
    // do create
    loadCreationTool("org.eclipse.swt.custom.TableCursor");
    tree.moveOn(table);
    tree.assertCommandNotNull();
    tree.click();
    assertEditor(
        "public class Test extends Table {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TableCursor tableCursor = new TableCursor(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
    // only one TableCursor
    loadCreationTool("org.eclipse.swt.custom.TableCursor");
    tree.moveOn(table);
    tree.assertCommandNull();
  }
}
