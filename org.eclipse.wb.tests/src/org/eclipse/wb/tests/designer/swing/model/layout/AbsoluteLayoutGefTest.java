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
package org.eclipse.wb.tests.designer.swing.model.layout;

import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.jface.action.IAction;

/**
 * Tests for {@link AbsoluteLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class AbsoluteLayoutGefTest extends SwingGefTest {
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
	public void DISABLE_test_canvas_CREATE() throws Exception {
		prepareBox();
		ContainerInfo panel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(null);",
						"  }",
						"}");
		// create Box
		loadCreationBox();
		// use canvas
		canvas.sideMode().create(100, 50);
		canvas.target(panel).in(30, 40).move();
		canvas.click();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(null);",
				"    {",
				"      Box box = new Box();",
				"      box.setBounds(30, 40, 100, 50);",
				"      add(box);",
				"    }",
				"  }",
				"}");
	}

	public void DISABLE_test_canvas_PASTE() throws Exception {
		prepareBox();
		ContainerInfo panel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(null);",
						"    {",
						"      Box boxA = new Box();",
						"      boxA.setBounds(10, 20, 100, 50);",
						"      add(boxA);",
						"    }",
						"  }",
						"}");
		// copy "boxA"
		{
			// select "boxA"
			ComponentInfo boxA = panel.getChildrenComponents().get(0);
			canvas.select(boxA);
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
		// move
		canvas.sideMode().create(100, 50);
		canvas.target(panel).inX(50).inY(100).move();
		canvas.click();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(null);",
				"    {",
				"      Box boxA = new Box();",
				"      boxA.setBounds(10, 20, 100, 50);",
				"      add(boxA);",
				"    }",
				"    {",
				"      Box box = new Box();",
				"      box.setBounds(50, 100, 100, 50);",
				"      add(box);",
				"    }",
				"  }",
				"}");
	}

	public void test_canvas_MOVE() throws Exception {
		prepareBox();
		ContainerInfo panel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(null);",
						"    {",
						"      Box box = new Box();",
						"      box.setBounds(30, 40, 100, 50);",
						"      add(box);",
						"    }",
						"  }",
						"}");
		ComponentInfo box = panel.getChildrenComponents().get(0);
		// move
		canvas.sideMode().beginMove(box);
		canvas.target(panel).inX(50).inY(80).drag();
		canvas.endDrag();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(null);",
				"    {",
				"      Box box = new Box();",
				"      box.setBounds(50, 80, 100, 50);",
				"      add(box);",
				"    }",
				"  }",
				"}");
	}

	public void DISABLE_test_canvas_ADD() throws Exception {
		prepareBox();
		ContainerInfo panel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(null);",
						"    {",
						"      JPanel inner = new JPanel();",
						"      inner.setLayout(null);",
						"      add(inner);",
						"      inner.setBounds(20, 100, 200, 150);",
						"      {",
						"        Box box = new Box();",
						"        box.setBounds(10, 20, 100, 50);",
						"        inner.add(box);",
						"      }",
						"    }",
						"  }",
						"}");
		ContainerInfo inner = (ContainerInfo) panel.getChildrenComponents().get(0);
		ComponentInfo box = inner.getChildrenComponents().get(0);
		// move
		canvas.sideMode().beginMove(box);
		canvas.target(panel).in(50, 25).drag();
		canvas.endDrag();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(null);",
				"    {",
				"      JPanel inner = new JPanel();",
				"      inner.setLayout(null);",
				"      add(inner);",
				"      inner.setBounds(20, 100, 200, 150);",
				"    }",
				"    {",
				"      Box box = new Box();",
				"      box.setBounds(50, 25, 100, 50);",
				"      add(box);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tree
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_tree_CREATE() throws Exception {
		prepareBox();
		ContainerInfo panel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(null);",
						"  }",
						"}");
		// create Box
		ComponentInfo newBox = loadCreationBox();
		// use tree
		tree.moveOn(panel);
		tree.assertFeedback_on(panel);
		tree.click();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(null);",
				"    {",
				"      Box box = new Box();",
				"      box.setBounds(0, 0, 100, 50);",
				"      add(box);",
				"    }",
				"  }",
				"}");
		tree.assertPrimarySelected(newBox);
	}

	public void DISABLE_test_tree_PASTE() throws Exception {
		prepareBox();
		ContainerInfo panel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(null);",
						"    {",
						"      Box boxA = new Box();",
						"      boxA.setBounds(10, 20, 100, 50);",
						"      add(boxA);",
						"    }",
						"  }",
						"}");
		// copy "boxA"
		{
			// select "boxA"
			ComponentInfo boxA = panel.getChildrenComponents().get(0);
			canvas.select(boxA);
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
		// use tree
		tree.moveOn(panel);
		tree.assertFeedback_on(panel);
		tree.click();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(null);",
				"    {",
				"      Box boxA = new Box();",
				"      boxA.setBounds(10, 20, 100, 50);",
				"      add(boxA);",
				"    }",
				"    {",
				"      Box box = new Box();",
				"      box.setBounds(0, 0, 100, 50);",
				"      add(box);",
				"    }",
				"  }",
				"}");
	}

	public void test_tree_MOVE() throws Exception {
		prepareBox();
		ContainerInfo panel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(null);",
						"    {",
						"      Box boxA = new Box();",
						"      boxA.setBounds(10, 20, 100, 50);",
						"      add(boxA);",
						"    }",
						"    {",
						"      Box boxB = new Box();",
						"      boxB.setBounds(20, 100, 100, 50);",
						"      add(boxB);",
						"    }",
						"  }",
						"}");
		ComponentInfo boxA = panel.getChildrenComponents().get(0);
		ComponentInfo boxB = panel.getChildrenComponents().get(1);
		// use tree
		tree.startDrag(boxB);
		tree.dragBefore(boxA);
		tree.assertFeedback_before(boxA);
		tree.endDrag();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(null);",
				"    {",
				"      Box boxB = new Box();",
				"      boxB.setBounds(20, 100, 100, 50);",
				"      add(boxB);",
				"    }",
				"    {",
				"      Box boxA = new Box();",
				"      boxA.setBounds(10, 20, 100, 50);",
				"      add(boxA);",
				"    }",
				"  }",
				"}");
		tree.assertPrimarySelected(boxB);
	}

	public void test_tree_ADD() throws Exception {
		prepareBox();
		ContainerInfo panel =
				openContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(null);",
						"    {",
						"      JPanel inner = new JPanel();",
						"      inner.setLayout(null);",
						"      add(inner);",
						"      inner.setBounds(20, 100, 200, 150);",
						"      {",
						"        Box box = new Box();",
						"        box.setBounds(10, 20, 100, 50);",
						"        inner.add(box);",
						"      }",
						"    }",
						"  }",
						"}");
		ContainerInfo inner = (ContainerInfo) panel.getChildrenComponents().get(0);
		ComponentInfo box = inner.getChildrenComponents().get(0);
		// use tree
		tree.startDrag(box);
		tree.dragOn(panel);
		tree.assertFeedback_on(panel);
		tree.endDrag();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(null);",
				"    {",
				"      JPanel inner = new JPanel();",
				"      inner.setLayout(null);",
				"      add(inner);",
				"      inner.setBounds(20, 100, 200, 150);",
				"    }",
				"    {",
				"      Box box = new Box();",
				"      box.setBounds(0, 0, 100, 50);",
				"      add(box);",
				"    }",
				"  }",
				"}");
	}
}
