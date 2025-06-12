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
package org.eclipse.wb.tests.designer.core.model.property.accessor;

import org.eclipse.wb.internal.core.model.property.accessor.InvocationChildAssociationAccessor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.StringLiteral;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link InvocationChildAssociationAccessor}.
 *
 * @author scheglov_ke
 */
public class InvocationChildAssociationAccessorTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		if (m_testProject != null) {
			setFileContentSrc(
					"test/MyContainer.java",
					getTestSource(
							"public class MyContainer extends JPanel {",
							"  public void addChild(String text, Component component) {",
							"    add(component);",
							"  }",
							"}"));
			setFileContentSrc(
					"test/MyContainer.wbp-component.xml",
					getSourceDQ(
							"<?xml version='1.0' encoding='UTF-8'?>",
							"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
							"  <methods>",
							"    <method name='addChild'>",
							"      <parameter type='java.lang.String'/>",
							"      <parameter type='java.awt.Component' child='true'/>",
							"    </method>",
							"  </methods>",
							"</component>"));
			waitForAutoBuild();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_0() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends MyContainer {",
						"  public Test() {",
						"    addChild('text', new JButton());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		InvocationChildAssociationAccessor accessor = new InvocationChildAssociationAccessor(0, "\"\"");
		// check
		assertEquals("text", ((StringLiteral) accessor.getExpression(button)).getLiteralValue());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setExpression
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_setExpression_newValue() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends MyContainer {",
						"  public Test() {",
						"    addChild('text', new JButton());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		InvocationChildAssociationAccessor accessor = new InvocationChildAssociationAccessor(0, "\"\"");
		// check
		assertTrue(accessor.setExpression(button, "\"new text\""));
		assertEditor(
				"public class Test extends MyContainer {",
				"  public Test() {",
				"    addChild('new text', new JButton());",
				"  }",
				"}");
	}

	/**
	 * Use <code>null</code> to clear value.
	 */
	@Test
	public void test_setExpression_nullValue() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends MyContainer {",
						"  public Test() {",
						"    addChild('text', new JButton());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		InvocationChildAssociationAccessor accessor = new InvocationChildAssociationAccessor(0, "\"\"");
		// set "null", default value is used
		assertTrue(accessor.setExpression(button, null));
		assertEditor(
				"public class Test extends MyContainer {",
				"  public Test() {",
				"    addChild('', new JButton());",
				"  }",
				"}");
	}

	/**
	 * Use <code>null</code> to clear value.
	 */
	@Test
	public void test_setExpression_nullValue_noDefault() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends MyContainer {",
						"  public Test() {",
						"    addChild('text', new JButton());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		InvocationChildAssociationAccessor accessor = new InvocationChildAssociationAccessor(0, null);
		// set "null", but no default value, so ignored
		assertFalse(accessor.setExpression(button, null));
		assertEditor(
				"public class Test extends MyContainer {",
				"  public Test() {",
				"    addChild('text', new JButton());",
				"  }",
				"}");
	}
}
