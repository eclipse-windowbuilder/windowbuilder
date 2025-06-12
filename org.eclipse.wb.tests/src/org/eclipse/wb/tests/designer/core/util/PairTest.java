/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.core.util;

import org.eclipse.wb.internal.core.utils.Pair;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Pair}.
 *
 * @author scheglov_ke
 */
public class PairTest extends DesignerTestCase {
	@Test
	public void test_access() throws Exception {
		Pair<String, String> pair = Pair.create("One", "Odin");
		assertSame("One", pair.getLeft());
		assertSame("Odin", pair.getRight());
		// just ask hashCode(), not check
		pair.hashCode();
	}

	@Test
	public void test_equalsYes_1() throws Exception {
		Pair<String, String> pair = Pair.create("One", "Odin");
		assertEquals(pair, pair);
	}

	@Test
	public void test_equalsYes_2() throws Exception {
		Pair<String, String> pair_1 = Pair.create("One", "Odin");
		Pair<String, String> pair_2 = Pair.create("One", "Odin");
		assertEquals(pair_1, pair_2);
	}

	@Test
	public void test_equalsNo_1() throws Exception {
		Pair<String, String> pair_1 = Pair.create("One", "Odin");
		assertNotEquals(pair_1, "One");
	}

	@Test
	public void test_equalsNo_2() throws Exception {
		Pair<String, String> pair_1 = Pair.create("One", "Odin");
		Pair<String, String> pair_2 = Pair.create("Two", "Dva");
		assertNotEquals(pair_1, pair_2);
	}
}
