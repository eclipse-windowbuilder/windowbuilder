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
package org.eclipse.wb.tests.designer.XWT.model.widgets;

import org.eclipse.wb.tests.designer.XWT.model.widgets.menu.MenuTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for XWT widgets models.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		ControlTest.class,
		ButtonTest.class,
		CompositeTest.class,
		CompositeGefTest.class,
		CompositeTopBoundsSupportTest.class,
		ControlLiveManagerTest.class,
		SashFormTest.class,
		SashFormGefTest.class,
		TabFolderTest.class,
		TabFolderGefTest.class,
		CTabFolderTest.class,
		CTabFolderGefTest.class,
		ViewFormTest.class,
		ViewFormGefTest.class,
		CBannerTest.class,
		CBannerGefTest.class,
		ListTest.class,
		TableTest.class,
		TableGefTest.class,
		TreeTest.class,
		TreeGefTest.class,
		ToolBarTest.class,
		ToolBarGefTest.class,
		CoolBarTest.class,
		CoolBarGefTest.class,
		ExpandBarTest.class,
		DragSourceTest.class,
		DragSourceGefTest.class,
		DropTargetTest.class,
		DropTargetGefTest.class,
		MenuTests.class
})
public class WidgetTests {
}