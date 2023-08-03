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
package org.eclipse.wb.tests.designer.core.model.creation;

import org.eclipse.wb.internal.core.model.creation.SuperInvocationCreationSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

/**
 * Test for {@link SuperInvocationCreationSupport}.
 *
 * @author scheglov_ke
 */
public class SuperInvocationCreationSupportTest extends SwingModelTest {
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
	public void test_access() throws Exception {
		prepareMyPanel();
		parseContainer(
				"// filler filler filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    JButton button = super.getMyButton();",
				"    add(button);",
				"  }",
				"}");
		assertHierarchy(
				"{this: test.MyPanel} {this} {/add(button)/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {superInvocation: super.getMyButton()} {local-unique: button} {/super.getMyButton()/ /add(button)/}");
		ComponentInfo button = getJavaInfoByName("button");
		SuperInvocationCreationSupport creationSupport =
				(SuperInvocationCreationSupport) button.getCreationSupport();
		// check node
		assertEquals("super.getMyButton()", m_lastEditor.getSource(creationSupport.getNode()));
		assertEquals("superInvocation: super.getMyButton()", creationSupport.toString());
		assertTrue(creationSupport.isJavaInfo(creationSupport.getNode()));
		assertFalse(creationSupport.isJavaInfo(null));
		// operations validation
		assertTrue(creationSupport.canReorder());
		assertTrue(creationSupport.canReparent());
	}

	@Test
	public void test_delete() throws Exception {
		prepareMyPanel();
		parseContainer(
				"// filler filler filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    JButton button = super.getMyButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		//
		assertTrue(button.canDelete());
		assertTrue(button.getCreationSupport().canDelete());
		button.delete();
		assertEditor(
				"// filler filler filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"  }",
				"}");
	}

	private void prepareMyPanel() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  private JButton myButton = new JButton();",
						"  public JButton getMyButton() {",
						"    return myButton;",
						"  }",
						"}"));
		waitForAutoBuild();
	}
}
