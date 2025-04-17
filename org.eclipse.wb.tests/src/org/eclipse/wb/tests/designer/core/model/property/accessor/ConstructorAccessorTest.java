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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ConstructorAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

import org.junit.Test;

/**
 * Tests for {@link ConstructorAccessor}.
 *
 * @author scheglov_ke
 */
public class ConstructorAccessorTest extends SwingModelTest {
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
						"    add(new JButton('text'));",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GenericProperty property = (GenericProperty) button.getPropertyByTitle("text");
		ConstructorAccessor accessor =
				(ConstructorAccessor) getGenericPropertyAccessors(property).get(1);
		// check
		assertSame(Property.UNKNOWN_VALUE, accessor.getDefaultValue(button));
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
						"    add(new JButton('text'));",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GenericProperty property = (GenericProperty) button.getPropertyByTitle("text");
		ConstructorAccessor accessor =
				(ConstructorAccessor) getGenericPropertyAccessors(property).get(1);
		// check
		assertTrue(accessor.setExpression(button, "\"new text\""));
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    add(new JButton('new text'));",
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
						"    add(new JButton('text'));",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GenericProperty property = (GenericProperty) button.getPropertyByTitle("text");
		ConstructorAccessor accessor =
				(ConstructorAccessor) getGenericPropertyAccessors(property).get(1);
		// set "null", default value is used
		assertTrue(accessor.setExpression(button, null));
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    add(new JButton((String) null));",
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
						"    add(new JButton('text'));",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// create accessor
		ConstructorAccessor accessor = new ConstructorAccessor(0, null);
		// set "null", but no default value, so ignored
		assertFalse(accessor.setExpression(button, null));
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    add(new JButton('text'));",
				"  }",
				"}");
	}

	/**
	 * Test that {@link ConstructorAccessor} resolves deferred {@link JavaInfo} references.
	 */
	@Test
	public void test_setExpression_replaceComponent() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public MyPanel(JButton button) {",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		parseContainer(
				"public class Test extends JPanel {",
				"  private final JButton button_1 = new JButton();",
				"  private final JButton button_2 = new JButton();",
				"  private final MyPanel myPanel = new MyPanel(button_1);",
				"  public Test() {",
				"    add(button_1);",
				"    add(button_2);",
				"    add(myPanel);",
				"  }",
				"}");
		ContainerInfo myPanel = getJavaInfoByName("myPanel");
		ComponentInfo button_1 = getJavaInfoByName("button_1");
		ComponentInfo button_2 = getJavaInfoByName("button_2");
		// prepare ExpressionAccessor
		ExpressionAccessor setterAccessor;
		{
			GenericProperty property =
					(GenericProperty) PropertyUtils.getByPath(myPanel, "Constructor/button");
			assertNotNull(property);
			setterAccessor = getGenericPropertyAccessors(property).get(0);
		}
		// "button_1" initially
		{
			Expression expression = setterAccessor.getExpression(myPanel);
			assertTrue(button_1.isRepresentedBy(expression));
		}
		// use "button_2"
		{
			String source = TemplateUtils.format("{0}", button_2);
			setterAccessor.setExpression(myPanel, source);
		}
		assertEditor(
				"public class Test extends JPanel {",
				"  private final JButton button_1 = new JButton();",
				"  private final JButton button_2 = new JButton();",
				"  private final MyPanel myPanel = new MyPanel(button_2);",
				"  public Test() {",
				"    add(button_1);",
				"    add(button_2);",
				"    add(myPanel);",
				"  }",
				"}");
	}
}
