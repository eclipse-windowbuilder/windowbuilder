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
package org.eclipse.wb.tests.designer.core.util.check;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Assert}.
 *
 * @author scheglov_ke
 */
public class AssertTest extends Assertions {
	////////////////////////////////////////////////////////////////////////////
	//
	// isLegal
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_isLegal() {
		Assert.isLegal(true);
		Assert.isLegal(true, "message");
		//
		try {
			Assert.isLegal(false);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("", e.getMessage());
		}
		//
		try {
			Assert.isLegal(false, "message");
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("message", e.getMessage());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// isNull/isNotNull
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_isNull() {
		Assert.isNull(null);
		Assert.isNull(null, "message");
		//
		try {
			Assert.isNull(this);
			fail();
		} catch (AssertionFailedException e) {
			assertEquals("null argument expected", e.getMessage());
		}
		//
		try {
			Assert.isNull(this, "message");
			fail();
		} catch (AssertionFailedException e) {
			assertEquals("message", e.getMessage());
		}
	}

	/**
	 * Test for {@link Assert#isNull(Object, String, Object...)}.
	 */
	@Test
	public void test_isNull_1() {
		Assert.isNull(null, "errorFormat %d %d", 1, 2);
		//
		try {
			Assert.isNull(this, "errorFormat %d %d", 1, 2);
			fail();
		} catch (AssertionFailedException e) {
			assertEquals("errorFormat 1 2", e.getMessage());
		}
	}

	/**
	 * Test for {@link Assert#isNull2(Object, String, Object...)}.
	 */
	@Test
	public void test_isNull_2() {
		Assert.isNull2(null, "errorFormat {0} {1}", 1, 2);
		//
		try {
			Assert.isNull2(this, "errorFormat {0} {1}", 1, 2);
			fail();
		} catch (AssertionFailedException e) {
			assertEquals("errorFormat 1 2", e.getMessage());
		}
	}

	/**
	 * Test for {@link Assert#isNotNull(Object)}.
	 */
	@Test
	public void test_isNotNull_1() {
		Assert.isNotNull(this);
		//
		try {
			Assert.isNotNull(null);
			fail();
		} catch (AssertionFailedException e) {
			assertEquals("null argument", e.getMessage());
		}
	}

	/**
	 * Test for {@link Assert#isNotNull(Object, String)}.
	 */
	@Test
	public void test_isNotNull_2() {
		Assert.isNotNull(this, "message");
		//
		try {
			Assert.isNotNull(null, "message");
			fail();
		} catch (AssertionFailedException e) {
			assertEquals("message", e.getMessage());
		}
	}

	/**
	 * Test for {@link Assert#isNotNull(Object, String, Object...)}.
	 */
	@Test
	public void test_isNotNull_3() {
		Assert.isNotNull(this, "errorFormat %d %d", 1, 2);
		//
		try {
			Assert.isNotNull(null, "errorFormat %d %d", 1, 2);
			fail();
		} catch (AssertionFailedException e) {
			assertEquals("errorFormat 1 2", e.getMessage());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// isNotNull2
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_isNotNull2() throws Exception {
		Assert.isNotNull2(this, "errorFormat {0} {1}", 1, 2);
		//
		try {
			Assert.isNotNull2(null, "errorFormat {0} {1}", 1, 2);
			fail();
		} catch (AssertionFailedException e) {
			assertEquals("errorFormat 1 2", e.getMessage());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// fail
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_fail() throws Exception {
		String message = "My fail message";
		try {
			Assert.fail(message);
			fail();
		} catch (AssertionFailedException e) {
			assertSame(message, e.getMessage());
		}
	}

	/**
	 * Test for {@link Assert#fail(String, Object...)}.
	 */
	@Test
	public void test_fail_withParameters() throws Exception {
		try {
			Assert.fail("My {1} message {0}.", "second", "first");
			fail();
		} catch (AssertionFailedException e) {
			assertEquals("My first message second.", e.getMessage());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// isTrue() for %s
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link Assert#isTrue(boolean)}.
	 */
	@Test
	public void test_isTrue_1() {
		Assert.isTrue(true);
		//
		try {
			Assert.isTrue(false);
			fail();
		} catch (AssertionFailedException e) {
			assertEquals("assertion failed", e.getMessage());
		}
	}

	/**
	 * Test for {@link Assert#isTrue(boolean, String)}.
	 */
	@Test
	public void test_isTrue_2() {
		Assert.isTrue(true, "message");
		//
		try {
			Assert.isTrue(false, "message");
			fail();
		} catch (AssertionFailedException e) {
			assertEquals("message", e.getMessage());
		}
	}

	/**
	 * Test for {@link Assert#isTrue(boolean, String, Object...)}.
	 */
	@Test
	public void test_isTrue_3() {
		Assert.isTrue(true, "errorFormat %d %d", 1, 2);
		//
		try {
			Assert.isTrue(false, "errorFormat %d %d", 1, 2);
			fail();
		} catch (AssertionFailedException e) {
			assertEquals("errorFormat 1 2", e.getMessage());
		}
	}

	/**
	 * Test for {@link Assert#isTrue2(boolean, String, Object...)}.
	 */
	@Test
	public void test_isTrue2() {
		Assert.isTrue2(true, "errorFormat {0} {1}", "ABC", 2);
		//
		try {
			Assert.isTrue2(false, "errorFormat {0} {1}", "ABC", 2);
			fail();
		} catch (AssertionFailedException e) {
			assertEquals("errorFormat ABC 2", e.getMessage());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// equals - int
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_equals_int() throws Exception {
		Assert.equals(0, 0);
		//
		try {
			Assert.equals(0, 1);
			fail();
		} catch (AssertionFailedException e) {
			assertEquals("0 expected, but 1 found", e.getMessage());
		}
		//
		try {
			Assert.equals(0, 1, "message");
			fail();
		} catch (AssertionFailedException e) {
			assertEquals("message", e.getMessage());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// instanceOf
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_instanceOf() throws Exception {
		try {
			Assert.instanceOf(String.class, null);
			fail();
		} catch (AssertionFailedException e) {
		}
		//
		Assert.instanceOf(String.class, "");
		//
		try {
			Assert.instanceOf(String.class, Boolean.TRUE);
			fail();
		} catch (AssertionFailedException e) {
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DesignerException
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_isTrueException_success() throws Exception {
		int exceptionCode = -1;
		Assert.isTrueException(true, exceptionCode);
	}

	@Test
	public void test_isTrueException_String() throws Exception {
		int exceptionCode = -1;
		String message = "message";
		// fail, check exception
		try {
			Assert.isTrueException(false, exceptionCode, message);
			fail();
		} catch (DesignerException e) {
			assertEquals(exceptionCode, e.getCode());
			Object[] parameters = e.getParameters();
			assertEquals(1, parameters.length);
			assertSame(message, parameters[0]);
		}
	}

	@Test
	public void test_isTrueException_Object() throws Exception {
		int exceptionCode = -1;
		Object parameter = Integer.valueOf(10);
		// fail, check exception
		try {
			Assert.isTrueException(false, exceptionCode, parameter);
			fail();
		} catch (DesignerException e) {
			assertEquals(exceptionCode, e.getCode());
			Object[] parameters = e.getParameters();
			assertEquals(1, parameters.length);
			assertEquals("10", parameters[0]);
		}
	}

	@Test
	public void test_isTrueException_null() throws Exception {
		int exceptionCode = -1;
		Object parameter = null;
		// fail, check exception
		try {
			Assert.isTrueException(false, exceptionCode, parameter);
			fail();
		} catch (DesignerException e) {
			assertEquals(exceptionCode, e.getCode());
			Object[] parameters = e.getParameters();
			assertEquals(1, parameters.length);
			assertEquals("null", parameters[0]);
		}
	}
}
