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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.IAccessibleExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.IExposableExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SetterAccessor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

import javax.swing.JPanel;

/**
 * Tests for {@link SetterAccessor}.
 *
 * @author scheglov_ke
 */
public class SetterAccessorTest extends SwingModelTest {
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
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link SetterAccessor#getGetter()}.
	 */
	@Test
	public void test_getGetter() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setEnabled(false);",
						"  }",
						"}");
		GenericProperty property = (GenericProperty) panel.getPropertyByTitle("enabled");
		SetterAccessor accessor = (SetterAccessor) getGenericPropertyAccessors(property).get(0);
		// check
		Method getter = accessor.getGetter();
		assertNotNull(getter);
		assertEquals("isEnabled", getter.getName());
	}

	/**
	 * Test for {@link SetterAccessor#setGetter(Method)}.
	 */
	@Test
	public void test_setGetter() throws Exception {
		Method setter = ReflectionUtils.getMethodBySignature(JPanel.class, "setEnabled(boolean)");
		Method getter = ReflectionUtils.getMethodBySignature(JPanel.class, "isEnabled()");
		assertNotNull(setter);
		assertNotNull(getter);
		SetterAccessor accessor = new SetterAccessor(setter, getter);
		// check current values
		assertSame(setter, accessor.getSetter());
		assertSame(getter, accessor.getGetter());
		// set new getter
		Method newGetter = ReflectionUtils.getMethodBySignature(JPanel.class, "isVisible()");
		assertNotNull(newGetter);
		accessor.setGetter(newGetter);
		assertSame(newGetter, accessor.getGetter());
	}

	/**
	 * Test for {@link IAccessibleExpressionAccessor}.
	 */
	@Test
	public void test_IAccessibleExpressionAccessor() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		GenericProperty property = (GenericProperty) panel.getPropertyByTitle("enabled");
		SetterAccessor accessor = (SetterAccessor) getGenericPropertyAccessors(property).get(0);
		// check that IAccessibleExpressionAccessor is available
		// note, that we don't check it, I just don't know how to do this more easy than perform full check
		// but we expect that it should be from IAccessibleExpressionAccessor.Utils.forMethod()
		IAccessibleExpressionAccessor accessibleAccessor =
				accessor.getAdapter(IAccessibleExpressionAccessor.class);
		assertNotNull(accessibleAccessor);
		assertNotNull(ReflectionUtils.getFieldByName(accessibleAccessor.getClass(), "val$method"));
	}

	/**
	 * Test for {@link PropertyTooltipProvider} implementation.
	 */
	@Test
	public void test_PropertyTooltipProvider() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		GenericProperty property = (GenericProperty) panel.getPropertyByTitle("enabled");
		SetterAccessor accessor = (SetterAccessor) getGenericPropertyAccessors(property).get(0);
		// ignore arbitrary adapter
		assertNull(accessor.getAdapter(null));
		// check that PropertyTooltipProvider is available
		// note, that we don't check it, I just don't know how to do this more easy than perform full check
		// but we expect that it should be from IAccessibleExpressionAccessor.Utils.forMethod()
		PropertyTooltipProvider tooltipProvider = accessor.getAdapter(PropertyTooltipProvider.class);
		assertNotNull(tooltipProvider);
		assertNotNull(ReflectionUtils.getFieldByName(tooltipProvider.getClass(), "val$method"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setExpression()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * "source" == null, so remove invocation.
	 */
	@Test
	public void test_setExpression_remove() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setEnabled(false);",
						"  }",
						"}");
		GenericProperty property = (GenericProperty) panel.getPropertyByTitle("enabled");
		SetterAccessor accessor = (SetterAccessor) getGenericPropertyAccessors(property).get(0);
		// check
		assertFalse(((BooleanLiteral) accessor.getExpression(panel)).booleanValue());
		accessor.setExpression(panel, null);
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
	}

	/**
	 * "source" is same as existing, so ignore.
	 */
	@Test
	public void test_setExpression_sameSource() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setEnabled(false);",
						"  }",
						"}");
		GenericProperty property = (GenericProperty) panel.getPropertyByTitle("enabled");
		ExpressionAccessor setterAccessor = getGenericPropertyAccessors(property).get(0);
		// check
		assertFalse(((BooleanLiteral) setterAccessor.getExpression(panel)).booleanValue());
		setterAccessor.setExpression(panel, "false");
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setEnabled(false);",
				"  }",
				"}");
	}

	/**
	 * "source" is different, do replace.
	 */
	@Test
	public void test_setExpression_newSource() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setEnabled(true);",
						"  }",
						"}");
		GenericProperty property = (GenericProperty) panel.getPropertyByTitle("enabled");
		ExpressionAccessor setterAccessor = getGenericPropertyAccessors(property).get(0);
		// check
		assertTrue(((BooleanLiteral) setterAccessor.getExpression(panel)).booleanValue());
		setterAccessor.setExpression(panel, "false");
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setEnabled(false);",
				"  }",
				"}");
	}

	/**
	 * No invocation yet, add new one.
	 */
	@Test
	public void test_setExpression_newInvocation() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		GenericProperty property = (GenericProperty) panel.getPropertyByTitle("enabled");
		ExpressionAccessor setterAccessor = getGenericPropertyAccessors(property).get(0);
		// check
		assertNull(setterAccessor.getExpression(panel));
		setterAccessor.setExpression(panel, "false");
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setEnabled(false);",
				"  }",
				"}");
	}

	/**
	 * Test that {@link SetterAccessor} resolved deferred {@link JavaInfo} references.
	 */
	@Test
	public void test_setExpression_replaceComponent() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public void setFoo(String o) {",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends MyPanel {",
						"  private final JButton button_1 = new JButton();",
						"  private final JButton button_2 = new JButton();",
						"  public Test() {",
						"    setFoo(button_1.getName());",
						"    add(button_1);",
						"    add(button_2);",
						"  }",
						"}");
		ComponentInfo button_1 = getJavaInfoByName("button_1");
		ComponentInfo button_2 = getJavaInfoByName("button_2");
		// prepare ExpressionAccessor
		ExpressionAccessor setterAccessor;
		{
			GenericProperty property = (GenericProperty) panel.getPropertyByTitle("foo");
			assertNotNull(property);
			setterAccessor = getGenericPropertyAccessors(property).get(0);
		}
		// "button_1" initially
		{
			MethodInvocation invocation = (MethodInvocation) setterAccessor.getExpression(panel);
			assertTrue(button_1.isRepresentedBy(invocation.getExpression()));
		}
		// use "button_2"
		{
			String source = TemplateUtils.format("{0}.getName()", button_2);
			setterAccessor.setExpression(panel, source);
		}
		assertEditor(
				"public class Test extends MyPanel {",
				"  private final JButton button_1 = new JButton();",
				"  private final JButton button_2 = new JButton();",
				"  public Test() {",
				"    setFoo(button_2.getName());",
				"    add(button_1);",
				"    add(button_2);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link IExposableExpressionAccessor}.
	 */
	@Test
	public void test_exposable() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		// "enabled"
		{
			GenericProperty property = (GenericProperty) panel.getPropertyByTitle("enabled");
			SetterAccessor setterAccessor = (SetterAccessor) getGenericPropertyAccessors(property).get(0);
			IExposableExpressionAccessor exposableAccessor =
					setterAccessor.getAdapter(IExposableExpressionAccessor.class);
			//
			assertSame(boolean.class, exposableAccessor.getValueClass(panel));
			assertEquals("isEnabled()", exposableAccessor.getGetterCode(panel));
			assertEquals("setEnabled(true)", exposableAccessor.getSetterCode(panel, "true"));
		}
		// "alignmentX"
		{
			GenericProperty property = (GenericProperty) panel.getPropertyByTitle("alignmentX");
			SetterAccessor setterAccessor = (SetterAccessor) getGenericPropertyAccessors(property).get(0);
			IExposableExpressionAccessor exposableAccessor =
					setterAccessor.getAdapter(IExposableExpressionAccessor.class);
			//
			assertSame(float.class, exposableAccessor.getValueClass(panel));
			assertEquals("getAlignmentX()", exposableAccessor.getGetterCode(panel));
			assertEquals("setAlignmentX(1.0)", exposableAccessor.getSetterCode(panel, "1.0"));
		}
	}

	/**
	 * Test for {@link SetterAccessor#getDefaultValue(JavaInfo)}.<br>
	 * Default value should be fetched after each creation. Note, that constructor parameter is not
	 * bound to "text" property, we just get "text" property default value.
	 */
	@Test
	public void test_defaultValue() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public MyButton(String s) {",
						"    setText(s);",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new MyButton('A'));",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		Property constructorTextProperty = PropertyUtils.getByPath(button, "Constructor/s");
		Property textProperty = button.getPropertyByTitle("text");
		// after first creation
		assertEquals("A", constructorTextProperty.getValue());
		assertEquals("A", textProperty.getValue());
		// set new value in constructor
		constructorTextProperty.setValue("B");
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    add(new MyButton('B'));",
				"  }",
				"}");
		assertEquals("B", constructorTextProperty.getValue());
		assertEquals("B", textProperty.getValue());
	}

	/**
	 * Test for {@link ExpressionAccessor#NO_DEFAULT_VALUE_TAG} support in
	 * {@link GenericPropertyDescription}.
	 */
	@Test
	public void test_noDefaultValue() throws Exception {
		setFileContentSrc(
				"test/MyComponent.java",
				getTestSource(
						"public class MyComponent extends JComponent {",
						"  public MyComponent() {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyComponent.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <property-tag name='enabled' tag='noDefaultValue' value='true'/>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new MyComponent());",
						"  }",
						"}");
		ComponentInfo myComponent = panel.getChildrenComponents().get(0);
		// "panel" has default value for "enabled"
		{
			GenericProperty property = (GenericProperty) panel.getPropertyByTitle("enabled");
			List<ExpressionAccessor> accessors = getGenericPropertyAccessors(property);
			assertEquals(1, accessors.size());
			SetterAccessor setterAccessor = (SetterAccessor) accessors.get(0);
			//
			assertEquals(Boolean.TRUE, setterAccessor.getDefaultValue(panel));
		}
		// "myComponent" has NO default value for "enabled"
		{
			GenericProperty property = (GenericProperty) myComponent.getPropertyByTitle("enabled");
			List<ExpressionAccessor> accessors = getGenericPropertyAccessors(property);
			assertEquals(1, accessors.size());
			SetterAccessor setterAccessor = (SetterAccessor) accessors.get(0);
			//
			assertSame(Property.UNKNOWN_VALUE, setterAccessor.getDefaultValue(myComponent));
		}
	}

	/**
	 * Test that parameter {@link ExpressionAccessor#NO_DEFAULT_VALUES_THIS_TAG} prevents attempts to
	 * fetch default values in {@link SetterAccessor}.
	 */
	@Test
	public void test_noDefaultValuesForThis() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JComponent {",
						"  public int getFoo() {",
						"    return 555;",
						"  }",
						"  public void setFoo(int foo) {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <parameters>",
						"    <parameter name='noDefaultValuesForThis'>true</parameter>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		// we disabled default values, so "foo" has no default value
		assertSame(Property.UNKNOWN_VALUE, panel.getPropertyByTitle("foo").getValue());
	}

	/**
	 * Test that parameter {@link ExpressionAccessor#NO_DEFAULT_VALUES_THIS_TAG} prevents attempts to
	 * fetch default values in {@link SetterAccessor}.
	 */
	@Test
	public void test_exceptionInDefaultValue() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public int getFoo() {",
						"    throw new Error();",
						"  }",
						"  public void setFoo(int foo) {",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		// we throw exception in getFoo(), so "foo" has no default value
		assertSame(Property.UNKNOWN_VALUE, panel.getPropertyByTitle("foo").getValue());
	}
}
