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
import org.eclipse.wb.internal.rcp.model.widgets.AbstractTabFolderInfo;
import org.eclipse.wb.internal.rcp.model.widgets.AbstractTabItemInfo;
import org.eclipse.wb.internal.rcp.model.widgets.TabFolderInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

import org.eclipse.swt.widgets.TabItem;

/**
 * Test for {@link TabFolderInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class TabFolderGefTest extends RcpGefTest {
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
  /**
   * We should be able to select {@link TabItem} using double click.
   */
  public void test_canvas_selectItem() throws Exception {
    TabFolderInfo folder =
        openJavaInfo(
            "public class Test extends TabFolder {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    {",
            "      TabItem item_1 = new TabItem(this, SWT.NONE);",
            "      item_1.setText('Item 1');",
            "    }",
            "    {",
            "      TabItem item_2 = new TabItem(this, SWT.NONE);",
            "      item_2.setText('Item 2');",
            "    }",
            "  }",
            "}");
    JavaInfo item_1 = getJavaInfoByName("item_1");
    JavaInfo item_2 = getJavaInfoByName("item_2");
    // initially "item_1" is selected
    assertSame(item_1, folder.getSelectedItem());
    // double click "item_2"
    canvas.doubleClick(item_2);
    assertSame(item_2, folder.getSelectedItem());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE: control
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_control_canvas_CREATE_whenNoItems() throws Exception {
    TabFolderInfo folder =
        (TabFolderInfo) openJavaInfo(
            "public class Test extends TabFolder {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    //
    loadButton();
    canvas.moveTo(folder, 5, 100);
    canvas.assertEmptyFlowContainerFeedback(folder, true);
    canvas.click();
    assertEditor(
        "public class Test extends TabFolder {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      TabItem tabItem = new TabItem(this, SWT.NONE);",
        "      tabItem.setText('New Item');",
        "      {",
        "        Button button = new Button(this, SWT.NONE);",
        "        tabItem.setControl(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_control_canvas_CREATE_beforeItem() throws Exception {
    AbstractTabFolderInfo folder =
        (AbstractTabFolderInfo) openJavaInfo(
            "public class Test extends TabFolder {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    {",
            "      TabItem existingItem = new TabItem(this, SWT.NONE);",
            "      existingItem.setText('Existing item');",
            "    }",
            "  }",
            "}");
    AbstractTabItemInfo item = folder.getItems().get(0);
    //
    loadButton();
    canvas.moveTo(item, 5, 100);
    canvas.assertFeedbacks(canvas.getLinePredicate(item, IPositionConstants.LEFT));
    canvas.click();
    assertEditor(
        "public class Test extends TabFolder {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      TabItem tabItem = new TabItem(this, SWT.NONE);",
        "      tabItem.setText('New Item');",
        "      {",
        "        Button button = new Button(this, SWT.NONE);",
        "        tabItem.setControl(button);",
        "      }",
        "    }",
        "    {",
        "      TabItem existingItem = new TabItem(this, SWT.NONE);",
        "      existingItem.setText('Existing item');",
        "    }",
        "  }",
        "}");
  }

  public void test_control_canvas_CREATE_onItem() throws Exception {
    AbstractTabFolderInfo folder =
        (AbstractTabFolderInfo) openJavaInfo(
            "public class Test extends TabFolder {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    {",
            "      TabItem existingItem = new TabItem(this, SWT.NONE);",
            "      existingItem.setText('Existing item');",
            "    }",
            "  }",
            "}");
    AbstractTabItemInfo item = folder.getItems().get(0);
    //
    loadButton();
    canvas.moveTo(item, 0.5, 0.5);
    canvas.assertFeedbacks(canvas.getTargetPredicate(item));
    canvas.click();
    assertEditor(
        "public class Test extends TabFolder {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      TabItem existingItem = new TabItem(this, SWT.NONE);",
        "      existingItem.setText('Existing item');",
        "      {",
        "        Button button = new Button(this, SWT.NONE);",
        "        existingItem.setControl(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE tree: control
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_control_tree_CREATE_whenNoItems() throws Exception {
    TabFolderInfo folder =
        (TabFolderInfo) openJavaInfo(
            "public class Test extends TabFolder {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    //
    loadButton();
    tree.moveOn(folder);
    tree.assertFeedback_on(folder);
    tree.click();
    assertEditor(
        "public class Test extends TabFolder {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      TabItem tabItem = new TabItem(this, SWT.NONE);",
        "      tabItem.setText('New Item');",
        "      {",
        "        Button button = new Button(this, SWT.NONE);",
        "        tabItem.setControl(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_control_tree_CREATE_beforeItem() throws Exception {
    AbstractTabFolderInfo folder =
        (AbstractTabFolderInfo) openJavaInfo(
            "public class Test extends TabFolder {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    {",
            "      TabItem existingItem = new TabItem(this, SWT.NONE);",
            "      existingItem.setText('Existing item');",
            "    }",
            "  }",
            "}");
    AbstractTabItemInfo item = folder.getItems().get(0);
    //
    loadButton();
    tree.moveBefore(item);
    tree.assertFeedback_before(item);
    tree.click();
    assertEditor(
        "public class Test extends TabFolder {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      TabItem tabItem = new TabItem(this, SWT.NONE);",
        "      tabItem.setText('New Item');",
        "      {",
        "        Button button = new Button(this, SWT.NONE);",
        "        tabItem.setControl(button);",
        "      }",
        "    }",
        "    {",
        "      TabItem existingItem = new TabItem(this, SWT.NONE);",
        "      existingItem.setText('Existing item');",
        "    }",
        "  }",
        "}");
  }

  public void test_control_tree_CREATE_onItem() throws Exception {
    AbstractTabFolderInfo folder =
        (AbstractTabFolderInfo) openJavaInfo(
            "public class Test extends TabFolder {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    {",
            "      TabItem existingItem = new TabItem(this, SWT.NONE);",
            "      existingItem.setText('Existing item');",
            "    }",
            "  }",
            "}");
    AbstractTabItemInfo item = folder.getItems().get(0);
    //
    loadButton();
    tree.moveOn(item);
    tree.assertFeedback_on(item);
    tree.click();
    assertEditor(
        "public class Test extends TabFolder {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      TabItem existingItem = new TabItem(this, SWT.NONE);",
        "      existingItem.setText('Existing item');",
        "      {",
        "        Button button = new Button(this, SWT.NONE);",
        "        existingItem.setControl(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE canvas: item
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_item_canvas_CREATE_whenNoItems() throws Exception {
    TabFolderInfo folder =
        (TabFolderInfo) openJavaInfo(
            "public class Test extends TabFolder {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    //
    loadCreationTool("org.eclipse.swt.widgets.TabItem");
    canvas.moveTo(folder, 5, 100);
    canvas.assertEmptyFlowContainerFeedback(folder, true);
    canvas.click();
    assertEditor(
        "public class Test extends TabFolder {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      TabItem tabItem = new TabItem(this, SWT.NONE);",
        "      tabItem.setText('New Item');",
        "    }",
        "  }",
        "}");
  }

  public void test_item_canvas_CREATE_beforeItem() throws Exception {
    AbstractTabFolderInfo folder =
        (AbstractTabFolderInfo) openJavaInfo(
            "public class Test extends TabFolder {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    {",
            "      TabItem existingItem = new TabItem(this, SWT.NONE);",
            "      existingItem.setText('Existing item');",
            "    }",
            "  }",
            "}");
    AbstractTabItemInfo item = folder.getItems().get(0);
    //
    loadCreationTool("org.eclipse.swt.widgets.TabItem");
    canvas.moveTo(item, 5, 100);
    canvas.assertFeedbacks(canvas.getLinePredicate(item, IPositionConstants.LEFT));
    canvas.click();
    assertEditor(
        "public class Test extends TabFolder {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      TabItem tabItem = new TabItem(this, SWT.NONE);",
        "      tabItem.setText('New Item');",
        "    }",
        "    {",
        "      TabItem existingItem = new TabItem(this, SWT.NONE);",
        "      existingItem.setText('Existing item');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE tree: item
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_item_tree_CREATE_whenNoItems() throws Exception {
    TabFolderInfo folder =
        (TabFolderInfo) openJavaInfo(
            "public class Test extends TabFolder {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    //
    loadCreationTool("org.eclipse.swt.widgets.TabItem");
    tree.moveOn(folder);
    tree.assertFeedback_on(folder);
    tree.click();
    assertEditor(
        "public class Test extends TabFolder {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      TabItem tabItem = new TabItem(this, SWT.NONE);",
        "      tabItem.setText('New Item');",
        "    }",
        "  }",
        "}");
  }

  public void test_item_tree_CREATE_beforeItem() throws Exception {
    AbstractTabFolderInfo folder =
        (AbstractTabFolderInfo) openJavaInfo(
            "public class Test extends TabFolder {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    {",
            "      TabItem existingItem = new TabItem(this, SWT.NONE);",
            "      existingItem.setText('Existing item');",
            "    }",
            "  }",
            "}");
    AbstractTabItemInfo item = folder.getItems().get(0);
    //
    loadCreationTool("org.eclipse.swt.widgets.TabItem");
    tree.moveBefore(item);
    tree.assertFeedback_before(item);
    tree.click();
    assertEditor(
        "public class Test extends TabFolder {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      TabItem tabItem = new TabItem(this, SWT.NONE);",
        "      tabItem.setText('New Item');",
        "    }",
        "    {",
        "      TabItem existingItem = new TabItem(this, SWT.NONE);",
        "      existingItem.setText('Existing item');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE: item
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_item_canvas_MOVE() throws Exception {
    openJavaInfo(
        "public class Test extends TabFolder {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      TabItem item_1 = new TabItem(this, SWT.NONE);",
        "      item_1.setText('Item 1');",
        "    }",
        "    {",
        "      TabItem item_2 = new TabItem(this, SWT.NONE);",
        "      item_2.setText('Item 2');",
        "    }",
        "  }",
        "}");
    JavaInfo item_1 = getJavaInfoByName("item_1");
    JavaInfo item_2 = getJavaInfoByName("item_2");
    //
    canvas.beginMove(item_2).dragTo(item_1, 2, 0.5);
    canvas.endDrag();
    assertEditor(
        "public class Test extends TabFolder {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      TabItem item_2 = new TabItem(this, SWT.NONE);",
        "      item_2.setText('Item 2');",
        "    }",
        "    {",
        "      TabItem item_1 = new TabItem(this, SWT.NONE);",
        "      item_1.setText('Item 1');",
        "    }",
        "  }",
        "}");
  }

  public void test_item_tree_MOVE() throws Exception {
    openJavaInfo(
        "public class Test extends TabFolder {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      TabItem item_1 = new TabItem(this, SWT.NONE);",
        "      item_1.setText('Item 1');",
        "    }",
        "    {",
        "      TabItem item_2 = new TabItem(this, SWT.NONE);",
        "      item_2.setText('Item 2');",
        "    }",
        "  }",
        "}");
    JavaInfo item_1 = getJavaInfoByName("item_1");
    JavaInfo item_2 = getJavaInfoByName("item_2");
    //
    tree.startDrag(item_2).dragBefore(item_1);
    tree.endDrag();
    assertEditor(
        "public class Test extends TabFolder {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      TabItem item_2 = new TabItem(this, SWT.NONE);",
        "      item_2.setText('Item 2');",
        "    }",
        "    {",
        "      TabItem item_1 = new TabItem(this, SWT.NONE);",
        "      item_1.setText('Item 1');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ADD_control_canvas_onItem() throws Exception {
    openJavaInfo(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      TabFolder folder = new TabFolder(this, SWT.NONE);",
        "      {",
        "        TabItem item = new TabItem(folder, SWT.NONE);",
        "        item.setText('Item');",
        "      }",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('button');",
        "    }",
        "  }",
        "}");
    JavaInfo item = getJavaInfoByName("item");
    JavaInfo button = getJavaInfoByName("button");
    //
    canvas.beginMove(button).dragTo(item);
    canvas.endDrag();
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      TabFolder folder = new TabFolder(this, SWT.NONE);",
        "      {",
        "        TabItem item = new TabItem(folder, SWT.NONE);",
        "        item.setText('Item');",
        "        {",
        "          Button button = new Button(folder, SWT.NONE);",
        "          item.setControl(button);",
        "          button.setText('button');",
        "        }",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_ADD_control_tree_onItem() throws Exception {
    openJavaInfo(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      TabFolder folder = new TabFolder(this, SWT.NONE);",
        "      {",
        "        TabItem item = new TabItem(folder, SWT.NONE);",
        "        item.setText('Item');",
        "      }",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('button');",
        "    }",
        "  }",
        "}");
    JavaInfo item = getJavaInfoByName("item");
    JavaInfo button = getJavaInfoByName("button");
    //
    tree.startDrag(button).dragOn(item);
    tree.endDrag();
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      TabFolder folder = new TabFolder(this, SWT.NONE);",
        "      {",
        "        TabItem item = new TabItem(folder, SWT.NONE);",
        "        item.setText('Item');",
        "        {",
        "          Button button = new Button(folder, SWT.NONE);",
        "          item.setControl(button);",
        "          button.setText('button');",
        "        }",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_ADD_control_canvas_beforeItem() throws Exception {
    openJavaInfo(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      TabFolder folder = new TabFolder(this, SWT.NONE);",
        "      {",
        "        TabItem item = new TabItem(folder, SWT.NONE);",
        "        item.setText('Item');",
        "      }",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('button');",
        "    }",
        "  }",
        "}");
    JavaInfo item = getJavaInfoByName("item");
    JavaInfo button = getJavaInfoByName("button");
    //
    canvas.beginMove(button).dragTo(item, 5, 100);
    canvas.assertFeedbacks(canvas.getLinePredicate(item, IPositionConstants.LEFT));
    canvas.endDrag();
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      TabFolder folder = new TabFolder(this, SWT.NONE);",
        "      {",
        "        TabItem tabItem = new TabItem(folder, SWT.NONE);",
        "        tabItem.setText('New Item');",
        "        {",
        "          Button button = new Button(folder, SWT.NONE);",
        "          tabItem.setControl(button);",
        "          button.setText('button');",
        "        }",
        "      }",
        "      {",
        "        TabItem item = new TabItem(folder, SWT.NONE);",
        "        item.setText('Item');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_ADD_control_tree_beforeItem() throws Exception {
    openJavaInfo(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      TabFolder folder = new TabFolder(this, SWT.NONE);",
        "      {",
        "        TabItem item = new TabItem(folder, SWT.NONE);",
        "        item.setText('Item');",
        "      }",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('button');",
        "    }",
        "  }",
        "}");
    JavaInfo item = getJavaInfoByName("item");
    JavaInfo button = getJavaInfoByName("button");
    //
    tree.startDrag(button).dragBefore(item);
    tree.endDrag();
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      TabFolder folder = new TabFolder(this, SWT.NONE);",
        "      {",
        "        TabItem tabItem = new TabItem(folder, SWT.NONE);",
        "        tabItem.setText('New Item');",
        "        {",
        "          Button button = new Button(folder, SWT.NONE);",
        "          tabItem.setControl(button);",
        "          button.setText('button');",
        "        }",
        "      }",
        "      {",
        "        TabItem item = new TabItem(folder, SWT.NONE);",
        "        item.setText('Item');",
        "      }",
        "    }",
        "  }",
        "}");
  }
}
