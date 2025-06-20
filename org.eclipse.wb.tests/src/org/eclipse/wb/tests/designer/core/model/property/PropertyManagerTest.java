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

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.PropertyManager;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.swing.SwingToolkitDescription;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.tests.common.PropertyWithTitle;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link PropertyManager}.
 *
 * @author scheglov_ke
 */
public class PropertyManagerTest extends SwingModelTest {
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
	 * Just {@link Property} does not know its {@link ToolkitDescription}, so nowhere to remember
	 * category.
	 */
	@Test
	public void test_unknownProperty() throws Exception {
		Property property = new PropertyWithTitle("title");
		property.setCategory(PropertyCategory.ADVANCED);
		// ask category
		assertSame(PropertyCategory.ADVANCED, PropertyManager.getCategory(property));
		// try to set "preferred" - ignored
		PropertyManager.setCategory(property, PropertyCategory.PREFERRED);
		assertSame(PropertyCategory.ADVANCED, PropertyManager.getCategory(property));
	}

	/**
	 * {@link GenericProperty} can remember and return forced category.
	 */
	@Test
	public void test_GenericProperty() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		Property property = panel.getPropertyByTitle("background");
		// "normal" initially
		assertSame(null, PropertyManager.getCategoryForced(property));
		assertSame(PropertyCategory.NORMAL, PropertyManager.getCategory(property));
		// check categories
		checkCategory(property, PropertyCategory.PREFERRED);
		checkCategory(property, PropertyCategory.NORMAL);
		checkCategory(property, PropertyCategory.ADVANCED);
		// remove forced category, so "normal" again
		{
			PropertyManager.setCategory(property, null);
			assertSame(PropertyCategory.NORMAL, PropertyManager.getCategory(property));
		}
		// again set "preferred" for next test
		PropertyManager.setCategory(property, PropertyCategory.PREFERRED);
	}

	private void checkCategory(Property property, PropertyCategory category) {
		PropertyManager.setCategory(property, category);
		assertSame(category, PropertyManager.getCategoryForced(property));
		assertSame(category, PropertyManager.getCategory(property));
		// flush cache, so reload
		PropertyManager.flushCache();
		assertSame(category, PropertyManager.getCategory(property));
	}

	/**
	 * Previous test specified that "background" property is "preferred".
	 */
	@Test
	public void test_GenericProperty2() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		Property property = panel.getPropertyByTitle("background");
		// "preferred" from previous test
		assertSame(PropertyCategory.PREFERRED, PropertyManager.getCategory(property));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Test suite tear down
	//
	////////////////////////////////////////////////////////////////////////////
	@AfterAll
	public static void tearDownClass() throws Exception {
		AbstractJavaProjectTest.tearDownClass();
		PropertyManager.setCategory(SwingToolkitDescription.INSTANCE, "background", null);
	}
}
