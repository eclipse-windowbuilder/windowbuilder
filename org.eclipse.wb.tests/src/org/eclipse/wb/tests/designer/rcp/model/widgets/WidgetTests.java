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

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for RCP widgets models.
 *
 * @author scheglov_ke
 */
public class WidgetTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.rcp.model.widgets");
		suite.addTest(createSingleSuite(ControlTest.class));
		suite.addTest(createSingleSuite(AsyncMessagesSupportTest.class));
		suite.addTest(createSingleSuite(TabOrderPropertyTest.class));
		suite.addTest(createSingleSuite(LabelTest.class));
		suite.addTest(createSingleSuite(TableTest.class));
		suite.addTest(createSingleSuite(TableGefTest.class));
		suite.addTest(createSingleSuite(TreeTest.class));
		suite.addTest(createSingleSuite(TreeGefTest.class));
		suite.addTest(createSingleSuite(ToolBarTest.class));
		suite.addTest(createSingleSuite(ToolBarGefTest.class));
		suite.addTest(createSingleSuite(CoolBarTest.class));
		suite.addTest(createSingleSuite(CoolBarGefTest.class));
		suite.addTest(createSingleSuite(ExpandBarTest.class));
		suite.addTest(createSingleSuite(TabFolderTest.class));
		suite.addTest(createSingleSuite(TabFolderGefTest.class));
		suite.addTest(createSingleSuite(CTabFolderTest.class));
		suite.addTest(createSingleSuite(ViewFormTest.class));
		suite.addTest(createSingleSuite(ViewFormGefTest.class));
		suite.addTest(createSingleSuite(CBannerTest.class));
		suite.addTest(createSingleSuite(SashFormTest.class));
		suite.addTest(createSingleSuite(ScrolledCompositeTest.class));
		suite.addTest(createSingleSuite(DialogTest.class));
		suite.addTest(createSingleSuite(SwtAwtTest.class));
		suite.addTest(createSingleSuite(DragSourceTest.class));
		suite.addTest(createSingleSuite(DropTargetTest.class));
		suite.addTest(createSingleSuite(TrayItemTest.class));
		suite.addTest(createSingleSuite(MorphingSupportTest.class));
		suite.addTest(createSingleSuite(DialogTopBoundsSupportTest.class));
		return suite;
	}
}