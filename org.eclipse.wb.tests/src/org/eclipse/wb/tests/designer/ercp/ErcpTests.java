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
package org.eclipse.wb.tests.designer.ercp;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.ercp.gef.ErcpGefTests;
import org.eclipse.wb.tests.designer.ercp.model.GenerationSettingsTest;
import org.eclipse.wb.tests.designer.ercp.model.ModelTests;
import org.eclipse.wb.tests.designer.ercp.model.layouts.LayoutDescriptionTest;
import org.eclipse.wb.tests.designer.ercp.wizard.WizardsTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All eRCP tests.
 * 
 * @author scheglov_ke
 */
public class ErcpTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.ercp");
    suite.addTest(createSingleSuite(ActivatorTest.class));
    suite.addTest(createSingleSuite(LayoutDescriptionTest.class));
    suite.addTest(createSingleSuite(GenerationSettingsTest.class));
    suite.addTest(ModelTests.suite());
    suite.addTest(WizardsTests.suite());
    suite.addTest(ErcpGefTests.suite());
    return suite;
  }
}
