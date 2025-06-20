/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.TreeInfo;
import org.eclipse.wb.internal.swt.model.widgets.TreeItemInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.TreeItem;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Tests for {@link TreeInfo} and {@link TreeItemInfo}.
 *
 * @author scheglov_ke
 */
public class TreeTest extends RcpModelTest {
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
	@Test
	public void test_TreeItem_parse() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    Tree tree = new Tree(this, SWT.BORDER);",
						"    {",
						"      TreeItem item_1 = new TreeItem(tree, SWT.NONE);",
						"      item_1.setText('TreeItem 1');",
						"    }",
						"    {",
						"      TreeItem item_2 = new TreeItem(tree, SWT.NONE);",
						"      item_2.setText('TreeItem 2');",
						"    }",
						"  }",
						"}");
		shell.refresh();
		TreeInfo tree = getJavaInfoByName("tree");
		// prepare items
		List<TreeItemInfo> items = tree.getItems();
		Assertions.assertThat(items).hasSize(2);
		TreeItemInfo item_1 = items.get(0);
		TreeItemInfo item_2 = items.get(1);
		// no sub-items
		Assertions.assertThat(item_1.getItems()).isEmpty();
		Assertions.assertThat(item_2.getItems()).isEmpty();
		// check bounds
		Insets tableInsets = tree.getClientAreaInsets();
		{
			// "model" bounds
			Rectangle modelBounds = item_1.getModelBounds();
			{
				assertNotNull(modelBounds);
				assertTrue(modelBounds.x > 10); // some space for tree line
				Assertions.assertThat(modelBounds.y).isGreaterThanOrEqualTo(0); // platform-specific tolerance
				Assertions.assertThat(modelBounds.y).isLessThanOrEqualTo(5);
				assertTrue(modelBounds.width > 50);
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
			Assertions.assertThat(item_1.getModelBounds().bottom()).isGreaterThanOrEqualTo(modelBounds.y - 5); // platform-specific tolerance
			Assertions.assertThat(item_1.getModelBounds().bottom()).isLessThanOrEqualTo(modelBounds.y);
		}
	}

	/**
	 * Test for parsing {@link TreeItemInfo} with {@link TreeItemInfo} child.
	 */
	@Test
	public void test_TreeItem_parse_subItems() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    Tree tree = new Tree(this, SWT.BORDER);",
						"    {",
						"      TreeItem item = new TreeItem(tree, SWT.NONE);",
						"      {",
						"        TreeItem subItem = new TreeItem(item, SWT.NONE);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		TreeInfo tree = getJavaInfoByName("tree");
		// prepare "item"
		TreeItemInfo item;
		{
			List<TreeItemInfo> items = tree.getItems();
			Assertions.assertThat(items).hasSize(1);
			item = items.get(0);
		}
		// check for "subItem"
		List<TreeItemInfo> subItems = item.getItems();
		Assertions.assertThat(subItems).hasSize(1);
	}

	@Test
	public void test_TreeItem_addToTable() throws Exception {
		parseComposite(
				"class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    Tree tree = new Tree(this, SWT.BORDER);",
				"  }",
				"}");
		TreeInfo tree = getJavaInfoByName("tree");
		// no items initially
		assertTrue(tree.getItems().isEmpty());
		// add new TreeItem
		TreeItemInfo newItem = createJavaInfo("org.eclipse.swt.widgets.TreeItem");
		flowContainer_CREATE(tree, newItem, null);
		// check result
		List<TreeItemInfo> items = tree.getItems();
		Assertions.assertThat(items).containsExactly(newItem);
		assertEditor(
				"class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    Tree tree = new Tree(this, SWT.BORDER);",
				"    {",
				"      TreeItem treeItem = new TreeItem(tree, SWT.NONE);",
				"      treeItem.setText('New TreeItem');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_TreeItem_moveInTable() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    Tree tree = new Tree(this, SWT.BORDER);",
						"    {",
						"      TreeItem item_1 = new TreeItem(tree, SWT.NONE);",
						"      item_1.setText('TreeItem 1');",
						"    }",
						"    {",
						"      TreeItem item_2 = new TreeItem(tree, SWT.NONE);",
						"      item_2.setText('TreeItem 2');",
						"    }",
						"  }",
						"}");
		shell.refresh();
		TreeInfo tree = getJavaInfoByName("tree");
		// prepare items
		TreeItemInfo item_1;
		TreeItemInfo item_2;
		{
			List<TreeItemInfo> items = tree.getItems();
			Assertions.assertThat(items).hasSize(2);
			item_1 = items.get(0);
			item_2 = items.get(1);
		}
		// do move
		flowContainer_MOVE(tree, item_2, item_1);
		// check result
		List<TreeItemInfo> items = tree.getItems();
		Assertions.assertThat(items).containsExactly(item_2, item_1);
		assertEditor(
				"class Test extends Shell {",
				"  public Test() {",
				"    Tree tree = new Tree(this, SWT.BORDER);",
				"    {",
				"      TreeItem item_2 = new TreeItem(tree, SWT.NONE);",
				"      item_2.setText('TreeItem 2');",
				"    }",
				"    {",
				"      TreeItem item_1 = new TreeItem(tree, SWT.NONE);",
				"      item_1.setText('TreeItem 1');",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for adding new {@link TreeItemInfo} on existing {@link TreeItemInfo}.
	 */
	@Test
	public void test_TreeItem_addToItem() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    Tree tree = new Tree(this, SWT.BORDER);",
						"    {",
						"      TreeItem existingItem = new TreeItem(tree, SWT.NONE);",
						"      existingItem.setText('existing TreeItem');",
						"    }",
						"  }",
						"}");
		shell.refresh();
		TreeInfo tree = (TreeInfo) shell.getChildrenControls().get(0);
		// prepare existing item
		TreeItemInfo existingItem = tree.getItems().get(0);
		// add new TreeItem
		TreeItemInfo newItem = createJavaInfo("org.eclipse.swt.widgets.TreeItem");
		flowContainer_CREATE(existingItem, newItem, null);
		// check result
		{
			List<TreeItemInfo> items = existingItem.getItems();
			Assertions.assertThat(items).containsExactly(newItem);
		}
		assertEditor(
				"class Test extends Shell {",
				"  public Test() {",
				"    Tree tree = new Tree(this, SWT.BORDER);",
				"    {",
				"      TreeItem existingItem = new TreeItem(tree, SWT.NONE);",
				"      existingItem.setText('existing TreeItem');",
				"      {",
				"        TreeItem treeItem = new TreeItem(existingItem, SWT.NONE);",
				"        treeItem.setText('New TreeItem');",
				"      }",
				"      existingItem.setExpanded(true);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for moving {@link TreeItemInfo} on other {@link TreeItemInfo}.
	 */
	@Test
	public void test_TreeItem_moveToItem() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    Tree tree = new Tree(this, SWT.BORDER);",
						"    {",
						"      TreeItem item_1 = new TreeItem(tree, SWT.NONE);",
						"      item_1.setText('TreeItem 1');",
						"    }",
						"    {",
						"      TreeItem item_2 = new TreeItem(tree, SWT.NONE);",
						"      item_2.setText('TreeItem 2');",
						"    }",
						"  }",
						"}");
		shell.refresh();
		TreeInfo tree = getJavaInfoByName("tree");
		// prepare items
		TreeItemInfo item_1;
		TreeItemInfo item_2;
		{
			List<TreeItemInfo> items = tree.getItems();
			Assertions.assertThat(items).hasSize(2);
			item_1 = items.get(0);
			item_2 = items.get(1);
		}
		// do move
		try {
			tree.startEdit();
			flowContainer_MOVE(item_1, item_2, null);
		} finally {
			tree.endEdit();
		}
		// check result
		{
			List<TreeItemInfo> items = tree.getItems();
			Assertions.assertThat(items).containsExactly(item_1);
		}
		{
			List<TreeItemInfo> items = item_1.getItems();
			Assertions.assertThat(items).containsExactly(item_2);
		}
		Assertions.assertThat(item_2.getItems()).isEmpty();
		assertEditor(
				"class Test extends Shell {",
				"  public Test() {",
				"    Tree tree = new Tree(this, SWT.BORDER);",
				"    {",
				"      TreeItem item_1 = new TreeItem(tree, SWT.NONE);",
				"      item_1.setText('TreeItem 1');",
				"      {",
				"        TreeItem item_2 = new TreeItem(item_1, SWT.NONE);",
				"        item_2.setText('TreeItem 2');",
				"      }",
				"      item_1.setExpanded(true);",
				"    }",
				"  }",
				"}");
	}
}