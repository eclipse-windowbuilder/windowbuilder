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
import org.eclipse.wb.internal.rcp.model.widgets.TreeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

/**
 * Test for {@link TreeInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class TreeGefTest extends RcpGefTest {
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
        "public class Test extends Tree {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TreeColumn column = new TreeColumn(this, SWT.NONE);",
        "      column.setText('column');",
        "      column.setWidth(150);",
        "    }",
        "  }",
        "}");
    JavaInfo column = getJavaInfoByName("column");
    //
    loadCreationTool("org.eclipse.swt.widgets.TreeColumn");
    canvas.moveTo(column, 0.1, 0.5);
    canvas.assertFeedbacks(canvas.getLinePredicate(column, IPositionConstants.LEFT));
    canvas.click();
    assertEditor(
        "public class Test extends Tree {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TreeColumn treeColumn = new TreeColumn(this, SWT.NONE);",
        "      treeColumn.setWidth(100);",
        "      treeColumn.setText('New Column');",
        "    }",
        "    {",
        "      TreeColumn column = new TreeColumn(this, SWT.NONE);",
        "      column.setText('column');",
        "      column.setWidth(150);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_MOVE_column() throws Exception {
    openJavaInfo(
        "public class Test extends Tree {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TreeColumn column_1 = new TreeColumn(this, SWT.NONE);",
        "      column_1.setText('column_1');",
        "      column_1.setWidth(150);",
        "    }",
        "    {",
        "      TreeColumn column_2 = new TreeColumn(this, SWT.NONE);",
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
        "public class Test extends Tree {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TreeColumn column_2 = new TreeColumn(this, SWT.NONE);",
        "      column_2.setText('column_2');",
        "      column_2.setWidth(100);",
        "    }",
        "    {",
        "      TreeColumn column_1 = new TreeColumn(this, SWT.NONE);",
        "      column_1.setText('column_1');",
        "      column_1.setWidth(150);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_RESIZE_column() throws Exception {
    openJavaInfo(
        "public class Test extends Tree {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TreeColumn column = new TreeColumn(this, SWT.NONE);",
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
        "public class Test extends Tree {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TreeColumn column = new TreeColumn(this, SWT.NONE);",
        "      column.setText('column');",
        "      column.setWidth(100);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE_column() throws Exception {
    openJavaInfo(
        "public class Test extends Tree {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TreeColumn column = new TreeColumn(this, SWT.NONE);",
        "      column.setText('column');",
        "      column.setWidth(150);",
        "    }",
        "  }",
        "}");
    JavaInfo column = getJavaInfoByName("column");
    //
    loadCreationTool("org.eclipse.swt.widgets.TreeColumn");
    tree.moveBefore(column).click();
    assertEditor(
        "public class Test extends Tree {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TreeColumn treeColumn = new TreeColumn(this, SWT.NONE);",
        "      treeColumn.setWidth(100);",
        "      treeColumn.setText('New Column');",
        "    }",
        "    {",
        "      TreeColumn column = new TreeColumn(this, SWT.NONE);",
        "      column.setText('column');",
        "      column.setWidth(150);",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_MOVE_column() throws Exception {
    openJavaInfo(
        "public class Test extends Tree {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TreeColumn column_1 = new TreeColumn(this, SWT.NONE);",
        "      column_1.setText('column_1');",
        "      column_1.setWidth(150);",
        "    }",
        "    {",
        "      TreeColumn column_2 = new TreeColumn(this, SWT.NONE);",
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
        "public class Test extends Tree {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TreeColumn column_2 = new TreeColumn(this, SWT.NONE);",
        "      column_2.setText('column_2');",
        "      column_2.setWidth(100);",
        "    }",
        "    {",
        "      TreeColumn column_1 = new TreeColumn(this, SWT.NONE);",
        "      column_1.setText('column_1');",
        "      column_1.setWidth(150);",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_CREATE_item() throws Exception {
    openJavaInfo(
        "public class Test extends Tree {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TreeItem item = new TreeItem(this, SWT.NONE);",
        "      item.setText('item');",
        "    }",
        "  }",
        "}");
    JavaInfo column = getJavaInfoByName("item");
    //
    loadCreationTool("org.eclipse.swt.widgets.TreeItem");
    tree.moveBefore(column).click();
    assertEditor(
        "public class Test extends Tree {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TreeItem treeItem = new TreeItem(this, SWT.NONE);",
        "      treeItem.setText('New TreeItem');",
        "    }",
        "    {",
        "      TreeItem item = new TreeItem(this, SWT.NONE);",
        "      item.setText('item');",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_MOVE_item() throws Exception {
    openJavaInfo(
        "public class Test extends Tree {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TreeItem item_1 = new TreeItem(this, SWT.NONE);",
        "      item_1.setText('item_1');",
        "    }",
        "    {",
        "      TreeItem item_2 = new TreeItem(this, SWT.NONE);",
        "      item_2.setText('item_2');",
        "    }",
        "  }",
        "}");
    JavaInfo item_1 = getJavaInfoByName("item_1");
    JavaInfo item_2 = getJavaInfoByName("item_2");
    //
    tree.startDrag(item_2).dragBefore(item_1).endDrag();
    assertEditor(
        "public class Test extends Tree {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setHeaderVisible(true);",
        "    {",
        "      TreeItem item_2 = new TreeItem(this, SWT.NONE);",
        "      item_2.setText('item_2');",
        "    }",
        "    {",
        "      TreeItem item_1 = new TreeItem(this, SWT.NONE);",
        "      item_1.setText('item_1');",
        "    }",
        "  }",
        "}");
  }
}
