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
package org.eclipse.wb.tests.designer.swing.model.bean;

import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo;
import org.eclipse.wb.internal.swing.model.bean.ActionInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JToolBarInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuBarInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuItemInfo;
import org.eclipse.wb.internal.swing.palette.ActionExternalEntryInfo;
import org.eclipse.wb.internal.swing.palette.ActionNewEntryInfo;
import org.eclipse.wb.internal.swing.palette.ActionUseEntryInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swtbot.swt.finder.SWTBot;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableRunnable;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Test for {@link ActionInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class ActionGefTest extends SwingGefTest {
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
	// JToolBar
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ActionNewEntryInfo}.
	 */
	@Test
	public void test_JToolBar_ActionNewEntryInfo() throws Exception {
		createExternalAction();
		ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							JToolBar toolBar = new JToolBar();
							add(toolBar, BorderLayout.NORTH);
						}
					}
				}""");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/setLayout(new BorderLayout())/ /add(toolBar, BorderLayout.NORTH)/}",
				"  {new: java.awt.BorderLayout} {empty} {/setLayout(new BorderLayout())/}",
				"  {new: javax.swing.JToolBar} {local-unique: toolBar} {/new JToolBar()/ /add(toolBar, BorderLayout.NORTH)/}");
		panel.refresh();
		JToolBarInfo toolBar = (JToolBarInfo) panel.getChildrenComponents().get(0);
		// load "action" tool
		{
			ActionNewEntryInfo entry = new ActionNewEntryInfo();
			entry.initialize(m_viewerCanvas, panel);
			Tool tool = entry.createTool();
			m_viewerCanvas.getEditDomain().setActiveTool(tool);
		}
		// drop new "action" on "toolBar"...
		{
			canvas.target(panel).in(100, 100).move();
			canvas.target(toolBar).in(20, 5).move();
			canvas.click();
		}
		assertEditor("""
				public class Test extends JPanel {
					private final Action action = new SwingAction();
					public Test() {
						setLayout(new BorderLayout());
						{
							JToolBar toolBar = new JToolBar();
							add(toolBar, BorderLayout.NORTH);
							{
								JButton button = toolBar.add(action);
							}
						}
					}
					private class SwingAction extends AbstractAction {
						public SwingAction() {
							putValue(NAME, "SwingAction");
							putValue(SHORT_DESCRIPTION, "Some short description");
						}
						public void actionPerformed(ActionEvent e) {
						}
					}
				}""");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/setLayout(new BorderLayout())/ /add(toolBar, BorderLayout.NORTH)/}",
				"  {new: java.awt.BorderLayout} {empty} {/setLayout(new BorderLayout())/}",
				"  {new: javax.swing.JToolBar} {local-unique: toolBar} {/new JToolBar()/ /add(toolBar, BorderLayout.NORTH)/ /toolBar.add(action)/}",
				"    {implicit-factory} {local-unique: button} {/toolBar.add(action)/}",
				"  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}",
				"    {innerAction} {field-initializer: action} {/new SwingAction()/ /toolBar.add(action)/}");
		// ...new "JButton" created, it should be selected
		ComponentInfo button = toolBar.getChildrenComponents().get(0);
		canvas.assertPrimarySelected(button);
	}

	/**
	 * Test for {@link ActionUseEntryInfo}.
	 */
	@Test
	public void test_JToolBar_ActionUseEntryInfo_canvas() throws Exception {
		createExternalAction();
		ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					private ExternalAction action = new ExternalAction();
					public Test() {
						setLayout(new BorderLayout());
						{
							JToolBar toolBar = new JToolBar();
							add(toolBar, BorderLayout.NORTH);
						}
					}
				}""");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/setLayout(new BorderLayout())/ /add(toolBar, BorderLayout.NORTH)/}",
				"  {new: java.awt.BorderLayout} {empty} {/setLayout(new BorderLayout())/}",
				"  {new: javax.swing.JToolBar} {local-unique: toolBar} {/new JToolBar()/ /add(toolBar, BorderLayout.NORTH)/}",
				"  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}",
				"    {new: test.ExternalAction} {field-initializer: action} {/new ExternalAction()/}");
		panel.refresh();
		JToolBarInfo toolBar = (JToolBarInfo) panel.getChildrenComponents().get(0);
		ActionInfo action = ActionContainerInfo.getActions(panel).get(0);
		// drop "action" on "toolBar"...
		loadUseAction(action);
		canvas.target(toolBar).in(10, 5).move();
		canvas.click();
		assertNoErrors(panel);
		assertEditor("""
				public class Test extends JPanel {
					private ExternalAction action = new ExternalAction();
					public Test() {
						setLayout(new BorderLayout());
						{
							JToolBar toolBar = new JToolBar();
							add(toolBar, BorderLayout.NORTH);
							{
								JButton button = toolBar.add(action);
							}
						}
					}
				}""");
		// ...new "JButton" created, it should be selected
		ComponentInfo button = toolBar.getChildrenComponents().get(0);
		tree.assertPrimarySelected(button);
		canvas.assertPrimarySelected(button);
	}

	/**
	 * Test for {@link ActionUseEntryInfo}.
	 */
	@Test
	public void test_JToolBar_ActionUseEntryInfo_tree() throws Exception {
		createExternalAction();
		ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					private ExternalAction action = new ExternalAction();
					public Test() {
						setLayout(new BorderLayout());
						{
							JToolBar toolBar = new JToolBar();
							add(toolBar, BorderLayout.NORTH);
						}
					}
				}""");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/setLayout(new BorderLayout())/ /add(toolBar, BorderLayout.NORTH)/}",
				"  {new: java.awt.BorderLayout} {empty} {/setLayout(new BorderLayout())/}",
				"  {new: javax.swing.JToolBar} {local-unique: toolBar} {/new JToolBar()/ /add(toolBar, BorderLayout.NORTH)/}",
				"  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}",
				"    {new: test.ExternalAction} {field-initializer: action} {/new ExternalAction()/}");
		panel.refresh();
		JToolBarInfo toolBar = (JToolBarInfo) panel.getChildrenComponents().get(0);
		ActionInfo action = ActionContainerInfo.getActions(panel).get(0);
		// drop "action" on "toolBar"...
		loadUseAction(action);
		tree.moveOn(toolBar);
		tree.click();
		assertNoErrors(panel);
		assertEditor("""
				public class Test extends JPanel {
					private ExternalAction action = new ExternalAction();
					public Test() {
						setLayout(new BorderLayout());
						{
							JToolBar toolBar = new JToolBar();
							add(toolBar, BorderLayout.NORTH);
							{
								JButton button = toolBar.add(action);
							}
						}
					}
				}""");
		// ...new "JButton" created, it should be selected
		ComponentInfo button = toolBar.getChildrenComponents().get(0);
		tree.assertPrimarySelected(button);
		canvas.assertPrimarySelected(button);
	}

	/**
	 * Test for {@link ActionExternalEntryInfo}.
	 */
	@Ignore
	@Test
	public void test_JToolBar_ActionExternalEntryInfo() throws Exception {
		createExternalAction();
		final ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							JToolBar toolBar = new JToolBar();
							add(toolBar, BorderLayout.NORTH);
						}
					}
				}""");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/setLayout(new BorderLayout())/ /add(toolBar, BorderLayout.NORTH)/}",
				"  {new: java.awt.BorderLayout} {empty} {/setLayout(new BorderLayout())/}",
				"  {new: javax.swing.JToolBar} {local-unique: toolBar} {/new JToolBar()/ /add(toolBar, BorderLayout.NORTH)/}");
		panel.refresh();
		JToolBarInfo toolBar = (JToolBarInfo) panel.getChildrenComponents().get(0);
		// load "action" tool
		new UiContext().executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() throws Exception {
				ActionExternalEntryInfo entry = new ActionExternalEntryInfo();
				entry.initialize(m_viewerCanvas, panel);
				Tool tool = entry.createTool();
				m_viewerCanvas.getEditDomain().setActiveTool(tool);
			}
		}, new FailableConsumer<>() {
			@Override
			public void accept(SWTBot bot) {
				animateOpenTypeSelection(bot, "ExternalAction", "OK");
			}
		});
		// drop new "action" on "toolBar"...
		canvas.target(toolBar).in(10, 5).move();
		canvas.click();
		assertNoErrors(panel);
		assertEditor("""
				public class Test extends JPanel {
					private final ExternalAction externalAction = new ExternalAction();
					public Test() {
						setLayout(new BorderLayout());
						{
							JToolBar toolBar = new JToolBar();
							add(toolBar, BorderLayout.NORTH);
							{
								JButton button = toolBar.add(externalAction);
							}
						}
					}
				}""");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/setLayout(new BorderLayout())/ /add(toolBar, BorderLayout.NORTH)/}",
				"  {new: java.awt.BorderLayout} {empty} {/setLayout(new BorderLayout())/}",
				"  {new: javax.swing.JToolBar} {local-unique: toolBar} {/new JToolBar()/ /add(toolBar, BorderLayout.NORTH)/ /toolBar.add(externalAction)/}",
				"    {implicit-factory} {local-unique: button} {/toolBar.add(externalAction)/}",
				"  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}",
				"    {new: test.ExternalAction} {field-initializer: externalAction} {/new ExternalAction()/ /toolBar.add(externalAction)/}");
		// ...new "JButton" created, it should be selected
		ComponentInfo button = toolBar.getChildrenComponents().get(0);
		canvas.assertPrimarySelected(button);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Other
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for dropping {@link ActionInfo} on {@link JButton}.
	 */
	@Test
	public void test_JButton_setAction() throws Exception {
		createExternalAction();
		ContainerInfo panel = openContainer("""
				public class Test extends JPanel {
					private ExternalAction action = new ExternalAction();
					public Test() {
						{
							JButton button = new JButton();
							add(button);
						}
					}
				}""");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/add(button)/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/}",
				"  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}",
				"    {new: test.ExternalAction} {field-initializer: action} {/new ExternalAction()/}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		ActionInfo action = ActionContainerInfo.getActions(panel).get(0);
		// drop "action" on "button"...
		loadUseAction(action);
		canvas.target(button).in(10, 5).move();
		canvas.click();
		assertEditor("""
				public class Test extends JPanel {
					private ExternalAction action = new ExternalAction();
					public Test() {
						{
							JButton button = new JButton();
							button.setAction(action);
							add(button);
						}
					}
				}""");
		// ...target "button" should be selected
		canvas.assertPrimarySelected(button);
	}

	/**
	 * Test for dropping {@link ActionInfo} between two {@link JMenuItem} in {@link JMenu}.
	 */
	@Test
	public void test_JMenu_dropBetween_JMenuItem() throws Exception {
		createExternalAction();
		ContainerInfo frame = openContainer("""
				public class Test extends JFrame {
					private ExternalAction action = new ExternalAction();
					public Test() {
						{
							JMenuBar menuBar = new JMenuBar();
							setJMenuBar(menuBar);
							{
								JMenu menu = new JMenu("Menu");
								menuBar.add(menu);
								{
									JMenuItem item_1 = new JMenuItem("AAA AAA AAA");
									menu.add(item_1);
								}
								{
									JMenuItem item_2 = new JMenuItem("BBB BBB BBB");
									menu.add(item_2);
								}
							}
						}
					}
				}""");
		assertHierarchy(
				"{this: javax.swing.JFrame} {this} {/setJMenuBar(menuBar)/}",
				"  {method: public java.awt.Container javax.swing.JFrame.getContentPane()} {property} {}",
				"    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}",
				"  {new: javax.swing.JMenuBar} {local-unique: menuBar} {/new JMenuBar()/ /setJMenuBar(menuBar)/ /menuBar.add(menu)/}",
				"    {new: javax.swing.JMenu} {local-unique: menu} {/new JMenu('Menu')/ /menuBar.add(menu)/ /menu.add(item_1)/ /menu.add(item_2)/}",
				"      {new: javax.swing.JMenuItem} {local-unique: item_1} {/new JMenuItem('AAA AAA AAA')/ /menu.add(item_1)/}",
				"      {new: javax.swing.JMenuItem} {local-unique: item_2} {/new JMenuItem('BBB BBB BBB')/ /menu.add(item_2)/}",
				"  {org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}",
				"    {new: test.ExternalAction} {field-initializer: action} {/new ExternalAction()/}");
		frame.refresh();
		JMenuBarInfo bar = frame.getChildren(JMenuBarInfo.class).get(0);
		JMenuInfo menu = bar.getChildrenMenus().get(0);
		JMenuItemInfo item_2 = menu.getChildrenItems().get(1);
		ActionInfo action = ActionContainerInfo.getActions(frame).get(0);
		// show "menu"
		tree.select(menu);
		canvas.assertNotNullEditPart(item_2);
		// drop "action" on before "item_2"
		loadUseAction(action);
		canvas.target(item_2).in(10, 1).move();
		canvas.click();
		assertEditor("""
				public class Test extends JFrame {
					private ExternalAction action = new ExternalAction();
					public Test() {
						{
							JMenuBar menuBar = new JMenuBar();
							setJMenuBar(menuBar);
							{
								JMenu menu = new JMenu("Menu");
								menuBar.add(menu);
								{
									JMenuItem item_1 = new JMenuItem("AAA AAA AAA");
									menu.add(item_1);
								}
								{
									JMenuItem menuItem = menu.add(action);
								}
								{
									JMenuItem item_2 = new JMenuItem("BBB BBB BBB");
									menu.add(item_2);
								}
							}
						}
					}
				}""");
		// ...new "JMenuItem" should be selected
		JMenuItemInfo newItem = menu.getChildrenItems().get(1);
		assertEquals("menuItem", newItem.getVariableSupport().getName());
		canvas.assertPrimarySelected(newItem);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates class with <code>ExternalAction</code>.
	 */
	private void createExternalAction() throws Exception {
		setFileContentSrc(
				"test/ExternalAction.java",
				getTestSource("""
						public class ExternalAction extends AbstractAction {
							public ExternalAction() {
								putValue(NAME, "My name");
								putValue(SHORT_DESCRIPTION, "My short description");
							}
							public void actionPerformed(ActionEvent e) {
							}
						}"""));
		waitForAutoBuild();
	}

	/**
	 * Loads {@link Tool} with given {@link ActionInfo}.
	 */
	private void loadUseAction(ActionInfo action) throws Exception {
		ActionUseEntryInfo entry = new ActionUseEntryInfo(action);
		entry.initialize(m_viewerCanvas, action.getRootJava());
		Tool tool = entry.createTool();
		m_viewerCanvas.getEditDomain().setActiveTool(tool);
	}
}
