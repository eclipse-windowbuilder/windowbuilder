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

import org.eclipse.wb.internal.core.eval.evaluators.ClassEvaluator;
import org.eclipse.wb.tests.designer.core.eval.AbstractEngineTest;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Test for {@link ClassEvaluator}.
 *
 * @author scheglov_ke
 */
public class ClassTest extends AbstractEngineTest {
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
	public void test_TypeLiteral_Object() throws Exception {
		assertEquals(ArrayList.class, evaluateExpression("java.util.ArrayList.class", "Class"));
	}

	@Test
	public void test_TypeLiteral_primitive() throws Exception {
		assertEquals(boolean.class, evaluateExpression("boolean.class", "Class"));
	}

	@Test
	public void test_getClass() throws Exception {
		Class<?> actualClass = (Class<?>) evaluateExpression("getClass()", "Class", true);
		assertEquals("test.Test", actualClass.getName());
	}

	@Test
	public void test_getClass_withThisQualifier() throws Exception {
		Class<?> actualClass = (Class<?>) evaluateExpression("this.getClass()", "Class", true);
		assertEquals("test.Test", actualClass.getName());
	}
}
