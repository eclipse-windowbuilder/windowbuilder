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
package org.eclipse.wb.tests.designer.core.eval.primities;

import org.eclipse.wb.tests.designer.core.eval.AbstractEngineTest;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author scheglov_ke
 */
public class FloatTest extends AbstractEngineTest {
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
	// float
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_float_value1() throws Exception {
		check_float("1F", 1F);
	}

	@Test
	public void test_float_value2() throws Exception {
		check_float("2f", 2f);
	}

	@Test
	public void test_float_positive_value() throws Exception {
		check_float("+3f", +3f);
	}

	@Test
	public void test_float_negative_value() throws Exception {
		check_float("-3f", -3f);
	}

	@Test
	public void test_float_plus() throws Exception {
		check_float("1F + 2F", 1F + 2F);
	}

	@Test
	public void test_float_plus3() throws Exception {
		check_float("1F + 2F + 3F", 1F + 2F + 3F);
	}

	@Test
	public void test_float_minus() throws Exception {
		check_float("5F - 1F", 5F - 1F);
	}

	@Test
	public void test_float_mul() throws Exception {
		check_float("2F * 3F", 2F * 3F);
	}

	@Test
	public void test_float_div() throws Exception {
		check_float("6F / 2F", 6F / 2F);
	}

	@Test
	public void test_float_div2() throws Exception {
		check_float("5F / 2F", 5F / 2F);
	}

	@Test
	public void test_float_mod() throws Exception {
		check_float("5F % 2F", 5F % 2F);
	}

	@Test
	public void test_float_mod2() throws Exception {
		check_float("-5F % 3F", -5F % 3F);
	}

	@Test
	public void test_float_mix_int() throws Exception {
		check_float("1F + 2", 1F + 2);
	}

	@Test
	public void test_float_mix_char() throws Exception {
		check_float("1F + '0'", 1F + '0');
	}

	@Test
	public void test_float_cast_to() throws Exception {
		check_float("((float)1) + 2", (float) 1 + 2);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void check_float(String expression, float expected) throws Exception {
		assertEquals(expected, evaluateExpression(expression, "float"));
	}
}
