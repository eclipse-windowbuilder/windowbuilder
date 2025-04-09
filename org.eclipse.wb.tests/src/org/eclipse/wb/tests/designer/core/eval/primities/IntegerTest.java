/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.core.eval.primities;

import org.eclipse.wb.tests.designer.core.eval.AbstractEngineTest;

import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author scheglov_ke
 */
public class IntegerTest extends AbstractEngineTest {
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
	// int
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_int_value() throws Exception {
		check_int("1", 1);
	}

	@Test
	public void test_int_positive_value() throws Exception {
		check_int("+2", 2);
	}

	@Test
	public void test_int_value_hex() throws Exception {
		check_int("0x0A", 0x0A);
	}

	@Test
	public void test_int_value_oct() throws Exception {
		check_int("010", 010);
	}

	@Test
	public void test_int_negative_value() throws Exception {
		check_int("-1", -1);
	}

	@Test
	public void test_int_negate() throws Exception {
		check_int("~1", ~1);
	}

	@Test
	public void test_int_shift_left() throws Exception {
		check_int("1 << 3", 1 << 3);
	}

	@Test
	public void test_int_shift_right() throws Exception {
		check_int("16 >> 2", 16 >> 2);
	}

	@Test
	public void test_int_shift_right2() throws Exception {
		check_int("-16 >> 2", -16 >> 2);
	}

	@Test
	public void test_int_shift_right3() throws Exception {
		check_int("-16 >>> 2", -16 >>> 2);
	}

	@Test
	public void test_int_plus() throws Exception {
		check_int("1 + 2", 1 + 2);
	}

	@Test
	public void test_int_plus3() throws Exception {
		check_int("1 + 2 + 3", 1 + 2 + 3);
	}

	@Test
	public void test_int_plus4() throws Exception {
		check_int("1 + 2 + 3 + 4", 1 + 2 + 3 + 4);
	}

	@Test
	public void test_int_plus_char() throws Exception {
		check_int("1 + '0'", 1 + '0');
	}

	@Test
	public void test_int_plus_char2() throws Exception {
		check_int("'0' + '1'", '0' + '1');
	}

	@Test
	public void test_int_minus() throws Exception {
		check_int("5 - 1", 5 - 1);
	}

	@Test
	public void test_int_mul() throws Exception {
		check_int("2 * 3", 2 * 3);
	}

	@Test
	public void test_int_div() throws Exception {
		check_int("6 / 2", 6 / 2);
	}

	@Test
	public void test_int_div2() throws Exception {
		check_int("5 / 2", 5 / 2);
	}

	@Test
	public void test_int_mod() throws Exception {
		check_int("5 % 2", 5 % 2);
	}

	@Test
	public void test_int_mod2() throws Exception {
		check_int("-5 % 3", -5 % 3);
	}

	@Test
	public void test_int_or_exclusive() throws Exception {
		check_int("1 ^ 2", 1 ^ 2);
	}

	@Test
	public void test_int_or() throws Exception {
		check_int("1 | 2", 1 | 2);
	}

	@Test
	public void test_int_or3() throws Exception {
		check_int("1 | 2 | 4", 1 | 2 | 4);
	}

	@Test
	public void test_int_and() throws Exception {
		check_int("5 & 2", 5 & 2);
	}

	@Test
	public void test_int_and2() throws Exception {
		check_int("7 & 11", 7 & 11);
	}

	@Test
	public void test_int_and3() throws Exception {
		check_int("7 & 11 & 6", 7 & 11 & 6);
	}

	@Test
	public void test_short_cast() throws Exception {
		check_short("(short)1", (short) 1);
	}

	@Test
	public void test_int_as_sum_of_shorts() throws Exception {
		check_int("(short)2 + (short)3", 5);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PostfixExpression
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_PostfixExpression_increment() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				public class Test {
					public int root() {
						int value = 4;
						value++;
						return value;
					}
				}""");
		Object actual = evaluateSingleMethod(typeDeclaration, "root()");
		assertEquals(5, actual);
	}

	@Test
	public void test_PostfixExpression_decrement() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				public class Test {
					public int root() {
						int value = 4;
						value--;
						return value;
					}
				}""");
		Object actual = evaluateSingleMethod(typeDeclaration, "root()");
		assertEquals(3, actual);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void check_short(String expression, short expected) throws Exception {
		Object actual = evaluateExpression(expression, "short");
		assertEquals(Short.valueOf(expected), actual);
	}

	private void check_int(String expression, int expected) throws Exception {
		Object actual = evaluateExpression(expression, "int");
		assertEquals(expected, actual);
	}
}
