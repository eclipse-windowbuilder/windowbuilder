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
package org.eclipse.wb.tests.designer.core.eval.other;

import org.eclipse.wb.tests.designer.core.eval.AbstractEngineTest;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author scheglov_ke
 */
public class StringTest extends AbstractEngineTest {
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
	// String
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_string_literal() throws Exception {
		check_String("string", "\"string\"");
	}

	@Test
	public void test_string_literal_quoted() throws Exception {
		check_String("a\"b\"", "\"a\\\"b\\\"\"");
	}

	@Test
	public void test_string_plus() throws Exception {
		check_String("a" + "b", "\"a\" + \"b\"");
	}

	@Test
	public void test_string_plus3() throws Exception {
		check_String("a" + "b" + "c", "\"a\" + \"b\" + \"c\"");
	}

	@Test
	public void test_string_plus4() throws Exception {
		check_String("a" + "b" + "c" + "d", "\"a\" + \"b\" + \"c\" + \"d\"");
	}

	@Test
	public void test_String_plus_int() throws Exception {
		check_String("a2", "\"a\" + 2");
	}

	@Test
	public void test_String_plus_null() throws Exception {
		check_String("anull", "\"a\" + null");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void check_String(String expected, String expression) throws Exception {
		assertEquals(expected, evaluateExpression(expression, "java.lang.String"));
	}
}
