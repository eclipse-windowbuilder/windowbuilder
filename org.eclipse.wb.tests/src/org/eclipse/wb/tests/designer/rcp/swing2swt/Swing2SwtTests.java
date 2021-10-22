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
package org.eclipse.wb.tests.designer.rcp.swing2swt;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for Swing2SWT.
 *
 * @author scheglov_ke
 */
public class Swing2SwtTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.rcp.Swing2SWT");
    suite.addTest(createSingleSuite(BoxLayoutTest.class));
    suite.addTest(createSingleSuite(FlowLayoutTest.class));
    suite.addTest(createSingleSuite(BorderLayoutTest.class));
    return suite;
  }
}