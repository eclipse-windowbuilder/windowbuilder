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
package org.eclipse.wb.tests.designer.swing.model.layout.model;

import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.BorderLayoutInfo;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;

import org.junit.Test;

import java.awt.BorderLayout;

/**
 * Test for {@link BorderLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class BorderLayoutTest extends AbstractLayoutTest {
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
	// setLayout
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for installing.
	 */
	@Test
	public void test_setLayout() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		setLayout(panel, BorderLayout.class);
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout(0, 0));",
				"  }",
				"}");
	}

	/**
	 * Test for {@link BorderLayoutInfo#getComponent(String)}.
	 */
	@Test
	public void test_getComponent() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      final JButton button = new JButton();",
						"      add(button, BorderLayout.NORTH);",
						"    }",
						"  }",
						"}");
		BorderLayoutInfo layout = (BorderLayoutInfo) panel.getLayout();
		//
		panel.refresh();
		try {
			{
				ComponentInfo component = panel.getChildrenComponents().get(0);
				assertSame(component.getObject(), layout.getComponent(BorderLayout.NORTH));
			}
			assertNull(layout.getComponent(BorderLayout.WEST));
			assertNull(layout.getComponent(BorderLayout.EAST));
			assertNull(layout.getComponent(BorderLayout.SOUTH));
			assertNull(layout.getComponent(BorderLayout.CENTER));
		} finally {
			panel.refresh_dispose();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CREATE
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link BorderLayoutInfo#command_CREATE(ComponentInfo, String)}.
	 */
	@Test
	public void test_CREATE() throws Exception {
		String[] lines =
			{
					"public class Test extends JPanel {",
					"  public Test() {",
					"    setLayout(new BorderLayout());",
					"  }",
			"}"};
		ContainerInfo panel = parseContainer(lines);
		BorderLayoutInfo layout = (BorderLayoutInfo) panel.getLayout();
		// add component
		ComponentInfo newComponent = createJButton();
		layout.command_CREATE(newComponent, BorderLayout.NORTH);
		assertInstanceOf(InvocationChildAssociation.class, newComponent.getAssociation());
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, BorderLayout.NORTH);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link BorderLayoutInfo#command_CREATE(ComponentInfo, String)}.
	 */
	@Test
	public void test_CREATE_nullRegion() throws Exception {
		String[] lines =
			{
					"public class Test extends JPanel {",
					"  public Test() {",
					"    setLayout(new BorderLayout());",
					"  }",
			"}"};
		ContainerInfo panel = parseContainer(lines);
		BorderLayoutInfo layout = (BorderLayoutInfo) panel.getLayout();
		// add component
		ComponentInfo newComponent = createJButton();
		layout.command_CREATE(newComponent, null);
		assertInstanceOf(InvocationChildAssociation.class, newComponent.getAssociation());
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link BorderLayoutInfo#command_CREATE(ComponentInfo, String, ComponentInfo)}.
	 */
	@Test
	public void test_CREATE_withTarget() throws Exception {
		String[] lines =
			{
					"public class Test extends JPanel {",
					"  public Test() {",
					"    setLayout(new BorderLayout());",
					"    add(new JButton(), BorderLayout.NORTH);",
					"  }",
			"}"};
		ContainerInfo panel = parseContainer(lines);
		BorderLayoutInfo layout = (BorderLayoutInfo) panel.getLayout();
		ComponentInfo target = panel.getChildrenComponents().get(0);
		// add component
		ComponentInfo newComponent = createJButton();
		layout.command_CREATE(newComponent, BorderLayout.WEST, target);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, BorderLayout.WEST);",
				"    }",
				"    add(new JButton(), BorderLayout.NORTH);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MOVE
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for moving to other direction.
	 */
	@Test
	public void test_MOVE() throws Exception {
		check_MOVE(new String[]{
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, BorderLayout.NORTH);",
				"    }",
				"  }",
		"}"});
	}

	/**
	 * Test for moving to other direction with {@link LazyVariableSupport}.
	 */
	@Test
	public void test_MOVE_lazy() throws Exception {
		String[] source =
				new String[]{
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    add(getButton(), BorderLayout.NORTH);",
						"  }",
						"  private JButton button;",
						"  private JButton getButton() {",
						"    if (button == null) {",
						"      button = new JButton();",
						"    }",
						"    return button;",
						"  }",
		"}"};
		String[] source2 = replace(source, "NORTH", "SOUTH");
		check_MOVE(source, source2);
	}

	/**
	 * Test for moving to other direction with implicit {@link BorderLayout#CENTER}.
	 */
	@Test
	public void test_MOVE_implicitCenter() throws Exception {
		check_MOVE(new String[]{
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"    }",
				"  }",
		"}"});
	}

	/**
	 * Test for moving to SOUTH.
	 */
	private void check_MOVE(String[] lines) throws Exception {
		check_MOVE(lines, new String[]{
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, BorderLayout.SOUTH);",
				"    }",
				"  }",
		"}"});
	}

	/**
	 * Test for moving to SOUTH.
	 */
	private void check_MOVE(String[] lines, String[] expectedLines) throws Exception {
		ContainerInfo panel = parseContainer(lines);
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// prepare original association
		Association initialAssociation = button.getAssociation();
		Statement initialStatement = initialAssociation.getStatement();
		// move component
		{
			BorderLayoutInfo layout = (BorderLayoutInfo) panel.getLayout();
			layout.command_MOVE(button, BorderLayout.SOUTH);
		}
		// check source
		assertEditor(expectedLines);
		// check association, no modification expected
		assertSame(initialAssociation, button.getAssociation());
		assertSame(initialStatement, button.getAssociation().getStatement());
	}

	/**
	 * Test for {@link BorderLayoutInfo#command_REGION(ComponentInfo, String)}.
	 */
	@Test
	public void test_REGION() throws Exception {
		String[] lines =
			{
					"public class Test extends JPanel {",
					"  public Test() {",
					"    setLayout(new BorderLayout());",
					"    add(new JButton(), BorderLayout.NORTH);",
					"  }",
			"}"};
		ContainerInfo panel = parseContainer(lines);
		BorderLayoutInfo layout = (BorderLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// set region
		layout.command_REGION(button, BorderLayout.WEST);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    add(new JButton(), BorderLayout.WEST);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Reparenting
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for reparenting, normal variable.
	 */
	@Test
	public void test_reparentingVariable() throws Exception {
		String[] lines =
				new String[]{
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      JPanel panel = new JPanel();",
						"      add(panel, BorderLayout.NORTH);",
						"      {",
						"        JButton button = new JButton();",
						"        button.setEnabled(false);",
						"        panel.add(button);",
						"      }",
						"    }",
						"  }",
		"}"};
		String[] expectedLines =
				new String[]{
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      JPanel panel = new JPanel();",
						"      add(panel, BorderLayout.NORTH);",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, BorderLayout.SOUTH);",
						"      button.setEnabled(false);",
						"    }",
						"  }",
		"}"};
		check_reparenting(lines, expectedLines);
	}

	/**
	 * Test for reparenting, lazy variable.
	 */
	@Test
	public void test_reparentingLazy() throws Exception {
		String[] lines =
				new String[]{
						"public class Test extends JPanel {",
						"  private JPanel panel;",
						"  private JButton button;",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    add(getPanel(), BorderLayout.NORTH);",
						"  }",
						"  private JPanel getPanel() {",
						"    if (panel == null) {",
						"      panel = new JPanel();",
						"      panel.add(getButton());",
						"    }",
						"    return panel;",
						"  }",
						"  private JButton getButton() {",
						"    if (button == null) {",
						"      button = new JButton();",
						"    }",
						"    return button;",
						"  }",
		"}"};
		String[] expectedLines =
				new String[]{
						"public class Test extends JPanel {",
						"  private JPanel panel;",
						"  private JButton button;",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    add(getPanel(), BorderLayout.NORTH);",
						"    add(getButton(), BorderLayout.SOUTH);",
						"  }",
						"  private JPanel getPanel() {",
						"    if (panel == null) {",
						"      panel = new JPanel();",
						"    }",
						"    return panel;",
						"  }",
						"  private JButton getButton() {",
						"    if (button == null) {",
						"      button = new JButton();",
						"    }",
						"    return button;",
						"  }",
		"}"};
		check_reparenting(lines, expectedLines);
	}

	/**
	 * Test for reparenting to SOUTH.
	 */
	private void check_reparenting(String[] lines, String[] expectedLines) throws Exception {
		ContainerInfo panel = parseContainer(lines);
		// reparent component
		{
			ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
			ComponentInfo button = innerPanel.getChildrenComponents().get(0);
			//
			BorderLayoutInfo layout = (BorderLayoutInfo) panel.getLayout();
			layout.command_MOVE(button, BorderLayout.SOUTH);
			// check association
			{
				Association association = button.getAssociation();
				assertInstanceOf(CompilationUnit.class, association.getStatement().getRoot());
			}
		}
		// check source
		assertEditor(expectedLines);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// "Constraints" property
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_constraintsProperty() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, BorderLayout.NORTH);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// prepare "Constraints" property
		Property constraintsProperty = button.getPropertyByTitle("Constraints");
		assertNotNull(constraintsProperty);
		assertTrue(constraintsProperty.getCategory().isSystem());
		// "Constraints" property should be cached, so we return each time same instance
		assertSame(constraintsProperty, button.getPropertyByTitle("Constraints"));
		// move "button" to new region
		assertTrue(constraintsProperty.isModified());
		assertEquals("North", constraintsProperty.getValue());
		constraintsProperty.setValue("South");
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button, BorderLayout.SOUTH);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_constraintsProperty_unsupportedRegion() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      add(button, BorderLayout.BEFORE_FIRST_LINE);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// prepare "Constraints" property
		Property constraintsProperty = button.getPropertyByTitle("Constraints");
		assertEquals("", constraintsProperty.getValue());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getEmptyRegion()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link BorderLayoutInfo#getEmptyRegion()}.
	 */
	@Test
	public void test_getEmptyRegion_1() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    add(new JButton(), BorderLayout.NORTH);",
						"  }",
						"}");
		panel.refresh();
		BorderLayoutInfo layout = (BorderLayoutInfo) panel.getLayout();
		//
		assertSame(BorderLayout.SOUTH, layout.getEmptyRegion());
	}

	/**
	 * Test for {@link BorderLayoutInfo#getEmptyRegion()}.
	 */
	@Test
	public void test_getEmptyRegion_2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    add(new JButton(), BorderLayout.SOUTH);",
						"  }",
						"}");
		panel.refresh();
		BorderLayoutInfo layout = (BorderLayoutInfo) panel.getLayout();
		//
		assertSame(BorderLayout.NORTH, layout.getEmptyRegion());
	}

	/**
	 * Test for {@link BorderLayoutInfo#getEmptyRegion()}.
	 */
	@Test
	public void test_getEmptyRegion_3() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    add(new JButton(), BorderLayout.NORTH);",
						"    add(new JButton(), BorderLayout.SOUTH);",
						"  }",
						"}");
		panel.refresh();
		BorderLayoutInfo layout = (BorderLayoutInfo) panel.getLayout();
		//
		assertSame(BorderLayout.WEST, layout.getEmptyRegion());
	}

	/**
	 * Test for {@link BorderLayoutInfo#getEmptyRegion()}.
	 */
	@Test
	public void test_getEmptyRegion_4() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BorderLayout());",
						"    add(new JButton(), BorderLayout.NORTH);",
						"    add(new JButton(), BorderLayout.SOUTH);",
						"    add(new JButton(), BorderLayout.WEST);",
						"    add(new JButton(), BorderLayout.EAST);",
						"    add(new JButton(), BorderLayout.CENTER);",
						"  }",
						"}");
		panel.refresh();
		BorderLayoutInfo layout = (BorderLayoutInfo) panel.getLayout();
		//
		assertSame(null, layout.getEmptyRegion());
	}
}
