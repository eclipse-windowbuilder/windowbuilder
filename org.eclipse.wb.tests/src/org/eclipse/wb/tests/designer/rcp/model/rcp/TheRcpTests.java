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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for RCP models.
 * 
 * @author scheglov_ke
 */
public class TheRcpTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.rcp.model.rcp");
    suite.addTest(createSingleSuite(PropertyPageTest.class));
    suite.addTest(createSingleSuite(PdeUtilsTest.class));
    suite.addTest(createSingleSuite(ExtensionElementPropertyTest.class));
    suite.addTest(createSingleSuite(ViewPartTest.class));
    suite.addTest(createSingleSuite(ViewPartGefTest.class));
    suite.addTest(createSingleSuite(ViewCategoryPropertyEditorTest.class));
    suite.addTest(createSingleSuite(EditorPartTest.class));
    suite.addTest(createSingleSuite(AbstractSplashHandlerTest.class));
    suite.addTest(createSingleSuite(MultiPageEditorPartTest.class));
    suite.addTest(createSingleSuite(PageTest.class));
    suite.addTest(createSingleSuite(PageLayoutTest.class));
    suite.addTest(createSingleSuite(PageLayoutGefTest.class));
    suite.addTest(createSingleSuite(ActionBarAdvisorTest.class));
    suite.addTest(createSingleSuite(ActionFactoryTest.class));
    suite.addTest(createSingleSuite(FilteredItemsSelectionDialogTest.class));
    suite.addTest(createSingleSuite(RcpWizardsTest.class));
    //suite.addTest(createSingleSuite(WaitForMemoryProfilerTest.class));
    return suite;
  }
}