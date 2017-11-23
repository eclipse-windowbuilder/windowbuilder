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

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.ConstructorParentAssociation;
import org.eclipse.wb.internal.core.editor.DesignPageSite;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.model.variable.WrapperMethodControlVariableSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.jface.viewers.TableViewerColumnInfo;
import org.eclipse.wb.internal.rcp.model.jface.viewers.TableViewerColumnSorterPropertyEditor;
import org.eclipse.wb.internal.rcp.model.jface.viewers.ViewerColumnInfo;
import org.eclipse.wb.internal.rcp.model.jface.viewers.ViewerColumnWidgetAssociation;
import org.eclipse.wb.internal.rcp.model.jface.viewers.ViewerColumnWidgetCreationSupport;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.TableColumnInfo;
import org.eclipse.wb.internal.swt.model.widgets.TableInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.Table;

import static org.assertj.core.api.Assertions.assertThat;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import java.util.List;

/**
 * Test for {@link TableViewerColumnInfo} and {@link ViewerColumnInfo} in general.
 * 
 * @author scheglov_ke
 */
public class TableViewerColumnTest extends RcpModelTest {
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
   * "setContentProvider()" and "setLabelProvider()" should be after {@link TableViewerColumn}, so
   * after {@link Table} which contains them.
   */
  public void test_setContentProvider_afterTable() throws Exception {
    parseComposite(
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
        "    Table table = tableViewer.getTable();",
        "  }",
        "}");
    //
    ViewerInfo tableViewer = getJavaInfoByName("tableViewer");
    tableViewer.addMethodInvocation(
        "setContentProvider(org.eclipse.jface.viewers.IContentProvider)",
        "null");
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
        "    Table table = tableViewer.getTable();",
        "    tableViewer.setContentProvider(null);",
        "  }",
        "}");
  }

  /**
   * "setContentProvider()" and "setLabelProvider()" should be after {@link TableViewerColumn}, so
   * after {@link Table} which contains them.
   */
  public void test_setLabelProvider_afterTable() throws Exception {
    parseComposite(
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
        "    Table table = tableViewer.getTable();",
        "  }",
        "}");
    //
    ViewerInfo tableViewer = getJavaInfoByName("tableViewer");
    tableViewer.addMethodInvocation(
        "setLabelProvider(org.eclipse.jface.viewers.IBaseLabelProvider)",
        "null");
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
        "    Table table = tableViewer.getTable();",
        "    tableViewer.setLabelProvider(null);",
        "  }",
        "}");
  }

  public void test_parseNormalNoColumn() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
            "    {",
            "      TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new TableViewer(this, SWT.NONE)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {viewer: public org.eclipse.swt.widgets.Table org.eclipse.jface.viewers.TableViewer.getTable()} {viewer} {}",
        "    {new: org.eclipse.jface.viewers.TableViewer} {local-unique: tableViewer} {/new TableViewer(this, SWT.NONE)/ /new TableViewerColumn(tableViewer, SWT.NONE)/}",
        "    {viewer: public org.eclipse.swt.widgets.TableColumn org.eclipse.jface.viewers.TableViewerColumn.getColumn()} {viewer} {}",
        "      {new: org.eclipse.jface.viewers.TableViewerColumn} {local-unique: tableViewerColumn} {/new TableViewerColumn(tableViewer, SWT.NONE)/}");
    // Table should have TableColumn
    TableColumnInfo column;
    {
      List<TableColumnInfo> tableColumns = table.getColumns();
      assertThat(tableColumns).hasSize(1);
      column = tableColumns.get(0);
    }
    // TableColumn should have TableViewerColumn
    ViewerColumnInfo columnViewer;
    {
      List<JavaInfo> tableColumnChildren = column.getChildrenJava();
      assertThat(tableColumnChildren).hasSize(1);
      columnViewer = (ViewerColumnInfo) tableColumnChildren.get(0);
      // association
      {
        Association association = columnViewer.getAssociation();
        assertInstanceOf(ConstructorParentAssociation.class, association);
      }
    }
    // TableColumn association: association of TableViewerColumn
    {
      Association association = column.getAssociation();
      assertInstanceOf(ViewerColumnWidgetAssociation.class, association);
      assertEquals("new TableViewerColumn(tableViewer, SWT.NONE)", association.getSource());
      assertSame(association.getStatement(), columnViewer.getAssociation().getStatement());
    }
    // TableColumn variable
    {
      VariableSupport variable = column.getVariableSupport();
      assertInstanceOf(WrapperMethodControlVariableSupport.class, variable);
      assertEquals("viewer", variable.toString());
      assertTrue(variable.isDefault());
      assertEquals("tableViewerColumn.getColumn()", variable.getTitle());
    }
    // TableColumn creation
    {
      CreationSupport creation = column.getCreationSupport();
      assertInstanceOf(ViewerColumnWidgetCreationSupport.class, creation);
      assertEquals(
          "viewer: public org.eclipse.swt.widgets.TableColumn org.eclipse.jface.viewers.TableViewerColumn.getColumn()",
          creation.toString());
      assertEquals(columnViewer.getCreationSupport().getNode(), creation.getNode());
      assertTrue(creation.canReorder());
      assertTrue(creation.canReparent());
    }
    // TableViewerColumn: check CreationSupport
    {
      ConstructorCreationSupport creationSupport =
          (ConstructorCreationSupport) columnViewer.getCreationSupport();
      assertEquals(
          "new TableViewerColumn(tableViewer, SWT.NONE)",
          m_lastEditor.getSource(creationSupport.getNode()));
    }
    // TableColumn delete
    {
      assertTrue(column.canDelete());
      column.delete();
      assertEditor(
          "public class Test extends Shell {",
          "  public Test() {",
          "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
          "  }",
          "}");
    }
  }

  public void test_parseAroundColumn() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
            "    {",
            "      TableColumn tableColumn = new TableColumn(tableViewer.getTable(), SWT.NONE);",
            "      TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, tableColumn);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new TableViewer(this, SWT.NONE)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {viewer: public org.eclipse.swt.widgets.Table org.eclipse.jface.viewers.TableViewer.getTable()} {viewer} {/new TableColumn(tableViewer.getTable(), SWT.NONE)/}",
        "    {new: org.eclipse.jface.viewers.TableViewer} {local-unique: tableViewer} {/new TableViewer(this, SWT.NONE)/ /tableViewer.getTable()/ /new TableViewerColumn(tableViewer, tableColumn)/}",
        "    {new: org.eclipse.swt.widgets.TableColumn} {local-unique: tableColumn} {/new TableColumn(tableViewer.getTable(), SWT.NONE)/ /new TableViewerColumn(tableViewer, tableColumn)/}",
        "      {new: org.eclipse.jface.viewers.TableViewerColumn} {local-unique: tableViewerColumn} {/new TableViewerColumn(tableViewer, tableColumn)/}");
  }

  public void test_normalNoColumn_materialize() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
            "    {",
            "      TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    TableColumnInfo column = table.getColumns().get(0);
    // materialize TableColumn
    column.getPropertyByTitle("resizable").setValue(false);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
        "    {",
        "      TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);",
        "      TableColumn tableColumn = tableViewerColumn.getColumn();",
        "      tableColumn.setResizable(false);",
        "    }",
        "  }",
        "}");
    // check supports
    assertInstanceOf(ViewerColumnWidgetCreationSupport.class, column.getCreationSupport());
    assertInstanceOf(LocalUniqueVariableSupport.class, column.getVariableSupport());
    // check isJavaInfo()
    {
      MethodInvocation getColumnNode = (MethodInvocation) column.getRelatedNodes().get(0);
      assertTrue(column.isRepresentedBy(getColumnNode));
    }
  }

  public void test_normalNoColumn_move() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
            "    {",
            "      TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);",
            "    }",
            "    {",
            "      TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    TableColumnInfo column_1 = table.getColumns().get(0);
    TableColumnInfo column_2 = table.getColumns().get(1);
    // move TableColumn
    flowContainer_MOVE(table, column_2, column_1);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
        "    {",
        "      TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);",
        "    }",
        "    {",
        "      TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  public void test_normalNoColumn_reparent() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableViewer tableViewer_1 = new TableViewer(this, SWT.NONE);",
            "    {",
            "      TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer_1, SWT.NONE);",
            "    }",
            "    //",
            "    TableViewer tableViewer_2 = new TableViewer(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    TableInfo table_1 = (TableInfo) shell.getChildrenControls().get(0);
    TableInfo table_2 = (TableInfo) shell.getChildrenControls().get(1);
    TableColumnInfo column = table_1.getColumns().get(0);
    // move TableColumn
    flowContainer_MOVE(table_2, column, null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableViewer tableViewer_1 = new TableViewer(this, SWT.NONE);",
        "    //",
        "    TableViewer tableViewer_2 = new TableViewer(this, SWT.NONE);",
        "    {",
        "      TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer_2, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  public void test_normalWithColumn_move() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
            "    {",
            "      TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);",
            "      TableColumn tableColumn_1 = tableViewerColumn_1.getColumn();",
            "    }",
            "    {",
            "      TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);",
            "      TableColumn tableColumn_2 = tableViewerColumn_2.getColumn();",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    TableColumnInfo column_1 = table.getColumns().get(0);
    TableColumnInfo column_2 = table.getColumns().get(1);
    // move TableColumn
    flowContainer_MOVE(table, column_2, column_1);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
        "    {",
        "      TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);",
        "      TableColumn tableColumn_2 = tableViewerColumn_2.getColumn();",
        "    }",
        "    {",
        "      TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);",
        "      TableColumn tableColumn_1 = tableViewerColumn_1.getColumn();",
        "    }",
        "  }",
        "}");
  }

  public void test_normalWithColumn_reparent() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableViewer tableViewer_1 = new TableViewer(this, SWT.NONE);",
            "    {",
            "      TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer_1, SWT.NONE);",
            "      TableColumn tableColumn = tableViewerColumn.getColumn();",
            "    }",
            "    //",
            "    TableViewer tableViewer_2 = new TableViewer(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    TableInfo table_1 = (TableInfo) shell.getChildrenControls().get(0);
    TableInfo table_2 = (TableInfo) shell.getChildrenControls().get(1);
    TableColumnInfo column = table_1.getColumns().get(0);
    // move TableColumn
    flowContainer_MOVE(table_2, column, null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableViewer tableViewer_1 = new TableViewer(this, SWT.NONE);",
        "    //",
        "    TableViewer tableViewer_2 = new TableViewer(this, SWT.NONE);",
        "    {",
        "      TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer_2, SWT.NONE);",
        "      TableColumn tableColumn = tableViewerColumn.getColumn();",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_copyPaste() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
            "    {",
            "      TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.RIGHT);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    // prepare memento
    JavaInfoMemento memento;
    {
      TableColumnInfo column = table.getColumns().get(0);
      memento = JavaInfoMemento.createMemento(column);
    }
    // do paste
    {
      TableColumnInfo newColumn = (TableColumnInfo) memento.create(shell);
      flowContainer_CREATE(table, newColumn, null);
      memento.apply();
    }
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
        "    {",
        "      TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.RIGHT);",
        "    }",
        "    {",
        "      TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.RIGHT);",
        "      TableColumn tableColumn = tableViewerColumn.getColumn();",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    TableInfo table = (TableInfo) shell.getChildrenControls().get(0);
    // prepare TableViewerColumn, TableColumn
    ViewerColumnInfo columnViewer = createTableViewerColumn();
    TableColumnInfo column = (TableColumnInfo) JavaInfoUtils.getWrapped(columnViewer);
    assertSame(column, JavaInfoUtils.getWrapped(column));
    // check current CreationSupport
    {
      CreationSupport creationSupport = column.getCreationSupport();
      // no node yet, and we can not it, because when it is set, we replace this CreationSupport
      {
        ASTNode node = creationSupport.getNode();
        assertNull(node);
      }
      // isJavaInfo()
      assertFalse(creationSupport.isJavaInfo(null));
      // permissions
      assertFalse(creationSupport.canReorder());
      assertFalse(creationSupport.canReparent());
    }
    // CREATE
    flowContainer_CREATE(table, column, null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
        "    {",
        "      TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);",
        "      TableColumn tableColumn = tableViewerColumn.getColumn();",
        "      tableColumn.setWidth(100);",
        "      tableColumn.setText('New Column');",
        "    }",
        "  }",
        "}");
    // check TableColumn
    {
      assertSame(table, column.getParent());
      // creation
      {
        CreationSupport creationSupport = column.getCreationSupport();
        assertInstanceOf(ViewerColumnWidgetCreationSupport.class, creationSupport);
        assertSame(column, ReflectionUtils.getFieldObject(creationSupport, "m_javaInfo"));
      }
      // variable
      assertInstanceOf(LocalUniqueVariableSupport.class, column.getVariableSupport());
      // toString
      assertEquals(
          "{viewer: public org.eclipse.swt.widgets.TableColumn org.eclipse.jface.viewers.TableViewerColumn.getColumn()} {local-unique: tableColumn} {/tableViewerColumn.getColumn()/ /tableColumn.setText(\"New Column\")/ /tableColumn.setWidth(100)/}",
          column.toString());
    }
    // check TableViewerColumn
    {
      assertSame(columnViewer, column.getChildrenJava().get(0));
      // creation
      assertInstanceOf(ConstructorCreationSupport.class, columnViewer.getCreationSupport());
      // variable
      assertInstanceOf(LocalUniqueVariableSupport.class, columnViewer.getVariableSupport());
      assertEquals("tableViewerColumn", columnViewer.getVariableSupport().getName());
      // toString
      assertEquals(
          "{new: org.eclipse.jface.viewers.TableViewerColumn} {local-unique: tableViewerColumn} {/new TableViewerColumn(tableViewer, SWT.NONE)/ /tableViewerColumn.getColumn()/}",
          columnViewer.toString());
    }
    // check associations
    {
      Association viewerAssociation = columnViewer.getAssociation();
      Association columnAssociation = column.getAssociation();
      // viewer association
      assertInstanceOf(ConstructorParentAssociation.class, viewerAssociation);
      assertEquals("new TableViewerColumn(tableViewer, SWT.NONE)", viewerAssociation.getSource());
      // table association
      assertSame(viewerAssociation.getStatement(), columnAssociation.getStatement());
    }
  }

  /**
   * @return the new {@link ViewerColumnInfo} instance.
   */
  private ViewerColumnInfo createTableViewerColumn() throws Exception {
    return (ViewerColumnInfo) JavaInfoUtils.createJavaInfo(
        m_lastEditor,
        "org.eclipse.jface.viewers.TableViewerColumn",
        new ConstructorCreationSupport());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "sorter" property
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_sorterProperty() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
            "    {",
            "      TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    TableViewerColumnInfo column = getJavaInfoByName("tableViewerColumn");
    //
    Property sorterProperty = column.getPropertyByTitle("sorter");
    assertNotNull(sorterProperty);
    TableViewerColumnSorterPropertyEditor sorterEditor =
        (TableViewerColumnSorterPropertyEditor) sorterProperty.getEditor();
    // no value
    assertNull(sorterProperty.getValue());
    assertFalse(sorterProperty.isModified());
    // initially no sorter
    assertEquals("<double click>", getPropertyText(sorterProperty));
    // kick "doubleClick", assert that some position opened
    {
      Capture<Integer> positionCapture = new Capture<Integer>();
      // prepare scenario
      IMocksControl mocksControl = EasyMock.createStrictControl();
      IDesignPageSite designerPageSite = mocksControl.createMock(IDesignPageSite.class);
      designerPageSite.openSourcePosition(org.easymock.EasyMock.capture(positionCapture));
      mocksControl.replay();
      // use DesignPageSite, open position
      DesignPageSite.Helper.setSite(shell, designerPageSite);
      sorterEditor.doubleClick(sorterProperty, null);
      mocksControl.verify();
      // source
      assertThat(m_lastEditor.getSource()).contains(
          "new TableViewerColumnSorter(tableViewerColumn) {");
      // assert position
      {
        int position = positionCapture.getValue();
        String actual = m_lastEditor.getSource(position, 48);
        assertEquals("new TableViewerColumnSorter(tableViewerColumn) {", actual);
      }
    }
    // now has sorter
    assertEquals("<exists>", getPropertyText(sorterProperty));
    // delete sorter
    sorterProperty.setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "import org.eclipse.wb.swt.TableViewerColumnSorter;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    TableViewer tableViewer = new TableViewer(this, SWT.NONE);",
        "    {",
        "      TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);",
        "    }",
        "  }",
        "}");
    assertEquals("<double click>", getPropertyText(sorterProperty));
  }
}
