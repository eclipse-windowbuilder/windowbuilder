/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author scheglov_ke
 */
public class CharTest extends AbstractEngineTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Project creation
	//
	////////////////////////////////////////////////////////////////////////////
	@BeforeAll
	public static void setUpClass() throws Exception {
		do_projectCreate();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// int
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_char_value() throws Exception {
		check_char("'a'", 'a');
	}

	@Test
	public void test_char_cast_int() throws Exception {
		check_char("(char)0x30", (char) 0x30);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void check_char(String expression, char expected) throws Exception {
		Object actual = evaluateExpression(expression, "char");
		assertEquals(Character.valueOf(expected), actual);
	}
}
