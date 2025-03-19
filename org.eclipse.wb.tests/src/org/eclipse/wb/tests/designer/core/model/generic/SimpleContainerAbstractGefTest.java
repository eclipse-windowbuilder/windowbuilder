/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model.generic;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.jface.action.IAction;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for "simple container" support, such as {@link SimpleContainer} interface.
 *
 * @author scheglov_ke
 */
public abstract class SimpleContainerAbstractGefTest extends SwingGefTest {
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
	@Test
	public void test_canvas_CREATE_filled() throws Exception {
		prepareSimplePanel();
		ContainerInfo mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
							{
								JButton existingButton = new JButton();
								panel.setContent(existingButton);
							}
						}
					}
				}""");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		// begin creating Button
		loadCreationTool("javax.swing.JButton");
		// move on "panel": feedback appears, command not null
		canvas.moveTo(panel, 0, 0);
		canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
		canvas.assertCommandNull();
	}

	@Test
	public void test_canvas_CREATE_empty() throws Exception {
		prepareSimplePanel();
		ContainerInfo mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
						}
					}
				}""");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		// begin creating Button
		JavaInfo newButton = loadCreationTool("javax.swing.JButton");
		// move on "panel": feedback appears, command not null
		canvas.moveTo(panel, 0, 0);
		canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
		canvas.assertCommandNotNull();
		// click, so finish creation
		canvas.click();
		canvas.assertNoFeedbacks();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
							{
								JButton button = new JButton("New button");
								panel.setContent(button);
							}
						}
					}
				}""");
		canvas.assertPrimarySelected(newButton);
	}

	@Ignore
	@Test
	public void test_canvas_PASTE() throws Exception {
		prepareSimplePanel();
		ContainerInfo mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
						}
						{
							JButton rootButton = new JButton("A");
							add(rootButton, BorderLayout.NORTH);
						}
					}
				}""");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo rootButton = mainPanel.getChildrenComponents().get(1);
		// copy "rootButton"
		{
			// select "rootButton"
			canvas.select(rootButton);
			// do copy
			IAction copyAction = getCopyAction();
			assertTrue(copyAction.isEnabled());
			copyAction.run();
		}
		// paste
		{
			IAction pasteAction = getPasteAction();
			assertTrue(pasteAction.isEnabled());
			pasteAction.run();
		}
		// move on "panel": feedback appears, command not null
		canvas.moveTo(panel, 0, 0);
		canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
		canvas.assertCommandNotNull();
		// click, so finish creation
		canvas.click();
		canvas.assertNoFeedbacks();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
							{
								JButton button = new JButton("A");
								panel.setContent(button);
							}
						}
						{
							JButton rootButton = new JButton("A");
							add(rootButton, BorderLayout.NORTH);
						}
					}
				}""");
		// EditPart for "newButton" exists and selected
		{
			ComponentInfo newButton = panel.getChildrenComponents().get(0);
			canvas.assertPrimarySelected(newButton);
		}
	}

	@Test
	public void test_canvas_ADD_1() throws Exception {
		prepareSimplePanel();
		ContainerInfo mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
						}
						{
							JButton rootButton = new JButton();
							add(rootButton, BorderLayout.NORTH);
						}
					}
				}""");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo rootButton = mainPanel.getChildrenComponents().get(1);
		// drag "rootButton"
		canvas.beginDrag(rootButton).dragTo(panel);
		canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
		canvas.assertCommandNotNull();
		// done drag, so finish ADD
		canvas.endDrag();
		canvas.assertNoFeedbacks();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
							{
								JButton rootButton = new JButton();
								panel.setContent(rootButton);
							}
						}
					}
				}""");
		canvas.assertPrimarySelected(rootButton);
	}

	@Test
	public void test_canvas_ADD_2() throws Exception {
		prepareSimplePanel();
		ContainerInfo mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
						}
						{
							JButton button_1 = new JButton();
							add(button_1, BorderLayout.NORTH);
						}
						{
							JButton button_2 = new JButton();
							add(button_2, BorderLayout.SOUTH);
						}
					}
				}""");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo button_1 = mainPanel.getChildrenComponents().get(1);
		ComponentInfo button_2 = mainPanel.getChildrenComponents().get(2);
		// drag "button_1" and "button_2"
		canvas.select(button_2, button_1);
		canvas.beginDrag(button_1, 100, 5).dragTo(panel, 10, 10);
		canvas.assertFeedbacks(canvas.getTargetPredicate(panel));
		canvas.assertCommandNull();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tree
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_tree_CREATE_filled_1() throws Exception {
		prepareSimplePanel();
		ContainerInfo mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
							{
								JButton existingButton = new JButton();
								panel.setContent(existingButton);
							}
						}
					}
				}""");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo existingButton = panel.getChildrenComponents().get(0);
		// begin creating Button
		loadCreationTool("javax.swing.JButton");
		// move before "existingButton": feedback appears, command null
		tree.moveBefore(existingButton);
		tree.assertFeedback_before(existingButton);
		tree.assertCommandNull();
	}

	@Test
	public void test_tree_CREATE_filled_2() throws Exception {
		prepareSimplePanel();
		ContainerInfo mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
							{
								JButton existingButton = new JButton();
								panel.setContent(existingButton);
							}
						}
					}
				}""");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		// begin creating Button
		loadCreationTool("javax.swing.JButton");
		// move before "existingButton": feedback appears, command null
		tree.moveOn(panel);
		tree.assertFeedback_on(panel);
		tree.assertCommandNull();
	}

	@Test
	public void test_tree_CREATE_empty() throws Exception {
		prepareSimplePanel();
		ContainerInfo mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
						}
					}
				}""");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		// begin creating Button
		JavaInfo newButton = loadCreationTool("javax.swing.JButton");
		// move on "panel": feedback appears, command not null
		tree.moveOn(panel);
		tree.assertFeedback_on(panel);
		tree.assertCommandNotNull();
		// click, so finish creation
		tree.click();
		tree.assertFeedback_empty();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
							{
								JButton button = new JButton("New button");
								panel.setContent(button);
							}
						}
					}
				}""");
		tree.assertPrimarySelected(newButton);
	}

	@Ignore
	@Test
	public void test_tree_PASTE() throws Exception {
		prepareSimplePanel();
		ContainerInfo mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
						}
						{
							JButton rootButton = new JButton("A");
							add(rootButton, BorderLayout.NORTH);
						}
					}
				}""");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo rootButton = mainPanel.getChildrenComponents().get(1);
		// copy "rootButton"
		{
			// select "rootButton"
			canvas.select(rootButton);
			// do copy
			IAction copyAction = getCopyAction();
			assertTrue(copyAction.isEnabled());
			copyAction.run();
		}
		// paste
		{
			IAction pasteAction = getPasteAction();
			assertTrue(pasteAction.isEnabled());
			pasteAction.run();
		}
		// move on "panel": feedback appears, command not null
		tree.moveOn(panel);
		tree.assertFeedback_on(panel);
		tree.assertCommandNotNull();
		// click, so finish creation
		tree.click();
		tree.assertFeedback_empty();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
							{
								JButton button = new JButton("A");
								panel.setContent(button);
							}
						}
						{
							JButton rootButton = new JButton("A");
							add(rootButton, BorderLayout.NORTH);
						}
					}
				}""");
		// EditPart for "newButton" exists and selected
		{
			ComponentInfo newButton = panel.getChildrenComponents().get(0);
			tree.assertPrimarySelected(newButton);
		}
	}

	@Test
	public void test_tree_MOVE() throws Exception {
		prepareSimplePanel();
		ContainerInfo mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
							{
								JButton button = new JButton();
								panel.setContent(button);
							}
						}
					}
				}""");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// drag "button"
		tree.startDrag(button);
		tree.dragOn(panel);
		tree.assertFeedback_on(panel);
		tree.assertCommandNull();
	}

	@Test
	public void test_tree_ADD() throws Exception {
		prepareSimplePanel();
		ContainerInfo mainPanel = openContainer("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
						}
						{
							JButton button = new JButton();
							add(button, BorderLayout.NORTH);
						}
					}
				}""");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo button = mainPanel.getChildrenComponents().get(1);
		// drag "button"
		tree.startDrag(button);
		tree.dragOn(panel);
		tree.assertFeedback_on(panel);
		tree.assertCommandNotNull();
		// done drag, so finish ADD
		tree.endDrag();
		tree.assertFeedback_empty();
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setLayout(new BorderLayout());
						{
							SimplePanel panel = new SimplePanel();
							add(panel);
							{
								JButton button = new JButton();
								panel.setContent(button);
							}
						}
					}
				}""");
		tree.assertPrimarySelected(button);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	protected abstract void prepareSimplePanel() throws Exception;
}
