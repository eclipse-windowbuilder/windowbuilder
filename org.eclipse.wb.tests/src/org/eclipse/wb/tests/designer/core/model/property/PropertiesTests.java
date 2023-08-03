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

import org.eclipse.wb.tests.designer.core.model.property.accessor.AccessorsTests;
import org.eclipse.wb.tests.designer.core.model.property.editor.PropertyEditorsTests;
import org.eclipse.wb.tests.designer.core.model.property.table.PropertyTableTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		StandardConvertersTest.class,
		PropertyCategoryProvidersTest.class,
		PropertyCategoryTest.class,
		PropertyTest.class,
		PropertyManagerTest.class,
		EmptyPropertyTest.class,
		EventsPropertyTest.class,
		ComponentClassPropertyTest.class,
		TabOrderPropertyTest.class,
		ExposePropertySupportTest.class,
		AccessorsTests.class,
		PropertyEditorsTests.class,
		PropertyTableTests.class
})
public class PropertiesTests {
}
