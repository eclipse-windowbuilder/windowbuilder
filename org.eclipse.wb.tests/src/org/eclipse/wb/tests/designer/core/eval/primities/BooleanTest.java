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
package org.eclipse.wb.tests.designer.core.eval.primities;

import org.eclipse.wb.internal.core.eval.evaluators.BooleanEvaluator;
import org.eclipse.wb.tests.designer.core.eval.AbstractEngineTest;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link BooleanEvaluator}.
 *
 * @author scheglov_ke
 */
public class BooleanTest extends AbstractEngineTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Project creation
	//
	////////////////////////////////////////////////////////////////////////////
	@BeforeClass
	public static void setUpClass() throws Exception {
		do_projectCreate();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// boolean
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_true() throws Exception {
		check_boolean("true", true);
	}

	@Test
	public void test_false() throws Exception {
		check_boolean("false", false);
	}

	@Test
	public void test_equals_true() throws Exception {
		check_boolean("1000 == 1000", true);
	}

	@Test
	public void test_equals_false() throws Exception {
		check_boolean("1000 == 2000", false);
	}

	@Test
	public void test_notEquals_true() throws Exception {
		check_boolean("1000 != 2000", true);
	}

	@Test
	public void test_notEquals_false() throws Exception {
		check_boolean("1000 != 1000", false);
	}

	@Test
	public void test_not() throws Exception {
		check_boolean("!false", true);
	}

	@Test
	public void test_not2() throws Exception {
		check_boolean("!true", false);
	}

	@Test
	public void test_or() throws Exception {
		check_boolean("true || false", true);
	}

	@Test
	public void test_or3() throws Exception {
		check_boolean("false || false || true", true);
	}

	@Test
	public void test_and() throws Exception {
		check_boolean("true && true", true);
	}

	@Test
	public void test_and3() throws Exception {
		check_boolean("false && false && true", false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void check_boolean(String expression, boolean expected) throws Exception {
		assertEquals(Boolean.valueOf(expected), evaluateExpression(expression, "boolean"));
	}
}
