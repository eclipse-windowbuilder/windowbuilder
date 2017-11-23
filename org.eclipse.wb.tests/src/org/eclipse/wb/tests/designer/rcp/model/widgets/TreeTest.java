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

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.rcp.model.widgets.TreeColumnInfo;
import org.eclipse.wb.internal.rcp.model.widgets.TreeInfo;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for "big" SWT {@link Tree}.
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
  // Columns
  //
  ////////////////////////////////////////////////////////////////////////////
  private static int BORDER_WIDTH = 1;

  /**
   * Test for parsing {@link TreeColumn} and bounds of {@link TreeColumnInfo}.
   */
  public void test_TreeColumn() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    Tree tree = new Tree(this, SWT.BORDER);",
            "    tree.setHeaderVisible(true);",
            "    {",
            "      TreeColumn treeColumn_1 = new TreeColumn(tree, SWT.NONE);",
            "      treeColumn_1.setWidth(50);",
            "    }",
            "    {",
            "      TreeColumn treeColumn_2 = new TreeColumn(tree, SWT.NONE);",
            "      treeColumn_2.setWidth(100);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TreeInfo tree = (TreeInfo) shell.getChildrenControls().get(0);
    // prepare columns
    List<TreeColumnInfo> columns = tree.getColumns();
    assertThat(columns).hasSize(2);
    TreeColumnInfo column_1 = columns.get(0);
    TreeColumnInfo column_2 = columns.get(1);
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
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Tree tree = new Tree(this, SWT.BORDER);",
            "      tree.setHeaderVisible(true);",
            "      {",
            "        TreeColumn treeColumn_1 = new TreeColumn(tree, SWT.NONE);",
            "        treeColumn_1.setWidth(50);",
            "      }",
            "      {",
            "        TreeColumn treeColumn_2 = new TreeColumn(tree, SWT.RIGHT);",
            "        treeColumn_2.setWidth(100);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
    // prepare memento
    JavaInfoMemento memento;
    {
      TreeInfo tree = getJavaInfoByName("tree");
      memento = JavaInfoMemento.createMemento(tree);
    }
    // do paste
    {
      TreeInfo newTree = (TreeInfo) memento.create(shell);
      fillLayout.command_CREATE(newTree, null);
      memento.apply();
    }
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Tree tree = new Tree(this, SWT.BORDER);",
        "      tree.setHeaderVisible(true);",
        "      {",
        "        TreeColumn treeColumn_1 = new TreeColumn(tree, SWT.NONE);",
        "        treeColumn_1.setWidth(50);",
        "      }",
        "      {",
        "        TreeColumn treeColumn_2 = new TreeColumn(tree, SWT.RIGHT);",
        "        treeColumn_2.setWidth(100);",
        "      }",
        "    }",
        "    {",
        "      Tree tree = new Tree(this, SWT.BORDER);",
        "      tree.setHeaderVisible(true);",
        "      {",
        "        TreeColumn treeColumn = new TreeColumn(tree, SWT.NONE);",
        "        treeColumn.setWidth(50);",
        "      }",
        "      {",
        "        TreeColumn treeColumn = new TreeColumn(tree, SWT.RIGHT);",
        "        treeColumn.setWidth(100);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_TreeColumn_setWidth() throws Exception {
    parseComposite(
        "class Test extends Shell {",
        "  public Test() {",
        "    Tree tree = new Tree(this, SWT.BORDER);",
        "    {",
        "      TreeColumn treeColumn = new TreeColumn(tree, SWT.NONE);",
        "      treeColumn.setWidth(100);",
        "    }",
        "  }",
        "}");
    refresh();
    TreeInfo tree = getJavaInfoByName("tree");
    //
    TreeColumnInfo column = tree.getColumns().get(0);
    Property widthProperty = column.getPropertyByTitle("width");
    // check initial width
    assertEquals(100, widthProperty.getValue());
    // set new width
    column.setWidth(120);
    assertEquals(120, widthProperty.getValue());
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    Tree tree = new Tree(this, SWT.BORDER);",
        "    {",
        "      TreeColumn treeColumn = new TreeColumn(tree, SWT.NONE);",
        "      treeColumn.setWidth(120);",
        "    }",
        "  }",
        "}");
  }

  public void test_add_TreeColumn() throws Exception {
    parseComposite(
        "class Test extends Shell {",
        "  public Test() {",
        "    Tree tree = new Tree(this, SWT.BORDER);",
        "  }",
        "}");
    TreeInfo tree = getJavaInfoByName("tree");
    // no columns initially
    assertTrue(tree.getColumns().isEmpty());
    // add new TreeColumn
    TreeColumnInfo newColumn = createJavaInfo("org.eclipse.swt.widgets.TreeColumn");
    flowContainer_CREATE(tree, newColumn, null);
    // check result
    List<TreeColumnInfo> columns = tree.getColumns();
    assertThat(columns).hasSize(1);
    assertTrue(columns.contains(newColumn));
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    Tree tree = new Tree(this, SWT.BORDER);",
        "    {",
        "      TreeColumn treeColumn = new TreeColumn(tree, SWT.NONE);",
        "      treeColumn.setWidth(100);",
        "      treeColumn.setText('New Column');",
        "    }",
        "  }",
        "}");
  }

  public void test_move_TreeColumn() throws Exception {
    parseComposite(
        "class Test extends Shell {",
        "  public Test() {",
        "    Tree tree = new Tree(this, SWT.BORDER);",
        "    {",
        "      TreeColumn treeColumn_1 = new TreeColumn(tree, SWT.NONE);",
        "      treeColumn_1.setText('Column 1');",
        "    }",
        "    {",
        "      TreeColumn treeColumn_2 = new TreeColumn(tree, SWT.NONE);",
        "      treeColumn_2.setText('Column 2');",
        "    }",
        "  }",
        "}");
    TreeInfo tree = getJavaInfoByName("tree");
    // prepare columns
    List<TreeColumnInfo> columns = tree.getColumns();
    assertThat(columns).hasSize(2);
    TreeColumnInfo column_1 = columns.get(0);
    TreeColumnInfo column_2 = columns.get(1);
    // move column
    flowContainer_MOVE(tree, column_2, column_1);
    // check result
    assertSame(column_2, tree.getColumns().get(0));
    assertSame(column_1, tree.getColumns().get(1));
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    Tree tree = new Tree(this, SWT.BORDER);",
        "    {",
        "      TreeColumn treeColumn_2 = new TreeColumn(tree, SWT.NONE);",
        "      treeColumn_2.setText('Column 2');",
        "    }",
        "    {",
        "      TreeColumn treeColumn_1 = new TreeColumn(tree, SWT.NONE);",
        "      treeColumn_1.setText('Column 1');",
        "    }",
        "  }",
        "}");
  }

  public void test_reparent_TreeColumn() throws Exception {
    parseComposite(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    //",
        "    Tree tree_1 = new Tree(this, SWT.BORDER);",
        "    {",
        "      TreeColumn treeColumn_1 = new TreeColumn(tree_1, SWT.NONE);",
        "      treeColumn_1.setText('Column 1');",
        "    }",
        "    //",
        "    Tree tree_2 = new Tree(this, SWT.BORDER);",
        "    {",
        "      TreeColumn treeColumn_2 = new TreeColumn(tree_2, SWT.NONE);",
        "      treeColumn_2.setText('Column 2');",
        "    }",
        "  }",
        "}");
    // prepare tree_1
    TreeInfo tree_1 = getJavaInfoByName("tree_1");
    TreeColumnInfo column_1 = tree_1.getColumns().get(0);
    // prepare tree_2
    TreeInfo tree_2 = getJavaInfoByName("tree_2");
    TreeColumnInfo column_2 = tree_2.getColumns().get(0);
    // move column_1 before column_2
    flowContainer_MOVE(tree_2, column_1, column_2);
    // check result
    {
      List<TreeColumnInfo> columns = tree_2.getColumns();
      assertThat(columns).hasSize(2);
      assertSame(column_1, columns.get(0));
      assertSame(column_2, columns.get(1));
    }
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    //",
        "    Tree tree_1 = new Tree(this, SWT.BORDER);",
        "    //",
        "    Tree tree_2 = new Tree(this, SWT.BORDER);",
        "    {",
        "      TreeColumn treeColumn_1 = new TreeColumn(tree_2, SWT.NONE);",
        "      treeColumn_1.setText('Column 1');",
        "    }",
        "    {",
        "      TreeColumn treeColumn_2 = new TreeColumn(tree_2, SWT.NONE);",
        "      treeColumn_2.setText('Column 2');",
        "    }",
        "  }",
        "}");
  }

  public void test_column_exposed() throws Exception {
    setFileContentSrc(
        "test/ExposedComposite.java",
        getTestSource(
            "public class ExposedComposite extends Composite {",
            "  private TreeColumn m_treeColumn;",
            "  private Tree m_tree;",
            "  //",
            "  public ExposedComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout(2, false));",
            "    m_tree = new Tree(this, SWT.BORDER);",
            "    {",
            "      m_treeColumn = new TreeColumn(m_tree, SWT.NONE);",
            "      m_treeColumn.setWidth(100);",
            "      m_treeColumn.setText('New Column');",
            "    }",
            "  }",
            "  public TreeColumn getColumn() {",
            "    return m_treeColumn;",
            "  }",
            "  public Tree getTree() {",
            "    return m_tree;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseComposite(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    ExposedComposite composite = new ExposedComposite(this, SWT.NONE);",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new ExposedComposite(this, SWT.NONE)/}",
        "  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
        "  {new: test.ExposedComposite} {local-unique: composite} {/new ExposedComposite(this, SWT.NONE)/}",
        "    {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}",
        "    {method: public org.eclipse.swt.widgets.Tree test.ExposedComposite.getTree()} {property} {}",
        "      {method: public org.eclipse.swt.widgets.TreeColumn test.ExposedComposite.getColumn()} {property} {}",
        "      {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}");
  }

  public void test_item_exposed() throws Exception {
    setFileContentSrc(
        "test/ExposedComposite.java",
        getTestSource(
            "public class ExposedComposite extends Composite {",
            "  private TreeItem m_treeItem;",
            "  private Tree m_tree;",
            "  //",
            "  public ExposedComposite(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new GridLayout(2, false));",
            "    m_tree = new Tree(this, SWT.BORDER);",
            "    {",
            "      TreeColumn treeColumn = new TreeColumn(m_tree, SWT.NONE);",
            "      treeColumn.setWidth(100);",
            "      treeColumn.setText('New Column');",
            "    }",
            "    {",
            "      m_treeItem = new TreeItem(m_tree, SWT.NONE);",
            "      m_treeItem.setText('New Item');",
            "    }",
            "  }",
            "  public TreeItem getItem() {",
            "    return m_treeItem;",
            "  }",
            "  public Tree getTree() {",
            "    return m_tree;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseComposite(
        "class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    ExposedComposite composite = new ExposedComposite(this, SWT.NONE);",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new ExposedComposite(this, SWT.NONE)/}",
        "  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
        "  {new: test.ExposedComposite} {local-unique: composite} {/new ExposedComposite(this, SWT.NONE)/}",
        "    {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}",
        "    {method: public org.eclipse.swt.widgets.Tree test.ExposedComposite.getTree()} {property} {}",
        "      {method: public org.eclipse.swt.widgets.TreeItem test.ExposedComposite.getItem()} {property} {}",
        "      {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}");
  }
}