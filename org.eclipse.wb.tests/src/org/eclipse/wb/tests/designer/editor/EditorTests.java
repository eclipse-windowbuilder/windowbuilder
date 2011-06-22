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
package org.eclipse.wb.tests.designer.editor;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.editor.action.ActionTests;
import org.eclipse.wb.tests.designer.editor.validator.LayoutRequestValidatorTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author scheglov_ke
 */
public class EditorTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.editor");
    // basic policy
    suite.addTest(createSingleSuite(TopSelectionEditPolicyTest.class));
    // basic features
    suite.addTest(createSingleSuite(UndoManagerTest.class));
    suite.addTest(createSingleSuite(ReparseOnModificationTest.class));
    suite.addTest(createSingleSuite(SelectSupportTest.class));
    suite.addTest(createSingleSuite(ComponentsPropertiesPageTest.class));
    suite.addTest(createSingleSuite(JavaPropertiesToolBarContributorTest.class));
    // suites
    suite.addTest(ActionTests.suite());
    suite.addTest(LayoutRequestValidatorTests.suite());
    return suite;
  }
}
