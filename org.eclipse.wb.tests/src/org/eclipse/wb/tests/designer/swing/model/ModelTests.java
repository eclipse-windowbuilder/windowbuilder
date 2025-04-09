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
package org.eclipse.wb.tests.designer.swing.model;

import org.eclipse.wb.tests.designer.swing.model.bean.BeanTests;
import org.eclipse.wb.tests.designer.swing.model.component.ComponentTests;
import org.eclipse.wb.tests.designer.swing.model.layout.LayoutTests;
import org.eclipse.wb.tests.designer.swing.model.property.PropertiesTests;
import org.eclipse.wb.tests.designer.swing.model.top.TopLevelTests;
import org.eclipse.wb.tests.designer.swing.model.util.UtilTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for Swing objects models.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		CoordinateUtilsTest.class,
		LayoutTests.class,
		ComponentTests.class,
		BeanTests.class,
		UtilTests.class,
		PropertiesTests.class,
		ClipboardTest.class,
		TopLevelTests.class
})
public class ModelTests {
}
