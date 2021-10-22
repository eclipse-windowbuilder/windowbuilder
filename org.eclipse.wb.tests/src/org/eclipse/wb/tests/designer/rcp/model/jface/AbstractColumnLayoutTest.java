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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.core.model.association.InvocationSecondaryAssociation;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.rcp.model.jface.layout.AbstractColumnLayoutInfo;
import org.eclipse.wb.internal.rcp.model.jface.layout.ColumnLayoutDataInfo;
import org.eclipse.wb.internal.rcp.model.jface.layout.ColumnPixelDataInfo;
import org.eclipse.wb.internal.rcp.model.jface.layout.ColumnWeightDataInfo;
import org.eclipse.wb.internal.rcp.model.jface.layout.TableColumnLayoutInfo;
import org.eclipse.wb.internal.rcp.model.jface.layout.TreeColumnLayoutInfo;
import org.eclipse.wb.internal.rcp.model.widgets.TableInfo;
import org.eclipse.wb.internal.rcp.model.widgets.TreeColumnInfo;
import org.eclipse.wb.internal.rcp.model.widgets.TreeInfo;
import org.eclipse.wb.internal.rcp.palette.TableCompositeEntryInfo;
import org.eclipse.wb.internal.rcp.palette.TableViewerCompositeEntryInfo;
import org.eclipse.wb.internal.rcp.palette.TreeCompositeEntryInfo;
import org.eclipse.wb.internal.rcp.palette.TreeViewerCompositeEntryInfo;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;
import org.eclipse.wb.internal.swt.model.widgets.TableColumnInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

/**
 * Test for {@link AbstractColumnLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class AbstractColumnLayoutTest extends RcpModelTest {
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
  public void test_parse_TableColumnLayout() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TableColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
            "    setLayout(tableColumnLayout);",
            "    Table table = new Table(this, SWT.NONE);",
            "    {",
            "      TableColumn column_1 = new TableColumn(table, SWT.NONE);",
            "      tableColumnLayout.setColumnData(column_1, new ColumnPixelData(150));",
            "    }",
            "    {",
            "      TableColumn column_2 = new TableColumn(table, SWT.NONE);",
            "      tableColumnLayout.setColumnData(column_2, new ColumnWeightData(1, 200));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableColumnLayoutInfo layout = (TableColumnLayoutInfo) shell.getLayout();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    // ColumnPixelData
    {
      TableColumnInfo column = table.getColumns().get(0);
      ColumnPixelDataInfo layoutData = (ColumnPixelDataInfo) layout.getLayoutData(column);
      // can delete layoutData and column
      {
        assertInstanceOf(InvocationSecondaryAssociation.class, layoutData.getAssociation());
        assertTrue(layoutData.canDelete());
        assertTrue(column.canDelete());
      }
      // "width" property
      assertEquals(150, layoutData.getPropertyByTitle("width").getValue());
      // LayoutData type property
      {
        Property typeProperty = column.getPropertyByTitle("LayoutDataType");
        assertTrue(typeProperty.isModified());
        assertEquals("ColumnPixelData", typeProperty.getValue());
      }
      // LayoutData properties
      {
        Property layoutDataProperty = column.getPropertyByTitle("LayoutData");
        Property[] subProperties = getSubProperties(layoutDataProperty);
        assertNull(getPropertyByTitle(subProperties, "Constructor"));
        assertNull(getPropertyByTitle(subProperties, "Class"));
        assertNotNull(getPropertyByTitle(subProperties, "width"));
        assertNotNull(getPropertyByTitle(subProperties, "resizable"));
        assertNotNull(getPropertyByTitle(subProperties, "addTrim"));
      }
    }
    // ColumnWeightData
    {
      TableColumnInfo column = table.getColumns().get(1);
      ColumnWeightDataInfo layoutData = (ColumnWeightDataInfo) layout.getLayoutData(column);
      assertEquals(1, layoutData.getPropertyByTitle("weight").getValue());
      assertEquals(200, layoutData.getPropertyByTitle("minimumWidth").getValue());
      // LayoutData type property
      {
        Property typeProperty = column.getPropertyByTitle("LayoutDataType");
        assertTrue(typeProperty.isModified());
        assertEquals("ColumnWeightData", typeProperty.getValue());
      }
      // LayoutData properties
      {
        Property layoutDataProperty = column.getPropertyByTitle("LayoutData");
        Property[] subProperties = getSubProperties(layoutDataProperty);
        assertNull(getPropertyByTitle(subProperties, "Constructor"));
        assertNull(getPropertyByTitle(subProperties, "Class"));
        assertNotNull(getPropertyByTitle(subProperties, "weight"));
        assertNotNull(getPropertyByTitle(subProperties, "minimumWidth"));
        assertNotNull(getPropertyByTitle(subProperties, "resizable"));
      }
    }
  }

  public void test_parse_TreeColumnLayout() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TreeColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    TreeColumnLayout treeColumnLayout = new TreeColumnLayout();",
            "    setLayout(treeColumnLayout);",
            "    Tree tree = new Tree(this, SWT.NONE);",
            "    {",
            "      TreeColumn column_1 = new TreeColumn(tree, SWT.NONE);",
            "      treeColumnLayout.setColumnData(column_1, new ColumnPixelData(150));",
            "    }",
            "    {",
            "      TreeColumn column_2 = new TreeColumn(tree, SWT.NONE);",
            "      treeColumnLayout.setColumnData(column_2, new ColumnWeightData(1, 200));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TreeColumnLayoutInfo layout = (TreeColumnLayoutInfo) shell.getLayout();
    TreeInfo table = (TreeInfo) shell.getChildrenControls().get(0);
    // ColumnPixelData
    {
      TreeColumnInfo column = table.getColumns().get(0);
      ColumnPixelDataInfo layoutData = (ColumnPixelDataInfo) layout.getLayoutData(column);
      assertEquals(150, layoutData.getPropertyByTitle("width").getValue());
      // LayoutData type property
      {
        Property typeProperty = column.getPropertyByTitle("LayoutDataType");
        assertTrue(typeProperty.isModified());
        assertEquals("ColumnPixelData", typeProperty.getValue());
      }
      // LayoutData properties
      {
        Property layoutDataProperty = column.getPropertyByTitle("LayoutData");
        Property[] subProperties = getSubProperties(layoutDataProperty);
        assertNull(getPropertyByTitle(subProperties, "Constructor"));
        assertNull(getPropertyByTitle(subProperties, "Class"));
        assertNotNull(getPropertyByTitle(subProperties, "width"));
        assertNotNull(getPropertyByTitle(subProperties, "resizable"));
        assertNotNull(getPropertyByTitle(subProperties, "addTrim"));
      }
    }
    // ColumnWeightData
    {
      TreeColumnInfo column = table.getColumns().get(1);
      ColumnWeightDataInfo layoutData = (ColumnWeightDataInfo) layout.getLayoutData(column);
      assertEquals(1, layoutData.getPropertyByTitle("weight").getValue());
      assertEquals(200, layoutData.getPropertyByTitle("minimumWidth").getValue());
      // LayoutData type property
      {
        Property typeProperty = column.getPropertyByTitle("LayoutDataType");
        assertTrue(typeProperty.isModified());
        assertEquals("ColumnWeightData", typeProperty.getValue());
      }
      // LayoutData properties
      {
        Property layoutDataProperty = column.getPropertyByTitle("LayoutData");
        Property[] subProperties = getSubProperties(layoutDataProperty);
        assertNull(getPropertyByTitle(subProperties, "Constructor"));
        assertNull(getPropertyByTitle(subProperties, "Class"));
        assertNotNull(getPropertyByTitle(subProperties, "weight"));
        assertNotNull(getPropertyByTitle(subProperties, "minimumWidth"));
        assertNotNull(getPropertyByTitle(subProperties, "resizable"));
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TableColumnLayout: no Table
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Users often try to drop {@link TableColumnLayout} on {@link Composite} and see exception. We
   * should prevent this.
   */
  public void test_parse_TableColumnLayout_noTable() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TableColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
            "    setLayout(tableColumnLayout);",
            "  }",
            "}");
    shell.refresh();
    assertNoErrors(shell);
  }

  /**
   * Test for CREATE new {@link Table} on {@link TableColumnLayout}.
   */
  public void test_parse_TableColumnLayout_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TableColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
            "    setLayout(tableColumnLayout);",
            "  }",
            "}");
    shell.refresh();
    assertNoErrors(shell);
    TableColumnLayoutInfo layout = (TableColumnLayoutInfo) shell.getLayout();
    // create new Table
    ControlInfo newTable = BTestUtils.createControl("org.eclipse.swt.widgets.Table");
    SimpleContainer simpleContainer = new SimpleContainerFactory(layout, true).get().get(0);
    simpleContainer.command_CREATE(newTable);
    assertEditor(
        "import org.eclipse.jface.layout.TableColumnLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
        "    setLayout(tableColumnLayout);",
        "    {",
        "      Table table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);",
        "      table.setHeaderVisible(true);",
        "      table.setLinesVisible(true);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for ADD existing {@link Table} on {@link TableColumnLayout}.
   */
  public void test_parse_TableColumnLayout_ADD() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TableColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new TableColumnLayout());",
            "    }",
            "    {",
            "      Table table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    assertNoErrors(shell);
    CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
    TableColumnLayoutInfo layout = (TableColumnLayoutInfo) composite.getLayout();
    ControlInfo table = shell.getChildrenControls().get(1);
    // add Table
    SimpleContainer simpleContainer = new SimpleContainerFactory(layout, true).get().get(0);
    simpleContainer.command_ADD(table);
    assertEditor(
        "import org.eclipse.jface.layout.TableColumnLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new TableColumnLayout());",
        "      {",
        "        Table table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TreeColumnLayout: no Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Users often try to drop {@link TreeColumnLayout} on {@link Composite} and see exception. We
   * should prevent this.
   */
  public void test_parse_TreeColumnLayout_noTree() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TreeColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new TreeColumnLayout());",
            "  }",
            "}");
    shell.refresh();
    assertNoErrors(shell);
  }

  /**
   * Test for CREATE new {@link Tree} on {@link TreeColumnLayout}.
   */
  public void test_parse_TreeColumnLayout_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TreeColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new TreeColumnLayout());",
            "  }",
            "}");
    shell.refresh();
    assertNoErrors(shell);
    TreeColumnLayoutInfo layout = (TreeColumnLayoutInfo) shell.getLayout();
    // create new Tree
    ControlInfo newTree = BTestUtils.createControl("org.eclipse.swt.widgets.Tree");
    SimpleContainer simpleContainer = new SimpleContainerFactory(layout, true).get().get(0);
    simpleContainer.command_CREATE(newTree);
    assertEditor(
        "import org.eclipse.jface.layout.TreeColumnLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new TreeColumnLayout());",
        "    {",
        "      Tree tree = new Tree(this, SWT.BORDER);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for ADD existing {@link Tree} on {@link TreeColumnLayout}.
   */
  public void test_parse_TreeColumnLayout_ADD() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TreeColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      composite.setLayout(new TreeColumnLayout());",
            "    }",
            "    {",
            "      Tree tree = new Tree(this, SWT.BORDER);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    assertNoErrors(shell);
    CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
    TreeColumnLayoutInfo layout = (TreeColumnLayoutInfo) composite.getLayout();
    ControlInfo tree = shell.getChildrenControls().get(1);
    // add Table
    SimpleContainer simpleContainer = new SimpleContainerFactory(layout, true).get().get(0);
    simpleContainer.command_ADD(tree);
    assertEditor(
        "import org.eclipse.jface.layout.TreeColumnLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new TreeColumnLayout());",
        "      {",
        "        Tree tree = new Tree(composite, SWT.BORDER);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete manager Table/Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_deleteComposite_whenDeleteTable() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TableColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    Composite composite = new Composite(this, SWT.NONE);",
            "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
            "    composite.setLayout(tableColumnLayout);",
            "    Table table = new Table(composite, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
    TableInfo table = (TableInfo) composite.getChildrenControls().get(0);
    //
    table.delete();
    assertEditor(
        "import org.eclipse.jface.layout.TableColumnLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Intercept column.setWidth()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Intercept setting property "column.width" and set "width" property of {@link ColumnPixelData}.
   */
  public void test_setWidth_PIXEL() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TableColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
            "    setLayout(tableColumnLayout);",
            "    Table table = new Table(this, SWT.NONE);",
            "    {",
            "      TableColumn column = new TableColumn(table, SWT.NONE);",
            "      tableColumnLayout.setColumnData(column, new ColumnPixelData(150));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    TableColumnInfo column = table.getColumns().get(0);
    //
    column.getPropertyByTitle("width").setValue(200);
    assertEditor(
        "import org.eclipse.jface.layout.TableColumnLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
        "    setLayout(tableColumnLayout);",
        "    Table table = new Table(this, SWT.NONE);",
        "    {",
        "      TableColumn column = new TableColumn(table, SWT.NONE);",
        "      tableColumnLayout.setColumnData(column, new ColumnPixelData(200));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Intercept setting property "column.width" and set "minimumWidth" property of
   * {@link ColumnWeightData}.
   */
  public void test_setWidth_WEIGHT() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TableColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
            "    setLayout(tableColumnLayout);",
            "    Table table = new Table(this, SWT.NONE);",
            "    {",
            "      TableColumn column = new TableColumn(table, SWT.NONE);",
            "      tableColumnLayout.setColumnData(column, new ColumnWeightData(1, 150));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    TableColumnInfo column = table.getColumns().get(0);
    //
    column.getPropertyByTitle("width").setValue(200);
    assertEditor(
        "import org.eclipse.jface.layout.TableColumnLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
        "    setLayout(tableColumnLayout);",
        "    Table table = new Table(this, SWT.NONE);",
        "    {",
        "      TableColumn column = new TableColumn(table, SWT.NONE);",
        "      tableColumnLayout.setColumnData(column, new ColumnWeightData(1, 200));",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setLayoutData()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AbstractColumnLayoutInfo#setLayoutData(ItemInfo, ColumnLayoutDataInfo)}.<br>
   * Sets {@link ColumnPixelDataInfo}.
   */
  public void test_setLayoutData_PIXEL() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TableColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
            "    setLayout(tableColumnLayout);",
            "    Table table = new Table(this, SWT.NONE);",
            "    {",
            "      TableColumn column = new TableColumn(table, SWT.NONE);",
            "      tableColumnLayout.setColumnData(column, new ColumnWeightData(1, 200));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableColumnLayoutInfo layout = (TableColumnLayoutInfo) shell.getLayout();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    TableColumnInfo column = table.getColumns().get(0);
    // set ColumnPixelData
    shell.startEdit();
    layout.setLayoutData(column, "org.eclipse.jface.viewers.ColumnPixelData");
    assertEditor(
        "import org.eclipse.jface.layout.TableColumnLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
        "    setLayout(tableColumnLayout);",
        "    Table table = new Table(this, SWT.NONE);",
        "    {",
        "      TableColumn column = new TableColumn(table, SWT.NONE);",
        "      tableColumnLayout.setColumnData(column, new ColumnPixelData(150, true, true));",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(tableColumnLayout)/ /new Table(this, SWT.NONE)/}",
        "  {new: org.eclipse.jface.layout.TableColumnLayout} {local-unique: tableColumnLayout} {/new TableColumnLayout()/ /setLayout(tableColumnLayout)/ /tableColumnLayout.setColumnData(column, new ColumnPixelData(150, true, true))/}",
        "  {new: org.eclipse.swt.widgets.Table} {local-unique: table} {/new Table(this, SWT.NONE)/ /new TableColumn(table, SWT.NONE)/}",
        "    {new: org.eclipse.swt.widgets.TableColumn} {local-unique: column} {/new TableColumn(table, SWT.NONE)/ /tableColumnLayout.setColumnData(column, new ColumnPixelData(150, true, true))/}",
        "      {new: org.eclipse.jface.viewers.ColumnPixelData} {empty} {/tableColumnLayout.setColumnData(column, new ColumnPixelData(150, true, true))/}");
  }

  /**
   * Test for {@link AbstractColumnLayoutInfo#setLayoutData(ItemInfo, ColumnLayoutDataInfo)}.<br>
   * Sets {@link ColumnWeightDataInfo}.
   */
  public void test_setLayoutData_WEIGHT() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TableColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
            "    setLayout(tableColumnLayout);",
            "    Table table = new Table(this, SWT.NONE);",
            "    {",
            "      TableColumn column = new TableColumn(table, SWT.NONE);",
            "      tableColumnLayout.setColumnData(column, new ColumnPixelData(100));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableColumnLayoutInfo layout = (TableColumnLayoutInfo) shell.getLayout();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    TableColumnInfo column = table.getColumns().get(0);
    // set ColumnWeightData
    shell.startEdit();
    layout.setLayoutData(column, "org.eclipse.jface.viewers.ColumnWeightData");
    assertEditor(
        "import org.eclipse.jface.layout.TableColumnLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
        "    setLayout(tableColumnLayout);",
        "    Table table = new Table(this, SWT.NONE);",
        "    {",
        "      TableColumn column = new TableColumn(table, SWT.NONE);",
        "      tableColumnLayout.setColumnData(column, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(tableColumnLayout)/ /new Table(this, SWT.NONE)/}",
        "  {new: org.eclipse.jface.layout.TableColumnLayout} {local-unique: tableColumnLayout} {/new TableColumnLayout()/ /setLayout(tableColumnLayout)/ /tableColumnLayout.setColumnData(column, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true))/}",
        "  {new: org.eclipse.swt.widgets.Table} {local-unique: table} {/new Table(this, SWT.NONE)/ /new TableColumn(table, SWT.NONE)/}",
        "    {new: org.eclipse.swt.widgets.TableColumn} {local-unique: column} {/new TableColumn(table, SWT.NONE)/ /tableColumnLayout.setColumnData(column, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true))/}",
        "      {new: org.eclipse.jface.viewers.ColumnWeightData} {empty} {/tableColumnLayout.setColumnData(column, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true))/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ColumnLayoutDataType_Property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for changing type of {@link ColumnLayoutDataInfo} using property.<br>
   * Sets {@link ColumnPixelDataInfo}.
   */
  public void test_LayoutDataType_Property_PIXEL() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TableColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
            "    setLayout(tableColumnLayout);",
            "    Table table = new Table(this, SWT.NONE);",
            "    {",
            "      TableColumn column = new TableColumn(table, SWT.NONE);",
            "      tableColumnLayout.setColumnData(column, new ColumnWeightData(1, 200));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    TableColumnInfo column = table.getColumns().get(0);
    // set ColumnPixelData
    Property typeProperty = column.getPropertyByTitle("LayoutDataType");
    typeProperty.setValue("ColumnPixelData");
    assertEditor(
        "import org.eclipse.jface.layout.TableColumnLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
        "    setLayout(tableColumnLayout);",
        "    Table table = new Table(this, SWT.NONE);",
        "    {",
        "      TableColumn column = new TableColumn(table, SWT.NONE);",
        "      tableColumnLayout.setColumnData(column, new ColumnPixelData(150, true, true));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for changing type of {@link ColumnLayoutDataInfo} using property.<br>
   * Sets {@link ColumnWeightDataInfo}.
   */
  public void test_LayoutDataType_Property_WEIGHT() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TableColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
            "    setLayout(tableColumnLayout);",
            "    Table table = new Table(this, SWT.NONE);",
            "    {",
            "      TableColumn column = new TableColumn(table, SWT.NONE);",
            "      tableColumnLayout.setColumnData(column, new ColumnPixelData(100));",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    TableColumnInfo column = table.getColumns().get(0);
    // set ColumnWeightData
    Property typeProperty = column.getPropertyByTitle("LayoutDataType");
    typeProperty.setValue("ColumnWeightData");
    assertEditor(
        "import org.eclipse.jface.layout.TableColumnLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
        "    setLayout(tableColumnLayout);",
        "    Table table = new Table(this, SWT.NONE);",
        "    {",
        "      TableColumn column = new TableColumn(table, SWT.NONE);",
        "      tableColumnLayout.setColumnData(column, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link ColumnLayoutDataInfo} is automatically created during column create.
   */
  public void test_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TableColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
            "    setLayout(tableColumnLayout);",
            "    Table table = new Table(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    // new column
    TableColumnInfo column = createJavaInfo("org.eclipse.swt.widgets.TableColumn");
    shell.startEdit();
    flowContainer_CREATE(table, column, null);
    assertEditor(
        "import org.eclipse.jface.layout.TableColumnLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableColumnLayout tableColumnLayout = new TableColumnLayout();",
        "    setLayout(tableColumnLayout);",
        "    Table table = new Table(this, SWT.NONE);",
        "    {",
        "      TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
        "      tableColumnLayout.setColumnData(tableColumn, new ColumnPixelData(150, true, true));",
        "      tableColumn.setText('New Column');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test that {@link ColumnLayoutDataInfo} is automatically removed during moving column out from
   * controlled table.
   */
  public void test_MOVE_out() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TableColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Composite tableComposite = new Composite(this, SWT.NONE);",
            "      TableColumnLayout tableColumnLayout = new TableColumnLayout();",
            "      tableComposite.setLayout(tableColumnLayout);",
            "      Table table = new Table(tableComposite, SWT.NONE);",
            "      {",
            "        TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
            "        tableColumnLayout.setColumnData(tableColumn, new ColumnPixelData(150, true, true));",
            "      }",
            "    }",
            "    {",
            "      Table table_2 = new Table(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    CompositeInfo tableComposite = (CompositeInfo) shell.getChildrenControls().get(0);
    TableInfo table = (TableInfo) tableComposite.getChildrenControls().get(0);
    TableInfo table_2 = (TableInfo) shell.getChildrenControls().get(1);
    TableColumnInfo column = table.getColumns().get(0);
    // move column
    shell.startEdit();
    flowContainer_MOVE(table_2, column, null);
    assertEditor(
        "import org.eclipse.jface.layout.TableColumnLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite tableComposite = new Composite(this, SWT.NONE);",
        "      TableColumnLayout tableColumnLayout = new TableColumnLayout();",
        "      tableComposite.setLayout(tableColumnLayout);",
        "      Table table = new Table(tableComposite, SWT.NONE);",
        "    }",
        "    {",
        "      Table table_2 = new Table(this, SWT.NONE);",
        "      {",
        "        TableColumn tableColumn = new TableColumn(table_2, SWT.NONE);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test that {@link ColumnLayoutDataInfo} is automatically added during moving column to
   * controlled table.
   */
  public void test_MOVE_in() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.layout.TableColumnLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Composite tableComposite = new Composite(this, SWT.NONE);",
            "      TableColumnLayout tableColumnLayout = new TableColumnLayout();",
            "      tableComposite.setLayout(tableColumnLayout);",
            "      Table table = new Table(tableComposite, SWT.NONE);",
            "    }",
            "    {",
            "      Table table_2 = new Table(this, SWT.NONE);",
            "      {",
            "        TableColumn tableColumn = new TableColumn(table_2, SWT.NONE);",
            "        tableColumn.setWidth(99);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    CompositeInfo tableComposite = (CompositeInfo) shell.getChildrenControls().get(0);
    TableInfo table = (TableInfo) tableComposite.getChildrenControls().get(0);
    TableInfo table_2 = (TableInfo) shell.getChildrenControls().get(1);
    TableColumnInfo column = table_2.getColumns().get(0);
    // move column
    shell.startEdit();
    flowContainer_MOVE(table, column, null);
    assertEditor(
        "import org.eclipse.jface.layout.TableColumnLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite tableComposite = new Composite(this, SWT.NONE);",
        "      TableColumnLayout tableColumnLayout = new TableColumnLayout();",
        "      tableComposite.setLayout(tableColumnLayout);",
        "      Table table = new Table(tableComposite, SWT.NONE);",
        "      {",
        "        TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
        "        tableColumnLayout.setColumnData(tableColumn, new ColumnPixelData(99, true, true));",
        "      }",
        "    }",
        "    {",
        "      Table table_2 = new Table(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Palette: TableComposite
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link TableCompositeEntryInfo}.
   */
  public void test_palette_TableComposite() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    shell.refresh();
    FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
    // configure to inherit layouts
    RcpToolkitDescription.INSTANCE.getPreferences().setValue(
        IPreferenceConstants.P_LAYOUT_OF_PARENT,
        true);
    // prepare palette entry
    TableCompositeEntryInfo entry = new TableCompositeEntryInfo();
    assertNotNull(entry.getIcon());
    assertNotNull(entry.getName());
    assertNotNull(entry.getDescription());
    assertTrue(entry.initialize(null, shell));
    // use Tool to create Composite
    CompositeInfo newComposite;
    {
      CreationTool creationTool = (CreationTool) entry.createTool();
      ICreationFactory creationFactory = creationTool.getFactory();
      creationFactory.activate();
      newComposite = (CompositeInfo) creationFactory.getNewObject();
    }
    fillLayout.command_CREATE(newComposite, null);
    waitEventLoop(0);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new TableColumnLayout());",
        "      {",
        "        Table table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);",
        "        table.setHeaderVisible(true);",
        "        table.setLinesVisible(true);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link TableViewerCompositeEntryInfo}.
   */
  public void test_palette_TableViewerComposite() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    shell.refresh();
    FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
    // configure to inherit layouts
    RcpToolkitDescription.INSTANCE.getPreferences().setValue(
        IPreferenceConstants.P_LAYOUT_OF_PARENT,
        true);
    // prepare palette entry
    ToolEntryInfo entry = new TableViewerCompositeEntryInfo();
    assertNotNull(entry.getIcon());
    assertNotNull(entry.getName());
    assertNotNull(entry.getDescription());
    assertTrue(entry.initialize(null, shell));
    // use Tool to create Composite
    CompositeInfo newComposite;
    {
      CreationTool creationTool = (CreationTool) entry.createTool();
      ICreationFactory creationFactory = creationTool.getFactory();
      creationFactory.activate();
      newComposite = (CompositeInfo) creationFactory.getNewObject();
    }
    fillLayout.command_CREATE(newComposite, null);
    waitEventLoop(0);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new TableColumnLayout());",
        "      {",
        "        TableViewer tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);",
        "        Table table = tableViewer.getTable();",
        "        table.setHeaderVisible(true);",
        "        table.setLinesVisible(true);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Palette: TreeComposite
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link TreeCompositeEntryInfo}.
   */
  public void test_palette_TreeComposite() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    shell.refresh();
    FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
    // configure to inherit layouts
    RcpToolkitDescription.INSTANCE.getPreferences().setValue(
        IPreferenceConstants.P_LAYOUT_OF_PARENT,
        true);
    // prepare palette entry
    TreeCompositeEntryInfo entry = new TreeCompositeEntryInfo();
    assertNotNull(entry.getIcon());
    assertNotNull(entry.getName());
    assertNotNull(entry.getDescription());
    assertTrue(entry.initialize(null, shell));
    // use Tool to create Composite
    CompositeInfo newComposite;
    {
      CreationTool creationTool = (CreationTool) entry.createTool();
      ICreationFactory creationFactory = creationTool.getFactory();
      creationFactory.activate();
      newComposite = (CompositeInfo) creationFactory.getNewObject();
    }
    fillLayout.command_CREATE(newComposite, null);
    waitEventLoop(0);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new TreeColumnLayout());",
        "      {",
        "        Tree tree = new Tree(composite, SWT.BORDER);",
        "        tree.setHeaderVisible(true);",
        "        tree.setLinesVisible(true);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link TreeViewerCompositeEntryInfo}.
   */
  public void test_palette_TreeViewerComposite() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    shell.refresh();
    FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
    // configure to inherit layouts
    RcpToolkitDescription.INSTANCE.getPreferences().setValue(
        IPreferenceConstants.P_LAYOUT_OF_PARENT,
        true);
    // prepare palette entry
    ToolEntryInfo entry = new TreeViewerCompositeEntryInfo();
    assertNotNull(entry.getIcon());
    assertNotNull(entry.getName());
    assertNotNull(entry.getDescription());
    assertTrue(entry.initialize(null, shell));
    // use Tool to create Composite
    CompositeInfo newComposite;
    {
      CreationTool creationTool = (CreationTool) entry.createTool();
      ICreationFactory creationFactory = creationTool.getFactory();
      creationFactory.activate();
      newComposite = (CompositeInfo) creationFactory.getNewObject();
    }
    fillLayout.command_CREATE(newComposite, null);
    waitEventLoop(0);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      composite.setLayout(new TreeColumnLayout());",
        "      {",
        "        TreeViewer treeViewer = new TreeViewer(composite, SWT.BORDER);",
        "        Tree tree = treeViewer.getTree();",
        "        tree.setHeaderVisible(true);",
        "        tree.setLinesVisible(true);",
        "      }",
        "    }",
        "  }",
        "}");
  }
}