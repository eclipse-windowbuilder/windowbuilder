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
package org.eclipse.wb.tests.designer.core.model.operations;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.ExposedPropertyCreationSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

import javax.swing.JPanel;

/**
 * Test for different {@link JavaInfo#delete()} cases.
 *
 * @author scheglov_ke
 */
public class DeleteTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Components with {@link ExposedPropertyCreationSupport} can be "deleted", but for them this
	 * means that their delete children and related nodes, but keep themselves in parent.
	 */
	@Test
	public void test_exposedProperty() throws Exception {
		ContainerInfo frame =
				parseContainer(
						"public final class Test extends JFrame {",
						"  public Test() {",
						"    getContentPane().setEnabled(false);",
						"    setTitle('My frame');",
						"    //",
						"    JButton button = new JButton();",
						"    getContentPane().add(button);",
						"  }",
						"}");
		assertEquals(1, frame.getChildrenComponents().size());
		ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
		//
		assertTrue(contentPane.canDelete());
		contentPane.delete();
		assertEditor(
				"public final class Test extends JFrame {",
				"  public Test() {",
				"    setTitle('My frame');",
				"  }",
				"}");
		assertEquals(1, frame.getChildrenComponents().size());
		assertTrue(contentPane.getChildrenComponents().isEmpty());
	}

	/**
	 * Test {@link JavaInfo} delete for {@link JPanel} with children.
	 */
	@Test
	public void test_withChildren() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public final class Test extends JPanel {",
						"  public Test() {",
						"    JPanel innerPanel = new JPanel();",
						"    add(innerPanel);",
						"    //",
						"    JButton button = new JButton();",
						"    innerPanel.add(button);",
						"  }",
						"}");
		ComponentInfo innerPanel = panel.getChildrenComponents().get(0);
		//
		assertTrue(innerPanel.canDelete());
		innerPanel.delete();
		assertEditor(
				"// filler filler filler",
				"public final class Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
		assertTrue(panel.getChildrenComponents().isEmpty());
	}

	/**
	 * Test {@link JavaInfo} delete for {@link JPanel} with exposed children.
	 */
	@Test
	public void test_withExposedChildren() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  private JButton m_button;",
						"  public MyPanel() {",
						"    m_button = new JButton();",
						"    add(m_button);",
						"  }",
						"  public JButton getButton() {",
						"    return m_button;",
						"  }",
						"}"));
		waitForAutoBuild();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public final class Test extends JPanel {",
						"  public Test() {",
						"    MyPanel myPanel = new MyPanel();",
						"    add(myPanel);",
						"}",
						"}");
		// prepare panel with exposed button
		ContainerInfo myPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
		assertEquals(1, myPanel.getChildrenComponents().size());
		// prepare exposed button, check that it is really exposed
		ComponentInfo exposedButton = myPanel.getChildrenComponents().get(0);
		assertInstanceOf(ExposedPropertyCreationSupport.class, exposedButton.getCreationSupport());
		//
		assertTrue(myPanel.canDelete());
		myPanel.delete();
		assertEditor(
				"// filler filler filler",
				"public final class Test extends JPanel {",
				"  public Test() {",
				"}",
				"}");
		assertTrue(panel.getChildrenComponents().isEmpty());
	}
}
