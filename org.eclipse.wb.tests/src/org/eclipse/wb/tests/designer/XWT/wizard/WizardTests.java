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
package org.eclipse.wb.tests.designer.XWT.wizard;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for XWT wizards.
 * 
 * @author scheglov_ke
 */
public class WizardTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.xwt.wizard");
    suite.addTest(createSingleSuite(ApplicationWizardTest.class));
    suite.addTest(createSingleSuite(CompositeWizardTest.class));
    suite.addTest(createSingleSuite(FormsApplicationWizardTest.class));
    suite.addTest(createSingleSuite(FormsCompositeWizardTest.class));
    return suite;
  }
}