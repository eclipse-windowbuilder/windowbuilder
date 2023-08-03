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
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.AccessorUtils;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.IAccessibleExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.IExposableExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Test for {@link AccessorUtils}.
 *
 * @author scheglov_ke
 */
public class AccessorUtilsTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Test objects
	//
	////////////////////////////////////////////////////////////////////////////
	private Class<?> getAccessObjectClass() throws Exception {
		setFileContentSrc(
				"test/AccessObject.java",
				getSourceDQ(
						"package test;",
						"public class AccessObject {",
						"  public int publicField;",
						"  protected int protectedField;",
						"  public void publicMethod() {",
						"  }",
						"  protected void protectedMethod() {",
						"  }",
						"}"));
		waitForAutoBuild();
		return m_lastLoader.loadClass("test.AccessObject");
	}

	private Class<?> getTooltipObjectClass() throws Exception {
		setFileContentSrc(
				"test/TooltipObject.java",
				getSourceDQ(
						"package test;",
						"public class TooltipObject {",
						"  /**",
						"  * Some javadoc for field.",
						"  */",
						"  public int javadocField;",
						"  public int emptyField;",
						"  /**",
						"  * Some javadoc for method.",
						"  */",
						"  public void javadocMethod() {",
						"  }",
						"  public void emptyMethod() {",
						"  }",
						"}"));
		waitForAutoBuild();
		return m_lastLoader.loadClass("test.TooltipObject");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IAccessibleExpressionAccessor_forMethod()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link AccessorUtils#IAccessibleExpressionAccessor_forMethod(Method)}.
	 */
	@Test
	public void test_IAccessibleExpressionAccessor_forMethod_public() throws Exception {
		parseContainer(
				"// filler filler filler",
				"public final class Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
		// check IAccessibleExpressionAccessor
		Method method = ReflectionUtils.getMethod(getAccessObjectClass(), "publicMethod");
		IAccessibleExpressionAccessor accessor =
				AccessorUtils.IAccessibleExpressionAccessor_forMethod(method);
		assertTrue(accessor.isAccessible(null));
	}

	/**
	 * Test for {@link AccessorUtils#IAccessibleExpressionAccessor_forMethod(Method)}.<br>
	 * Note that we use same {@link ExpressionAccessor} for correct and <em>wrong</em>
	 * {@link JavaInfo} class, but we know that only type of {@link CreationSupport} is checked, so
	 * this is OK.
	 */
	@Test
	public void test_IAccessibleExpressionAccessor_forMethod_protected() throws Exception {
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// check IAccessibleExpressionAccessor
		Method method = ReflectionUtils.getMethod(getAccessObjectClass(), "protectedMethod");
		IAccessibleExpressionAccessor accessor =
				AccessorUtils.IAccessibleExpressionAccessor_forMethod(method);
		assertTrue(accessor.isAccessible(panel));
		assertFalse(accessor.isAccessible(button));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IAccessibleExpressionAccessor_forField()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link AccessorUtils#IAccessibleExpressionAccessor_forField(Field)}.
	 */
	@Test
	public void test_IAccessibleExpressionAccessor_forField_public() throws Exception {
		parseContainer(
				"// filler filler filler",
				"public final class Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
		// check IAccessibleExpressionAccessor
		Field field = ReflectionUtils.getFieldByName(getAccessObjectClass(), "publicField");
		IAccessibleExpressionAccessor accessor =
				AccessorUtils.IAccessibleExpressionAccessor_forField(field);
		assertTrue(accessor.isAccessible(null));
	}

	/**
	 * Test for {@link AccessorUtils#IAccessibleExpressionAccessor_forField(Field)}.<br>
	 * Note that we use same {@link ExpressionAccessor} for correct and <em>wrong</em>
	 * {@link JavaInfo} class, but we know that only type of {@link CreationSupport} is checked, so
	 * this is OK.
	 */
	@Test
	public void test_IAccessibleExpressionAccessor_forField_protected() throws Exception {
		// prepare JavaInfo
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// check IAccessibleExpressionAccessor
		Field field = ReflectionUtils.getFieldByName(getAccessObjectClass(), "protectedField");
		IAccessibleExpressionAccessor accessor =
				AccessorUtils.IAccessibleExpressionAccessor_forField(field);
		assertTrue(accessor.isAccessible(panel));
		assertFalse(accessor.isAccessible(button));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IExposableExpressionAccessor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link AccessorUtils#getExposableExpressionAccessor(Property)}.
	 */
	@Test
	public void test_getExposableExpressionAccessor() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		{
			Property property = panel.getPropertyByTitle("Class");
			IExposableExpressionAccessor accessor =
					AccessorUtils.getExposableExpressionAccessor(property);
			assertNull(accessor);
		}
		{
			Property property = panel.getPropertyByTitle("enabled");
			IExposableExpressionAccessor accessor =
					AccessorUtils.getExposableExpressionAccessor(property);
			assertNotNull(accessor);
			assertEquals("isEnabled()", accessor.getGetterCode(panel));
			assertEquals("setEnabled(false)", accessor.getSetterCode(panel, "false"));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PropertyTooltipProvider_forMethod()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link AccessorUtils#PropertyTooltipProvider_forMethod(Method)}.
	 */
	@Test
	public void test_PropertyTooltipProvider_forMethod_noJavadoc() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public final class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		Property property = panel.getPropertyByTitle("enabled");
		// check PropertyTooltipProvider
		Method method = ReflectionUtils.getMethod(getTooltipObjectClass(), "emptyMethod");
		PropertyTooltipProvider tooltipProvider =
				AccessorUtils.PropertyTooltipProvider_forMethod(method);
		assertEquals("enabled", getTooltipText(tooltipProvider, property));
	}

	/**
	 * Test for {@link AccessorUtils#PropertyTooltipProvider_forMethod(Method)}.
	 */
	@Test
	public void test_PropertyTooltipProvider_forMethod_withJavadoc() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public final class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		Property property = panel.getPropertyByTitle("enabled");
		// check PropertyTooltipProvider
		Method method = ReflectionUtils.getMethod(getTooltipObjectClass(), "javadocMethod");
		PropertyTooltipProvider tooltipProvider =
				AccessorUtils.PropertyTooltipProvider_forMethod(method);
		assertEquals("Some javadoc for method.", getTooltipText(tooltipProvider, property));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PropertyTooltipProvider_forField()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link AccessorUtils#PropertyTooltipProvider_forField(Field)}.
	 */
	@Test
	public void test_PropertyTooltipProvider_forField_noJavadoc() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public final class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		Property property = panel.getPropertyByTitle("enabled");
		// check PropertyTooltipProvider
		Field method = ReflectionUtils.getFieldByName(getTooltipObjectClass(), "emptyField");
		PropertyTooltipProvider tooltipProvider =
				AccessorUtils.PropertyTooltipProvider_forField(method);
		assertEquals("enabled", getTooltipText(tooltipProvider, property));
	}

	/**
	 * Test for {@link AccessorUtils#PropertyTooltipProvider_forField(Field)}.
	 */
	@Test
	public void test_PropertyTooltipProvider_forField_withJavadoc() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public final class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		Property property = panel.getPropertyByTitle("enabled");
		// check PropertyTooltipProvider
		Field method = ReflectionUtils.getFieldByName(getTooltipObjectClass(), "javadocField");
		PropertyTooltipProvider tooltipProvider =
				AccessorUtils.PropertyTooltipProvider_forField(method);
		assertEquals("Some javadoc for field.", getTooltipText(tooltipProvider, property));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the "text" of {@link PropertyTooltipTextProvider}.
	 */
	private String getTooltipText(PropertyTooltipProvider tooltipProvider, Property someProperty)
			throws Exception {
		return (String) ReflectionUtils.invokeMethod2(
				tooltipProvider,
				"getText",
				Property.class,
				someProperty);
	}
}
