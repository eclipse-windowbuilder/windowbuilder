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
package org.eclipse.wb.tests.designer.core.model.generic;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidator;
import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidators;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.function.Predicate;

/**
 * Test for {@link ContainerObjectValidators}.
 *
 * @author scheglov_ke
 */
public class ContainerObjectValidatorsTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ContainerObjectValidators#alwaysTrue()}.
	 */
	@Test
	public void test_alwaysTrue() throws Exception {
		ContainerObjectValidator validator = ContainerObjectValidators.alwaysTrue();
		assertEquals("alwaysTrue", validator.toString());
		assertTrue(validator.validate(null, null));
	}

	/**
	 * Test for {@link ContainerObjectValidators#forList(String[])}.
	 */
	@Test
	public void test_forList() throws Exception {
		ContainerObjectValidator validator;
		{
			String typesString = "javax.swing.JButton javax.swing.JTextField";
			String[] types = StringUtils.split(typesString);
			validator = ContainerObjectValidators.forList(types);
			assertEquals(typesString, validator.toString());
		}
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"    add(new JTextField());",
						"    add(new JLabel());",
						"  }",
						"}");
		assertFalse(validator.validate(null, null));
		assertFalse(validator.validate(null, new Object()));
		assertTrue(validator.validate(null, panel.getChildrenComponents().get(0)));
		assertTrue(validator.validate(null, panel.getChildrenComponents().get(1)));
		assertFalse(validator.validate(null, panel.getChildrenComponents().get(2)));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// forContainerExpression
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ContainerObjectValidators#forContainerExpression(String)}.
	 */
	@Test
	public void test_forContainerExpression() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// not JavaInfo
		assertContainerValidatorFalse("no matter", this);
		// literals
		assertContainerValidatorTrue("true", panel);
		assertContainerValidatorFalse("false", panel);
		// simple expression
		assertContainerValidatorTrue("1 == 1", panel);
		assertContainerValidatorFalse("1 == 2", panel);
		// type
		assertContainerValidatorTrue("isContainerType('javax.swing.JPanel')", panel);
		assertContainerValidatorTrue("isContainerType('java.awt.Component')", panel);
		assertContainerValidatorFalse("isContainerType('javax.swing.JButton')", panel);
		assertContainerValidatorTrue("isContainerType('javax.swing.JButton')", button);
		// ThisCreationSupport
		assertContainerValidatorTrue("isContainerThis()", panel);
		assertContainerValidatorFalse("isContainerThis()", button);
	}

	private static void assertContainerValidatorFalse(String expression, Object container) {
		assertContainerValidator(false, expression, container);
	}

	private static void assertContainerValidatorTrue(String expression, Object container) {
		assertContainerValidator(true, expression, container);
	}

	private static void assertContainerValidator(boolean expected, String expression, Object container) {
		Predicate<Object> validator = ContainerObjectValidators.forContainerExpression(expression);
		assertEquals(expression, validator.toString());
		assertEquals(expected, validator.test(container));
		if (container instanceof JavaInfo) {
			assertEquals(expected, ContainerObjectValidators.validateContainer(container, expression));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// forComponentExpression
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ContainerObjectValidators#forComponentExpression(String)}.
	 */
	@Test
	public void test_forComponentExpression() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"    add(new JTextField());",
						"    add(new JLabel());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		ComponentInfo textField = panel.getChildrenComponents().get(1);
		// "container" is not not JavaInfo
		assertComponentValidatorFalse("true", null, button);
		// "component" is not not JavaInfo
		assertComponentValidatorFalse("true", panel, null);
		// "false" literal
		assertComponentValidatorFalse("false", panel, button);
		// "true" literal
		assertComponentValidatorTrue("true", panel, button);
		// use "isComponentType" function
		assertComponentValidatorTrue("isComponentType(java.awt.Component)", panel, button);
		assertComponentValidatorTrue("isComponentType(java.awt.Component)", panel, textField);
		assertComponentValidatorTrue("isComponentType(javax.swing.AbstractButton)", panel, button);
		assertComponentValidatorFalse("isComponentType(javax.swing.AbstractButton)", panel, textField);
	}

	/**
	 * Test for {@link ContainerObjectValidators#forComponentExpression(String)}.
	 * <p>
	 * Use <code>isComponentType</code> for some type that is not in standard {@link ClassLoader}.
	 */
	@Test
	public void test_forComponentExpression_externalType() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public class MyButton extends JButton {",
						"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new MyButton());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// use "isComponentType" function
		assertComponentValidatorTrue("isComponentType(javax.swing.JButton)", panel, button);
		assertComponentValidatorTrue("isComponentType('test.MyButton')", panel, button);
	}

	/**
	 * Test for {@link ContainerObjectValidators#forComponentExpression(String)}.
	 * <p>
	 * Call method of "container" that is not in standard {@link ClassLoader}.
	 */
	@Test
	public void test_forComponentExpression_externalType2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// Call "container" method.
		// We do this 100 times to force optimizer activator.
		ContainerObjectValidator validator =
				ContainerObjectValidators.forComponentExpression("container.hasLayout()");
		for (int i = 0; i < 100; i++) {
			assertTrue(validator.validate(panel, button));
		}
	}

	private static void assertComponentValidatorFalse(String expression,
			Object container,
			Object component) {
		assertComponentValidator(false, expression, container, component);
	}

	private static void assertComponentValidatorTrue(String expression,
			Object container,
			Object component) {
		assertComponentValidator(true, expression, container, component);
	}

	private static void assertComponentValidator(boolean expected,
			String expression,
			Object container,
			Object component) {
		ContainerObjectValidator validator =
				ContainerObjectValidators.forComponentExpression(expression);
		assertEquals(expression, validator.toString());
		assertEquals(expected, validator.validate(container, component));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// forReferenceExpression
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ContainerObjectValidators#forReferenceExpression(String)}.
	 */
	@Test
	public void test_forReferenceExpression() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"    add(new JTextField());",
						"    add(new JLabel());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		ComponentInfo textField = panel.getChildrenComponents().get(1);
		// "container" is not not JavaInfo
		assertReferenceValidatorFalse("true", null, button);
		// "reference" is not not JavaInfo
		assertReferenceValidatorFalse("true", panel, null);
		// "false" literal
		assertReferenceValidatorFalse("false", panel, button);
		// "true" literal
		assertReferenceValidatorTrue("true", panel, button);
		// use "isReferenceType" function
		assertReferenceValidatorTrue("isReferenceType(java.awt.Component)", panel, button);
		assertReferenceValidatorTrue("isReferenceType(java.awt.Component)", panel, textField);
		assertReferenceValidatorTrue("isReferenceType(javax.swing.AbstractButton)", panel, button);
		assertReferenceValidatorFalse("isReferenceType(javax.swing.AbstractButton)", panel, textField);
	}

	private static void assertReferenceValidatorFalse(String expression,
			Object container,
			Object reference) {
		assertReferenceValidator(false, expression, container, reference);
	}

	private static void assertReferenceValidatorTrue(String expression,
			Object container,
			Object reference) {
		assertReferenceValidator(true, expression, container, reference);
	}

	private static void assertReferenceValidator(boolean expected,
			String expression,
			Object container,
			Object reference) {
		ContainerObjectValidator validator =
				ContainerObjectValidators.forReferenceExpression(expression);
		assertEquals(expression, validator.toString());
		assertEquals(expected, validator.validate(container, reference));
	}
}
