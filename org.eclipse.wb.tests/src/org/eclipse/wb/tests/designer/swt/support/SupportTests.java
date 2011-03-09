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
package org.eclipse.wb.tests.designer.swt.support;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for support utils
 * 
 * @author scheglov_ke
 */
public class SupportTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.swt.support");
    suite.addTest(createSingleSuite(CoordinateUtilsTest.class));
    suite.addTest(createSingleSuite(ColorSupportTest.class));
    suite.addTest(createSingleSuite(FontSupportTest.class));
    suite.addTest(createSingleSuite(ImageSupportTest.class));
    suite.addTest(createSingleSuite(DisplaySupportTest.class));
    suite.addTest(createSingleSuite(FillLayoutSupportTest.class));
    suite.addTest(createSingleSuite(RowLayoutSupportTest.class));
    suite.addTest(createSingleSuite(ControlSupportTest.class));
    suite.addTest(createSingleSuite(ContainerSupportTest.class));
    suite.addTest(createSingleSuite(PointSupportTest.class));
    suite.addTest(createSingleSuite(RectangleSupportTest.class));
    suite.addTest(createSingleSuite(LabelSupportTest.class));
    suite.addTest(createSingleSuite(TableSupportTest.class));
    return suite;
  }
}