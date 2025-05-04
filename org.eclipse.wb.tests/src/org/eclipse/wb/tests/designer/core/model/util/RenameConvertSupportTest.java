/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model.util;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.util.RenameConvertSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import static org.mockito.Mockito.mock;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableRunnable;
import org.junit.Test;

import java.util.List;

/**
 * Tests for {@link RenameConvertSupport}.
 *
 * @author scheglov_ke
 */
public class RenameConvertSupportTest extends SwingModelTest {
	
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
	 * Request {@link RenameConvertSupport} with zero or one component.
	 */
	@Test
	public void test_action_zeroOrOne() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// no objects -> action
		{
			assertNull(getRenameAction());
		}
		// no JavaInfo's -> action
		{
			ObjectInfo objectMock = mock(ObjectInfo.class);
			assertNull(getRenameAction(objectMock));
		}
		// give JavaInfo -> receive action
		{
			assertNotNull(getRenameAction(button));
		}
		// ask action using broadcast (without objects)
		{
			MenuManager manager = getDesignerMenuManager();
			button.getBroadcastObject().addContextMenu(null, button, manager);
			assertNull(findChildAction(manager, "Rename..."));
		}
		// ask action using broadcast (good)
		{
			MenuManager manager = getDesignerMenuManager();
			button.getBroadcastObject().addContextMenu(List.of(button), button, manager);
			assertNotNull(findChildAction(manager, "Rename..."));
		}
	}

	/**
	 * Test for {@link RenameConvertSupport} action.
	 */
	@Test
	public void test_action_hashEquals() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		//
		IAction action = getRenameAction(button);
		assertNotNull(action);
		// test known behavior of equals/hashCode
		assertEquals(0, action.hashCode());
		assertEquals(action, mock(action.getClass()));
	}

	/**
	 * Request {@link RenameConvertSupport} with two components.
	 */
	@Test
	public void test_action_multiSelect() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    add(button);",
						"    //",
						"    JTextField textField = new JTextField();",
						"    add(textField);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		ComponentInfo textField = panel.getChildrenComponents().get(1);
		// ask directly RenameConvertSupport
		assertNotNull(getRenameAction(button, textField));
		// ask using broadcast
		{
			List<ComponentInfo> objects = List.of(button, textField);
			{
				MenuManager manager = getDesignerMenuManager();
				button.getBroadcastObject().addContextMenu(objects, button, manager);
				assertNotNull(findChildAction(manager, "Rename..."));
			}
			{
				MenuManager manager = getDesignerMenuManager();
				button.getBroadcastObject().addContextMenu(objects, textField, manager);
				assertNotNull(findChildAction(manager, "Rename..."));
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// UI
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Open dialog using {@link RenameConvertSupport#rename(List)}.
	 */
	@Test
	public void test_animateUI_openDialog() throws Exception {
		parseContainer(
				"// filler filler filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		final ComponentInfo button = getJavaInfoByName("button");
		// animate
		new UiContext().executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() {
				RenameConvertSupport.rename(List.of(button));
			}
		}, new FailableConsumer<>() {
			@Override
			public void accept(SWTBot bot) {
				SWTBot shell = bot.shell("Rename/convert").bot();
				shell.button("Cancel").click();
			}
		});
		waitEventLoop(10);
	}

	/**
	 * Set new name.
	 */
	@Test
	public void test_animateUI_setName() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		// prepare action
		final IAction renameAction = getRenameAction(button);
		assertNotNull(renameAction);
		// animate
		new UiContext().executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() {
				renameAction.run();
			}
		}, new FailableConsumer<>() {
			@Override
			public void accept(SWTBot bot) {
				SWTBot shell = bot.shell("Rename/convert").bot();
				{
					SWTBotText nameField = shell.text("button");
					nameField.setText("myButton");
				}
				shell.button("OK").click();
			}
		});
		waitEventLoop(10);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton myButton = new JButton();",
				"    add(myButton);",
				"  }",
				"}");
	}

	/**
	 * Convert local -> field.
	 */
	@Test
	public void test_animateUI_toField() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		// prepare action
		final IAction renameAction = getRenameAction(button);
		assertNotNull(renameAction);
		// animate
		new UiContext().executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() {
				renameAction.run();
			}
		}, new FailableConsumer<>() {
			@Override
			public void accept(SWTBot bot) {
				SWTBot shell = bot.shell("Rename/convert").bot();
				{
					SWTBotToolbarButton item = shell.toolbarRadioButtonWithTooltip("Be field");
					item.click();
				}
				shell.button("OK").click();
			}
		});
		waitEventLoop(10);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton button;",
				"  public Test() {",
				"    button = new JButton();",
				"    add(button);",
				"  }",
				"}");
	}

	/**
	 * Set new name.
	 */
	@Test
	public void test_animateUI_setName_lazy() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  private JButton button;",
				"  public Test() {",
				"    add(getButton());",
				"  }",
				"  private JButton getButton() {",
				"    if (button == null) {",
				"      button = new JButton();",
				"    }",
				"    return button;",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		// prepare action
		final IAction renameAction = getRenameAction(button);
		assertNotNull(renameAction);
		// animate
		new UiContext().executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() {
				renameAction.run();
			}
		}, new FailableConsumer<>() {
			@Override
			public void accept(SWTBot bot) {
				SWTBot shell = bot.shell("Rename/convert").bot();
				{
					SWTBotText nameField = shell.text("button");
					nameField.setText("myButton");
				}
				// "lazy" can not be converted to local/field
				assertFalse(shell.toolbarRadioButtonWithTooltip("Be local").isEnabled());
				assertFalse(shell.toolbarRadioButtonWithTooltip("Be field").isEnabled());
				shell.button("OK").click();
			}
		});
		waitEventLoop(10);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton myButton;",
				"  public Test() {",
				"    add(getMyButton());",
				"  }",
				"  private JButton getMyButton() {",
				"    if (myButton == null) {",
				"      myButton = new JButton();",
				"    }",
				"    return myButton;",
				"  }",
				"}");
	}

	private static IAction getRenameAction(JavaInfo javaInfo) throws Exception {
		IMenuManager contextMenu = getContextMenu(javaInfo);
		return findChildAction(contextMenu, "Rename...");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands: execute
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that if there are no commands, no refresh will happen on "execute" request.
	 */
	@Test
	public void test_commands_executeNoCommands() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		// add listener
		panel.addBroadcastListener(new ObjectEventListener() {
			@Override
			public void refreshed() throws Exception {
				fail("No refresh expected.");
			}
		});
		// execute
		RenameConvertSupport support = getRenameSupport(panel);
		ReflectionUtils.invokeMethod(support, "executeCommands()");
	}

	/**
	 * We should return same command for all requests with same {@link JavaInfo}.
	 */
	@Test
	public void test_commands_sameCommand() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		RenameConvertSupport support = getRenameSupport(panel);
		// check for same command
		Object command = getCommand(support, panel);
		assertSame(command, getCommand(support, panel));
	}

	/**
	 * Set new name, single component.
	 */
	@Test
	public void test_commands_setName_single() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		RenameConvertSupport support = getRenameSupport(button);
		// set new name
		Object command = getCommand(support, button);
		ReflectionUtils.invokeMethod2(command, "setName", String.class, "myButton");
		// execute commands
		ReflectionUtils.invokeMethod(support, "executeCommands()");
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton myButton = new JButton();",
				"    add(myButton);",
				"  }",
				"}");
	}

	/**
	 * Set new name, auto-generate unique.
	 */
	@Test
	public void test_commands_setName_autoUnique() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    int myButton;",
				"    //",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		RenameConvertSupport support = getRenameSupport(button);
		// set new name
		Object command = getCommand(support, button);
		ReflectionUtils.invokeMethod2(command, "setName", String.class, "myButton");
		// execute commands
		ReflectionUtils.invokeMethod(support, "executeCommands()");
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    int myButton;",
				"    //",
				"    JButton myButton_1 = new JButton();",
				"    add(myButton_1);",
				"  }",
				"}");
	}

	/**
	 * Convert local -> field.
	 */
	@Test
	public void test_commands_toField() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		RenameConvertSupport support = getRenameSupport(button);
		// convert to field
		Object command = getCommand(support, button);
		ReflectionUtils.invokeMethod2(command, "toField");
		// execute commands
		ReflectionUtils.invokeMethod(support, "executeCommands()");
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton button;",
				"  public Test() {",
				"    button = new JButton();",
				"    add(button);",
				"  }",
				"}");
	}

	/**
	 * Convert field -> local.
	 */
	@Test
	public void test_commands_toLocal() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  private JButton button;",
				"  public Test() {",
				"    button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		RenameConvertSupport support = getRenameSupport(button);
		// convert to local
		Object command = getCommand(support, button);
		ReflectionUtils.invokeMethod2(command, "toLocal");
		// execute commands
		ReflectionUtils.invokeMethod(support, "executeCommands()");
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
	}

	/**
	 * Conversion local -> field -> local is ignored.
	 */
	@Test
	public void test_commands_toField_toLocal() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		String initialSource = m_lastEditor.getSource();
		ComponentInfo button = getJavaInfoByName("button");
		RenameConvertSupport support = getRenameSupport(button);
		// convert
		Object command = getCommand(support, button);
		ReflectionUtils.invokeMethod2(command, "toField");
		ReflectionUtils.invokeMethod2(command, "toLocal");
		// execute commands
		ReflectionUtils.invokeMethod(support, "executeCommands()");
		assertEditor(initialSource, m_lastEditor);
	}

	/**
	 * Conversion field -> local -> field is ignored.
	 */
	@Test
	public void test_commands_toLocal_toField() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  private JButton button;",
				"  public Test() {",
				"    button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		String initialSource = m_lastEditor.getSource();
		ComponentInfo button = getJavaInfoByName("button");
		RenameConvertSupport support = getRenameSupport(button);
		// convert
		Object command = getCommand(support, button);
		ReflectionUtils.invokeMethod2(command, "toLocal");
		ReflectionUtils.invokeMethod2(command, "toField");
		// execute commands
		ReflectionUtils.invokeMethod(support, "executeCommands()");
		assertEditor(initialSource, m_lastEditor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands: validate
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * No commands -> all OK.
	 */
	@Test
	public void test_validate_OK() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		RenameConvertSupport support = getRenameSupport(button);
		// no any command, so OK
		assertNull(validateCommands(support));
	}

	/**
	 * Attempt to set invalid identifier.
	 */
	@Test
	public void test_validate_invalidIdentifier() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		RenameConvertSupport support = getRenameSupport(button);
		// add command
		Object command = getCommand(support, button);
		ReflectionUtils.invokeMethod2(command, "setName", String.class, "invalid-name");
		// validate commands
		assertTrue(validateCommands(support).contains("identifier"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link RenameConvertSupport} for given objects.
	 */
	private RenameConvertSupport getRenameSupport(ObjectInfo... objects) throws Exception {
		return ReflectionUtils.newInstance(RenameConvertSupport.class, "<init>(java.lang.Iterable)", List.of(objects));
	}

	/**
	 * @return the <code>RenameCommand</code> for given {@link JavaInfo}.
	 */
	private static Object getCommand(RenameConvertSupport support, JavaInfo javaInfo)
			throws Exception {
		return ReflectionUtils.invokeMethod2(support, "getCommand", JavaInfo.class, javaInfo);
	}

	/**
	 * @return the error message for validating commands, or <code>null</code>.
	 */
	private static String validateCommands(RenameConvertSupport support) throws Exception {
		return (String) ReflectionUtils.invokeMethod2(support, "validateCommands");
	}

	/**
	 * @return the {@link RenameConvertSupport} action for given objects.
	 */
	private IAction getRenameAction(ObjectInfo... objects) {
		// prepare manager
		MenuManager menuManager = getDesignerMenuManager();
		// add action
		RenameConvertSupport.contribute(List.of(objects), menuManager);
		return findChildAction(menuManager, "Rename...");
	}
}
