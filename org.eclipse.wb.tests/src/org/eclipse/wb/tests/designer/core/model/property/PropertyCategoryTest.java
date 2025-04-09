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
package org.eclipse.wb.tests.designer.core.model.property;

import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

/**
 * Test for {@link PropertyCategory}.
 *
 * @author scheglov_ke
 */
public class PropertyCategoryTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_is() throws Exception {
		assertTrue(PropertyCategory.PREFERRED.isPreferred());
		assertTrue(PropertyCategory.ADVANCED.isAdvanced());
		assertTrue(PropertyCategory.ADVANCED_REALLY.isAdvancedReally());
		assertTrue(PropertyCategory.HIDDEN.isHidden());
		{
			PropertyCategory systemCategory = PropertyCategory.system(10);
			assertTrue(systemCategory.isSystem());
			assertEquals(1000 + 10, systemCategory.getPriority());
		}
	}

	@Test
	public void test_system() throws Exception {
		PropertyCategory category = PropertyCategory.system(10);
		assertTrue(category.isSystem());
		assertEquals(1000 + 10, category.getPriority());
		//
		PropertyCategory categoryAdd = PropertyCategory.system(category, 5);
		assertTrue(categoryAdd.isSystem());
		assertEquals(1000 + 10 + 5, categoryAdd.getPriority());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_toString() throws Exception {
		assertEquals("PREFERRED", PropertyCategory.PREFERRED.toString());
		assertEquals("NORMAL", PropertyCategory.NORMAL.toString());
		assertEquals("ADVANCED", PropertyCategory.ADVANCED.toString());
		assertEquals("ADVANCED_REALLY", PropertyCategory.ADVANCED_REALLY.toString());
		assertEquals("HIDDEN", PropertyCategory.HIDDEN.toString());
		assertEquals("SYSTEM:10", PropertyCategory.system(10).toString());
	}

	@Test
	public void test_equals() throws Exception {
		assertEquals(PropertyCategory.PREFERRED, PropertyCategory.PREFERRED);
		assertEquals(PropertyCategory.ADVANCED, PropertyCategory.ADVANCED);
		assertEquals(PropertyCategory.system(10), PropertyCategory.system(10));
		assertFalse(PropertyCategory.PREFERRED.equals(PropertyCategory.ADVANCED));
		assertFalse(PropertyCategory.system(10).equals(PropertyCategory.system(11)));
		assertFalse(PropertyCategory.PREFERRED.equals(this));
	}

	@Test
	public void test_hashCode() throws Exception {
		PropertyCategory category = PropertyCategory.PREFERRED;
		assertEquals(category.getPriority(), category.hashCode());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parsing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link PropertyCategory#get(String, PropertyCategory)}.
	 */
	@Test
	public void test_get() throws Exception {
		assertSame(PropertyCategory.NORMAL, PropertyCategory.get("normal", null));
		assertSame(PropertyCategory.PREFERRED, PropertyCategory.get("preferred", null));
		assertSame(PropertyCategory.ADVANCED, PropertyCategory.get("advanced", null));
		assertSame(PropertyCategory.ADVANCED_REALLY, PropertyCategory.get("advanced-really", null));
		assertSame(PropertyCategory.HIDDEN, PropertyCategory.get("hidden", null));
		assertSame(
				PropertyCategory.ADVANCED,
				PropertyCategory.get("NoSuchCategory", PropertyCategory.ADVANCED));
		// system(5)
		{
			PropertyCategory category = PropertyCategory.get("system(5)", null);
			assertTrue(category.isSystem());
			assertEquals(1000 + 5, category.getPriority());
		}
		// system(bad), with default
		{
			PropertyCategory category = PropertyCategory.get("system(bad)", PropertyCategory.ADVANCED);
			assertSame(PropertyCategory.ADVANCED, category);
		}
		// bad, with default
		{
			PropertyCategory category = PropertyCategory.get("bad", PropertyCategory.ADVANCED);
			assertSame(PropertyCategory.ADVANCED, category);
		}
		// bad, no default
		try {
			PropertyCategory.get("bad", null);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}
}
