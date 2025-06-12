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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for RCP widgets models.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
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