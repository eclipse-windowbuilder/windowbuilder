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
package org.eclipse.wb.tests.designer.core.model.util;

import org.eclipse.wb.internal.core.model.util.predicate.AlwaysPredicate;
import org.eclipse.wb.internal.core.model.util.predicate.ComponentSubclassPredicate;
import org.eclipse.wb.internal.core.model.util.predicate.ExpressionPredicate;
import org.eclipse.wb.internal.core.model.util.predicate.SubclassPredicate;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

import java.util.function.Predicate;

/**
 * Test for {@link Predicate} implementations.
 *
 * @author scheglov_ke
 */
public class PredicatesTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link AlwaysPredicate}.
	 */
	@Test
	public void test_AlwaysPredicate_true() throws Exception {
		Predicate<Object> predicate = new AlwaysPredicate<>(true);
		assertTrue(predicate.test("yes"));
		assertTrue(predicate.test(this));
		assertTrue(predicate.test(null));
	}

	/**
	 * Test for {@link AlwaysPredicate}.
	 */
	@Test
	public void test_AlwaysPredicate_false() throws Exception {
		Predicate<Object> predicate = new AlwaysPredicate<>(false);
		assertFalse(predicate.test("yes"));
		assertFalse(predicate.test(this));
		assertFalse(predicate.test(null));
	}

	/**
	 * Test for {@link SubclassPredicate}.
	 */
	@Test
	public void test_SubclassPredicate() throws Exception {
		Predicate<Object> predicate = new SubclassPredicate(String.class);
		assertTrue(predicate.test("yes"));
		assertFalse(predicate.test(this));
		assertTrue(predicate.test(null));
	}

	/**
	 * Test for {@link ComponentSubclassPredicate}.
	 */
	@Test
	public void test_ComponentSubclassPredicate() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		ComponentSubclassPredicate predicate = new ComponentSubclassPredicate("java.awt.Component");
		assertEquals("java.awt.Component", predicate.toString());
		assertTrue(predicate.test(panel));
		assertFalse(predicate.test(panel.getLayout()));
		assertFalse(predicate.test("not JavaInfo"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Expression predicate
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ExpressionPredicate}.
	 */
	@Test
	public void test_ExpressionPredicate_alwaysTrue() throws Exception {
		Predicate<Object> predicate = new ExpressionPredicate<>("true");
		assertEquals("true", predicate.toString());
		assertTrue(predicate.test(null));
	}

	/**
	 * Test for {@link ExpressionPredicate}.
	 */
	@Test
	public void test_ExpressionPredicate_alwaysFalse() throws Exception {
		Predicate<Object> predicate = new ExpressionPredicate<>("false");
		assertFalse(predicate.test(null));
	}

	/**
	 * Test for {@link ExpressionPredicate}.
	 */
	@Test
	public void test_ExpressionPredicate_checkLength() throws Exception {
		Predicate<Object> predicate = new ExpressionPredicate<>("length() > 5");
		assertFalse(predicate.test("123"));
		assertTrue(predicate.test("123456"));
	}
}
