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
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for XWT widgets models.
 * 
 * @author scheglov_ke
 */
public class WidgetTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.xwt.model.widgets");
    suite.addTest(createSingleSuite(ControlTest.class));
    suite.addTest(createSingleSuite(ButtonTest.class));
    suite.addTest(createSingleSuite(CompositeTest.class));
    suite.addTest(createSingleSuite(CompositeGefTest.class));
    suite.addTest(createSingleSuite(CompositeTopBoundsSupportTest.class));
    suite.addTest(createSingleSuite(ControlLiveManagerTest.class));
    suite.addTest(createSingleSuite(SashFormTest.class));
    suite.addTest(createSingleSuite(SashFormGefTest.class));
    suite.addTest(createSingleSuite(TabFolderTest.class));
    suite.addTest(createSingleSuite(TabFolderGefTest.class));
    suite.addTest(createSingleSuite(CTabFolderTest.class));
    suite.addTest(createSingleSuite(CTabFolderGefTest.class));
    suite.addTest(createSingleSuite(ViewFormTest.class));
    suite.addTest(createSingleSuite(ViewFormGefTest.class));
    suite.addTest(createSingleSuite(CBannerTest.class));
    suite.addTest(createSingleSuite(CBannerGefTest.class));
    suite.addTest(createSingleSuite(ListTest.class));
    suite.addTest(createSingleSuite(TableTest.class));
    suite.addTest(createSingleSuite(TableGefTest.class));
    suite.addTest(createSingleSuite(TreeTest.class));
    suite.addTest(createSingleSuite(TreeGefTest.class));
    suite.addTest(createSingleSuite(ToolBarTest.class));
    suite.addTest(createSingleSuite(ToolBarGefTest.class));
    suite.addTest(createSingleSuite(CoolBarTest.class));
    suite.addTest(createSingleSuite(CoolBarGefTest.class));
    suite.addTest(createSingleSuite(ExpandBarTest.class));
    suite.addTest(createSingleSuite(DragSourceTest.class));
    suite.addTest(createSingleSuite(DragSourceGefTest.class));
    suite.addTest(createSingleSuite(DropTargetTest.class));
    suite.addTest(createSingleSuite(DropTargetGefTest.class));
    suite.addTest(MenuTests.suite());
    return suite;
  }
}