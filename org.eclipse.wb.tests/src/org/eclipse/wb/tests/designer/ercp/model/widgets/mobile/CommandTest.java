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
package org.eclipse.wb.tests.designer.ercp.model.widgets.mobile;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.ercp.ErcpToolkitDescription;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.CommandInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;
import org.eclipse.wb.tests.designer.ercp.ErcpModelTest;

import java.util.List;

/**
 * Tests for {@link CommandInfo}.
 * 
 * @author scheglov_ke
 */
public class CommandTest extends ErcpModelTest {
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
  // Parse/refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parsing {@link CommandInfo} on {@link ControlInfo}.
   */
  public void test_parseOnControl() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Command command = new Command(this, Command.GENERAL, 0);",
            "    command.setText('My command');",
            "  }",
            "}");
    shell.refresh();
    // prepare CommandInfo
    CommandInfo commandInfo;
    {
      List<CommandInfo> commands = shell.getChildren(CommandInfo.class);
      assertEquals(1, commands.size());
      commandInfo = commands.get(0);
    }
    // check CommandInfo
    assertFalse(commandInfo.isGroup());
    {
      Object commandObject = commandInfo.getObject();
      assertEquals("My command", ReflectionUtils.invokeMethod2(commandObject, "getText"));
    }
    // IMenuPopupInfo
    {
      IMenuPopupInfo popupObject = MenuObjectInfoUtils.getMenuPopupInfo(commandInfo);
      assertNotNull(popupObject);
      assertSame(commandInfo, popupObject.getModel());
      assertNull(popupObject.getMenu());
      assertSame(commandInfo.getPresentation().getIcon(), popupObject.getImage());
      assertSame(16, popupObject.getBounds().width);
      assertSame(16, popupObject.getBounds().height);
    }
    // ask some random adapter
    assertNull(commandInfo.getAdapter(List.class));
  }

  /**
   * Parsing {@link CommandInfo} on other {@link CommandInfo}.
   */
  public void test_parseOnCommand() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Command commandGroup = new Command(this, Command.COMMANDGROUP, 0);",
            "    Command command = new Command(commandGroup, Command.GENERAL, 0);",
            "  }",
            "}");
    shell.refresh();
    // check for group CommandInfo
    CommandInfo commandGroup;
    {
      List<CommandInfo> commands = shell.getChildren(CommandInfo.class);
      assertEquals(1, commands.size());
      commandGroup = commands.get(0);
      assertTrue(commandGroup.isGroup());
    }
    // check for child CommandInfo
    CommandInfo command;
    {
      List<CommandInfo> commands = commandGroup.getChildren(CommandInfo.class);
      assertEquals(1, commands.size());
      command = commands.get(0);
      assertFalse(command.isGroup());
    }
    // IMenuItemInfo for "command"
    IMenuItemInfo itemObject;
    {
      itemObject = MenuObjectInfoUtils.getMenuItemInfo(command);
      assertNotNull(itemObject);
      assertSame(command, itemObject.getModel());
      // has presentation
      assertNotNull(itemObject.getImage());
      assertNotNull(itemObject.getBounds());
      // no child Command's
      assertNull(itemObject.getMenu());
    }
    // IMenuPopupInfo for "commandGroup"
    {
      IMenuPopupInfo popupObject = MenuObjectInfoUtils.getMenuPopupInfo(commandGroup);
      assertSame(commandGroup, popupObject.getModel());
      // get IMenuInfo
      IMenuInfo menuObject = popupObject.getMenu();
      assertNotNull(menuObject);
      assertSame(menuObject, menuObject.getModel());
      assertSame(popupObject.getPolicy(), menuObject.getPolicy());
      // presentation
      assertSame(commandGroup.getImage(), menuObject.getImage());
      assertSame(commandGroup.getBounds(), menuObject.getBounds());
      // items
      assertFalse(menuObject.isHorizontal());
      assertEquals(ImmutableList.of(itemObject), menuObject.getItems());
    }
  }

  /**
   * Test that we can prepare "item" presentation for {@link CommandInfo} with "image" property set.
   */
  public void test_withImage() throws Exception {
    ManagerUtils.ensure_SWTResourceManager(m_javaProject, ErcpToolkitDescription.INSTANCE);
    m_waitForAutoBuild = true;
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Command command = new Command(this, Command.GENERAL, 0);",
            "    command.setText('My command');",
            "    command.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(getClass(), '/javax/swing/plaf/basic/icons/JavaCup16.png'));",
            "  }",
            "}");
    shell.refresh();
    assertNoErrors(shell);
    // check CommandInfo
    CommandInfo command = shell.getChildren(CommandInfo.class).get(0);
    assertNotNull(ReflectionUtils.invokeMethod(command.getObject(), "getImage()"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Drop new {@link CommandInfo} on {@link ControlInfo}.
   */
  public void test_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    shell.refresh();
    // prepare command
    CommandInfo command = createJavaInfo("org.eclipse.ercp.swt.mobile.Command");
    // add command
    command.commandCreate(shell, null);
    assertEditor(
        "// filler filler filler",
        "class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Command command = new Command(this, Command.GENERAL, 0);",
        "      command.setText('New Command');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for moving {@link CommandInfo} in same {@link ControlInfo}.
   */
  public void test_MOVE_inControl() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Command command_1 = new Command(this, Command.GENERAL, 0);",
            "    Command command_2 = new Command(this, Command.GENERAL, 1);",
            "  }",
            "}");
    shell.refresh();
    // prepare CommandInfo's
    CommandInfo command_1;
    CommandInfo command_2;
    {
      List<CommandInfo> commands = shell.getChildren(CommandInfo.class);
      command_1 = commands.get(0);
      command_2 = commands.get(1);
    }
    // do move
    command_2.commandMove(shell, command_1);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    Command command_2 = new Command(this, Command.GENERAL, 1);",
        "    Command command_1 = new Command(this, Command.GENERAL, 0);",
        "  }",
        "}");
  }

  /**
   * Test for moving {@link CommandInfo} on new {@link ControlInfo}.
   */
  public void test_MOVE_onControl() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Command command = new Command(this, Command.GENERAL, 0);",
            "    Button button = new Button(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    // prepare children
    CommandInfo command = shell.getChildren(CommandInfo.class).get(0);
    ControlInfo button = shell.getChildrenControls().get(0);
    // do move
    command.commandMove(button, null);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    Button button = new Button(this, SWT.NONE);",
        "    Command command = new Command(button, Command.GENERAL, 0);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuPolicy operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When drop new {@link CommandInfo} on non-group {@link CommandInfo}, it should be converted to
   * "group".
   */
  public void test_IMenuPolicy_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Command command = new Command(this, Command.GENERAL, 0);",
            "  }",
            "}");
    shell.refresh();
    CommandInfo command = shell.getChildren(CommandInfo.class).get(0);
    IMenuPopupInfo commandObject = MenuObjectInfoUtils.getMenuPopupInfo(command);
    // add new CommandInfo
    CommandInfo newCommand = createJavaInfo("org.eclipse.ercp.swt.mobile.Command");
    assertTrue(commandObject.getPolicy().validateCreate(newCommand));
    commandObject.getPolicy().commandCreate(newCommand, null);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    Command command = new Command(this, Command.COMMANDGROUP, 0);",
        "    {",
        "      Command command_1 = new Command(command, Command.GENERAL, 0);",
        "      command_1.setText('New Command');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for moving {@link CommandInfo} in same group {@link CommandInfo}.
   */
  public void test_IMenuPolicy_MOVE_inGroup() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Command commandGroup = new Command(this, Command.COMMANDGROUP, 0);",
            "    Command command_1 = new Command(commandGroup, Command.GENERAL, 0);",
            "    Command command_2 = new Command(commandGroup, Command.GENERAL, 1);",
            "  }",
            "}");
    shell.refresh();
    // prepare CommandInfo's
    CommandInfo commandGroup;
    CommandInfo command_1;
    CommandInfo command_2;
    {
      commandGroup = shell.getChildren(CommandInfo.class).get(0);
      List<CommandInfo> commands = commandGroup.getChildren(CommandInfo.class);
      command_1 = commands.get(0);
      command_2 = commands.get(1);
    }
    // prepare IMenu objects
    IMenuPopupInfo commandGroupObject = MenuObjectInfoUtils.getMenuPopupInfo(commandGroup);
    IMenuItemInfo commandObject_1 = MenuObjectInfoUtils.getMenuItemInfo(command_1);
    // can not move random object on "commandGroup"
    assertFalse(commandGroupObject.getPolicy().validateMove(new Object()));
    // can not move "commandGroup" on its child "command_1"
    assertFalse(commandObject_1.getPolicy().validateMove(commandGroup));
    // move "command_2" before "command_1"
    assertTrue(commandGroupObject.getPolicy().validateMove(command_2));
    commandGroupObject.getPolicy().commandMove(command_2, command_1);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    Command commandGroup = new Command(this, Command.COMMANDGROUP, 0);",
        "    Command command_2 = new Command(commandGroup, Command.GENERAL, 1);",
        "    Command command_1 = new Command(commandGroup, Command.GENERAL, 0);",
        "  }",
        "}");
  }

  /**
   * When move one {@link CommandInfo} into other {@link CommandInfo}, it is converted into "group".
   */
  public void test_IMenuPolicy_MOVE_toGroup() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Command command_1 = new Command(this, Command.GENERAL, 0);",
            "    Command command_2 = new Command(this, Command.GENERAL, 1);",
            "  }",
            "}");
    shell.refresh();
    // prepare CommandInfo's
    CommandInfo command_1;
    CommandInfo command_2;
    {
      List<CommandInfo> commands = shell.getChildren(CommandInfo.class);
      command_1 = commands.get(0);
      command_2 = commands.get(1);
    }
    IMenuPopupInfo commandObject_1 = MenuObjectInfoUtils.getMenuPopupInfo(command_1);
    // move "command_2" into "command_1"
    assertTrue(commandObject_1.getPolicy().validateMove(command_2));
    commandObject_1.getPolicy().commandMove(command_2, null);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    Command command_1 = new Command(this, Command.COMMANDGROUP, 0);",
        "    Command command_2 = new Command(command_1, Command.GENERAL, 1);",
        "  }",
        "}");
  }

  /**
   * We can paste one {@link CommandInfo} into other {@link CommandInfo}.
   */
  public void test_IMenuPolicy_PASTE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Command command_1 = new Command(this, Command.GENERAL, 0);",
            "    Command command_2 = new Command(this, Command.GENERAL, 1);",
            "  }",
            "}");
    shell.refresh();
    // prepare CommandInfo's
    CommandInfo command_1;
    CommandInfo command_2;
    {
      List<CommandInfo> commands = shell.getChildren(CommandInfo.class);
      command_1 = commands.get(0);
      command_2 = commands.get(1);
    }
    IMenuPopupInfo commandObject_1 = MenuObjectInfoUtils.getMenuPopupInfo(command_1);
    // paste copy of "command_2" into "command_1"
    JavaInfoMemento memento = JavaInfoMemento.createMemento(command_2);
    List<JavaInfoMemento> mementos = ImmutableList.of(memento);
    assertTrue(commandObject_1.getPolicy().validatePaste(mementos));
    commandObject_1.getPolicy().commandPaste(mementos, null);
    assertEditor(
        "class Test extends Shell {",
        "  public Test() {",
        "    Command command_1 = new Command(this, Command.COMMANDGROUP, 0);",
        "    {",
        "      Command command = new Command(command_1, Command.GENERAL, 1);",
        "    }",
        "    Command command_2 = new Command(this, Command.GENERAL, 1);",
        "  }",
        "}");
  }

  /**
   * Don't allow to paste not {@link CommandInfo}.
   */
  public void test_IMenuPolicy_PASTE_notCommand() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    Command command = new Command(this, Command.GENERAL, 0);",
            "    Button button = new Button(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    // prepare models
    CommandInfo command = shell.getChildren(CommandInfo.class).get(0);
    ControlInfo button = shell.getChildrenControls().get(0);
    IMenuPopupInfo commandObject = MenuObjectInfoUtils.getMenuPopupInfo(command);
    // try to paste "button" into "command"
    JavaInfoMemento memento = JavaInfoMemento.createMemento(button);
    assertFalse(commandObject.getPolicy().validatePaste(ImmutableList.of(memento)));
  }
}