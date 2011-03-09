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
package org.eclipse.wb.tests.designer.swing;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.swing.ams.AmsTests;
import org.eclipse.wb.tests.designer.swing.jsr296.ApplicationFrameworkTests;
import org.eclipse.wb.tests.designer.swing.laf.LookAndFeelTest;
import org.eclipse.wb.tests.designer.swing.model.ModelTests;
import org.eclipse.wb.tests.designer.swing.swingx.SwingXTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author scheglov_ke
 */
public class SwingTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.swing");
    suite.addTest(createSingleSuite(LookAndFeelTest.class));
    suite.addTest(createSingleSuite(ConvertersTest.class));
    suite.addTest(createSingleSuite(CustomizeTest.class));
    suite.addTest(ModelTests.suite());
    suite.addTest(AmsTests.suite());
    suite.addTest(SwingXTests.suite());
    suite.addTest(ApplicationFrameworkTests.suite());
    //suite.addTest(createSingleSuite(WaitForMemoryProfilerTest.class));
    return suite;
  }
}
