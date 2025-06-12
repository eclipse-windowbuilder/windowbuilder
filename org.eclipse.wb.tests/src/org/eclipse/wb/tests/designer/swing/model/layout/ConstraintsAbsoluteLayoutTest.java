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
package org.eclipse.wb.tests.designer.swing.model.layout;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.absolute.ConstraintsAbsoluteLayoutDataInfo;
import org.eclipse.wb.internal.swing.model.layout.absolute.ConstraintsAbsoluteLayoutInfo;
import org.eclipse.wb.tests.designer.ResourceUtils;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ConstraintsAbsoluteLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class ConstraintsAbsoluteLayoutTest extends AbstractLayoutTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureNewProject() throws Exception {
		super.configureNewProject();
		ResourceUtils.resources2project(m_testProject, "resources/Swing/absolute");
		waitForAutoBuild();
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
	@Test
	public void test_parse() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new MyLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, new MyConstraints(1, 2, 3, 4));",
						"    }",
						"  }",
						"}");
		panel.refresh();
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/setLayout(new MyLayout())/ /add(button, new MyConstraints(1, 2, 3, 4))/}",
				"  {new: test.MyLayout} {empty} {/setLayout(new MyLayout())/}",
				"  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button, new MyConstraints(1, 2, 3, 4))/}",
				"    {new: test.MyConstraints} {empty} {/add(button, new MyConstraints(1, 2, 3, 4))/}");
		//
		ComponentInfo button = panel.getChildrenComponents().get(0);
		{
			Property boundsProperty = button.getPropertyByTitle("Bounds");
			assertEquals("(1, 2, 3, 4)", getPropertyText(boundsProperty));
			assertBoundsSubProperty_getValue(button, "x", 1);
			assertBoundsSubProperty_getValue(button, "y", 2);
			assertBoundsSubProperty_getValue(button, "width", 3);
			assertBoundsSubProperty_getValue(button, "height", 4);
		}
	}

	private static void assertBoundsSubProperty_getValue(ComponentInfo component,
			String name,
			int expected) throws Exception {
		Property property = PropertyUtils.getByPath(component, "Bounds/" + name);
		assertNotNull(property);
		assertEquals(expected, property.getValue());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Constraints
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ConstraintsAbsoluteLayoutInfo#getConstraints(ComponentInfo)}.
	 */
	@Test
	public void test_getConstraints_existing() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new MyLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, new MyConstraints(1, 2, 3, 4));",
						"    }",
						"  }",
						"}");
		panel.refresh();
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/setLayout(new MyLayout())/ /add(button, new MyConstraints(1, 2, 3, 4))/}",
				"  {new: test.MyLayout} {empty} {/setLayout(new MyLayout())/}",
				"  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button, new MyConstraints(1, 2, 3, 4))/}",
				"    {new: test.MyConstraints} {empty} {/add(button, new MyConstraints(1, 2, 3, 4))/}");
		String expectedSource = m_lastEditor.getSource();
		//
		ComponentInfo button = panel.getChildrenComponents().get(0);
		ConstraintsAbsoluteLayoutDataInfo constraints =
				ConstraintsAbsoluteLayoutInfo.getConstraints(button);
		assertNotNull(constraints);
		// source not changed
		assertEditor(expectedSource, m_lastEditor);
	}

	/**
	 * Test for {@link ConstraintsAbsoluteLayoutInfo#getConstraints(ComponentInfo)}.
	 */
	@Test
	public void test_getConstraints_new() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new MyLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/setLayout(new MyLayout())/ /add(button)/}",
				"  {new: test.MyLayout} {empty} {/setLayout(new MyLayout())/}",
				"  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/}");
		//
		ComponentInfo button = panel.getChildrenComponents().get(0);
		ConstraintsAbsoluteLayoutDataInfo constraints =
				ConstraintsAbsoluteLayoutInfo.getConstraints(button);
		assertNotNull(constraints);
		// source changed
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new MyLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, new MyConstraints(0, 0, 0, 0));",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/setLayout(new MyLayout())/ /add(button, new MyConstraints(0, 0, 0, 0))/}",
				"  {new: test.MyLayout} {empty} {/setLayout(new MyLayout())/}",
				"  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button, new MyConstraints(0, 0, 0, 0))/}",
				"    {new: test.MyConstraints} {empty} {/add(button, new MyConstraints(0, 0, 0, 0))/}");
	}

	/**
	 * {@link ConstraintsAbsoluteLayoutDataInfo} should not be displayed in components tree.
	 */
	@Test
	public void test_Constraints_isNotVisibleInTree() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new MyLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, new MyConstraints(1, 2, 3, 4));",
						"    }",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		ConstraintsAbsoluteLayoutDataInfo constraints =
				ConstraintsAbsoluteLayoutInfo.getConstraints(button);
		//
		assertVisibleInTree(constraints, false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ConstraintsAbsoluteLayoutInfo#command_CREATE(ComponentInfo, ComponentInfo)}.
	 */
	@Test
	public void test_CREATE() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new MyLayout());",
						"  }",
						"}");
		panel.refresh();
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/setLayout(new MyLayout())/}",
				"  {new: test.MyLayout} {empty} {/setLayout(new MyLayout())/}");
		//
		ConstraintsAbsoluteLayoutInfo layout = (ConstraintsAbsoluteLayoutInfo) panel.getLayout();
		ComponentInfo newButton = createJButton();
		layout.command_CREATE(newButton, null);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new MyLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/setLayout(new MyLayout())/ /add(button)/}",
				"  {new: test.MyLayout} {empty} {/setLayout(new MyLayout())/}",
				"  {new: javax.swing.JButton empty} {local-unique: button} {/new JButton()/ /add(button)/}");
		// set bounds
		layout.command_BOUNDS(newButton, new Point(1, 2), new Dimension(3, 4));
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new MyLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, new MyConstraints(1, 2, 3, 4));",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/setLayout(new MyLayout())/ /add(button, new MyConstraints(1, 2, 3, 4))/}",
				"  {new: test.MyLayout} {empty} {/setLayout(new MyLayout())/}",
				"  {new: javax.swing.JButton empty} {local-unique: button} {/new JButton()/ /add(button, new MyConstraints(1, 2, 3, 4))/}",
				"    {new: test.MyConstraints} {empty} {/add(button, new MyConstraints(1, 2, 3, 4))/}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Bounds
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * When set "width" to preferred size, then <code>0</code> should be used.
	 */
	@Test
	public void test_setWidth_preferred() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new MyLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, new MyConstraints(10, 20, 100, 50));",
						"    }",
						"  }",
						"}");
		panel.refresh();
		//
		ComponentInfo button = panel.getChildrenComponents().get(0);
		Property property = PropertyUtils.getByPath(button, "Bounds/width");
		property.setValue(button.getPreferredSize().width);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new MyLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, new MyConstraints(10, 20, 0, 50));",
				"    }",
				"  }",
				"}");
	}

	/**
	 * When set "height" to preferred size, then <code>0</code> should be used.
	 */
	@Test
	public void test_setHeight_preferred() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new MyLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, new MyConstraints(10, 20, 100, 50));",
						"    }",
						"  }",
						"}");
		panel.refresh();
		//
		ComponentInfo button = panel.getChildrenComponents().get(0);
		Property property = PropertyUtils.getByPath(button, "Bounds/height");
		property.setValue(button.getPreferredSize().height);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new MyLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, new MyConstraints(10, 20, 100, 0));",
				"    }",
				"  }",
				"}");
	}
}
