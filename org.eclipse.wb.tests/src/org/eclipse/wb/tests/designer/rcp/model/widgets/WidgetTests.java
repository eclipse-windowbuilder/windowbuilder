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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for RCP widgets models.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		ControlTest.class,
		AsyncMessagesSupportTest.class,
		TabOrderPropertyTest.class,
		LabelTest.class,
		TableTest.class,
		TableGefTest.class,
		TreeTest.class,
		TreeGefTest.class,
		ToolBarTest.class,
		ToolBarGefTest.class,
		CoolBarTest.class,
		CoolBarGefTest.class,
		ExpandBarTest.class,
		TabFolderTest.class,
		TabFolderGefTest.class,
		CTabFolderTest.class,
		ViewFormTest.class,
		ViewFormGefTest.class,
		CBannerTest.class,
		SashFormTest.class,
		ScrolledCompositeTest.class,
		DialogTest.class,
		SwtAwtTest.class,
		DragSourceTest.class,
		DropTargetTest.class,
		TrayItemTest.class,
		MorphingSupportTest.class,
		DialogTopBoundsSupportTest.class
})
public class WidgetTests {
}