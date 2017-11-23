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
package org.eclipse.wb.tests.designer.rcp.nebula;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.nebula.ctabletree.CContainerColumnInfo;
import org.eclipse.wb.internal.rcp.nebula.ctabletree.CTableTreeInfo;
import org.eclipse.wb.internal.rcp.nebula.ctabletree.CTableTreeItemInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test {@link CTableTree} items models.
 * 
 * @author sablin_aa
 */
public class CTableTreeTest extends AbstractNebulaTest {
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
   * General test for {@link CContainerColumnInfo} && {@link CTableTreeItemInfo}.
   */
  public void test_General() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.swt.nebula.widgets.ctabletree.*;",
            "import org.eclipse.swt.nebula.widgets.ctabletree.ccontainer.*;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    CTableTree tableTree = new CTableTree(this, SWT.NONE);",
            "    {",
            "      CContainerColumn column1 = new CContainerColumn(tableTree, SWT.NONE);",
            "      column1.setText('Column 1');",
            "      column1.setWidth(200);",
            "    }",
            "    {",
            "      CTableTreeItem item1 = new CTableTreeItem(tableTree, SWT.NONE);",
            "      item1.setExpanded(true);",
            "      item1.setText('row 1');",
            "      {",
            "        CTableTreeItem item11 = new CTableTreeItem(item1, SWT.NONE);",
            "        item11.setText('row 1 - 1');",
            "      }",
            "    }",
            "    tableTree.setTreeColumn(0);",
            "    tableTree.setHeaderVisible(true);",
            "  }",
            "}");
    // refresh() also should be successful
    shell.refresh();
    // info
    CompositeInfo table = shell.getChildren(CompositeInfo.class).get(0);
    assertEquals(2, table.getChildren().size());
    assertEquals(1, table.getChildren(CContainerColumnInfo.class).size());
    assertEquals(1, table.getChildren(CTableTreeItemInfo.class).size());
    // column
    {
      CContainerColumnInfo column = table.getChildren(CContainerColumnInfo.class).get(0);
      Rectangle bounds = column.getBounds();
      assertThat(bounds.width).isEqualTo(200);
      assertThat(bounds.height).isEqualTo(
          (Integer) ReflectionUtils.invokeMethod(table.getObject(), "getHeaderHeight()"));
    }
    // row
    {
      CTableTreeItemInfo row = table.getChildren(CTableTreeItemInfo.class).get(0);
      Rectangle bounds = row.getBounds();
      assertThat(bounds.width).isGreaterThan(200);
      assertThat(bounds.height).isGreaterThan(15);
    }
  }

  /**
   * Test collapsed {@link CTableTreeItemInfo}.
   */
  public void test_Expanded() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.swt.nebula.widgets.ctabletree.*;",
            "import org.eclipse.swt.nebula.widgets.ctabletree.ccontainer.*;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    CTableTree tableTree = new CTableTree(this, SWT.NONE);",
            "    {",
            "      CContainerColumn column1 = new CContainerColumn(tableTree, SWT.NONE);",
            "      column1.setText('Column 1');",
            "      column1.setWidth(200);",
            "    }",
            "    tableTree.setTreeColumn(0);",
            "    {",
            "      CTableTreeItem item1 = new CTableTreeItem(tableTree, SWT.NONE);",
            "      item1.setExpanded(false);",
            "      item1.setText('row 1');",
            "      {",
            "        CTableTreeItem item11 = new CTableTreeItem(item1, SWT.NONE);",
            "        item11.setText('row 1 - 1');",
            "      }",
            "    }",
            "  }",
            "}");
    // refresh() also should be successful
    shell.refresh();
    // info
    CompositeInfo table = shell.getChildren(CompositeInfo.class).get(0);
    // row
    CTableTreeItemInfo row = table.getChildren(CTableTreeItemInfo.class).get(0);
    {
      Rectangle bounds = row.getBounds();
      assertThat(bounds.width).isGreaterThan(200);
      assertThat(bounds.height).isGreaterThanOrEqualTo(15);
    }
    // subrow
    {
      CTableTreeItemInfo subRow = row.getChildren(CTableTreeItemInfo.class).get(0);
      Rectangle bounds = subRow.getBounds();
      assertThat(bounds.width).isGreaterThan(200);
      assertThat(bounds.height).isEqualTo(0);
    }
  }

  /**
   * Test for {@link CContainerColumn} adding when exists {@link CTableTreeItem}'s (expression must
   * be placed directly before first {@link CTableTreeItem}).
   */
  public void test_addColumn_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.swt.nebula.widgets.ctabletree.*;",
            "import org.eclipse.swt.nebula.widgets.ctabletree.ccontainer.*;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    CTableTree tableTree = new CTableTree(this, SWT.NONE);",
            "    {",
            "      CContainerColumn column1 = new CContainerColumn(tableTree, SWT.NONE);",
            "      column1.setText('Column 1');",
            "      column1.setWidth(200);",
            "    }",
            "    {",
            "      CTableTreeItem item1 = new CTableTreeItem(tableTree, SWT.NONE);",
            "      item1.setExpanded(true);",
            "      item1.setText('row 1');",
            "      {",
            "        CTableTreeItem item11 = new CTableTreeItem(item1, SWT.NONE);",
            "        item11.setText('row 1 - 1');",
            "      }",
            "    }",
            "  }",
            "}");
    // refresh() also should be successful
    shell.refresh();
    // info
    CTableTreeInfo tree = shell.getChildren(CTableTreeInfo.class).get(0);
    {
      // check setHeaderVisible(boolean) as last
      Property property = tree.getPropertyByTitle("headerVisible");
      property.setValue(true);
      assertEditor(
          "import org.eclipse.swt.nebula.widgets.ctabletree.*;",
          "import org.eclipse.swt.nebula.widgets.ctabletree.ccontainer.*;",
          "public class Test extends Shell {",
          "  public Test() {",
          "    setLayout(new FillLayout());",
          "    CTableTree tableTree = new CTableTree(this, SWT.NONE);",
          "    {",
          "      CContainerColumn column1 = new CContainerColumn(tableTree, SWT.NONE);",
          "      column1.setText('Column 1');",
          "      column1.setWidth(200);",
          "    }",
          "    {",
          "      CTableTreeItem item1 = new CTableTreeItem(tableTree, SWT.NONE);",
          "      item1.setExpanded(true);",
          "      item1.setText('row 1');",
          "      {",
          "        CTableTreeItem item11 = new CTableTreeItem(item1, SWT.NONE);",
          "        item11.setText('row 1 - 1');",
          "      }",
          "    }",
          "    tableTree.setHeaderVisible(true);",
          "  }",
          "}");
    }
    {
      // create new column
      CContainerColumnInfo column =
          (CContainerColumnInfo) JavaInfoUtils.createJavaInfo(
              tree.getEditor(),
              "org.eclipse.swt.nebula.widgets.ctabletree.ccontainer.CContainerColumn",
              new ConstructorCreationSupport());
      JavaInfoUtils.add(column, null, tree, null);
      assertEditor(
          "import org.eclipse.swt.nebula.widgets.ctabletree.*;",
          "import org.eclipse.swt.nebula.widgets.ctabletree.ccontainer.*;",
          "public class Test extends Shell {",
          "  public Test() {",
          "    setLayout(new FillLayout());",
          "    CTableTree tableTree = new CTableTree(this, SWT.NONE);",
          "    {",
          "      CContainerColumn column1 = new CContainerColumn(tableTree, SWT.NONE);",
          "      column1.setText('Column 1');",
          "      column1.setWidth(200);",
          "    }",
          "    {",
          "      CContainerColumn containerColumn = new CContainerColumn(tableTree, SWT.NONE);",
          "      containerColumn.setText('New Column');",
          "      containerColumn.setWidth(150);",
          "    }",
          "    {",
          "      CTableTreeItem item1 = new CTableTreeItem(tableTree, SWT.NONE);",
          "      item1.setExpanded(true);",
          "      item1.setText('row 1');",
          "      {",
          "        CTableTreeItem item11 = new CTableTreeItem(item1, SWT.NONE);",
          "        item11.setText('row 1 - 1');",
          "      }",
          "    }",
          "    tableTree.setHeaderVisible(true);",
          "  }",
          "}");
    }
  }

  /**
   * Test for {@link CContainerColumn} adding when exists {@link CTableTreeItem}'s (expression must
   * be placed directly before {@link CTableTreeItem#setTreeColumn(int)} invocation ).
   */
  public void test_addColumn_2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.swt.nebula.widgets.ctabletree.*;",
            "import org.eclipse.swt.nebula.widgets.ctabletree.ccontainer.*;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    CTableTree tableTree = new CTableTree(this, SWT.NONE);",
            "    {",
            "      CContainerColumn column1 = new CContainerColumn(tableTree, SWT.NONE);",
            "      column1.setText('Column 1');",
            "      column1.setWidth(200);",
            "    }",
            "    {",
            "      CTableTreeItem item1 = new CTableTreeItem(tableTree, SWT.NONE);",
            "      item1.setExpanded(true);",
            "      item1.setText('row 1');",
            "      {",
            "        CTableTreeItem item11 = new CTableTreeItem(item1, SWT.NONE);",
            "        item11.setText('row 1 - 1');",
            "      }",
            "    }",
            "  }",
            "}");
    // refresh() also should be successful
    shell.refresh();
    // info
    CTableTreeInfo tree = shell.getChildren(CTableTreeInfo.class).get(0);
    {
      // check setTreeColumn(int) in special location
      Property property = tree.getPropertyByTitle("treeColumn");
      property.setValue(0);
      assertEditor(
          "import org.eclipse.swt.nebula.widgets.ctabletree.*;",
          "import org.eclipse.swt.nebula.widgets.ctabletree.ccontainer.*;",
          "public class Test extends Shell {",
          "  public Test() {",
          "    setLayout(new FillLayout());",
          "    CTableTree tableTree = new CTableTree(this, SWT.NONE);",
          "    {",
          "      CContainerColumn column1 = new CContainerColumn(tableTree, SWT.NONE);",
          "      column1.setText('Column 1');",
          "      column1.setWidth(200);",
          "    }",
          "    tableTree.setTreeColumn(0);",
          "    {",
          "      CTableTreeItem item1 = new CTableTreeItem(tableTree, SWT.NONE);",
          "      item1.setExpanded(true);",
          "      item1.setText('row 1');",
          "      {",
          "        CTableTreeItem item11 = new CTableTreeItem(item1, SWT.NONE);",
          "        item11.setText('row 1 - 1');",
          "      }",
          "    }",
          "  }",
          "}");
    }
    {
      // create new column
      CContainerColumnInfo column =
          (CContainerColumnInfo) JavaInfoUtils.createJavaInfo(
              tree.getEditor(),
              "org.eclipse.swt.nebula.widgets.ctabletree.ccontainer.CContainerColumn",
              new ConstructorCreationSupport());
      JavaInfoUtils.add(column, null, tree, null);
      assertEditor(
          "import org.eclipse.swt.nebula.widgets.ctabletree.*;",
          "import org.eclipse.swt.nebula.widgets.ctabletree.ccontainer.*;",
          "public class Test extends Shell {",
          "  public Test() {",
          "    setLayout(new FillLayout());",
          "    CTableTree tableTree = new CTableTree(this, SWT.NONE);",
          "    {",
          "      CContainerColumn column1 = new CContainerColumn(tableTree, SWT.NONE);",
          "      column1.setText('Column 1');",
          "      column1.setWidth(200);",
          "    }",
          "    {",
          "      CContainerColumn containerColumn = new CContainerColumn(tableTree, SWT.NONE);",
          "      containerColumn.setText('New Column');",
          "      containerColumn.setWidth(150);",
          "    }",
          "    tableTree.setTreeColumn(0);",
          "    {",
          "      CTableTreeItem item1 = new CTableTreeItem(tableTree, SWT.NONE);",
          "      item1.setExpanded(true);",
          "      item1.setText('row 1');",
          "      {",
          "        CTableTreeItem item11 = new CTableTreeItem(item1, SWT.NONE);",
          "        item11.setText('row 1 - 1');",
          "      }",
          "    }",
          "  }",
          "}");
    }
  }
}