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

import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;

import java.awt.FlowLayout;

import javax.swing.JPanel;

/**
 * Test for {@link FlowLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class FlowLayoutTest extends AbstractLayoutTest {
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
	public void test_setLayout() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		setLayout(panel, FlowLayout.class);
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));",
				"  }",
				"}");
	}

	/**
	 * Test for installing, remove other layout constraints.
	 */
	public void test_setLayout2() throws Exception {
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
		setLayout(panel, FlowLayout.class);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for adding new component: as last.
	 */
	public void test_add_last() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FlowLayout());",
						"  }",
						"}");
		// add component
		{
			FlowLayoutInfo layout = (FlowLayoutInfo) panel.getLayout();
			ComponentInfo newComponent = createJButton();
			layout.add(newComponent, null);
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FlowLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for adding new component: before existing.
	 */
	public void test_add_before() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new FlowLayout());",
						"    {",
						"      JButton button = new JButton('111');",
						"      add(button);",
						"    }",
						"  }",
						"}");
		// add component
		{
			FlowLayoutInfo layout = (FlowLayoutInfo) panel.getLayout();
			ComponentInfo existingButton = panel.getChildrenComponents().get(0);
			//
			ComponentInfo newComponent = createJButton();
			layout.add(newComponent, existingButton);
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new FlowLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"    }",
				"    {",
				"      JButton button = new JButton(\"111\");",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Move
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for moving: before.
	 */
	public void test_move_before() throws Exception {
		String[] lines =
				new String[]{
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button_1 = new JButton();",
						"      add(button_1);",
						"    }",
						"    {",
						"      JButton button_2 = new JButton();",
						"      add(button_2);",
						"    }",
						"  }",
		"}"};
		String[] expectedLines =
				new String[]{
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button_2 = new JButton();",
						"      add(button_2);",
						"    }",
						"    {",
						"      JButton button_1 = new JButton();",
						"      add(button_1);",
						"    }",
						"  }",
		"}"};
		//
		check_move_before(lines, expectedLines);
	}

	/**
	 * Test for moving {@link JPanel} with children.
	 */
	public void test_moveComplex() throws Exception {
		String[] lines =
				new String[]{
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"    {",
						"      JPanel complexPanel = new JPanel();",
						"      add(complexPanel);",
						"      {",
						"        JButton button2 = new JButton();",
						"        complexPanel.add(button2);",
						"      }",
						"    }",
						"  }",
		"}"};
		String[] expectedLines =
				new String[]{
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JPanel complexPanel = new JPanel();",
						"      add(complexPanel);",
						"      {",
						"        JButton button2 = new JButton();",
						"        complexPanel.add(button2);",
						"      }",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"  }",
		"}"};
		//
		check_move_before(lines, expectedLines);
	}

	/**
	 * Test for moving: before, lazy.
	 */
	public void test_move_before_lazy() throws Exception {
		String[] lines =
				new String[]{
						"public class Test extends JPanel {",
						"  private JButton button_1;",
						"  private JButton button_2;",
						"  public Test() {",
						"    add(getButton_1());",
						"    add(getButton_2());",
						"  }",
						"  private JButton getButton_1() {",
						"    if (button_1 == null) {",
						"      button_1 = new JButton(\"Button 1\");",
						"    }",
						"    return button_1;",
						"  }",
						"  private JButton getButton_2() {",
						"    if (button_2 == null) {",
						"      button_2 = new JButton(\"button 2\");",
						"    }",
						"    return button_2;",
						"  }",
		"}"};
		String[] expectedLines =
				new String[]{
						"public class Test extends JPanel {",
						"  private JButton button_1;",
						"  private JButton button_2;",
						"  public Test() {",
						"    add(getButton_2());",
						"    add(getButton_1());",
						"  }",
						"  private JButton getButton_1() {",
						"    if (button_1 == null) {",
						"      button_1 = new JButton(\"Button 1\");",
						"    }",
						"    return button_1;",
						"  }",
						"  private JButton getButton_2() {",
						"    if (button_2 == null) {",
						"      button_2 = new JButton(\"button 2\");",
						"    }",
						"    return button_2;",
						"  }",
		"}"};
		//
		check_move_before(lines, expectedLines);
	}

	/**
	 * Test for moving.
	 */
	private void check_move_before(String[] lines, String[] expectedLines) throws Exception {
		ContainerInfo panel = parseContainer(lines);
		// move button_2 before button_1
		{
			FlowLayoutInfo layout = (FlowLayoutInfo) panel.getLayout();
			ComponentInfo button_1 = panel.getChildrenComponents().get(0);
			ComponentInfo button_2 = panel.getChildrenComponents().get(1);
			//
			layout.move(button_2, button_1);
		}
		// check source
		assertEditor(expectedLines);
	}

	/**
	 * Test for move last.
	 */
	public void test_move_last() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button_1 = new JButton();",
						"      add(button_1);",
						"    }",
						"    {",
						"      JButton button_2 = new JButton();",
						"      add(button_2);",
						"    }",
						"  }",
						"}");
		// move button_1 to last
		{
			FlowLayoutInfo layout = (FlowLayoutInfo) panel.getLayout();
			ComponentInfo button_1 = panel.getChildrenComponents().get(0);
			//
			layout.move(button_1, null);
		}
		// check source
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JButton button_2 = new JButton();",
				"      add(button_2);",
				"    }",
				"    {",
				"      JButton button_1 = new JButton();",
				"      add(button_1);",
				"    }",
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
	public void test_reparentingVariable() throws Exception {
		String[] lines =
				new String[]{
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JPanel panel = new JPanel();",
						"      panel.setLayout(new BorderLayout());",
						"      panel.setPreferredSize(new Dimension(150, 150));",
						"      add(panel);",
						"      {",
						"        JButton button = new JButton();",
						"        panel.add(button, BorderLayout.NORTH);",
						"      }",
						"    }",
						"  }",
		"}"};
		String[] expectedLines =
				new String[]{
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JPanel panel = new JPanel();",
						"      panel.setLayout(new BorderLayout());",
						"      panel.setPreferredSize(new Dimension(150, 150));",
						"      add(panel);",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"  }",
		"}"};
		check_reparenting(lines, expectedLines);
	}

	/**
	 * Test for reparenting.
	 */
	private void check_reparenting(String[] lines, String[] expectedLines) throws Exception {
		ContainerInfo panel = parseContainer(lines);
		// reparent component
		{
			ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
			ComponentInfo button = innerPanel.getChildrenComponents().get(0);
			//
			FlowLayoutInfo layout = (FlowLayoutInfo) panel.getLayout();
			layout.move(button, null);
		}
		// check source
		assertEditor(expectedLines);
	}

	/**
	 * Test for copy/paste.
	 */
	public void DISABLE_test_clipboard() throws Exception {
		String[] lines1 =
			{
					"public class Test extends JPanel {",
					"  public Test() {",
					"    {",
					"      JPanel inner = new JPanel();",
					"      add(inner);",
					"      {",
					"        JButton button = new JButton('A');",
					"        inner.add(button);",
					"      }",
					"      {",
					"        JButton button = new JButton('B');",
					"        inner.add(button);",
					"      }",
					"    }",
					"  }",
			"}"};
		ContainerInfo panel = parseContainer(lines1);
		panel.refresh();
		// prepare memento
		JavaInfoMemento memento;
		{
			ComponentInfo inner = panel.getChildrenComponents().get(0);
			memento = JavaInfoMemento.createMemento(inner);
		}
		// add copy
		ContainerInfo copy = (ContainerInfo) memento.create(panel);
		((FlowLayoutInfo) panel.getLayout()).add(copy, null);
		memento.apply();
		String[] lines =
			{
					"public class Test extends JPanel {",
					"  public Test() {",
					"    {",
					"      JPanel inner = new JPanel();",
					"      add(inner);",
					"      {",
					"        JButton button = new JButton('A');",
					"        inner.add(button);",
					"      }",
					"      {",
					"        JButton button = new JButton('B');",
					"        inner.add(button);",
					"      }",
					"    }",
					"    {",
					"      JPanel panel = new JPanel();",
					"      add(panel);",
					"      {",
					"        JButton button = new JButton('A');",
					"        panel.add(button);",
					"      }",
					"      {",
					"        JButton button = new JButton('B');",
					"        panel.add(button);",
					"      }",
					"    }",
					"  }",
			"}"};
		assertEditor(lines);
	}
}
