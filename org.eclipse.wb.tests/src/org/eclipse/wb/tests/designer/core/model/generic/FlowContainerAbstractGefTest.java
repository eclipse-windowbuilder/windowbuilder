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
package org.eclipse.wb.tests.designer.core.model.generic;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.action.IAction;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for "flow container" support for container itself or for its "layout manager".
 *
 * @author scheglov_ke
 */
public abstract class FlowContainerAbstractGefTest extends SwingGefTest {
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
	public void test_canvas_CREATE_empty() throws Exception {
		prepareFlowPanel();
		ContainerInfo mainPanel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      FlowPanel panel = new FlowPanel();",
						"      add(panel);",
						"    }",
						"  }",
						"}");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		// begin creating Button
		JavaInfo newButton = loadCreationTool("javax.swing.JButton", "empty");
		// move on "panel": feedback appears, command not null
		canvas.moveTo(panel, 100, 100);
		canvas.assertEmptyFlowContainerFeedback(panel, true);
		canvas.assertCommandNotNull();
		// click, so finish creation
		canvas.click();
		canvas.assertNoFeedbacks();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      FlowPanel panel = new FlowPanel();",
				"      add(panel);",
				"      {",
				"        JButton button = new JButton();",
				"        panel.add(button);",
				"      }",
				"    }",
				"  }",
				"}");
		canvas.assertPrimarySelected(newButton);
	}

	@Test
	public void test_canvas_CREATE() throws Exception {
		prepareFlowPanel();
		ContainerInfo mainPanel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      FlowPanel panel = new FlowPanel();",
						"      add(panel);",
						"      {",
						"        JButton existingButton = new JButton();",
						"        panel.add(existingButton);",
						"      }",
						"    }",
						"  }",
						"}");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo existingButton = panel.getChildrenComponents().get(0);
		// begin creating Button
		JavaInfo newButton = loadCreationTool("javax.swing.JButton");
		// move on "panel": feedback appears, command not null
		canvas.moveTo(existingButton, 0, 0);
		canvas.assertFeedbacks(canvas.getLinePredicate(existingButton, PositionConstants.LEFT));
		canvas.assertCommandNotNull();
		// click, so finish creation
		canvas.click();
		canvas.assertNoFeedbacks();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      FlowPanel panel = new FlowPanel();",
				"      add(panel);",
				"      {",
				"        JButton button = new JButton('New button');",
				"        panel.add(button);",
				"      }",
				"      {",
				"        JButton existingButton = new JButton();",
				"        panel.add(existingButton);",
				"      }",
				"    }",
				"  }",
				"}");
		canvas.assertPrimarySelected(newButton);
	}

	@Test
	public void test_canvas_MOVE() throws Exception {
		prepareFlowPanel();
		ContainerInfo mainPanel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      FlowPanel panel = new FlowPanel();",
						"      add(panel);",
						"      {",
						"        JButton buttonA = new JButton();",
						"        panel.add(buttonA);",
						"      }",
						"      {",
						"        JButton buttonB = new JButton();",
						"        panel.add(buttonB);",
						"      }",
						"    }",
						"  }",
						"}");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo buttonA = panel.getChildrenComponents().get(0);
		ComponentInfo buttonB = panel.getChildrenComponents().get(1);
		// drag "buttonB"
		canvas.beginDrag(buttonB, 10, 10).dragTo(buttonA);
		canvas.assertFeedbacks(canvas.getLinePredicate(buttonA, PositionConstants.LEFT));
		canvas.assertCommandNotNull();
		// done drag, so finish MOVE
		canvas.endDrag();
		canvas.assertNoFeedbacks();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      FlowPanel panel = new FlowPanel();",
				"      add(panel);",
				"      {",
				"        JButton buttonB = new JButton();",
				"        panel.add(buttonB);",
				"      }",
				"      {",
				"        JButton buttonA = new JButton();",
				"        panel.add(buttonA);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	@Ignore
	@Test
	public void test_canvas_PASTE() throws Exception {
		prepareFlowPanel();
		ContainerInfo mainPanel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      FlowPanel panel = new FlowPanel();",
						"      add(panel);",
						"      {",
						"        JButton existingButton = new JButton();",
						"        panel.add(existingButton);",
						"      }",
						"    }",
						"    {",
						"      JButton rootButton = new JButton('A');",
						"      add(rootButton, BorderLayout.NORTH);",
						"    }",
						"  }",
						"}");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo existingButton = panel.getChildrenComponents().get(0);
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
		canvas.moveTo(existingButton, 0, 0);
		canvas.assertFeedbacks(canvas.getLinePredicate(existingButton, PositionConstants.LEFT));
		canvas.assertCommandNotNull();
		// click, so finish creation
		canvas.click();
		canvas.assertNoFeedbacks();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      FlowPanel panel = new FlowPanel();",
				"      add(panel);",
				"      {",
				"        JButton button = new JButton('A');",
				"        panel.add(button);",
				"      }",
				"      {",
				"        JButton existingButton = new JButton();",
				"        panel.add(existingButton);",
				"      }",
				"    }",
				"    {",
				"      JButton rootButton = new JButton('A');",
				"      add(rootButton, BorderLayout.NORTH);",
				"    }",
				"  }",
				"}");
		// EditPart for "newButton" exists and selected
		{
			ComponentInfo newButton = panel.getChildrenComponents().get(0);
			canvas.assertPrimarySelected(newButton);
		}
	}

	@Test
	public void test_canvas_ADD() throws Exception {
		prepareFlowPanel();
		ContainerInfo mainPanel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      FlowPanel panel = new FlowPanel();",
						"      add(panel);",
						"      {",
						"        JButton existingButton = new JButton();",
						"        panel.add(existingButton);",
						"      }",
						"    }",
						"    {",
						"      JButton rootButton = new JButton();",
						"      add(rootButton, BorderLayout.NORTH);",
						"    }",
						"  }",
						"}");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo existingButton = panel.getChildrenComponents().get(0);
		ComponentInfo rootButton = mainPanel.getChildrenComponents().get(1);
		// drag "rootButton"
		canvas.beginDrag(rootButton).dragTo(existingButton);
		canvas.assertFeedbacks(canvas.getLinePredicate(existingButton, PositionConstants.LEFT));
		canvas.assertCommandNotNull();
		// done drag, so finish ADD
		canvas.endDrag();
		canvas.assertNoFeedbacks();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      FlowPanel panel = new FlowPanel();",
				"      add(panel);",
				"      {",
				"        JButton rootButton = new JButton();",
				"        panel.add(rootButton);",
				"      }",
				"      {",
				"        JButton existingButton = new JButton();",
				"        panel.add(existingButton);",
				"      }",
				"    }",
				"  }",
				"}");
		canvas.assertPrimarySelected(rootButton);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tree
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_tree_CREATE() throws Exception {
		prepareFlowPanel();
		ContainerInfo mainPanel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      FlowPanel panel = new FlowPanel();",
						"      add(panel);",
						"      {",
						"        JButton existingButton = new JButton();",
						"        panel.add(existingButton);",
						"      }",
						"    }",
						"  }",
						"}");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo existingButton = panel.getChildrenComponents().get(0);
		// begin creating Button
		JavaInfo newButton = loadCreationTool("javax.swing.JButton");
		// move before "existingButton": feedback appears, command not null
		tree.moveBefore(existingButton);
		tree.assertFeedback_before(existingButton);
		tree.assertCommandNotNull();
		// click, so finish creation
		tree.click();
		tree.assertFeedback_empty();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      FlowPanel panel = new FlowPanel();",
				"      add(panel);",
				"      {",
				"        JButton button = new JButton('New button');",
				"        panel.add(button);",
				"      }",
				"      {",
				"        JButton existingButton = new JButton();",
				"        panel.add(existingButton);",
				"      }",
				"    }",
				"  }",
				"}");
		tree.assertPrimarySelected(newButton);
	}

	@Test
	public void test_tree_MOVE() throws Exception {
		prepareFlowPanel();
		ContainerInfo mainPanel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      FlowPanel panel = new FlowPanel();",
						"      add(panel);",
						"      {",
						"        JButton buttonA = new JButton();",
						"        panel.add(buttonA);",
						"      }",
						"      {",
						"        JButton buttonB = new JButton();",
						"        panel.add(buttonB);",
						"      }",
						"    }",
						"  }",
						"}");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo buttonA = panel.getChildrenComponents().get(0);
		ComponentInfo buttonB = panel.getChildrenComponents().get(1);
		// select "buttonB", so ensure that it has EditPart
		canvas.select(buttonB);
		// drag "buttonB"
		tree.startDrag(buttonB);
		tree.dragBefore(buttonA);
		tree.assertFeedback_before(buttonA);
		tree.assertCommandNotNull();
		// done drag, so finish MOVE
		tree.endDrag();
		tree.assertFeedback_empty();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      FlowPanel panel = new FlowPanel();",
				"      add(panel);",
				"      {",
				"        JButton buttonB = new JButton();",
				"        panel.add(buttonB);",
				"      }",
				"      {",
				"        JButton buttonA = new JButton();",
				"        panel.add(buttonA);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	@Ignore
	@Test
	public void test_tree_PASTE() throws Exception {
		prepareFlowPanel();
		ContainerInfo mainPanel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      FlowPanel panel = new FlowPanel();",
						"      add(panel);",
						"      {",
						"        JButton existingButton = new JButton();",
						"        panel.add(existingButton);",
						"      }",
						"    }",
						"    {",
						"      JButton rootButton = new JButton('A');",
						"      add(rootButton, BorderLayout.NORTH);",
						"    }",
						"  }",
						"}");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo existingButton = panel.getChildrenComponents().get(0);
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
		// move before "existingButton": feedback appears, command not null
		tree.moveOn(panel);
		tree.moveBefore(existingButton);
		tree.assertFeedback_before(existingButton);
		tree.assertCommandNotNull();
		// click, so finish creation
		tree.click();
		tree.assertFeedback_empty();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      FlowPanel panel = new FlowPanel();",
				"      add(panel);",
				"      {",
				"        JButton button = new JButton('A');",
				"        panel.add(button);",
				"      }",
				"      {",
				"        JButton existingButton = new JButton();",
				"        panel.add(existingButton);",
				"      }",
				"    }",
				"    {",
				"      JButton rootButton = new JButton('A');",
				"      add(rootButton, BorderLayout.NORTH);",
				"    }",
				"  }",
				"}");
		// EditPart for "newButton" exists and selected
		{
			ComponentInfo newButton = panel.getChildrenComponents().get(0);
			tree.assertPrimarySelected(newButton);
		}
	}

	@Test
	public void test_tree_ADD() throws Exception {
		prepareFlowPanel();
		ContainerInfo mainPanel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      FlowPanel panel = new FlowPanel();",
						"      add(panel);",
						"      {",
						"        JButton existingButton = new JButton();",
						"        panel.add(existingButton);",
						"      }",
						"    }",
						"    {",
						"      JButton rootButton = new JButton();",
						"      add(rootButton, BorderLayout.NORTH);",
						"    }",
						"  }",
						"}");
		ContainerInfo panel = (ContainerInfo) mainPanel.getChildrenComponents().get(0);
		ComponentInfo existingButton = panel.getChildrenComponents().get(0);
		ComponentInfo rootButton = mainPanel.getChildrenComponents().get(1);
		// drag "rootButton"
		tree.startDrag(rootButton);
		tree.dragBefore(existingButton);
		tree.endDrag();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      FlowPanel panel = new FlowPanel();",
				"      add(panel);",
				"      {",
				"        JButton rootButton = new JButton();",
				"        panel.add(rootButton);",
				"      }",
				"      {",
				"        JButton existingButton = new JButton();",
				"        panel.add(existingButton);",
				"      }",
				"    }",
				"  }",
				"}");
		tree.assertPrimarySelected(rootButton);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates <code>FlowPanel</code> component with <code>MyLayout</code> layout manager. One of them
	 * should be configured to have "flow container" description.
	 */
	protected abstract void prepareFlowPanel() throws Exception;
}
