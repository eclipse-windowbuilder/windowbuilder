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

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.SashFormInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Test for {@link SashFormInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class SashFormGefTest extends XwtGefTest {
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
  public void test_tree_CREATE_noNext() throws Exception {
    CompositeInfo composite =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<SashForm>",
            "</SashForm>");
    //
    loadButtonWithText();
    tree.moveOn(composite).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<SashForm weights='1'>",
        "  <Button text='New Button'/>",
        "</SashForm>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_CREATE_noNext() throws Exception {
    CompositeInfo composite =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<SashForm>",
            "</SashForm>");
    //
    loadButtonWithText();
    canvas.moveTo(composite, 100, 100).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<SashForm weights='1'>",
        "  <Button text='New Button'/>",
        "</SashForm>");
  }

  public void test_canvas_CREATE_withNext() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<SashForm weights='1'>",
        "  <Button wbp:name='reference' text='Reference'/>",
        "</SashForm>");
    XmlObjectInfo reference = getObjectByName("reference");
    //
    loadButtonWithText();
    canvas.moveTo(reference, 0, 0.5).click();
    assertXML(
        "// filler filler filler filler filler",
        "<SashForm weights='1, 1'>",
        "  <Button text='New Button'/>",
        "  <Button wbp:name='reference' text='Reference'/>",
        "</SashForm>");
  }

  public void test_canvas_MOVE_reorder() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<SashForm weights='1, 2'>",
        "  <Button wbp:name='reference' text='Reference'/>",
        "  <Button wbp:name='button' text='Button'/>",
        "</SashForm>");
    XmlObjectInfo button = getObjectByName("button");
    XmlObjectInfo reference = getObjectByName("reference");
    //
    canvas.beginMove(button);
    canvas.dragTo(reference, 0, 0.5).endDrag();
    assertXML(
        "<SashForm weights='2, 1'>",
        "  <Button wbp:name='button' text='Button'/>",
        "  <Button wbp:name='reference' text='Reference'/>",
        "</SashForm>");
  }

  public void test_canvas_RESIZE_horizontal() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<SashForm weights='1, 1'>",
        "  <Button wbp:name='buttonA' text='Button A'/>",
        "  <Button wbp:name='buttonB' text='Button B'/>",
        "</SashForm>");
    XmlObjectInfo buttonA = getObjectByName("buttonA");
    //
    canvas.target(buttonA).outX(1).inY(0.5).move();
    canvas.beginDrag().dragOn(-100, 0).endDrag();
    assertXML(
        "<SashForm weights='123, 324'>",
        "  <Button wbp:name='buttonA' text='Button A'/>",
        "  <Button wbp:name='buttonB' text='Button B'/>",
        "</SashForm>");
  }

  public void test_canvas_RESIZE_vertical() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<SashForm x:style='VERTICAL' weights='1, 1'>",
        "  <Button wbp:name='buttonA' text='Button A'/>",
        "  <Button wbp:name='buttonB' text='Button B'/>",
        "</SashForm>");
    XmlObjectInfo buttonA = getObjectByName("buttonA");
    //
    canvas.target(buttonA).outY(1).inX(0.5).move();
    canvas.beginDrag().dragOn(0, 50).endDrag();
    assertXML(
        "<SashForm x:style='VERTICAL' weights='198, 99'>",
        "  <Button wbp:name='buttonA' text='Button A'/>",
        "  <Button wbp:name='buttonB' text='Button B'/>",
        "</SashForm>");
  }
}
