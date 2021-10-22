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
package org.eclipse.wb.tests.designer.XWT.model.forms.table;

import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Tests for {@link TableWrapLayout} in GEF.
 *
 * @author scheglov_ke
 */
public class TableWrapLayoutGefTest extends XwtGefTest {
  private static final int M = 5;
  private static final int S = 5;
  private static final int VS = 25;
  private static final int VG = 5;

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
  // Source
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getTestSource_namespaces() {
    return super.getTestSource_namespaces()
        + " xmlns:f='clr-namespace:org.eclipse.ui.forms.widgets'";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When we delete component of expandable/collapsible container, selection {@link Handle} receives
   * ancestor resize event, so tries to update {@link Handle} location. However at this time
   * component may be already deleted, so we can not ask for its cell/bounds.
   */
  public void test_deleteChildAndAncestorResize() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout/>",
        "  </Shell.layout>",
        "  <Composite>",
        "    <Composite.layout>",
        "      <f:TableWrapLayout/>",
        "    </Composite.layout>",
        "    <Label text='Label'/>",
        "    <Button wbp:name='button' text='Button'/>",
        "  </Composite>",
        "</Shell>");
    ControlInfo button = getObjectByName("button");
    // select "button"
    canvas.select(button);
    waitEventLoop(0);
    // delete
    {
      IAction deleteAction = getDeleteAction();
      assertTrue(deleteAction.isEnabled());
      deleteAction.run();
      assertXML(
          "// filler filler filler filler filler",
          "<Shell>",
          "  <Shell.layout>",
          "    <f:TableWrapLayout/>",
          "  </Shell.layout>",
          "  <Composite>",
          "    <Composite.layout>",
          "      <f:TableWrapLayout/>",
          "    </Composite.layout>",
          "    <Label text='Label'/>",
          "  </Composite>",
          "</Shell>");
    }
  }

  /**
   * When user externally (not using design canvas) changes "numColumns", we should recalculate
   * positions of controls, in other case we will have incorrect count of column/row headers.
   */
  public void test_change_numColumns() throws Exception {
    CompositeInfo shell =
        openEditor(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <f:TableWrapLayout wbp:name='gridLayout'/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button_00'/>",
            "  <Button wbp:name='button_01'/>",
            "</Shell>");
    TableWrapLayoutInfo layout = getObjectByName("gridLayout");
    // select "shell", so show headers
    canvas.select(shell);
    // initially: 1 column, 2 rows
    assertEquals(1, layout.getColumns().size());
    assertEquals(2, layout.getRows().size());
    // set: 2 columns, so 1 row
    // this caused exception in headers refresh
    layout.getPropertyByTitle("numColumns").setValue(2);
    assertNoLoggedExceptions();
    assertEquals(2, layout.getColumns().size());
    assertEquals(1, layout.getRows().size());
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='gridLayout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_00'/>",
        "  <Button wbp:name='button_01'/>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Size hint
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setSizeHint_height() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button' text='New Button'/>",
        "</Shell>");
    ControlInfo button = getObjectByName("button");
    // resize SOUTH of "button"
    canvas.toResizeHandle(button, "resize_size", IPositionConstants.SOUTH).beginDrag();
    canvas.target(button).in(0, 50).drag().endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button' text='New Button'>",
        "    <Button.layoutData>",
        "      <f:TableWrapData heightHint='50'/>",
        "    </Button.layoutData>",
        "  </Button>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_filled() throws Exception {
    CompositeInfo composite =
        openEditor(
            "// filler filler filler filler filler",
            "<Composite>",
            "  <Composite.layout>",
            "    <f:TableWrapLayout/>",
            "  </Composite.layout>",
            "  <Button wbp:name='existingButton' text='Existing Button'/>",
            "</Composite>");
    //
    loadButtonWithText();
    canvas.moveTo(composite, M, M);
    canvas.assertCommandNull();
  }

  public void test_CREATE_virtual_0x0() throws Exception {
    CompositeInfo composite =
        openEditor(
            "// filler filler filler filler filler",
            "<Composite>",
            "  <Composite.layout>",
            "    <f:TableWrapLayout/>",
            "  </Composite.layout>",
            "</Composite>");
    //
    loadButtonWithText();
    canvas.moveTo(composite, M, M);
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout/>",
        "  </Composite.layout>",
        "  <Button text='New Button'/>",
        "</Composite>");
  }

  public void test_CREATE_virtual_0x1() throws Exception {
    CompositeInfo composite =
        openEditor(
            "// filler filler filler filler filler",
            "<Composite>",
            "  <Composite.layout>",
            "    <f:TableWrapLayout/>",
            "  </Composite.layout>",
            "</Composite>");
    //
    loadButtonWithText();
    canvas.moveTo(composite, M + VS + VG, M);
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout numColumns='2'/>",
        "  </Composite.layout>",
        "  <Label/>",
        "  <Button text='New Button'/>",
        "</Composite>");
  }

  public void test_CREATE_appendToColumn_1x0() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout/>",
        "  </Composite.layout>",
        "  <Button wbp:name='existingButton' text='Existing Button'/>",
        "</Composite>");
    XmlObjectInfo existingButton = getObjectByName("existingButton");
    //
    loadButtonWithText();
    canvas.target(existingButton).inX(0.5).outY(S + 1).move();
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout/>",
        "  </Composite.layout>",
        "  <Button wbp:name='existingButton' text='Existing Button'/>",
        "  <Button text='New Button'/>",
        "</Composite>");
  }

  public void test_CREATE_appendToRow_0x1() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout/>",
        "  </Composite.layout>",
        "  <Button wbp:name='existingButton' text='Existing Button'/>",
        "</Composite>");
    XmlObjectInfo existingButton = getObjectByName("existingButton");
    //
    loadButtonWithText();
    canvas.target(existingButton).inY(0.5).outX(S + 1).move();
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout numColumns='2'/>",
        "  </Composite.layout>",
        "  <Button wbp:name='existingButton' text='Existing Button'/>",
        "  <Button text='New Button'/>",
        "</Composite>");
  }

  public void test_CREATE_beforeFirstRow() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout/>",
        "  </Composite.layout>",
        "  <Button wbp:name='existingButton' text='Existing Button'/>",
        "</Composite>");
    XmlObjectInfo existingButton = getObjectByName("existingButton");
    //
    loadButtonWithText();
    canvas.target(existingButton).inX(0.5).outY(-2).move();
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout/>",
        "  </Composite.layout>",
        "  <Button text='New Button'/>",
        "  <Button wbp:name='existingButton' text='Existing Button'/>",
        "</Composite>");
  }

  public void test_CREATE_beforeFirstColumn() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout/>",
        "  </Composite.layout>",
        "  <Button wbp:name='existingButton' text='Existing Button'/>",
        "</Composite>");
    XmlObjectInfo existingButton = getObjectByName("existingButton");
    //
    loadButtonWithText();
    canvas.target(existingButton).inY(0.5).outX(-2).move();
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout numColumns='2'/>",
        "  </Composite.layout>",
        "  <Button text='New Button'/>",
        "  <Button wbp:name='existingButton' text='Existing Button'/>",
        "</Composite>");
  }

  public void test_CREATE_insertColumn() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout numColumns='2'/>",
        "  </Composite.layout>",
        "  <Button wbp:name='button_1' text='Button 1'/>",
        "  <Button wbp:name='button_2' text='Button 2'/>",
        "</Composite>");
    XmlObjectInfo button_1 = getObjectByName("button_1");
    //
    loadButtonWithText();
    canvas.target(button_1).inY(0.5).outX(S / 2).move();
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout numColumns='3'/>",
        "  </Composite.layout>",
        "  <Button wbp:name='button_1' text='Button 1'/>",
        "  <Button text='New Button'/>",
        "  <Button wbp:name='button_2' text='Button 2'/>",
        "</Composite>");
  }

  public void test_CREATE_insertRow() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout/>",
        "  </Composite.layout>",
        "  <Button wbp:name='button_1' text='Button 1'/>",
        "  <Button wbp:name='button_2' text='Button 2'/>",
        "</Composite>");
    XmlObjectInfo button_1 = getObjectByName("button_1");
    //
    loadButtonWithText();
    canvas.target(button_1).inX(0.5).outY(S / 2).move();
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout/>",
        "  </Composite.layout>",
        "  <Button wbp:name='button_1' text='Button 1'/>",
        "  <Button text='New Button'/>",
        "  <Button wbp:name='button_2' text='Button 2'/>",
        "</Composite>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_PASTE_virtual_1x0() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout/>",
        "  </Composite.layout>",
        "  <Button wbp:name='existingButton' text='My Button'/>",
        "</Composite>");
    XmlObjectInfo existingButton = getObjectByName("existingButton");
    //
    doCopyPaste(existingButton);
    canvas.target(existingButton).inX(0.5).outY(S + 1).move();
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout/>",
        "  </Composite.layout>",
        "  <Button wbp:name='existingButton' text='My Button'/>",
        "  <Button text='My Button'/>",
        "</Composite>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_virtual_1x0() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout/>",
        "  </Composite.layout>",
        "  <Button wbp:name='button' text='Button'/>",
        "</Composite>");
    XmlObjectInfo button = getObjectByName("button");
    //
    canvas.beginDrag(button);
    canvas.target(button).inX(0.5).outY(S + 1).drag();
    canvas.endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <f:TableWrapLayout/>",
        "  </Composite.layout>",
        "  <Label/>",
        "  <Button wbp:name='button' text='Button'/>",
        "</Composite>");
  }

  public void test_ADD_virtual_0x0() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <FillLayout/>",
        "  </Composite.layout>",
        "  <Button wbp:name='button' text='Button'/>",
        "  <Composite wbp:name='composite'>",
        "    <Composite.layout>",
        "      <f:TableWrapLayout/>",
        "    </Composite.layout>",
        "  </Composite>",
        "</Composite>");
    XmlObjectInfo button = getObjectByName("button");
    XmlObjectInfo composite = getObjectByName("composite");
    //
    canvas.beginDrag(button);
    canvas.dragTo(composite, M + VS / 2, M + VS / 2);
    canvas.endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<Composite>",
        "  <Composite.layout>",
        "    <FillLayout/>",
        "  </Composite.layout>",
        "  <Composite wbp:name='composite'>",
        "    <Composite.layout>",
        "      <f:TableWrapLayout/>",
        "    </Composite.layout>",
        "    <Button wbp:name='button' text='Button'/>",
        "  </Composite>",
        "</Composite>");
  }
}
