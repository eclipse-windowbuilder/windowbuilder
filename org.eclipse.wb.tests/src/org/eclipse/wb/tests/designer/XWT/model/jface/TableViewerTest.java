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
package org.eclipse.wb.tests.designer.XWT.model.jface;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.xwt.model.jface.TableViewerColumnInfo;
import org.eclipse.wb.internal.xwt.model.jface.TableViewerInfo;
import org.eclipse.wb.internal.xwt.model.widgets.TableInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link TableViewerInfo} and {@link TableViewerColumnInfo}.
 * 
 * @author scheglov_ke
 */
public class TableViewerTest extends XwtModelTest {
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
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_parse() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.table headerVisible='true'/>",
        "    <TableViewerColumn text='A' width='50'/>",
        "    <TableViewerColumn text='B' width='100'/>",
        "  </TableViewer>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <TableViewer.table headerVisible='true'>",
        "    <TableViewer wbp:name='viewer'>",
        "      <TableViewerColumn text='A' width='50'>",
        "      <TableViewerColumn text='B' width='100'>");
    refresh();
    TableViewerInfo viewer = getObjectByName("viewer");
    TableInfo table = viewer.getTable();
    int aiLeft = table.getClientAreaInsets().left;
    int aiTop = table.getClientAreaInsets().top;
    // columns
    List<TableViewerColumnInfo> columns = viewer.getColumns();
    assertThat(columns).hasSize(2);
    {
      TableViewerColumnInfo column = columns.get(0);
      assertEquals(new Rectangle(0, 0, 50, COLUMN_HEIGHT), column.getModelBounds());
      assertEquals(new Rectangle(aiLeft, aiTop, 50, COLUMN_HEIGHT), column.getBounds());
    }
    {
      TableViewerColumnInfo column = columns.get(1);
      assertEquals(new Rectangle(50, 0, 100, COLUMN_HEIGHT), column.getModelBounds());
      assertEquals(new Rectangle(aiLeft + 50, aiTop, 100, COLUMN_HEIGHT), column.getBounds());
    }
  }

  public void test_TableViewerColumn_properties() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.table headerVisible='true'/>",
        "    <TableViewerColumn text='A' width='50'/>",
        "  </TableViewer>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <TableViewer.table headerVisible='true'>",
        "    <TableViewer wbp:name='viewer'>",
        "      <TableViewerColumn text='A' width='50'>");
    refresh();
    TableViewerInfo viewer = getObjectByName("viewer");
    TableViewerColumnInfo column = viewer.getColumns().get(0);
    // text
    {
      Property property = column.getPropertyByTitle("text");
      assertNotNull(property);
      // value
      assertTrue(property.isModified());
      assertEquals("A", property.getValue());
      // tooltip
      assertEquals("Sets the column text.", getPropertyTooltipText(property));
      callExpressionAccessor_getAdapter_withWrongType(property);
    }
    // width
    {
      Property property = column.getPropertyByTitle("width");
      assertNotNull(property);
      // value
      assertTrue(property.isModified());
      assertEquals(50, property.getValue());
      // tooltip
      assertEquals("Sets the column width in pixels.", getPropertyTooltipText(property));
      callExpressionAccessor_getAdapter_withWrongType(property);
    }
    // "text" is text property, so included into presentation
    assertEquals("TableViewerColumn - \"A\"", ObjectsLabelProvider.INSTANCE.getText(column));
    // set "text" and "width"
    {
      column.getPropertyByTitle("text").setValue("B");
      column.getPropertyByTitle("width").setValue(100);
      assertXML(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<Shell>",
          "  <Shell.layout>",
          "    <FillLayout/>",
          "  </Shell.layout>",
          "  <TableViewer wbp:name='viewer'>",
          "    <TableViewer.table headerVisible='true'/>",
          "    <TableViewerColumn text='B' width='100'/>",
          "  </TableViewer>",
          "</Shell>");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Flow container
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_flowContainer_CREATE() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.table headerVisible='true'/>",
        "  </TableViewer>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <TableViewer.table headerVisible='true'>",
        "    <TableViewer wbp:name='viewer'>");
    refresh();
    TableViewerInfo viewer = getObjectByName("viewer");
    //
    TableViewerColumnInfo newColumn = createObject("org.eclipse.jface.viewers.TableViewerColumn");
    flowContainer_CREATE(viewer, newColumn, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.table headerVisible='true'/>",
        "    <TableViewerColumn text='New Column' width='100'/>",
        "  </TableViewer>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <TableViewer.table headerVisible='true'>",
        "    <TableViewer wbp:name='viewer'>",
        "      <TableViewerColumn text='New Column' width='100'>");
  }
}