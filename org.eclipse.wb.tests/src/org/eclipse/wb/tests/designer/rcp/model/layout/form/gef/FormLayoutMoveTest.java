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
package org.eclipse.wb.tests.designer.rcp.model.layout.form.gef;

import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.swt.gef.policy.layout.form.FormLayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GEF tests for FormLayout support moving.
 * 
 * @author mitin_aa
 */
public class FormLayoutMoveTest extends RcpGefTest {
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
  public void test_move_with_both_sides_attached() throws Exception {
    CompositeInfo shell =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    Button button = new Button(this, SWT.NONE);",
            "    button.setText('New Button');",
            "    FormData data = new FormData();",
            "    data.bottom = new FormAttachment(100, -100);",
            "    data.left = new FormAttachment(0, 133);",
            "    data.right = new FormAttachment(100, -200);",
            "    button.setLayoutData(data);",
            "  }",
            "}");
    ControlInfo button = (ControlInfo) shell.getChildrenJava().get(1);
    moveByRight(shell, button, 400, 100);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    Button button = new Button(this, SWT.NONE);",
        "    button.setText('New Button');",
        "    FormData data = new FormData();",
        "    data.top = new FormAttachment(0, 100);",
        "    data.right = new FormAttachment(100, -50);",
        "    data.left = new FormAttachment(0, 283);",
        "    button.setLayoutData(data);",
        "  }",
        "}");
  }

  public void test_move_with_both_sides_attached_left_control_attached() throws Exception {
    prepareComponent();
    CompositeInfo shell =
        openComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    setLayout(new FormLayout());",
            "    //",
            "    Button button1 = new Button(this, SWT.NONE);",
            "    button1.setText('Button 1');",
            "    //",
            "    Button button2 = new Button(this, SWT.NONE);",
            "    button2.setText('Button 2');",
            "    //",
            "    FormData data1 = new FormData();",
            "    data1.bottom = new FormAttachment(100, -50);",
            "    data1.right = new FormAttachment(100, -275);",
            "    data1.left = new FormAttachment(button2, 0, SWT.LEFT);",
            "    button1.setLayoutData(data1);",
            "    //",
            "    FormData data2 = new FormData();",
            "    data2.top = new FormAttachment(0, 100);",
            "    data2.left = new FormAttachment(0, 50);",
            "    button2.setLayoutData(data2);",
            "  }",
            "}");
    ControlInfo button = shell.getChildrenControls().get(0);
    canvas.beginMove(button);
    canvas.target(shell).rightSide().inX(-50).inY(100).drag();
    canvas.endDrag();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setLayout(new FormLayout());",
        "    //",
        "    Button button1 = new Button(this, SWT.NONE);",
        "    button1.setText('Button 1');",
        "    //",
        "    Button button2 = new Button(this, SWT.NONE);",
        "    button2.setText('Button 2');",
        "    //",
        "    FormData data1 = new FormData();",
        "    data1.bottom = new FormAttachment(100, -41);",
        "    data1.right = new FormAttachment(100, -274);",
        "    data1.left = new FormAttachment(button2, 0, SWT.LEFT);",
        "    button1.setLayoutData(data1);",
        "    //",
        "    FormData data2 = new FormData();",
        "    data2.top = new FormAttachment(0, 90);",
        "    data2.left = new FormAttachment(0, 43);",
        "    button2.setLayoutData(data2);",
        "  }",
        "}");
  }

  public void test_move_left_to_container() throws Exception {
    ControlInfo shell = openEditor1(100);
    ControlInfo button = (ControlInfo) shell.getChildrenJava().get(1);
    moveByLeft(shell, button, 0, 100, 1, 1);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New Button');",
        "      FormData data = new FormData();",
        "      data.top = new FormAttachment(0, 99);",
        "      data.left = new FormAttachment(0);",
        "      button.setLayoutData(data);",
        "    }",
        "  }",
        "}");
  }

  public void test_attach_to_component_seq_left_to_right() throws Exception {
    ControlInfo shell = openEditor3();
    ControlInfo button1 = (ControlInfo) shell.getChildrenJava().get(1);
    ControlInfo button2 = (ControlInfo) shell.getChildrenJava().get(2);
    //
    int button1Right = button1.getAbsoluteBounds().right();
    moveByLeft(shell, button2, button1Right + 5, 100);
    assertThat(m_lastEditor.getSource()).contains("data2.left = new FormAttachment(button, 6);");
  }

  public void test_attach_to_component_par_left_to_left() throws Exception {
    ControlInfo shell = openEditor3();
    ControlInfo button2 = (ControlInfo) shell.getChildrenJava().get(2);
    //
    moveByLeft(shell, button2, 50, 150);
    assertThat(m_lastEditor.getSource()).contains(
        "data2.left = new FormAttachment(button, 0, SWT.LEFT);");
  }

  public void _test_delete_with_another_attached() throws Exception {
    prepareComponent();
    CompositeInfo shell =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    //",
            "    Button button = new Button(this, SWT.NONE);",
            "    button.setText('New Button');",
            "    //",
            "    FormData data = new FormData();",
            "    data.top = new FormAttachment(0, 100);",
            "    data.left = new FormAttachment(0, 50);",
            "    button.setLayoutData(data);",
            "    //",
            "    Button button2 = new Button(this, SWT.NONE);",
            "    button2.setText('New Button2');",
            "    //",
            "    FormData data2 = new FormData();",
            "    data2.top = new FormAttachment(0, 0);",
            "    data2.left = new FormAttachment(button, 10);",
            "    button2.setLayoutData(data2);",
            "  }",
            "}");
    ControlInfo button = shell.getChildrenControls().get(0);
    // delete
    button.delete();
    // test
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    //",
        "    Button button2 = new Button(this, SWT.NONE);",
        "    button2.setText(\"New Button2\");",
        "    //",
        "    FormData data2 = new FormData();",
        "    data2.left = new FormAttachment(0, 135);",
        "    data2.top = new FormAttachment(0, 0);",
        "    button2.setLayoutData(data2);",
        "  }",
        "}");
  }

  public void test_move_from_left_to_right_by_half_of_shell() throws Exception {
    ControlInfo shell = openEditor1(100);
    ControlInfo button = (ControlInfo) shell.getChildrenJava().get(1);
    moveByRight(shell, button, 340, 100);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText(\"New Button\");",
        "      FormData data = new FormData();",
        "      data.top = new FormAttachment(0, 100);",
        "      data.right = new FormAttachment(100, -110);",
        "      button.setLayoutData(data);",
        "    }",
        "  }",
        "}");
  }

  public void test_move_twice() throws Exception {
    ControlInfo shell = openEditor1(100);
    ControlInfo button = (ControlInfo) shell.getChildrenJava().get(1);
    moveByLeft(shell, button, 50, 100);
    canvas.select();
    moveByLeft(shell, button, 10, 100, 1, 1);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText(\"New Button\");",
        "      FormData data = new FormData();",
        "      data.top = new FormAttachment(0, 99);",
        "      data.left = new FormAttachment(0, 10);",
        "      button.setLayoutData(data);",
        "    }",
        "  }",
        "}");
  }

  public void test_move_leftAttached_to_right_50_100() throws Exception {
    ControlInfo shell = openEditor1(50);
    ControlInfo button = (ControlInfo) shell.getChildrenJava().get(1);
    moveByLeft(shell, button, 100, 100);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText(\"New Button\");",
        "      FormData data = new FormData();",
        "      data.top = new FormAttachment(0, 100);",
        "      data.left = new FormAttachment(0, 100);",
        "      button.setLayoutData(data);",
        "    }",
        "  }",
        "}");
  }

  public void test_move_right_to_container() throws Exception {
    ControlInfo shell = openEditor1(100);
    ControlInfo button = (ControlInfo) shell.getChildrenJava().get(1);
    moveByRight(shell, button, 450, 100);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText(\"New Button\");",
        "      FormData data = new FormData();",
        "      data.top = new FormAttachment(0, 100);",
        "      data.right = new FormAttachment(100);",
        "      button.setLayoutData(data);",
        "    }",
        "  }",
        "}");
  }

  public void test_move_from_right_to_left_by_half_of_shell() throws Exception {
    ControlInfo shell = openEditor2(100);
    // prepare objects
    ControlInfo button = (ControlInfo) shell.getChildrenJava().get(1);
    moveByLeft(shell, button, 50, 100);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText(\"New Button\");",
        "      FormData data = new FormData();",
        "      data.top = new FormAttachment(0, 100);",
        "      data.left = new FormAttachment(0, 50);",
        "      button.setLayoutData(data);",
        "    }",
        "  }",
        "}");
  }

  public void test_move_left_to_0() throws Exception {
    ControlInfo shell = openEditor1(100);
    ControlInfo button = (ControlInfo) shell.getChildrenJava().get(1);
    moveByLeft(shell, button, 40, 100);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText(\"New Button\");",
        "      FormData data = new FormData();",
        "      data.top = new FormAttachment(0, 100);",
        "      data.left = new FormAttachment(0, 40);",
        "      button.setLayoutData(data);",
        "    }",
        "  }",
        "}");
  }

  public void test_move_left_to_container_gap() throws Exception {
    ControlInfo shell = openEditor1(100);
    // prepare objects
    ControlInfo button = (ControlInfo) shell.getChildrenJava().get(1);
    int containerGap = getEditPolicy(shell).getContainerGapValue(shell, IPositionConstants.LEFT);
    moveByLeft(shell, button, containerGap, 100);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText(\"New Button\");",
        "      FormData data = new FormData();",
        "      data.top = new FormAttachment(0, 100);",
        "      data.left = new FormAttachment(0, " + containerGap + ");",
        "      button.setLayoutData(data);",
        "    }",
        "  }",
        "}");
  }

  public void test_move_right_to_container_gap() throws Exception {
    ControlInfo shell = openEditor1(100);
    ControlInfo button = (ControlInfo) shell.getChildrenJava().get(1);
    int containerGap = getEditPolicy(shell).getContainerGapValue(button, IPositionConstants.RIGHT);
    moveByRight(shell, button, 450 - containerGap - 1, 100);
    // done drag
    canvas.endDrag();
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText(\"New Button\");",
        "      FormData data = new FormData();",
        "      data.top = new FormAttachment(0, 100);",
        "      data.right = new FormAttachment(100, -" + containerGap + ");",
        "      button.setLayoutData(data);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates the editor with one button attached by left at offset <code>modelX</code>.
   */
  private CompositeInfo openEditor1(int modelX) throws Exception {
    return openComposite(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New Button');",
        "      FormData data = new FormData();",
        "      data.left = new FormAttachment(0, " + modelX + ");",
        "      button.setLayoutData(data);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Creates the editor with one button attached by right at offset <code>modelX</code>.
   */
  private CompositeInfo openEditor2(int modelX) throws Exception {
    return openComposite(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setText('New Button');",
        "      FormData data = new FormData();",
        "      data.right = new FormAttachment(100, -" + modelX + ");",
        "      button.setLayoutData(data);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Creates the editor with two buttons
   */
  private CompositeInfo openEditor3() throws Exception {
    return openComposite(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    Button button = new Button(this, SWT.NONE);",
        "    button.setText('New Button');",
        "    FormData data = new FormData();",
        "    data.top = new FormAttachment(0, 100);",
        "    data.left = new FormAttachment(0, 50);",
        "    button.setLayoutData(data);",
        "    Button button2 = new Button(this, SWT.NONE);",
        "    button2.setText('New Button2');",
        "    FormData data2 = new FormData();",
        "    data2.top = new FormAttachment(0, 0);",
        "    data2.left = new FormAttachment(0, 200);",
        "    button2.setLayoutData(data2);",
        "  }",
        "}");
  }

  private void moveByLeft(ControlInfo parent, ControlInfo widget, int modelX, int modelY)
      throws Exception {
    moveByLeft(parent, widget, modelX, modelY, 0, 0);
  }

  private void moveByLeft(ControlInfo parent,
      ControlInfo widget,
      int modelX,
      int modelY,
      int widgetX,
      int widgetY) throws Exception {
    // prepare EditParts
    GraphicalEditPart parentPart = canvas.getEditPart(parent);
    GraphicalEditPart widgetPart = canvas.getEditPart(widget);
    // drag "button"
    canvas.beginDrag(widgetPart, widgetX, widgetY);
    Insets parentInsets = parent.getClientAreaInsets();
    int gefX = parentInsets.left + modelX;
    int gefY = parentInsets.top + modelY;
    // do drag twice, it updates the mouse move direction in SnapPoints
    Point currentLocation = widgetPart.getFigure().getLocation();
    int deltaX = currentLocation.x - gefX;
    int deltaY = currentLocation.y - gefY;
    dragNorm(parentPart, currentLocation.x - deltaX / 2, currentLocation.y - deltaY / 2);
    dragNorm(parentPart, gefX, gefY);
    canvas.assertCommandNotNull();
    // done drag
    canvas.endDrag();
  }

  /**
   * modelX and modelY are offsets of the right side of the widget relative to client are of the
   * parent.
   * 
   * @param modelX
   * @param modelY
   */
  private void moveByRight(ControlInfo parent, ControlInfo widget, int modelX, int modelY)
      throws Exception {
    // prepare EditParts
    GraphicalEditPart parentPart = canvas.getEditPart(parent);
    GraphicalEditPart widgetPart = canvas.getEditPart(widget);
    // drag "button"
    canvas.beginDrag(widgetPart);
    Rectangle widgetBounds = widgetPart.getFigure().getBounds();
    Insets parentInsets = parent.getClientAreaInsets();
    int gefX = modelX - widgetBounds.width - parentInsets.left;
    int gefY = parentInsets.top + modelY;
    // do drag twice, it updates the mouse move direction in SnapPoints
    Point currentLocation = widgetPart.getFigure().getLocation();
    int deltaX = currentLocation.x - gefX;
    int deltaY = currentLocation.y - gefY;
    dragNorm(parentPart, currentLocation.x - deltaX / 2, currentLocation.y - deltaY / 2);
    dragNorm(parentPart, gefX, gefY);
    canvas.assertCommandNotNull();
    // done drag
    canvas.endDrag();
  }

  @SuppressWarnings("unchecked")
  private FormLayoutEditPolicy<ControlInfo> getEditPolicy(ControlInfo parent) {
    GraphicalEditPart parentPart = canvas.getEditPart(parent);
    return (FormLayoutEditPolicy<ControlInfo>) parentPart.getEditPolicy(EditPolicy.LAYOUT_ROLE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void dragNorm(Object object, int deltaX, int deltaY) {
    Point location = canvas.getLocation(object);
    location.translate(deltaX, deltaY);
    canvas.dragTo(location.x, location.y);
  }
}
