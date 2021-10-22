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
package org.eclipse.wb.tests.designer.swing.jsr296;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test for "Swing Application Framework", JSR-296.
 *
 * @author scheglov_ke
 */
public class ApplicationFrameworkTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.swing.jsr296");
    suite.addTest(createSingleSuite(ActivatorTest.class));
    suite.addTest(createSingleSuite(LoadResourcesTest.class));
    suite.addTest(createSingleSuite(FrameViewTest.class));
    suite.addTest(createSingleSuite(FrameViewGefTest.class));
    return suite;
  }
}
