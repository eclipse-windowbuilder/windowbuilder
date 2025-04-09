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
package org.eclipse.wb.tests.designer.swt.model.menu;

import org.eclipse.swt.widgets.Menu;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for {@link Menu}.
 *
 * @author mitin_aa
 */
@RunWith(Suite.class)
@SuiteClasses({
		AbstractMenuObjectTest.class,
		MenuItemTest.class,
		MenuTest.class,
		MenuObjectInfoUtilsTest.class,
		MenuPopupSimpleTest.class,
		MenuComplexTest.class,
		MenuBarPopupTest.class,
		MenuProblemsTest.class
})
public class MenuTests {
}