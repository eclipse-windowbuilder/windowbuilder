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

import org.eclipse.wb.core.model.association.ConstructorChildAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JScrollPaneInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

import javax.swing.JScrollPane;

/**
 * Test for {@link JScrollPane}.
 *
 * @author scheglov_ke
 */
public class JScrollPaneTest extends SwingModelTest {
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
	 * Test for association using constructor.
	 */
	@Test
	public void test_association_constructor() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    JButton button = new JButton();",
						"    //",
						"    JScrollPane scroll = new JScrollPane(button);",
						"    add(scroll);",
						"  }",
						"}");
		JScrollPaneInfo scroll = (JScrollPaneInfo) panel.getChildrenComponents().get(0);
		assertEquals(1, scroll.getChildrenComponents().size());
		// check association
		ComponentInfo button = scroll.getChildrenComponents().get(0);
		assertInstanceOf(ConstructorChildAssociation.class, button.getAssociation());
		// check positions
		panel.refresh();
		try {
			assertTrue(scroll.isEmptyPosition("getColumnHeader"));
			assertTrue(scroll.isEmptyPosition("getRowHeader"));
			assertFalse(scroll.isEmptyPosition("getViewport"));
		} finally {
			panel.refresh_dispose();
		}
	}

	/**
	 * Test for association using "setColumnHeaderView".
	 */
	@Test
	public void test_association_setColumnHeaderView() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"class Test extends JPanel {",
						"  Test() {",
						"    JScrollPane scroll = new JScrollPane();",
						"    add(scroll);",
						"    {",
						"      JButton button = new JButton();",
						"      scroll.setColumnHeaderView(button);",
						"    }",
						"  }",
						"}");
		JScrollPaneInfo scroll = (JScrollPaneInfo) panel.getChildrenComponents().get(0);
		assertEquals(1, scroll.getChildrenComponents().size());
		// check association
		ComponentInfo button = scroll.getChildrenComponents().get(0);
		assertInstanceOf(InvocationChildAssociation.class, button.getAssociation());
		// check positions
		panel.refresh();
		try {
			assertFalse(scroll.isEmptyPosition("getColumnHeader"));
			assertTrue(scroll.isEmptyPosition("getRowHeader"));
			assertTrue(scroll.isEmptyPosition("getViewport"));
			assertFalse(scroll.isEmptyPosition("no-such-method"));
		} finally {
			panel.refresh_dispose();
		}
	}

	/**
	 * We should be able to parse <code>scroll.getViewport().add()</code>.
	 */
	@Test
	public void test_getViewport_add() throws Exception {
		parseContainer(
				"class Test extends JPanel {",
				"  Test() {",
				"    JScrollPane scroll = new JScrollPane();",
				"    add(scroll);",
				"    {",
				"      JButton button = new JButton();",
				"      scroll.getViewport().add(button);",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/add(scroll)/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {new: javax.swing.JScrollPane} {local-unique: scroll} {/new JScrollPane()/ /add(scroll)/ /scroll.setViewportView(button)/}",
				"    {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /scroll.setViewportView(button)/}");
		assertEditor(
				"class Test extends JPanel {",
				"  Test() {",
				"    JScrollPane scroll = new JScrollPane();",
				"    add(scroll);",
				"    {",
				"      JButton button = new JButton();",
				"      scroll.setViewportView(button);",
				"    }",
				"  }",
				"}");
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
						"    JScrollPane scroll = new JScrollPane();",
						"    add(scroll);",
						"  }",
						"}");
		JScrollPaneInfo scroll = (JScrollPaneInfo) panel.getChildrenComponents().get(0);
		// add component
		{
			ComponentInfo newComponent = createJButton();
			scroll.command_CREATE(newComponent, "setRowHeaderView");
			assertInstanceOf(InvocationChildAssociation.class, newComponent.getAssociation());
		}
		assertEditor(
				"class Test extends JPanel {",
				"  Test() {",
				"    JScrollPane scroll = new JScrollPane();",
				"    add(scroll);",
				"    {",
				"      JButton button = new JButton();",
				"      scroll.setRowHeaderView(button);",
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
						"    JScrollPane scroll = new JScrollPane();",
						"    add(scroll);",
						"    {",
						"      JButton button = new JButton();",
						"      scroll.setRowHeaderView(button);",
						"    }",
						"    {",
						"      JPanel innerPanel = new JPanel();",
						"      add(innerPanel);",
						"    }",
						"  }",
						"}");
		// prepare source
		JScrollPaneInfo scroll = (JScrollPaneInfo) panel.getChildrenComponents().get(0);
		ComponentInfo button = scroll.getChildrenComponents().get(0);
		// prepare target
		ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(1);
		FlowLayoutInfo innerLayout = (FlowLayoutInfo) innerPanel.getLayout();
		// do move
		innerLayout.move(button, null);
		assertEditor(
				"class Test extends JPanel {",
				"  Test() {",
				"    JScrollPane scroll = new JScrollPane();",
				"    add(scroll);",
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
						"    JScrollPane scroll = new JScrollPane();",
						"    add(scroll);",
						"    {",
						"      JButton button = new JButton();",
						"      scroll.setRowHeaderView(button);",
						"    }",
						"  }",
						"}");
		// prepare source
		JScrollPaneInfo scroll = (JScrollPaneInfo) panel.getChildrenComponents().get(0);
		ComponentInfo button = scroll.getChildrenComponents().get(0);
		// do move
		scroll.command_MOVE(button, "setColumnHeaderView");
		assertEditor(
				"class Test extends JPanel {",
				"  Test() {",
				"    JScrollPane scroll = new JScrollPane();",
				"    add(scroll);",
				"    {",
				"      JButton button = new JButton();",
				"      scroll.setColumnHeaderView(button);",
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
						"    JScrollPane scroll = new JScrollPane();",
						"    add(scroll);",
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
		JScrollPaneInfo scroll = (JScrollPaneInfo) panel.getChildrenComponents().get(0);
		ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(1);
		ComponentInfo button = innerPanel.getChildrenComponents().get(0);
		//
		scroll.command_ADD(button, "setRowHeaderView");
		assertEditor(
				"class Test extends JPanel {",
				"  Test() {",
				"    JScrollPane scroll = new JScrollPane();",
				"    add(scroll);",
				"    {",
				"      JButton button = new JButton();",
				"      scroll.setRowHeaderView(button);",
				"    }",
				"    {",
				"      JPanel innerPanel = new JPanel();",
				"      add(innerPanel);",
				"    }",
				"  }",
				"}");
	}
}
