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

import org.eclipse.wb.tests.designer.core.model.property.accessor.AccessorsTests;
import org.eclipse.wb.tests.designer.core.model.property.editor.PropertyEditorsTests;
import org.eclipse.wb.tests.designer.core.model.property.table.PropertyTableTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
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
