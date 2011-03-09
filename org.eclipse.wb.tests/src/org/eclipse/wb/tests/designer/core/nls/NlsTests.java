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
package org.eclipse.wb.tests.designer.core.nls;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author scheglov_ke
 */
public class NlsTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.core.nls");
    suite.addTest(createSingleSuite(LocaleInfoTest.class));
    suite.addTest(createSingleSuite(BundleInfoTest.class));
    suite.addTest(createSingleSuite(NlsSupportTest.class));
    suite.addTest(createSingleSuite(EditableSupportTest.class));
    suite.addTest(createSingleSuite(SourceEclipseOldTest.class));
    suite.addTest(createSingleSuite(SourceEclipseModernTest.class));
    suite.addTest(createSingleSuite(SourceDirectTest.class));
    suite.addTest(createSingleSuite(SourceFieldTest.class));
    suite.addTest(createSingleSuite(SourceAbstractSpecialTest.class));
    //suite.addTest(NLSUITests.suite());
    return suite;
  }
}
