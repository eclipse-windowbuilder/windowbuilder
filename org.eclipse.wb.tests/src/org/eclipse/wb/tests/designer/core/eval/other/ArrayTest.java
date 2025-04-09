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
package org.eclipse.wb.tests.designer.core.eval.other;

import org.eclipse.wb.tests.designer.core.eval.AbstractEngineTest;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author scheglov_ke
 */
public class ArrayTest extends AbstractEngineTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		if (m_testProject == null) {
			do_projectCreate();
		}
	}

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
	// Array
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_array_compare() throws Exception {
		int[] a_1 = new int[]{1, 2, 3};
		int[] a_2 = new int[]{1, 2, 3};
		assertTrue(ArrayUtils.isEquals(a_1, a_2));
	}

	@Test
	public void test_array_int_1() throws Exception {
		check_array(new int[]{1, 2, 3}, "new int[]{1, 2, 3}", "int[]");
	}

	@Test
	public void test_array_int_2_1() throws Exception {
		check_array(
				new int[][]{new int[]{11, 12}, new int[]{21, 22}, new int[]{31, 32}},
				"new int[][]{new int[]{11, 12}, new int[]{21, 22}, new int[]{31, 32}}",
				"int[][]");
	}

	@Test
	public void test_array_int_2_2() throws Exception {
		check_array(
				new int[][]{{11, 12}, {21, 22}, {31, 32}},
				"new int[][]{{11, 12}, {21, 22}, {31, 32}}",
				"int[][]");
	}

	@Test
	public void test_array_boolean() throws Exception {
		check_array(new boolean[]{true, false, true}, "new boolean[]{true, false, true}", "boolean[]");
	}

	@Test
	public void test_array_byte() throws Exception {
		check_array(new byte[]{1, 2, 3}, "new byte[]{1, 2, 3}", "byte[]");
	}

	@Test
	public void test_array_short() throws Exception {
		check_array(new short[]{1, 2, 3}, "new short[]{1, 2, 3}", "short[]");
	}

	@Test
	public void test_array_char() throws Exception {
		check_array(new char[]{'a', 'b', 'c'}, "new char[]{\'a\', \'b\', \'c\'}", "char[]");
	}

	@Test
	public void test_array_char2() throws Exception {
		check_array(new char[]{0x30, 0x31, 0x32}, "new char[]{0x30, 0x31, 0x32}", "char[]");
	}

	@Test
	public void test_array_long() throws Exception {
		check_array(new long[]{1, 2, 3}, "new long[]{1, 2, 3}", "long[]");
	}

	@Test
	public void test_array_float() throws Exception {
		check_array(new float[]{1, 2, 3}, "new float[]{1, 2, 3}", "float[]");
	}

	@Test
	public void test_array_double() throws Exception {
		check_array(new double[]{1, 2, 3}, "new double[]{1, 2, 3}", "double[]");
	}

	@Test
	public void test_array_String_1() throws Exception {
		check_array(
				new String[]{"1", "2", "3"},
				"new String[]{\"1\", \"2\", \"3\"}",
				"java.lang.String[]");
	}

	@Test
	public void test_array_String_2() throws Exception {
		check_array(
				new String[][]{{"11", "12"}, {"21", "22"}, {"31", "32"}},
				"new String[][]{{\"11\", \"12\"}, {\"21\", \"22\"}, {\"31\", \"32\"}}",
				"java.lang.String[][]");
	}

	@Test
	public void test_array_String_empty_1() throws Exception {
		check_array(new String[2], "new String[2]", "java.lang.String[]");
	}

	@Test
	public void test_array_String_empty_2() throws Exception {
		check_array(new String[2][3], "new String[2][3]", "java.lang.String[][]");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Array element
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_arrayElement_1() throws Exception {
		assertEquals(11, evaluateExpression("(new int[]{0, 11, 22})[1]", "int"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void check_array(Object expected, String expression, String returnType) throws Exception {
		assertTrue(ArrayUtils.isEquals(expected, evaluateExpression(expression, returnType)));
	}
}
