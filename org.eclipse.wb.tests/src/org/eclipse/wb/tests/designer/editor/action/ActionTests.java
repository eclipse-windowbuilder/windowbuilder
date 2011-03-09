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
package org.eclipse.wb.tests.designer.editor.action;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author scheglov_ke
 */
public class ActionTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.editor.action");
    suite.addTest(createSingleSuite(ActionsTest.class));
    suite.addTest(createSingleSuite(DeleteActionTest.class));
    suite.addTest(createSingleSuite(CopyActionTest.class));
    suite.addTest(createSingleSuite(CutActionTest.class));
    suite.addTest(createSingleSuite(SwitchActionTest.class));
    suite.addTest(createSingleSuite(RefreshActionTest.class));
    return suite;
  }
}
