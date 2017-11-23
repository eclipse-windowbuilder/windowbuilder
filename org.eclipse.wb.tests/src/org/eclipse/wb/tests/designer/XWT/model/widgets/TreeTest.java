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
import org.eclipse.wb.internal.xwt.model.widgets.TreeColumnInfo;
import org.eclipse.wb.internal.xwt.model.widgets.TreeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.TreeItemInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Tests for {@link TreeInfo} and {@link TreeItemInfo}.
 * 
 * @author scheglov_ke
 */
public class TreeTest extends XwtModelTest {
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
   * Test for parsing {@link TreeItem} and bounds of {@link TreeItemInfo}.
   */
  public void test_TreeItem_parse() throws Exception {
    TreeInfo tree =
        parse(
            "// filler filler filler filler filler",
            "<Tree>",
            "  <TreeItem wbp:name='item_1' text='TreeItem 1'/>",
            "  <TreeItem wbp:name='item_2' text='TreeItem 2'/>",
            "</Tree>");
    refresh();
    // prepare items
    List<TreeItemInfo> items = tree.getItems();
    assertThat(items).hasSize(2);
    TreeItemInfo item_1 = items.get(0);
    TreeItemInfo item_2 = items.get(1);
    // no sub-items
    assertThat(item_1.getItems()).isEmpty();
    assertThat(item_2.getItems()).isEmpty();
    // check bounds
    Insets tableInsets = tree.getClientAreaInsets();
    {
      // "model" bounds
      Rectangle modelBounds = item_1.getModelBounds();
      {
        assertNotNull(modelBounds);
        assertThat(modelBounds.x).isGreaterThan(10); // some space for tree line
        assertThat(modelBounds.y).isEqualTo(0);
        assertThat(modelBounds.width).isGreaterThan(50);
        assertThat(modelBounds.height).isGreaterThan(15).isLessThan(30);
      }
      // "shot" bounds
      Rectangle bounds = item_1.getBounds();
      assertEquals(tableInsets.left, bounds.x - modelBounds.x);
      assertEquals(tableInsets.top, bounds.y - modelBounds.y);
      assertEquals(modelBounds.width, bounds.width);
      assertEquals(modelBounds.height, bounds.height);
    }
    {
      Rectangle modelBounds = item_2.getModelBounds();
      assertEquals(item_1.getModelBounds().x, modelBounds.x);
      assertEquals(item_1.getModelBounds().bottom(), modelBounds.y);
    }
  }

  /**
   * Test for parsing {@link TreeItemInfo} with {@link TreeItemInfo} child.
   */
  public void test_TreeItem_parse_subItems() throws Exception {
    TreeInfo tree =
        parse(
            "// filler filler filler filler filler",
            "<Tree>",
            "  <TreeItem wbp:name='item_1' text='TreeItem 1'>",
            "    <TreeItem wbp:name='item_2' text='TreeItem 2'/>",
            "  </TreeItem>",
            "</Tree>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Tree>",
        "  <TreeItem wbp:name='item_1' text='TreeItem 1'>",
        "    <TreeItem wbp:name='item_2' text='TreeItem 2'>");
    TreeItemInfo item_1 = getObjectByName("item_1");
    TreeItemInfo item_2 = getObjectByName("item_2");
    // check hierarchy
    assertThat(tree.getItems()).containsExactly(item_1);
    assertThat(item_1.getItems()).containsExactly(item_2);
    assertThat(item_2.getItems()).isEmpty();
  }

  public void test_TreeItem_addToTree() throws Exception {
    TreeInfo tree =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Tree/>");
    // no items initially
    assertThat(tree.getItems()).isEmpty();
    // add new TreeItem
    TreeItemInfo newItem = createObject("org.eclipse.swt.widgets.TreeItem");
    flowContainer_CREATE(tree, newItem, null);
    assertThat(tree.getItems()).containsExactly(newItem);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Tree>",
        "  <TreeItem text='New TreeItem'/>",
        "</Tree>");
  }

  public void test_TreeItem_moveInTree() throws Exception {
    TreeInfo tree =
        parse(
            "// filler filler filler filler filler",
            "<Tree>",
            "  <TreeItem wbp:name='item_1' text='TreeItem 1'/>",
            "  <TreeItem wbp:name='item_2' text='TreeItem 2'/>",
            "</Tree>");
    refresh();
    TreeItemInfo item_1 = getObjectByName("item_1");
    TreeItemInfo item_2 = getObjectByName("item_2");
    // do move
    flowContainer_MOVE(tree, item_2, item_1);
    assertThat(tree.getItems()).containsExactly(item_2, item_1);
    assertXML(
        "// filler filler filler filler filler",
        "<Tree>",
        "  <TreeItem wbp:name='item_2' text='TreeItem 2'/>",
        "  <TreeItem wbp:name='item_1' text='TreeItem 1'/>",
        "</Tree>");
  }

  /**
   * Test for adding new {@link TreeItemInfo} on existing {@link TreeItemInfo}.
   */
  public void test_TreeItem_addToItem() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Tree>",
        "  <TreeItem wbp:name='existingItem'/>",
        "</Tree>");
    refresh();
    // prepare existing TreeItem
    TreeItemInfo existingItem = getObjectByName("existingItem");
    assertThat(existingItem.getItems()).isEmpty();
    // add new TreeItem
    TreeItemInfo newItem = createObject("org.eclipse.swt.widgets.TreeItem");
    flowContainer_CREATE(existingItem, newItem, null);
    assertThat(existingItem.getItems()).containsExactly(newItem);
    assertXML(
        "// filler filler filler filler filler",
        "<Tree>",
        "  <TreeItem wbp:name='existingItem' expanded='true'>",
        "    <TreeItem text='New TreeItem'/>",
        "  </TreeItem>",
        "</Tree>");
  }

  /**
   * Test for moving {@link TreeItemInfo} on other {@link TreeItemInfo}.
   */
  public void test_TreeItem_moveToItem() throws Exception {
    TreeInfo tree =
        parse(
            "// filler filler filler filler filler",
            "<Tree>",
            "  <TreeItem wbp:name='item_1' text='TreeItem 1'/>",
            "  <TreeItem wbp:name='item_2' text='TreeItem 2'/>",
            "</Tree>");
    refresh();
    TreeItemInfo item_1 = getObjectByName("item_1");
    TreeItemInfo item_2 = getObjectByName("item_2");
    // do move
    flowContainer_MOVE(item_1, item_2, null);
    assertThat(tree.getItems()).containsExactly(item_1);
    assertThat(item_1.getItems()).containsExactly(item_2);
    assertThat(item_2.getItems()).isEmpty();
    assertXML(
        "// filler filler filler filler filler",
        "<Tree>",
        "  <TreeItem wbp:name='item_1' text='TreeItem 1' expanded='true'>",
        "    <TreeItem wbp:name='item_2' text='TreeItem 2'/>",
        "  </TreeItem>",
        "</Tree>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Columns
  //
  ////////////////////////////////////////////////////////////////////////////
  private static int BORDER_WIDTH = 1;

  /**
   * Test for parsing {@link TreeColumn} and bounds of {@link TreeColumnInfo}.
   */
  public void test_TreeColumn() throws Exception {
    TreeInfo tree =
        parse(
            "// filler filler filler filler filler",
            "<Tree headerVisible='true'>",
            "  <TreeColumn wbp:name='column_1' width='50'/>",
            "  <TreeColumn wbp:name='column_2' width='100'/>",
            "</Tree>");
    refresh();
    TreeColumnInfo column_1 = getObjectByName("column_1");
    TreeColumnInfo column_2 = getObjectByName("column_2");
    // check Tree columns
    assertThat(tree.getColumns()).containsExactly(column_1, column_2);
    // check bounds
    {
      // "model" bounds
      Rectangle modelBounds = column_1.getModelBounds();
      assertNotNull(modelBounds);
      assertEquals(BORDER_WIDTH, modelBounds.x);
      assertEquals(BORDER_WIDTH, modelBounds.y);
      assertEquals(50, modelBounds.width);
      assertThat(modelBounds.height).isGreaterThan(15).isLessThan(25);
      // "shot" bounds
      Rectangle bounds = column_1.getBounds();
      assertEquals(BORDER_WIDTH, bounds.x);
      assertEquals(BORDER_WIDTH, bounds.y);
      assertEquals(modelBounds.width, bounds.width);
      assertEquals(modelBounds.height, bounds.height);
    }
    {
      // "model" bounds
      Rectangle modelBounds = column_2.getModelBounds();
      assertNotNull(modelBounds);
      assertEquals(BORDER_WIDTH + 50, modelBounds.x);
      assertEquals(BORDER_WIDTH, modelBounds.y);
    }
  }

  /**
   * Test for copy/paste {@link TreeColumn}.
   */
  public void test_TreeColumn_copyPaste() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Tree wbp:name='tree' headerVisible='true'>",
            "    <TreeColumn width='50'/>",
            "    <TreeColumn width='100'/>",
            "  </Tree>",
            "</Shell>");
    refresh();
    // prepare memento
    XmlObjectMemento memento;
    {
      TreeInfo tree = getObjectByName("tree");
      memento = XmlObjectMemento.createMemento(tree);
    }
    // do paste
    {
      TreeInfo newTree = (TreeInfo) memento.create(shell);
      shell.getLayout().command_CREATE(newTree, null);
      memento.apply();
    }
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Tree wbp:name='tree' headerVisible='true'>",
        "    <TreeColumn width='50'/>",
        "    <TreeColumn width='100'/>",
        "  </Tree>",
        "  <Tree headerVisible='true'>",
        "    <TreeColumn width='50'/>",
        "    <TreeColumn width='100'/>",
        "  </Tree>",
        "</Shell>");
  }

  public void test_TreeColumn_setWidth() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Tree headerVisible='true'>",
        "  <TreeColumn wbp:name='column' width='100'/>",
        "</Tree>");
    refresh();
    TreeColumnInfo column = getObjectByName("column");
    Property widthProperty = column.getPropertyByTitle("width");
    // check initial width
    assertEquals(100, widthProperty.getValue());
    // set new width
    column.setWidth(120);
    assertEquals(120, widthProperty.getValue());
    assertXML(
        "// filler filler filler filler filler",
        "<Tree headerVisible='true'>",
        "  <TreeColumn wbp:name='column' width='120'/>",
        "</Tree>");
  }

  public void test_add_TreeColumn() throws Exception {
    TreeInfo tree =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Tree headerVisible='true'/>");
    // no columns initially
    assertThat(tree.getColumns()).isEmpty();
    // add new TreeColumn
    TreeColumnInfo newColumn = createObject("org.eclipse.swt.widgets.TreeColumn");
    flowContainer_CREATE(tree, newColumn, null);
    assertThat(tree.getColumns()).containsExactly(newColumn);
    assertXML(
        "// filler filler filler filler filler",
        "<Tree headerVisible='true'>",
        "  <TreeColumn text='New Column' width='100'/>",
        "</Tree>");
  }

  public void test_move_TreeColumn() throws Exception {
    TreeInfo tree =
        parse(
            "// filler filler filler filler filler",
            "<Tree headerVisible='true'>",
            "  <TreeColumn wbp:name='column_1' width='50'/>",
            "  <TreeColumn wbp:name='column_2' width='100'/>",
            "</Tree>");
    TreeColumnInfo column_1 = getObjectByName("column_1");
    TreeColumnInfo column_2 = getObjectByName("column_2");
    // move column
    flowContainer_MOVE(tree, column_2, column_1);
    assertThat(tree.getColumns()).containsExactly(column_2, column_1);
    assertXML(
        "// filler filler filler filler filler",
        "<Tree headerVisible='true'>",
        "  <TreeColumn wbp:name='column_2' width='100'/>",
        "  <TreeColumn wbp:name='column_1' width='50'/>",
        "</Tree>");
  }
}