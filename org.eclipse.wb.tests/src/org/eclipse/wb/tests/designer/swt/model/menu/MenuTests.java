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

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import org.eclipse.swt.widgets.Menu;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link Menu}.
 * 
 * @author mitin_aa
 */
public class MenuTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.swt.model.menu");
    suite.addTest(createSingleSuite(AbstractMenuObjectTest.class));
    suite.addTest(createSingleSuite(MenuSupportTest.class));
    suite.addTest(createSingleSuite(MenuItemTest.class));
    suite.addTest(createSingleSuite(MenuTest.class));
    suite.addTest(createSingleSuite(MenuObjectInfoUtilsTest.class));
    suite.addTest(createSingleSuite(MenuPopupSimpleTest.class));
    suite.addTest(createSingleSuite(MenuComplexTest.class));
    suite.addTest(createSingleSuite(MenuBarPopupTest.class));
    suite.addTest(createSingleSuite(MenuProblemsTest.class));
    return suite;
  }
}