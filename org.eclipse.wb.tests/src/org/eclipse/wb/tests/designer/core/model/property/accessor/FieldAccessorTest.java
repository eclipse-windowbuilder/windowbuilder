/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.FieldAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.IAccessibleExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.IExposableExpressionAccessor;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.Assignment;

import org.junit.Test;

import java.util.List;

/**
 * Tests for {@link FieldAccessor}.
 *
 * @author scheglov_ke
 */
public class FieldAccessorTest extends SwingModelTest {
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
	/**
	 * Test for {@link FieldAccessor}.
	 */
	@Test
	public void test_fieldAccessor() throws Exception {
		defineMyButton();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyButton button = new MyButton();",
						"    button.hgap = 1;",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GenericProperty property = (GenericProperty) button.getPropertyByTitle("hgap");
		// check getValue()
		assertEquals(1, property.getValue());
		// check setValue()
		{
			property.setValue(3);
			assertEquals(3, property.getValue());
			assertNotNull(property.getExpression());
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    MyButton button = new MyButton();",
					"    button.hgap = 3;",
					"    add(button);",
					"  }",
					"}");
		}
		// remove property
		{
			property.setValue(Property.UNKNOWN_VALUE);
			assertEquals(0, property.getValue());
			assertNull(property.getExpression());
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    MyButton button = new MyButton();",
					"    add(button);",
					"  }",
					"}");
		}
		// add property
		{
			property.setValue(2);
			assertEquals(2, property.getValue());
			assertNotNull(property.getExpression());
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    MyButton button = new MyButton();",
					"    button.hgap = 2;",
					"    add(button);",
					"  }",
					"}");
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PropertyTooltipProvider
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for many {@link FieldAccessor} features.
	 */
	@Test
	public void test_0() throws Exception {
		setFileContentSrc(
				"test/MyComponent.java",
				getTestSource(
						"public class MyComponent extends JComponent {",
						"  /**",
						"  * my documentation",
						"  */",
						"  public int field_1 = 1;",
						"  public double field_2 = 2.0;",
						"  public MyComponent() {",
						"  }",
						"}"));
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
		// field_1
		{
			GenericProperty property = (GenericProperty) myComponent.getPropertyByTitle("field_1");
			// prepare FieldAccessor
			FieldAccessor fieldAccessor;
			{
				List<ExpressionAccessor> accessors = getGenericPropertyAccessors(property);
				assertEquals(1, accessors.size());
				fieldAccessor = (FieldAccessor) accessors.get(0);
			}
			// do checks
			assertEquals(1, fieldAccessor.getDefaultValue(myComponent));
			assertEquals("my documentation", getPropertyTooltipText(fieldAccessor, property));
			assertNull(fieldAccessor.getAdapter(null));
			// check IExposableExpressionAccessor
			{
				IExposableExpressionAccessor exposableAccessor =
						fieldAccessor.getAdapter(IExposableExpressionAccessor.class);
				assertSame(int.class, exposableAccessor.getValueClass(myComponent));
				assertEquals("field_1", exposableAccessor.getGetterCode(myComponent));
				assertEquals("field_1 = 123", exposableAccessor.getSetterCode(myComponent, "123"));
			}
		}
		// field_2
		{
			GenericProperty property = (GenericProperty) myComponent.getPropertyByTitle("field_2");
			// prepare FieldAccessor
			FieldAccessor fieldAccessor;
			{
				List<ExpressionAccessor> accessors = getGenericPropertyAccessors(property);
				assertEquals(1, accessors.size());
				fieldAccessor = (FieldAccessor) accessors.get(0);
			}
			// do checks
			assertEquals(Double.valueOf(2.0), fieldAccessor.getDefaultValue(myComponent));
			assertEquals("field_2", getPropertyTooltipText(fieldAccessor, property));
			assertNull(fieldAccessor.getAdapter(null));
			// check IExposableExpressionAccessor
			{
				IExposableExpressionAccessor exposableAccessor =
						fieldAccessor.getAdapter(IExposableExpressionAccessor.class);
				assertSame(double.class, exposableAccessor.getValueClass(myComponent));
				assertEquals("field_2", exposableAccessor.getGetterCode(myComponent));
				assertEquals("field_2 = 12.3", exposableAccessor.getSetterCode(myComponent, "12.3"));
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Default value
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link FieldAccessor#getDefaultValue(JavaInfo)}.<br>
	 * Default value should be fetched after each creation. Note, that constructor parameter is not
	 * bound to "text" property, we just get "text" property default value.
	 */
	@Test
	public void test_defaultValue() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public String foo;",
						"  public MyButton(String s) {",
						"    foo = s;",
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
		Property fooProperty = button.getPropertyByTitle("foo");
		// after first creation
		assertEquals("A", constructorTextProperty.getValue());
		assertEquals("A", fooProperty.getValue());
		// set new value in constructor
		constructorTextProperty.setValue("B");
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    add(new MyButton('B'));",
				"  }",
				"}");
		assertEquals("B", constructorTextProperty.getValue());
		assertEquals("B", fooProperty.getValue());
	}

	/**
	 * When component was replaced with placeholder, this should not cause subsequent exception during
	 * getting default value. We should just ignore such exceptions.
	 */
	@Test
	public void test_defaultValue_whenReplacedWithPlaceholder() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public String foo;",
						"  public MyButton() {",
						"    throw new IllegalStateException();",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyButton button = new MyButton();",
						"    add(button);",
						"  }",
						"}");
		panel.refresh();
		//
		ComponentInfo button = getJavaInfoByName("button");
		Property fooProperty = button.getPropertyByTitle("foo");
		assertSame(Property.UNKNOWN_VALUE, fooProperty.getValue());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Sequence
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for separate {@link Assignment} to field, not sequence.
	 */
	@Test
	public void test_separateAssignment() throws Exception {
		defineMyButton();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyButton button = new MyButton();",
						"    add(button);",
						"    button.hgap = 5;",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		GenericProperty property = (GenericProperty) button.getPropertyByTitle("hgap");
		// check value of field
		{
			Object buttonObject = button.getObject();
			assertEquals(5, ReflectionUtils.getFieldInt(buttonObject, "hgap"));
		}
		// remember old Assignment
		Assignment oldAssignment = (Assignment) property.getExpression().getParent();
		assertEquals("button.hgap = 5", m_lastEditor.getSource(oldAssignment));
		// set new value
		property.setExpression("6", 5);
		// just assignment value should be changed, still same Assignment should be used
		Assignment newAssignment = (Assignment) property.getExpression().getParent();
		assertEquals("button.hgap = 6", m_lastEditor.getSource(newAssignment));
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    MyButton button = new MyButton();",
				"    add(button);",
				"    button.hgap = 6;",
				"  }",
				"}");
		assertSame(oldAssignment, newAssignment);
	}

	/**
	 * Test for {@link FieldAccessor} with sequence of assignments.
	 */
	@Test
	public void test_sequence_parse() throws Exception {
		defineMyButton();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyButton button = new MyButton();",
						"    add(button);",
						"    button.hgap = button.vgap = 5;",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		panel.refresh();
		// check that hgap/vgap have value "5"
		{
			Object buttonObject = button.getObject();
			assertEquals(5, ReflectionUtils.getFieldInt(buttonObject, "hgap"));
			assertEquals(5, ReflectionUtils.getFieldInt(buttonObject, "vgap"));
		}
	}

	/**
	 * Set new value for last part of assignments sequence.
	 */
	@Test
	public void test_sequence_modifyLast() throws Exception {
		defineMyButton();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyButton button = new MyButton();",
						"    add(button);",
						"    button.hgap = button.vgap = 5;",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		//
		Property vgapProperty = button.getPropertyByTitle("vgap");
		assertEquals(5, vgapProperty.getValue());
		//
		vgapProperty.setValue(10);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    MyButton button = new MyButton();",
				"    add(button);",
				"    button.hgap = 5;",
				"    button.vgap = 10;",
				"  }",
				"}");
		assertEquals(10, vgapProperty.getValue());
	}

	/**
	 * Set new value for first part of assignments sequence.
	 */
	@Test
	public void test_sequence_modifyFirst() throws Exception {
		defineMyButton();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyButton button = new MyButton();",
						"    add(button);",
						"    button.hgap = button.vgap = 5;",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		//
		Property hgapProperty = button.getPropertyByTitle("hgap");
		assertEquals(5, hgapProperty.getValue());
		//
		hgapProperty.setValue(10);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    MyButton button = new MyButton();",
				"    add(button);",
				"    button.hgap = 10;",
				"    button.vgap = 5;",
				"  }",
				"}");
		assertEquals(10, hgapProperty.getValue());
	}

	/**
	 * Test for {@link FieldAccessor} with sequence of assignments to "this" fields.
	 */
	/*public void test_fieldAccessor_sequenceThis() throws Exception {
  	setFileContentSrc("test/MyPanel.java", getTestSource(
  			"public class MyPanel extends JPanel {",
  			"  public int hgap;",
  			"  public int vgap;",
  			"}"));
  	waitForAutoBuild();
  	//
  	ContainerInfo panel =
  			parseTestSource(new String[]{
  					"public class Test extends MyPanel {",
  					"  public Test() {",
  					"    hgap = vgap = 5;",
  					"  }",
  					"}"});
  	panel.refresh();
  	// check that hgap/vgap have value "5"
  	assertEquals(5, ReflectionUtils.getFieldInt(panel, "hgap"));
  	assertEquals(5, ReflectionUtils.getFieldInt(panel, "vgap"));
  }*/
	////////////////////////////////////////////////////////////////////////////
	//
	// IAccessibleExpressionAccessor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link IAccessibleExpressionAccessor}.
	 */
	@Test
	public void test_IAccessibleExpressionAccessor() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public class MyPanel extends JPanel {",
						"  public int foo;",
						"}"));
		waitForAutoBuild();
		// parse
		// parse
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		GenericProperty property = (GenericProperty) panel.getPropertyByTitle("foo");
		FieldAccessor accessor = (FieldAccessor) getGenericPropertyAccessors(property).get(0);
		// check that IAccessibleExpressionAccessor is available
		// note, that we don't check it, I just don't know how to do this more easy than perform full check
		// but we expect that it should be from IAccessibleExpressionAccessor.Utils.forField()
		IAccessibleExpressionAccessor accessibleAccessor =
				accessor.getAdapter(IAccessibleExpressionAccessor.class);
		assertNotNull(accessibleAccessor);
		assertNotNull(ReflectionUtils.getFieldByName(accessibleAccessor.getClass(), "val$field"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Defines <code>MyButton</code> class.
	 */
	private void defineMyButton() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public int hgap;",
						"  public int vgap;",
						"}"));
		waitForAutoBuild();
	}
}
