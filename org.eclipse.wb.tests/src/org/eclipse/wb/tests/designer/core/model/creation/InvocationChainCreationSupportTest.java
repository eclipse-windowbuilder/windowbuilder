/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.core.model.creation;

import org.eclipse.wb.internal.core.model.creation.InvocationChainCreationSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link InvocationChainCreationSupport}.
 *
 * @author scheglov_ke
 */
public class InvocationChainCreationSupportTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * "button_1" declared with fully qualified class name.<br>
	 * "button_2" has no fully qualified class name, so return type of method should be used.<br>
	 * "button_3" has no declaration, so should not be visible.
	 */
	@Test
	public void test_0() throws Exception {
		setFileContentSrc(
				"test/Wrapper.java",
				getTestSource(
						"public class Wrapper {",
						"  private final JButton button_1 = new JButton();",
						"  private final JButton button_2 = new JButton();",
						"  private final JButton button_3 = new JButton();",
						"  public JButton getButton_1() {",
						"    return button_1;",
						"  }",
						"  public JButton getButton_2() {",
						"    return button_2;",
						"  }",
						"  public JButton getButton_3() {",
						"    return button_3;",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  private final Wrapper wrapper = new Wrapper();",
						"  public MyPanel() {",
						"    add(wrapper.getButton_1());",
						"    add(wrapper.getButton_2());",
						"  }",
						"  public Wrapper getWrapper() {",
						"    return wrapper;",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <parameters>",
						"    <parameter name='invocationChain: getWrapper().getButton()'>javax.swing.JButton</parameter>",
						"    <parameter name='invocationChain: getWrapper().getButton_1()'>javax.swing.JButton</parameter>",
						"    <parameter name='invocationChain: getWrapper().getButton_2()'/>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    JButton button_1 = getWrapper().getButton_1();",
						"    JButton button_2 = getWrapper().getButton_2();",
						"    JButton button_3 = getWrapper().getButton_3();",
						"  }",
						"}");
		// check hierarchy
		assertHierarchy(
				"{this: test.MyPanel} {this} {/getWrapper().getButton_1()/ /getWrapper().getButton_2()/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {invocationChain: getWrapper().getButton_1()} {local-unique: button_1} {/getWrapper().getButton_1()/}",
				"  {invocationChain: getWrapper().getButton_2()} {local-unique: button_2} {/getWrapper().getButton_2()/}");
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		ComponentInfo button_2 = panel.getChildrenComponents().get(1);
		// check CreationSupport for "button_1"
		{
			InvocationChainCreationSupport creationSupport =
					(InvocationChainCreationSupport) button_1.getCreationSupport();
			assertEquals("getWrapper().getButton_1()", m_lastEditor.getSource(creationSupport.getNode()));
			assertFalse(creationSupport.canDelete());
			assertFalse(creationSupport.canReorder());
			assertFalse(creationSupport.canReparent());
		}
		// refresh
		panel.refresh();
		assertNoErrors(panel);
		assertNotNull(button_1.getObject());
		assertNotNull(button_2.getObject());
		assertNotSame(button_1.getObject(), button_2.getObject());
	}
}
