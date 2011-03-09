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

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ViewFormInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Test for {@link ViewFormInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class ViewFormGefTest extends XwtGefTest {
  private ViewFormInfo viewForm;

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
    return super.getTestSource_namespaces() + " xmlns:c='clr-namespace:org.eclipse.swt.custom'";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Canvas, CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_CREATE_topLeft() throws Exception {
    prepare_canvas_CREATE();
    // use canvas
    canvas.target(viewForm).in(0.1, 0.1).move();
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ViewForm>",
        "  <ViewForm.topLeft>",
        "    <Button/>",
        "  </ViewForm.topLeft>",
        "</ViewForm>");
  }

  public void test_canvas_CREATE_topCenter() throws Exception {
    prepare_canvas_CREATE();
    // use canvas
    canvas.target(viewForm).in(0.6, 0.1).move();
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ViewForm>",
        "  <ViewForm.topCenter>",
        "    <Button/>",
        "  </ViewForm.topCenter>",
        "</ViewForm>");
  }

  public void test_canvas_CREATE_topRight() throws Exception {
    prepare_canvas_CREATE();
    // use canvas
    canvas.target(viewForm).in(-0.1, 0.1).move();
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ViewForm>",
        "  <ViewForm.topRight>",
        "    <Button/>",
        "  </ViewForm.topRight>",
        "</ViewForm>");
  }

  public void test_canvas_CREATE_content() throws Exception {
    prepare_canvas_CREATE();
    // use canvas
    canvas.target(viewForm).in(0.3, 0.3).move();
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ViewForm>",
        "  <ViewForm.content>",
        "    <Button/>",
        "  </ViewForm.content>",
        "</ViewForm>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  private ViewFormInfo prepare_canvas_CREATE() throws Exception {
    viewForm =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ViewForm/>");
    // create Button
    loadButton();
    canvas.create(0, 0);
    // use this ViewForm_Info
    return viewForm;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_PASTE() throws Exception {
    viewForm =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ViewForm>",
            "  <ViewForm.topLeft>",
            "    <Button wbp:name='button'/>",
            "  </ViewForm.topLeft>",
            "</ViewForm>");
    ControlInfo button = getObjectByName("button");
    // operation
    doCopyPaste(button);
    canvas.create();
    canvas.target(viewForm).in(-0.1, 0.1).move();
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ViewForm>",
        "  <ViewForm.topLeft>",
        "    <Button wbp:name='button'/>",
        "  </ViewForm.topLeft>",
        "  <ViewForm.topRight>",
        "    <Button/>",
        "  </ViewForm.topRight>",
        "</ViewForm>");
  }

  public void test_canvas_MOVE() throws Exception {
    viewForm =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ViewForm>",
            "  <ViewForm.topLeft>",
            "    <Button wbp:name='button'/>",
            "  </ViewForm.topLeft>",
            "</ViewForm>");
    ControlInfo button = getObjectByName("button");
    // move to "topRight"
    canvas.beginMove(button);
    canvas.target(viewForm).in(-0.1, 0.1).drag();
    canvas.endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ViewForm>",
        "  <ViewForm.topRight>",
        "    <Button wbp:name='button'/>",
        "  </ViewForm.topRight>",
        "</ViewForm>");
    // move to "content"
    canvas.beginMove(button);
    canvas.target(viewForm).in(0.3, 0.3).drag();
    canvas.endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ViewForm>",
        "  <ViewForm.content>",
        "    <Button wbp:name='button'/>",
        "  </ViewForm.content>",
        "</ViewForm>");
  }

  public void test_canvas_ADD() throws Exception {
    openEditor(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <ViewForm wbp:name='viewForm'/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    viewForm = getObjectByName("viewForm");
    ControlInfo button = getObjectByName("button");
    // move
    canvas.beginMove(button);
    canvas.target(viewForm).in(-0.1, 0.1).drag();
    canvas.endDrag();
    assertXML(
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <ViewForm wbp:name='viewForm'>",
        "    <ViewForm.topRight>",
        "      <Button wbp:name='button'/>",
        "    </ViewForm.topRight>",
        "  </ViewForm>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE_topLeft() throws Exception {
    prepare_canvas_CREATE();
    // use tree
    EditPart position = tree.getEditPart(viewForm).getChildren().get(0);
    tree.moveBefore(position);
    tree.moveOn(position).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ViewForm>",
        "  <ViewForm.topLeft>",
        "    <Button/>",
        "  </ViewForm.topLeft>",
        "</ViewForm>");
  }

  public void test_tree_MOVE() throws Exception {
    viewForm =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ViewForm>",
            "  <ViewForm.topLeft>",
            "    <Button wbp:name='button'/>",
            "  </ViewForm.topLeft>",
            "</ViewForm>");
    ControlInfo button = getObjectByName("button");
    // use tree
    EditPart position = tree.getEditPart(viewForm).getChildren().get(1);
    tree.startDrag(button);
    tree.dragOn(position).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ViewForm>",
        "  <ViewForm.topCenter>",
        "    <Button wbp:name='button'/>",
        "  </ViewForm.topCenter>",
        "</ViewForm>");
  }

  public void test_tree_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <ViewForm wbp:name='viewForm'/>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    viewForm = getObjectByName("viewForm");
    ControlInfo button = getObjectByName("button");
    // use tree
    EditPart position = tree.getEditPart(viewForm).getChildren().get(1);
    tree.startDrag(button);
    tree.dragOn(position).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <ViewForm wbp:name='viewForm'>",
        "    <ViewForm.topCenter>",
        "      <Button wbp:name='button'/>",
        "    </ViewForm.topCenter>",
        "  </ViewForm>",
        "</Shell>");
  }

  public void test_tree_ADD2() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <ViewForm wbp:name='viewForm'/>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    viewForm = getObjectByName("viewForm");
    ControlInfo button_1 = getObjectByName("button_1");
    ControlInfo button_2 = getObjectByName("button_2");
    // use tree
    EditPart position = tree.getEditPart(viewForm).getChildren().get(1);
    tree.startDrag(button_1, button_2);
    tree.dragOn(position);
    tree.assertCommandNull();
    tree.endDrag();
  }
}
