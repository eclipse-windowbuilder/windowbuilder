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
package org.eclipse.wb.tests.designer.XML.model.generic;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidator;
import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidators;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.function.Predicate;

/**
 * Test for {@link ContainerObjectValidators}.
 *
 * @author scheglov_ke
 */
public class ContainerObjectValidatorsTest extends AbstractCoreTest {
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
			String typesString = "org.eclipse.swt.widgets.Button org.eclipse.swt.widgets.Text";
			String[] types = StringUtils.split(typesString);
			validator = ContainerObjectValidators.forList(types);
			assertEquals(typesString, validator.toString());
		}
		// parse
		parse(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button wbp:name='button'/>",
				"  <Text wbp:name='text'/>",
				"  <Label wbp:name='label'/>",
				"</Shell>");
		XmlObjectInfo buttonModel = getObjectByName("button");
		XmlObjectInfo textModel = getObjectByName("text");
		XmlObjectInfo labelModel = getObjectByName("label");
		// validate
		assertFalse(validator.validate(null, null));
		assertFalse(validator.validate(null, new Object()));
		assertTrue(validator.validate(null, buttonModel));
		assertTrue(validator.validate(null, textModel));
		assertFalse(validator.validate(null, labelModel));
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
		XmlObjectInfo shell =
				parse(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Button wbp:name='button'/>",
						"  <Text wbp:name='text'/>",
						"  <Label wbp:name='label'/>",
						"</Shell>");
		XmlObjectInfo button = getObjectByName("button");
		// not XMObject_Info
		assertContainerValidatorFalse("no matter", this);
		// literals
		assertContainerValidatorTrue("true", shell);
		assertContainerValidatorFalse("false", shell);
		// simple expression
		assertContainerValidatorTrue("1 == 1", shell);
		assertContainerValidatorFalse("1 == 2", shell);
		// type
		assertContainerValidatorTrue("isContainerType('org.eclipse.swt.widgets.Shell')", shell);
		assertContainerValidatorTrue("isContainerType('org.eclipse.swt.widgets.Control')", shell);
		assertContainerValidatorFalse("isContainerType('org.eclipse.swt.widgets.Button')", shell);
		assertContainerValidatorTrue("isContainerType('org.eclipse.swt.widgets.Button')", button);
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
		XmlObjectInfo shell =
				parse(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Button wbp:name='button'/>",
						"  <Text wbp:name='text'/>",
						"</Shell>");
		XmlObjectInfo buttonModel = getObjectByName("button");
		XmlObjectInfo textModel = getObjectByName("text");
		// "container" is not not model
		assertComponentValidatorFalse("true", null, buttonModel);
		// "component" is not not model
		assertComponentValidatorFalse("true", shell, null);
		// "false" literal
		assertComponentValidatorFalse("false", shell, buttonModel);
		// "true" literal
		assertComponentValidatorTrue("true", shell, buttonModel);
		// use "isComponentType" function
		assertComponentValidatorTrue(
				"isComponentType(org.eclipse.swt.widgets.Control)",
				shell,
				buttonModel);
		assertComponentValidatorTrue(
				"isComponentType(org.eclipse.swt.widgets.Control)",
				shell,
				textModel);
		assertComponentValidatorTrue(
				"isComponentType(org.eclipse.swt.widgets.Button)",
				shell,
				buttonModel);
		assertComponentValidatorFalse(
				"isComponentType(org.eclipse.swt.widgets.Button)",
				shell,
				textModel);
	}

	/**
	 * Test for {@link ContainerObjectValidators#forComponentExpression(String)}.
	 * <p>
	 * Use <code>isComponentType</code> for some type that is not in standard {@link ClassLoader}.
	 */
	@Test
	public void test_forComponentExpression_externalType() throws Exception {
		prepareMyComponent();
		// parse
		XmlObjectInfo shell =
				parse(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Shell>",
						"  <t:MyComponent wbp:name='myComponent'/>",
						"</Shell>");
		XmlObjectInfo myComponent = getObjectByName("myComponent");
		// use "isComponentType" function
		assertComponentValidatorTrue(
				"isComponentType(org.eclipse.swt.widgets.Control)",
				shell,
				myComponent);
		assertComponentValidatorTrue("isComponentType('test.MyComponent')", shell, myComponent);
	}

	/**
	 * Test for {@link ContainerObjectValidators#forComponentExpression(String)}.
	 * <p>
	 * Call method of "container" that is not in standard {@link ClassLoader}.
	 */
	@Test
	public void test_forComponentExpression_externalType2() throws Exception {
		XmlObjectInfo shell =
				parse(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Button wbp:name='button'/>",
						"</Shell>");
		XmlObjectInfo button = getObjectByName("button");
		// Call "container" method.
		// We do this 100 times to force optimizer activator.
		ContainerObjectValidator validator =
				ContainerObjectValidators.forComponentExpression("container.hasLayout()");
		for (int i = 0; i < 100; i++) {
			assertTrue(validator.validate(shell, button));
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
		XmlObjectInfo shell =
				parse(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Button wbp:name='button'/>",
						"  <Text wbp:name='text'/>",
						"</Shell>");
		XmlObjectInfo buttonModel = getObjectByName("button");
		XmlObjectInfo textModel = getObjectByName("text");
		// "container" is not not model
		assertReferenceValidatorFalse("true", null, buttonModel);
		// "reference" is not not model
		assertReferenceValidatorFalse("true", shell, null);
		// "false" literal
		assertReferenceValidatorFalse("false", shell, buttonModel);
		// "true" literal
		assertReferenceValidatorTrue("true", shell, buttonModel);
		// use "isReferenceType" function
		assertReferenceValidatorTrue(
				"isReferenceType(org.eclipse.swt.widgets.Control)",
				shell,
				buttonModel);
		assertReferenceValidatorTrue(
				"isReferenceType(org.eclipse.swt.widgets.Control)",
				shell,
				textModel);
		assertReferenceValidatorTrue(
				"isReferenceType(org.eclipse.swt.widgets.Button)",
				shell,
				buttonModel);
		assertReferenceValidatorFalse(
				"isReferenceType(org.eclipse.swt.widgets.Button)",
				shell,
				textModel);
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
