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

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.xwt.model.jface.ViewerInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.swt.graphics.Image;

/**
 * Test for {@link ViewerInfo}.
 * 
 * @author scheglov_ke
 */
public class ViewerTest extends XwtModelTest {
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
  public void test_parse_noControlElement() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'/>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <TableViewer.table?>",
        "    <TableViewer wbp:name='viewer'>");
    refresh();
    // check getTitle() for Control
    {
      ViewerInfo viewer = getObjectByName("viewer");
      ControlInfo table = viewer.getControl();
      assertEquals("TableViewer.table", table.getCreationSupport().getTitle());
    }
  }

  public void test_parse_hasControlElement() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer>",
        "    <TableViewer.table>",
        "      <TableColumn text='1'/>",
        "      <TableColumn text='2'/>",
        "    </TableViewer.table>",
        "  </TableViewer>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <TableViewer.table>",
        "    <TableViewer>",
        "    <TableColumn text='1'>",
        "    <TableColumn text='2'>");
    refresh();
  }

  /**
   * There was problem that we tried to find exactly "table" element, and ignored "Table".
   */
  public void test_parse_hasControlElementInUpperCase() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer>",
        "    <TableViewer.Table headerVisible='true'/>",
        "  </TableViewer>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <TableViewer.Table headerVisible='true'>",
        "    <TableViewer>");
    refresh();
    // no changes
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer>",
        "    <TableViewer.Table headerVisible='true'/>",
        "  </TableViewer>",
        "</Shell>");
  }

  public void test_parse_hasControlElement_hasRowData() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <TableViewer>",
        "    <TableViewer.table>",
        "      <TableViewer.table.layoutData>",
        "        <RowData width='300' height='150'/>",
        "      </TableViewer.table.layoutData>",
        "    </TableViewer.table>",
        "  </TableViewer>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <RowLayout>",
        "  <TableViewer.table>",
        "    <TableViewer>",
        "    <RowData width='300' height='150'>");
    refresh();
  }

  public void test_parse_hasControlElement_andItsAttributes() throws Exception {
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
    //
    ViewerInfo viewer = getObjectByName("viewer");
    ControlInfo table = viewer.getControl();
    {
      Property property = table.getPropertyByTitle("headerVisible");
      assertTrue(property.isModified());
      assertEquals(Boolean.TRUE, property.getValue());
    }
    {
      Property property = table.getPropertyByTitle("linesVisible");
      assertFalse(property.isModified());
      assertEquals(Boolean.FALSE, property.getValue());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_styleProperty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.table/>",
        "  </TableViewer>",
        "</Shell>");
    refresh();
    ViewerInfo viewer = getObjectByName("viewer");
    ControlInfo table = viewer.getControl();
    // no "Style" for "table"
    assertNull(table.getPropertyByTitle("Style"));
    // has "Style" for "viewer"
    assertNotNull(viewer.getPropertyByTitle("Style"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>");
    refresh();
    //
    ViewerInfo viewer = createObject("org.eclipse.jface.viewers.TableViewer");
    ControlInfo table = (ControlInfo) XmlObjectUtils.getWrapped(viewer);
    shell.getLayout().command_CREATE(table, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer x:Style='BORDER | FULL_SELECTION'>",
        "    <TableViewer.table headerVisible='true' linesVisible='true'/>",
        "  </TableViewer>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <TableViewer.table headerVisible='true' linesVisible='true'>",
        "    <TableViewer x:Style='BORDER | FULL_SELECTION'>");
  }

  public void test_CREATE_liveImage() throws Exception {
    parse("<Shell/>");
    // prepare viewer and table
    ViewerInfo viewer = createObject("org.eclipse.jface.viewers.TableViewer");
    ControlInfo table = (ControlInfo) XmlObjectUtils.getWrapped(viewer);
    // check that table has "create" image
    Image image = table.getImage();
    assertNotNull(image);
  }

  public void test_DELETE_control() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.table/>",
        "  </TableViewer>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <TableViewer.table>",
        "    <TableViewer wbp:name='viewer'>");
    refresh();
    ViewerInfo viewer = getObjectByName("viewer");
    ControlInfo table = viewer.getControl();
    //
    assertTrue(table.canDelete());
    table.delete();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>");
  }

  public void test_MOVE_reorder() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <FillLayout/>",
            "  </Shell.layout>",
            "  <TableViewer wbp:name='viewer'>",
            "    <TableViewer.table/>",
            "  </TableViewer>",
            "  <Button/>",
            "</Shell>");
    ViewerInfo viewer = getObjectByName("viewer");
    //
    shell.getLayout().command_MOVE(viewer.getControl(), null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Button/>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.table/>",
        "  </TableViewer>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <Button>",
        "  <TableViewer.table>",
        "    <TableViewer wbp:name='viewer'>");
  }

  public void test_DELETE_viewer() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.table/>",
        "  </TableViewer>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>",
        "  <TableViewer.table>",
        "    <TableViewer wbp:name='viewer'>");
    refresh();
    ViewerInfo viewer = getObjectByName("viewer");
    //
    assertTrue(viewer.canDelete());
    viewer.delete();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <FillLayout>");
  }
}