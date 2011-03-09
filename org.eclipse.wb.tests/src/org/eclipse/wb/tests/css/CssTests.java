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
package org.eclipse.wb.tests.css;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test for "org.eclipse.wb.css" project.
 * 
 * @author scheglov_ke
 */
public class CssTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.css");
    suite.addTestSuite(CssDocumentTest.class);
    suite.addTestSuite(SemanticsTest.class);
    suite.addTestSuite(DefaultScannerTest.class);
    return suite;
  }
}
