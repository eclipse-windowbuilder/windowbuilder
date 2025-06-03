/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JSplitPaneInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

import org.junit.Test;

import javax.swing.JSplitPane;

/**
 * Test for {@link JSplitPane}.
 *
 * @author scheglov_ke
 */
public class JSplitPaneTest extends SwingModelTest {
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
	 * Test for association using setLeftComponent/setRightComponent.
	 */
	@Test
	public void test_association_setLeftRight() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    JSplitPane split = new JSplitPane();",
						"    add(split);",
						"    {",
						"      JButton button = new JButton();",
						"      split.setLeftComponent(button);",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      split.setRightComponent(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		JSplitPaneInfo split = (JSplitPaneInfo) panel.getChildrenComponents().get(0);
		//
		assertEquals(2, split.getChildrenComponents().size());
		assertFalse(split.isEmptyPosition(true));
		assertFalse(split.isEmptyPosition(false));
		// check position rectangles
		{
			Insets insets = split.getInsets();
			// left
			{
				Rectangle r = split.getPositionRectangle(true);
				assertEquals(insets.left, r.x);
				assertEquals(insets.top, r.y);
			}
			// right
			{
				Rectangle r = split.getPositionRectangle(false);
				assertEquals(insets.top, r.y);
			}
		}
	}

	/**
	 * Test for empty positions.
	 */
	@Test
	public void test_association_empty() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    JSplitPane split = new JSplitPane();",
						"    add(split);",
						"  }",
						"}");
		panel.refresh();
		JSplitPaneInfo split = (JSplitPaneInfo) panel.getChildrenComponents().get(0);
		//
		assertTrue(split.isEmptyPosition(true));
		assertTrue(split.isEmptyPosition(false));
	}

	/**
	 * {@link JSplitPane#setDividerLocation(double)} should be added after all children.
	 */
	@Test
	public void test_association_setDividerLocation() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    JSplitPane split = new JSplitPane();",
						"    add(split);",
						"    {",
						"      JButton button_1 = new JButton();",
						"      split.setLeftComponent(button_1);",
						"    }",
						"    {",
						"      JButton button_2 = new JButton();",
						"      split.setRightComponent(button_2);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		JSplitPaneInfo split = (JSplitPaneInfo) panel.getChildrenComponents().get(0);
		//
		split.getPropertyByTitle("dividerLocation(double)").setValue(0.5);
		assertEditor(
				"class Test extends JPanel {",
				"  Test() {",
				"    JSplitPane split = new JSplitPane();",
				"    add(split);",
				"    {",
				"      JButton button_1 = new JButton();",
				"      split.setLeftComponent(button_1);",
				"    }",
				"    {",
				"      JButton button_2 = new JButton();",
				"      split.setRightComponent(button_2);",
				"    }",
				"    split.setDividerLocation(0.5);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Position rectangles
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for existing left/right components.
	 */
	@Test
	public void test_getPositionRectangle_1() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    JSplitPane split = new JSplitPane();",
						"    add(split);",
						"    {",
						"      JButton button = new JButton();",
						"      split.setLeftComponent(button);",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      split.setRightComponent(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		JSplitPaneInfo split = (JSplitPaneInfo) panel.getChildrenComponents().get(0);
		//
		Insets insets = split.getInsets();
		// left
		{
			Rectangle r = split.getPositionRectangle(true);
			assertEquals(insets.left, r.x);
			assertEquals(insets.top, r.y);
		}
		// right
		{
			Rectangle r = split.getPositionRectangle(false);
			assertEquals(insets.top, r.y);
		}
	}

	/**
	 * Test for empty component.
	 */
	@Test
	public void test_getPositionRectangle_2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, null, null);",
						"    add(split);",
						"  }",
						"}");
		panel.refresh();
		JSplitPaneInfo split = (JSplitPaneInfo) panel.getChildrenComponents().get(0);
		//
		Rectangle r = split.getPositionRectangle(true);
		assertEquals(new Rectangle(0, 0, 0, 0), r);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CREATE
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CREATE() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    JSplitPane split = new JSplitPane();",
						"    add(split);",
						"  }",
						"}");
		JSplitPaneInfo split = (JSplitPaneInfo) panel.getChildrenComponents().get(0);
		// add component
		{
			ComponentInfo newComponent = createJButton();
			split.command_CREATE(newComponent, true);
			assertInstanceOf(InvocationChildAssociation.class, newComponent.getAssociation());
		}
		// check source
		assertEditor(
				"class Test extends JPanel {",
				"  Test() {",
				"    JSplitPane split = new JSplitPane();",
				"    add(split);",
				"    {",
				"      JButton button = new JButton();",
				"      split.setLeftComponent(button);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Move
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_OUT() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    JSplitPane split = new JSplitPane();",
						"    add(split);",
						"    {",
						"      JButton button = new JButton();",
						"      split.setLeftComponent(button);",
						"    }",
						"    {",
						"      JPanel innerPanel = new JPanel();",
						"      add(innerPanel);",
						"    }",
						"  }",
						"}");
		// prepare source
		JSplitPaneInfo split = (JSplitPaneInfo) panel.getChildrenComponents().get(0);
		ComponentInfo button = split.getChildrenComponents().get(0);
		// prepare target
		ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(1);
		FlowLayoutInfo innerLayout = (FlowLayoutInfo) innerPanel.getLayout();
		// do move
		innerLayout.move(button, null);
		assertEditor(
				"class Test extends JPanel {",
				"  Test() {",
				"    JSplitPane split = new JSplitPane();",
				"    add(split);",
				"    {",
				"      JPanel innerPanel = new JPanel();",
				"      add(innerPanel);",
				"      {",
				"        JButton button = new JButton();",
				"        innerPanel.add(button);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_MOVE() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    JSplitPane split = new JSplitPane();",
						"    add(split);",
						"    {",
						"      JButton button = new JButton();",
						"      split.setLeftComponent(button);",
						"    }",
						"  }",
						"}");
		// prepare source
		JSplitPaneInfo split = (JSplitPaneInfo) panel.getChildrenComponents().get(0);
		ComponentInfo button = split.getChildrenComponents().get(0);
		// do move
		split.command_MOVE(button, false);
		assertEditor(
				"class Test extends JPanel {",
				"  Test() {",
				"    JSplitPane split = new JSplitPane();",
				"    add(split);",
				"    {",
				"      JButton button = new JButton();",
				"      split.setRightComponent(button);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ADD
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_ADD() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    JSplitPane split = new JSplitPane();",
						"    add(split);",
						"    {",
						"      JPanel innerPanel = new JPanel();",
						"      add(innerPanel);",
						"      {",
						"        JButton button = new JButton();",
						"        innerPanel.add(button);",
						"      }",
						"    }",
						"  }",
						"}");
		JSplitPaneInfo split = (JSplitPaneInfo) panel.getChildrenComponents().get(0);
		ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(1);
		ComponentInfo button = innerPanel.getChildrenComponents().get(0);
		//
		split.command_ADD(button, true);
		assertEditor(
				"class Test extends JPanel {",
				"  Test() {",
				"    JSplitPane split = new JSplitPane();",
				"    add(split);",
				"    {",
				"      JButton button = new JButton();",
				"      split.setLeftComponent(button);",
				"    }",
				"    {",
				"      JPanel innerPanel = new JPanel();",
				"      add(innerPanel);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Clipboard
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_clipboard() throws Exception {
		final ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    {",
						"      JSplitPane split = new JSplitPane();",
						"      add(split);",
						"      {",
						"        JButton button = new JButton('A');",
						"        split.setLeftComponent(button);",
						"      }",
						"      {",
						"        JButton button = new JButton('B');",
						"        split.setRightComponent(button);",
						"      }",
						"    }",
						"  }",
						"}");
		refresh();
		//
		{
			ComponentInfo split = getJavaInfoByName("split");
			doCopyPaste(split, new PasteProcedure<ComponentInfo>() {
				@Override
				public void run(ComponentInfo copy) throws Exception {
					((FlowLayoutInfo) panel.getLayout()).add(copy, null);
				}
			});
		}
		assertEditor(
				"class Test extends JPanel {",
				"  Test() {",
				"    {",
				"      JSplitPane split = new JSplitPane();",
				"      add(split);",
				"      {",
				"        JButton button = new JButton('A');",
				"        split.setLeftComponent(button);",
				"      }",
				"      {",
				"        JButton button = new JButton('B');",
				"        split.setRightComponent(button);",
				"      }",
				"    }",
				"    {",
				"      JSplitPane splitPane = new JSplitPane();",
				"      add(splitPane);",
				"      {",
				"        JButton button = new JButton('A');",
				"        splitPane.setLeftComponent(button);",
				"      }",
				"      {",
				"        JButton button = new JButton('B');",
				"        splitPane.setRightComponent(button);",
				"      }",
				"    }",
				"  }",
				"}");
	}
}
