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

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.rcp.model.widgets.ViewFormInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

/**
 * Test for {@link ViewFormInfo}.
 *
 * @author scheglov_ke
 */
public class ViewFormGefTest extends RcpGefTest {
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
  // Canvas, CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_CREATE_topLeft() throws Exception {
    ViewFormInfo composite = prepare_canvas_CREATE();
    // use canvas
    canvas.target(composite).in(0.1, 0.1).move();
    canvas.click();
    assertEditor(
        "public class Test extends ViewForm {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      setTopLeft(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_CREATE_topCenter() throws Exception {
    ViewFormInfo composite = prepare_canvas_CREATE();
    // use canvas
    canvas.target(composite).in(0.6, 0.1).move();
    canvas.click();
    assertEditor(
        "public class Test extends ViewForm {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      setTopCenter(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_CREATE_topRight() throws Exception {
    ViewFormInfo composite = prepare_canvas_CREATE();
    // use canvas
    canvas.target(composite).in(-0.1, 0.1).move();
    canvas.click();
    assertEditor(
        "public class Test extends ViewForm {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      setTopRight(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_CREATE_content() throws Exception {
    ViewFormInfo composite = prepare_canvas_CREATE();
    // use canvas
    canvas.target(composite).in(0.3, 0.3).move();
    canvas.click();
    assertEditor(
        "public class Test extends ViewForm {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      setContent(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_CREATE_notControl() throws Exception {
    ViewFormInfo composite = prepare_canvas_CREATE0();
    // try to CREATE Menu
    loadCreationTool("org.eclipse.swt.widgets.Menu");
    // use canvas
    canvas.create(0, 0);
    canvas.target(composite).in(0.3, 0.3).move();
    canvas.click();
    assertEditor(
        "public class Test extends ViewForm {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      Menu menu = new Menu(this);",
        "      setMenu(menu);",
        "    }",
        "  }",
        "}");
  }

  private ViewFormInfo prepare_canvas_CREATE() throws Exception {
    ViewFormInfo composite = prepare_canvas_CREATE0();
    // create Button
    loadCreationButton();
    canvas.create(0, 0);
    // use this ViewForm_Info
    return composite;
  }

  private ViewFormInfo prepare_canvas_CREATE0() throws Exception {
    prepareComponent();
    return (ViewFormInfo) openJavaInfo(
        "public class Test extends ViewForm {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_PASTE() throws Exception {
    ViewFormInfo composite =
        (ViewFormInfo) openJavaInfo(
            "public class Test extends ViewForm {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    {",
            "      Button buttonA = new Button(this, SWT.NONE);",
            "      buttonA.setEnabled(false);",
            "      setTopLeft(buttonA);",
            "    }",
            "  }",
            "}");
    ControlInfo buttonA = composite.getChildrenControls().get(0);
    // operation
    doCopyPaste(buttonA);
    canvas.create();
    canvas.target(composite).in(-0.1, 0.1).move();
    canvas.click();
    assertEditor(
        "public class Test extends ViewForm {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      Button buttonA = new Button(this, SWT.NONE);",
        "      buttonA.setEnabled(false);",
        "      setTopLeft(buttonA);",
        "    }",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setEnabled(false);",
        "      setTopRight(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_MOVE() throws Exception {
    ViewFormInfo composite =
        (ViewFormInfo) openJavaInfo(
            "public class Test extends ViewForm {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      setTopLeft(button);",
            "    }",
            "  }",
            "}");
    ControlInfo button = composite.getChildrenControls().get(0);
    // move: topRight
    canvas.beginMove(button);
    canvas.target(composite).in(-0.1, 0.1).drag();
    canvas.endDrag();
    assertEditor(
        "public class Test extends ViewForm {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      setTopRight(button);",
        "    }",
        "  }",
        "}");
    // move: content
    canvas.beginMove(button);
    canvas.target(composite).in(0.3, 0.3).drag();
    canvas.endDrag();
    assertEditor(
        "public class Test extends ViewForm {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      setContent(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_ADD() throws Exception {
    ViewFormInfo composite =
        (ViewFormInfo) openJavaInfo(
            "public class Test extends ViewForm {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      setTopLeft(button);",
            "    }",
            "    {",
            "      Button centerButton = new Button(this, SWT.NONE);",
            "      setTopCenter(centerButton);",
            "    }",
            "    {",
            "      ViewForm inner = new ViewForm(this, SWT.NONE);",
            "      setContent(inner);",
            "    }",
            "  }",
            "}");
    ControlInfo button = composite.getChildrenControls().get(0);
    ViewFormInfo inner = (ViewFormInfo) composite.getChildrenControls().get(2);
    // move
    canvas.beginMove(button);
    canvas.target(inner).in(-0.1, 0.1).drag();
    canvas.endDrag();
    assertEditor(
        "public class Test extends ViewForm {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      Button centerButton = new Button(this, SWT.NONE);",
        "      setTopCenter(centerButton);",
        "    }",
        "    {",
        "      ViewForm inner = new ViewForm(this, SWT.NONE);",
        "      setContent(inner);",
        "      {",
        "        Button button = new Button(inner, SWT.NONE);",
        "        inner.setTopRight(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE_empty() throws Exception {
    ViewFormInfo composite =
        openJavaInfo(
            "public class Test extends ViewForm {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    EditPart topLeftPart = tree.getEditPart(composite).getChildren().get(0);
    // use tree
    loadButton();
    tree.moveOn(topLeftPart);
    tree.assertCommandNotNull();
    tree.click();
    assertEditor(
        "public class Test extends ViewForm {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      setTopLeft(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_MOVE() throws Exception {
    ViewFormInfo composite =
        openJavaInfo(
            "public class Test extends ViewForm {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      setTopLeft(button);",
            "    }",
            "  }",
            "}");
    ControlInfo button = getJavaInfoByName("button");
    EditPart topLeftPart = tree.getEditPart(composite).getChildren().get(2);
    // move: topRight
    tree.startDrag(button).dragOn(topLeftPart).endDrag();
    assertEditor(
        "public class Test extends ViewForm {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      setTopRight(button);",
        "    }",
        "  }",
        "}");
  }
}
