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
package org.eclipse.wb.tests.designer.ercp.gef;

import com.google.common.base.Predicate;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.CommandInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * GEF tests for {@link CommandInfo}.
 * 
 * @author scheglov_ke
 */
public class CommandGefTest extends ErcpGefTest {
  private CompositeInfo shell;
  private CommandInfo newCommand;

  // TODO clear all fields in DesignerEditorTestCase
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    shell = null;
    newCommand = null;
    super.tearDown();
  }

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
   * Simple {@link CommandInfo}, not a group.
   */
  public void test_select_onControl() throws Exception {
    shell =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Command command = new Command(this, Command.GENERAL, 0);",
            "  }",
            "}");
    CommandInfo commandInfo = shell.getChildren(CommandInfo.class).get(0);
    GraphicalEditPart commandPart = canvas.getEditPart(commandInfo);
    // initially "command" is not selected
    assertEquals(EditPart.SELECTED_NONE, commandPart.getSelected());
    assertEquals(0, commandPart.getChildren().size());
    // select: no children expected
    canvas.select(commandInfo);
    assertEquals(EditPart.SELECTED_PRIMARY, commandPart.getSelected());
    assertEquals(0, commandPart.getChildren().size());
  }

  /**
   * {@link CommandInfo}, deep in hierarchy, but still should be activated on selection in tree.
   */
  public void test_select_deep() throws Exception {
    shell =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Command command_1 = new Command(this, Command.COMMANDGROUP, 1);",
            "    Command command_2 = new Command(command_1, Command.COMMANDGROUP, 2);",
            "    Command command_3 = new Command(command_2, Command.GENERAL, 3);",
            "  }",
            "}");
    CommandInfo commandInfo_1 = shell.getChildren(CommandInfo.class).get(0);
    CommandInfo commandInfo_2 = commandInfo_1.getChildren(CommandInfo.class).get(0);
    CommandInfo commandInfo_3 = commandInfo_2.getChildren(CommandInfo.class).get(0);
    // initially not EditPart for "command_3"
    canvas.assertNullEditPart(commandInfo_3);
    // select "command_3" in tree, EditPart should be displayed and selected
    tree.select(commandInfo_3);
    assertEquals(EditPart.SELECTED_PRIMARY, canvas.getEditPart(commandInfo_3).getSelected());
  }

  /**
   * {@link CommandInfo} has {@link EditPart} with size 16x16, if several {@link CommandInfo}'s,
   * they have different location.
   */
  public void test_bounds() throws Exception {
    shell =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Command command_1 = new Command(this, Command.GENERAL, 1);",
            "    Command command_2 = new Command(this, Command.GENERAL, 2);",
            "  }",
            "}");
    CommandInfo commandInfo_1 = shell.getChildren(CommandInfo.class).get(0);
    CommandInfo commandInfo_2 = shell.getChildren(CommandInfo.class).get(1);
    GraphicalEditPart commandPart_1 = canvas.getEditPart(commandInfo_1);
    GraphicalEditPart commandPart_2 = canvas.getEditPart(commandInfo_2);
    // "command" is icon 16x16 on the right of client area
    {
      Rectangle commandBounds = commandPart_1.getFigure().getBounds();
      assertEquals(16, commandBounds.width);
      assertEquals(16, commandBounds.height);
    }
    // commands don't intersect
    assertFalse(commandPart_1.getFigure().getBounds().intersects(
        commandPart_2.getFigure().getBounds()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE: on Control
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adding new {@link CommandInfo} on "shell".
   */
  public void test_CREATE_OnControl_onCanvas() throws Exception {
    prepare_CREATE_OnControl();
    // create new "Command"
    canvas.moveTo(shell, 100, 100);
    canvas.click();
    assert_CREATE_OnControl();
  }

  /**
   * Adding new {@link CommandInfo} on "shell".
   */
  public void test_CREATE_OnControl_inTree() throws Exception {
    prepare_CREATE_OnControl();
    // create Command
    tree.moveOn(shell);
    tree.assertFeedback_on(shell);
    tree.click();
    assert_CREATE_OnControl();
  }

  private void prepare_CREATE_OnControl() throws Exception {
    shell =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "  // filler filler filler",
            "}");
    newCommand = loadCreationTool("org.eclipse.ercp.swt.mobile.Command");
  }

  private void assert_CREATE_OnControl() {
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Command command = new Command(this, Command.GENERAL, 0);",
        "      command.setText('New Command');",
        "    }",
        "  }",
        "  // filler filler filler",
        "}");
    assertNewCommandVisible();
  }

  /**
   * {@link EditPart} for "newCommand" exists and selected.
   */
  private void assertNewCommandVisible() {
    tree.assertNotNullEditPart(newCommand);
    tree.assertPrimarySelected(newCommand);
    canvas.assertNotNullEditPart(newCommand);
    canvas.assertPrimarySelected(newCommand);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE: deep
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add new {@link CommandInfo} into "popup" {@link CommandInfo}.
   */
  public void test_CREATE_deep_onCanvas() throws Exception {
    prepare_CREATE_deep();
    CommandInfo command_1 = shell.getChildren(CommandInfo.class).get(0);
    CommandInfo command_2 = command_1.getChildren(CommandInfo.class).get(0);
    // initially "command_2" is not visible
    canvas.assertNullEditPart(command_2);
    // move on "command_1": drop down appears, EditPart for "command_2" appears
    canvas.moveTo(command_1);
    canvas.assertNotNullEditPart(command_2);
    // move before "command_2"
    canvas.moveTo(command_2);
    {
      Predicate<Figure> predicate = canvas.getLinePredicate(command_2, IPositionConstants.TOP);
      canvas.assertFigures(IEditPartViewer.MENU_FEEDBACK_LAYER, predicate);
    }
    // click, so finish creation
    canvas.click();
    assert_CREATE_deep();
  }

  /**
   * Add new {@link CommandInfo} into "popup" {@link CommandInfo}.
   */
  public void test_CREATE_deep_inTree() throws Exception {
    prepare_CREATE_deep();
    CommandInfo command_1 = shell.getChildren(CommandInfo.class).get(0);
    CommandInfo command_2 = command_1.getChildren(CommandInfo.class).get(0);
    //
    tree.moveBefore(command_2);
    tree.assertFeedback_before(command_2);
    tree.click();
    assert_CREATE_deep();
  }

  private void assert_CREATE_deep() {
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Command command_1 = new Command(this, Command.COMMANDGROUP, 1);",
        "    command_1.setText('Command 1');",
        "    {",
        "      Command command = new Command(command_1, Command.GENERAL, 0);",
        "      command.setText('New Command');",
        "    }",
        "    Command command_2 = new Command(command_1, Command.GENERAL, 2);",
        "    command_2.setText('Command 2');",
        "  }",
        "}");
    assertNewCommandVisible();
  }

  private void prepare_CREATE_deep() throws Exception {
    shell =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Command command_1 = new Command(this, Command.COMMANDGROUP, 1);",
            "    command_1.setText('Command 1');",
            "    Command command_2 = new Command(command_1, Command.GENERAL, 2);",
            "    command_2.setText('Command 2');",
            "  }",
            "}");
    // begin creating Command
    newCommand = loadCreationTool("org.eclipse.ercp.swt.mobile.Command");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Move one {@link CommandInfo} before other on same {@link ControlInfo}.
   */
  public void test_MOVE_beforeOther() throws Exception {
    shell =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Command command_1 = new Command(this, Command.GENERAL, 1);",
            "    Command command_2 = new Command(this, Command.GENERAL, 2);",
            "  }",
            "}");
    CommandInfo commandInfo_1 = shell.getChildren(CommandInfo.class).get(0);
    CommandInfo commandInfo_2 = shell.getChildren(CommandInfo.class).get(1);
    // move "command_2" before "command_1"
    canvas.beginDrag(commandInfo_2).dragTo(commandInfo_1);
    canvas.assertFeedbacks(canvas.getLinePredicate(commandInfo_1, IPositionConstants.LEFT));
    canvas.endDrag();
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Command command_2 = new Command(this, Command.GENERAL, 2);",
        "    Command command_1 = new Command(this, Command.GENERAL, 1);",
        "  }",
        "}");
  }

  /**
   * Move {@link CommandInfo} on other {@link ControlInfo}.
   */
  public void test_MOVE_onOtherControl() throws Exception {
    shell =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setText('Button 1');",
            "      Command command_1 = new Command(button_1, Command.GENERAL, 1);",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "      button_2.setText('Button 2');",
            "      Command command_2 = new Command(button_2, Command.GENERAL, 2);",
            "    }",
            "  }",
            "}");
    ControlInfo buttonInfo_1 = shell.getChildrenControls().get(0);
    ControlInfo buttonInfo_2 = shell.getChildrenControls().get(1);
    CommandInfo commandInfo_1 = buttonInfo_1.getChildren(CommandInfo.class).get(0);
    CommandInfo commandInfo_2 = buttonInfo_2.getChildren(CommandInfo.class).get(0);
    // move "command_2" before "command_1"
    canvas.beginDrag(commandInfo_2).dragTo(commandInfo_1, 17, 0);
    canvas.assertFeedbacks(canvas.getLinePredicate(commandInfo_1, IPositionConstants.RIGHT));
    canvas.endDrag();
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button 1');",
        "      Command command_1 = new Command(button_1, Command.GENERAL, 1);",
        "      Command command_2 = new Command(button_1, Command.GENERAL, 2);",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button 2');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Move {@link CommandInfo} on other {@link CommandInfo}.
   */
  public void test_MOVE_onCommand() throws Exception {
    shell =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setText('Button 1');",
            "      Command command_1 = new Command(button_1, Command.GENERAL, 1);",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "      button_2.setText('Button 2');",
            "      Command command_2 = new Command(button_2, Command.GENERAL, 2);",
            "    }",
            "  }",
            "}");
    ControlInfo buttonInfo_1 = shell.getChildrenControls().get(0);
    ControlInfo buttonInfo_2 = shell.getChildrenControls().get(1);
    CommandInfo commandInfo_1 = buttonInfo_1.getChildren(CommandInfo.class).get(0);
    CommandInfo commandInfo_2 = buttonInfo_2.getChildren(CommandInfo.class).get(0);
    // do move, no exception in console
    MenuObjectInfoUtils.getMenuPopupInfo(commandInfo_1).getPolicy().commandMove(commandInfo_2, null);
  }
}
