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
		MenuSupportTest.class,
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