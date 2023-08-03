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
public class CastTest extends AbstractEngineTest {
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
	// cast's
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_cast_byte() throws Exception {
		assertEquals(new Byte((byte) 1), evaluateExpression("(byte)1", "byte"));
	}

	@Test
	public void test_cast_short() throws Exception {
		assertEquals(new Short((short) 1), evaluateExpression("(short)1", "short"));
	}

	@Test
	public void test_cast_int() throws Exception {
		assertEquals(new Integer(1), evaluateExpression("(int)1", "int"));
	}

	@Test
	public void test_cast_long() throws Exception {
		assertEquals(new Long(1L), evaluateExpression("(long)1", "long"));
	}

	@Test
	public void test_cast_float() throws Exception {
		assertEquals(new Float(1.2f), evaluateExpression("(float)1.2f", "float"));
	}

	@Test
	public void test_cast_float2() throws Exception {
		assertEquals(new Float(1.2f), evaluateExpression("(float)1.2", "float"));
	}

	@Test
	public void test_cast_float3() throws Exception {
		assertEquals(new Float(1.2f), evaluateExpression("(float)1.2d", "double"));
	}

	@Test
	public void test_cast_double() throws Exception {
		assertEquals(new Double(1.2d), evaluateExpression("(double)1.2d", "double"));
	}

	@Test
	public void test_cast_Object() throws Exception {
		assertEquals("abc", evaluateExpression("(Object)\"abc\"", "java.lang.Object"));
	}
}
