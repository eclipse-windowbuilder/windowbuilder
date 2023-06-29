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
package org.eclipse.wb.tests.designer.core.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.PropertyManager;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategoryProvider;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategoryProviders;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.tests.common.PropertyWithTitle;

/**
 * Test for {@link PropertyCategoryProviders}.
 *
 * @author scheglov_ke
 */
public class PropertyCategoryProvidersTest extends SwingModelTest {
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
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link PropertyCategoryProviders#fromProperty()}.
	 */
	public void test_fromProperty() throws Exception {
		PropertyCategoryProvider provider = PropertyCategoryProviders.fromProperty();
		Property property = new PropertyWithTitle("title");
		// ADVANCED
		{
			PropertyCategory category = PropertyCategory.ADVANCED;
			property.setCategory(category);
			assertSame(category, provider.getCategory(property));
		}
		// PREFERRED
		{
			PropertyCategory category = PropertyCategory.PREFERRED;
			property.setCategory(category);
			assertSame(category, provider.getCategory(property));
		}
	}

	/**
	 * Test for {@link PropertyCategoryProviders#forcedByUser()}.
	 */
	public void test_forcedByUser() throws Exception {
		PropertyCategoryProvider provider = PropertyCategoryProviders.forcedByUser();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		Property property = panel.getPropertyByTitle("background");
		// no forced initially
		assertSame(null, provider.getCategory(property));
		// force
		try {
			PropertyCategory category = PropertyCategory.PREFERRED;
			PropertyManager.setCategory(property, category);
			assertSame(category, provider.getCategory(property));
		} finally {
			PropertyManager.setCategory(property, null);
		}
		// no forced again
		assertSame(null, provider.getCategory(property));
	}

	/**
	 * Test for {@link PropertyCategoryProviders#combine(PropertyCategoryProvider...)}.
	 */
	public void test_combine_empty() throws Exception {
		PropertyCategoryProvider provider = PropertyCategoryProviders.combine();
		Property property = new PropertyWithTitle("title");
		try {
			provider.getCategory(property);
			fail();
		} catch (IllegalStateException e) {
		}
	}

	/**
	 * Test for {@link PropertyCategoryProviders#combine(PropertyCategoryProvider...)}.
	 */
	public void test_combine_atLeastNormal() throws Exception {
		PropertyCategoryProvider atLeastNormal = new PropertyCategoryProvider() {
			@Override
			public PropertyCategory getCategory(Property property) {
				if (property.getCategory() == PropertyCategory.ADVANCED) {
					return PropertyCategory.NORMAL;
				}
				return null;
			}
		};
		PropertyCategoryProvider provider =
				PropertyCategoryProviders.combine(atLeastNormal, PropertyCategoryProviders.fromProperty());
		Property property = new PropertyWithTitle("title");
		// no changes for NORMAL
		{
			PropertyCategory category = PropertyCategory.NORMAL;
			property.setCategory(category);
			assertSame(category, property.getCategory());
			assertSame(category, provider.getCategory(property));
		}
		// if ADVANCED, still should be forced NORMAL
		{
			PropertyCategory category = PropertyCategory.ADVANCED;
			property.setCategory(category);
			assertSame(category, property.getCategory());
			assertSame(PropertyCategory.NORMAL, provider.getCategory(property));
		}
		// if PREFERRED, return it
		{
			PropertyCategory category = PropertyCategory.PREFERRED;
			property.setCategory(category);
			assertSame(category, property.getCategory());
			assertSame(category, provider.getCategory(property));
		}
	}

	/**
	 * Test for {@link PropertyCategoryProviders#def()}.
	 */
	public void test_def() throws Exception {
		PropertyCategoryProvider provider = PropertyCategoryProviders.def();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		Property property = panel.getPropertyByTitle("background");
		// NORMAL initially
		assertSame(PropertyCategory.NORMAL, provider.getCategory(property));
		// force
		try {
			PropertyCategory category = PropertyCategory.PREFERRED;
			PropertyManager.setCategory(property, category);
			assertSame(category, provider.getCategory(property));
		} finally {
			PropertyManager.setCategory(property, null);
		}
		// NORMAL again
		assertSame(PropertyCategory.NORMAL, provider.getCategory(property));
	}
}
