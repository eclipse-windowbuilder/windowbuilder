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
package org.eclipse.wb.tests.designer.swing.model.layout.MigLayout;

import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests {@link MigLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class MigLayoutTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.swing.MigLayout");
    suite.addTest(createSingleSuite(MigColumnTest.class));
    suite.addTest(createSingleSuite(MigRowTest.class));
    suite.addTest(createSingleSuite(MigLayoutConstraintsTest.class));
    suite.addTest(createSingleSuite(MigLayoutConstraintsPropertiesTest.class));
    suite.addTest(createSingleSuite(MigLayoutTest.class));
    suite.addTest(createSingleSuite(MigLayoutAutoAlignmentTest.class));
    suite.addTest(createSingleSuite(MigLayoutConverterTest.class));
    suite.addTest(createSingleSuite(MigLayoutSurroundSupportTest.class));
    suite.addTest(createSingleSuite(MigLayoutSelectionActionsTest.class));
    suite.addTest(createSingleSuite(MigLayoutGefTest.class));
    return suite;
  }
}
