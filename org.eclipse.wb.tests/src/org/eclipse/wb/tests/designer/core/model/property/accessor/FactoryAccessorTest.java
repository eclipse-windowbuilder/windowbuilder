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
package org.eclipse.wb.tests.designer.core.model.property.accessor;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.accessor.FactoryAccessor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.StringLiteral;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link FactoryAccessor}.
 *
 * @author scheglov_ke
 */
public class FactoryAccessorTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		if (m_testProject != null) {
			setFileContentSrc(
					"test/StaticFactory.java",
					getTestSource(
							"public final class StaticFactory {",
							"  public static JButton createButton(String text) {",
							"    return new JButton(text);",
							"  }",
							"}"));
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
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(StaticFactory.createButton('text'));",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GenericProperty property = (GenericProperty) button.getPropertyByTitle("text");
		FactoryAccessor accessor = (FactoryAccessor) getGenericPropertyAccessors(property).get(1);
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
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(StaticFactory.createButton('text'));",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GenericProperty property = (GenericProperty) button.getPropertyByTitle("text");
		FactoryAccessor accessor = (FactoryAccessor) getGenericPropertyAccessors(property).get(1);
		// check
		assertTrue(accessor.setExpression(button, "\"new text\""));
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    add(StaticFactory.createButton('new text'));",
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
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(StaticFactory.createButton('text'));",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GenericProperty property = (GenericProperty) button.getPropertyByTitle("text");
		FactoryAccessor accessor = (FactoryAccessor) getGenericPropertyAccessors(property).get(1);
		// set "null", default value is used
		assertTrue(accessor.setExpression(button, null));
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    add(StaticFactory.createButton((String) null));",
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
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(StaticFactory.createButton('text'));",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// create accessor
		FactoryAccessor accessor = new FactoryAccessor(0, null);
		// set "null", but no default value, so ignored
		assertFalse(accessor.setExpression(button, null));
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    add(StaticFactory.createButton('text'));",
				"  }",
				"}");
	}
}
