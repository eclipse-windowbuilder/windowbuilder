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
package org.eclipse.wb.tests.designer.core.model.association;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.InvocationSecondaryAssociation;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Test;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

/**
 * Test for {@link InvocationSecondaryAssociation}.
 *
 * @author scheglov_ke
 */
public class InvocationSecondaryAssociationTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_0() throws Exception {
		setFileContentSrc(
				"test/AFrame.java",
				getTestSource(
						"public class AFrame extends JFrame {",
						"  protected void addGB(Container parent, Component child, String constraints) {",
						"    parent.add(child, constraints);",
						"  }",
						"}"));
		setFileContentSrc(
				"test/AFrame.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <methods>",
						"    <method name='addGB'>",
						"      <parameter type='java.awt.Container' parent2='true'/>",
						"      <parameter type='java.awt.Component' child2='true'/>",
						"      <parameter type='java.lang.String'/>",
						"    </method>",
						"  </methods>",
						"</component>"));
		waitForAutoBuild();
		//
		ContainerInfo frame =
				parseContainer(
						"public class Test extends AFrame {",
						"  public Test() {",
						"    addGB(getContentPane(), new JButton('north'), BorderLayout.NORTH);",
						"    addGB(getContentPane(), new JButton('west'), BorderLayout.WEST);",
						"  }",
						"}");
		frame.refresh();
		// prepare contentPane with BorderLayout
		assertEquals(1, frame.getChildrenComponents().size());
		ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
		BorderLayout borderLayout = (BorderLayout) contentPane.getContainer().getLayout();
		// check children of contentPane
		assertEquals(2, contentPane.getChildrenComponents().size());
		{
			Container container = contentPane.getContainer();
			Component[] components = container.getComponents();
			assertEquals(2, components.length);
		}
		// check "button north"
		{
			ComponentInfo button = contentPane.getChildrenComponents().get(0);
			assertSame(BorderLayout.NORTH, borderLayout.getConstraints(button.getComponent()));
		}
		// check "button west"
		{
			ComponentInfo button = contentPane.getChildrenComponents().get(1);
			assertSame(BorderLayout.WEST, borderLayout.getConstraints(button.getComponent()));
		}
		// check association for "button"
		{
			ComponentInfo button = contentPane.getChildrenComponents().get(0);
			InvocationSecondaryAssociation association =
					(InvocationSecondaryAssociation) button.getAssociation();
			assertEquals(
					"addGB(getContentPane(), new JButton(\"north\"), BorderLayout.NORTH)",
					association.getSource());
			assertEquals(
					"addGB(getContentPane(), new JButton(\"north\"), BorderLayout.NORTH);",
					m_lastEditor.getSource(association.getStatement()));
			// can not be moved
			try {
				association.move(null);
				fail();
			} catch (NotImplementedException e) {
			}
			// can not be reparented
			try {
				association.setParent(null);
				fail();
			} catch (NotImplementedException e) {
			}
		}
	}

	/**
	 * We can delete "secondary child", if there is alternative method without child argument.<br>
	 * For {@link MyPanel#add2(Component, Object)} there is {@link MyPanel#add2(Component)}
	 * alternative.
	 */
	@Test
	public void test_deletePossible() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public void add2(Component child, Object constraints) {",
						"    add(child, constraints);",
						"  }",
						"  public void add2(Component child) {",
						"    add(child);",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <methods>",
						"    <method name='add2'>",
						"      <parameter type='java.awt.Component' child='true' parent2='true'/>",
						"      <parameter type='java.lang.Object' child2='true'/>",
						"    </method>",
						"    <method name='add2'>",
						"      <parameter type='java.awt.Component' child='true'/>",
						"    </method>",
						"  </methods>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    add2(new JButton(), new GridBagConstraints());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		JavaInfo constraints = button.getChildrenJava().get(0);
		// check association
		InvocationSecondaryAssociation association =
				(InvocationSecondaryAssociation) constraints.getAssociation();
		assertEquals("add2(new JButton(), new GridBagConstraints())", association.getSource());
		// we can delete
		assertTrue(association.canDelete());
		assertTrue(button.canDelete());
		constraints.delete();
		assertEditor(
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"    add2(new JButton());",
				"  }",
				"}");
	}

	/**
	 * We can delete "secondary child", even if there are no alternative method without child
	 * argument, but we just have enabling tag for existing method.<br>
	 * For {@link MyPanel#add2(Component, Object)} there is {@link MyPanel#add2(Component)}
	 * alternative.
	 */
	@Test
	public void test_deletePossible2() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public void add2(Component child) {",
						"    add(child);",
						"  }",
						"  public void setConstraints2(Component child, Object constraints) {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <methods>",
						"    <method name='add2'>",
						"      <parameter type='java.awt.Component' child='true'/>",
						"    </method>",
						"    <method name='setConstraints2'>",
						"      <parameter type='java.awt.Component' parent2='true'/>",
						"      <parameter type='java.lang.Object' child2='true'/>",
						"      <tag name='secondaryAssociation.alwaysDelete' value='true'/>",
						"    </method>",
						"  </methods>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      add2(button);",
						"      setConstraints2(button, new GridBagConstraints());",
						"    }",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		JavaInfo constraints = button.getChildrenJava().get(0);
		// check association
		InvocationSecondaryAssociation association =
				(InvocationSecondaryAssociation) constraints.getAssociation();
		assertEquals("setConstraints2(button, new GridBagConstraints())", association.getSource());
		// we can delete
		assertTrue(association.canDelete());
		assertTrue(button.canDelete());
		constraints.delete();
		assertEditor(
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"    {",
				"      JButton button = new JButton();",
				"      add2(button);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * We can delete "secondary child", if there is alternative method without child argument.<br>
	 * For {@link MyPanel#add2(Component, Object)} there is no alternative.
	 */
	@Test
	public void test_deleteNotPossible() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public void add2(Component child, Object constraints) {",
						"    add(child, constraints);",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <methods>",
						"    <method name='add2'>",
						"      <parameter type='java.awt.Component' child='true' parent2='true'/>",
						"      <parameter type='java.lang.Object' child2='true'/>",
						"    </method>",
						"  </methods>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    add2(new JButton(), new GridBagConstraints());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		JavaInfo constraints = button.getChildrenJava().get(0);
		// check association
		InvocationSecondaryAssociation association =
				(InvocationSecondaryAssociation) constraints.getAssociation();
		assertEquals("add2(new JButton(), new GridBagConstraints())", association.getSource());
		// we can NOT delete
		assertFalse(association.canDelete());
		assertFalse(button.canDelete());
	}

	/**
	 * Test that when we delete parent ("button"), its secondary associated child also deleted
	 * correctly.<br>
	 * Problem is that we first delete association between "button" and "panel", so association
	 * between "button" and "constraints" become dangling. We should handle this situation correctly.
	 */
	@Test
	public void test_deleteParent() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public void add2(Component child, Object constraints) {",
						"    add(child, constraints);",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <methods>",
						"    <method name='add2'>",
						"      <parameter type='java.awt.Component' child='true' parent2='true'/>",
						"      <parameter type='java.lang.Object' child2='true'/>",
						"    </method>",
						"  </methods>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setLayout(new GridBagLayout());",
						"    add2(new JButton(), new GridBagConstraints());",
						"  }",
						"}");
		// delete "button"
		ComponentInfo button = panel.getChildrenComponents().get(0);
		button.delete();
		// check result
		assertEditor(
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setLayout(new GridBagLayout());",
				"  }",
				"}");
	}
}
