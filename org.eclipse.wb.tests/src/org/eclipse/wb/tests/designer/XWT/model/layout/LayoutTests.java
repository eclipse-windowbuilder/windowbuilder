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
package org.eclipse.wb.tests.designer.XWT.model.layout;

import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;
import org.eclipse.wb.tests.designer.XWT.model.layout.grid.GridLayoutTests;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for XWT {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class LayoutTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.xwt.model.layout");
    suite.addTest(createSingleSuite(LayoutTest.class));
    suite.addTest(createSingleSuite(AbsoluteLayoutTest.class));
    suite.addTest(createSingleSuite(AbsoluteLayoutGefTest.class));
    suite.addTest(createSingleSuite(RowLayoutTest.class));
    suite.addTest(createSingleSuite(RowLayoutGefTest.class));
    suite.addTest(createSingleSuite(FillLayoutTest.class));
    suite.addTest(createSingleSuite(FillLayoutGefTest.class));
    suite.addTest(createSingleSuite(StackLayoutTest.class));
    suite.addTest(createSingleSuite(StackLayoutGefTest.class));
    suite.addTest(createSingleSuite(FormLayoutTest.class));
    suite.addTest(createSingleSuite(FormLayoutGefTest.class));
    suite.addTest(GridLayoutTests.suite());
    return suite;
  }
}