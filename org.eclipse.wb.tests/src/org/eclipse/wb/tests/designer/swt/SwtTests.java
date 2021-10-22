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
package org.eclipse.wb.tests.designer.swt;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.swt.model.ModelTests;
import org.eclipse.wb.tests.designer.swt.support.SupportTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All SWT tests.
 *
 * @author sablin_aa
 */
public class SwtTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.swt");
    suite.addTest(createSingleSuite(ManagerUtilsTest.class));
    suite.addTest(ModelTests.suite());
    suite.addTest(SupportTests.suite());
    return suite;
  }
}
