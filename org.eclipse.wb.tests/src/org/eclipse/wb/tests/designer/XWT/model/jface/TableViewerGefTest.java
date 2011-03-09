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

import org.eclipse.wb.internal.xwt.model.jface.TableViewerInfo;
import org.eclipse.wb.internal.xwt.model.jface.ViewerInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Test for {@link TableViewerInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class TableViewerGefTest extends XwtGefTest {
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
  public void test_selecting() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <FillLayout/>",
        "  </Composite.layout>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.table headerVisible='true'/>",
        "    <TableViewerColumn wbp:name='column_1' text='A' width='100'/>",
        "    <TableViewerColumn wbp:name='column_2' text='B' width='150'/>",
        "  </TableViewer>",
        "</Composite>");
    ViewerInfo viewer = getObjectByName("viewer");
    ControlInfo table = viewer.getControl();
    Object column_1 = getObjectByName("column_1");
    Object column_2 = getObjectByName("column_2");
    // select "column_1"
    canvas.target(column_1).in(0.5, 0.5).move().click();
    canvas.assertSelection(column_1);
    // select "column_2"
    canvas.target(column_2).in(0.5, 0.5).move().click();
    canvas.assertSelection(column_2);
    // select "table"
    canvas.target(table).in(0.5, 0.5).move().click();
    canvas.assertSelection(table);
    // select "viewer"
    canvas.target(viewer).in(-3 - 16 / 2, -3 - 16 / 2).move().click();
    canvas.assertSelection(viewer);
  }

  public void test_TableViewerColumn_CREATE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <FillLayout/>",
        "  </Composite.layout>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.table headerVisible='true'/>",
        "  </TableViewer>",
        "</Composite>");
    ViewerInfo viewer = getObjectByName("viewer");
    ControlInfo table = viewer.getControl();
    //
    loadCreationTool("org.eclipse.jface.viewers.TableViewerColumn");
    canvas.moveTo(table, 10, 10).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <FillLayout/>",
        "  </Composite.layout>",
        "  <TableViewer wbp:name='viewer'>",
        "    <TableViewer.table headerVisible='true'/>",
        "    <TableViewerColumn text='New Column' width='100'/>",
        "  </TableViewer>",
        "</Composite>");
  }

  public void test_TableViewerColumn_MOVE() throws Exception {
    openEditor(
        "<Composite>",
        "  <Composite.layout>",
        "    <FillLayout/>",
        "  </Composite.layout>",
        "  <TableViewer>",
        "    <TableViewer.table headerVisible='true'/>",
        "    <TableViewerColumn wbp:name='column_1' text='A' width='100'/>",
        "    <TableViewerColumn wbp:name='column_2' text='B' width='150'/>",
        "  </TableViewer>",
        "</Composite>");
    Object column_1 = getObjectByName("column_1");
    Object column_2 = getObjectByName("column_2");
    //
    canvas.beginMove(column_2).dragTo(column_1, 10, 10).endDrag();
    assertXML(
        "<Composite>",
        "  <Composite.layout>",
        "    <FillLayout/>",
        "  </Composite.layout>",
        "  <TableViewer>",
        "    <TableViewer.table headerVisible='true'/>",
        "    <TableViewerColumn wbp:name='column_2' text='B' width='150'/>",
        "    <TableViewerColumn wbp:name='column_1' text='A' width='100'/>",
        "  </TableViewer>",
        "</Composite>");
  }

  public void test_TableViewerColumn_RESIZE() throws Exception {
    openEditor(
        "<Composite>",
        "  <Composite.layout>",
        "    <FillLayout/>",
        "  </Composite.layout>",
        "  <TableViewer>",
        "    <TableViewer.table headerVisible='true'/>",
        "    <TableViewerColumn wbp:name='column' text='A' width='100'/>",
        "  </TableViewer>",
        "</Composite>");
    Object column = getObjectByName("column");
    //
    canvas.target(column).outX(1).inY(0.5);
    canvas.beginDrag().dragOn(50, 0).endDrag();
    assertXML(
        "<Composite>",
        "  <Composite.layout>",
        "    <FillLayout/>",
        "  </Composite.layout>",
        "  <TableViewer>",
        "    <TableViewer.table headerVisible='true'/>",
        "    <TableViewerColumn wbp:name='column' text='A' width='150'/>",
        "  </TableViewer>",
        "</Composite>");
  }
}
