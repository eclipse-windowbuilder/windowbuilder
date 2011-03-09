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
package org.eclipse.wb.tests.designer.ercp.model.widgets.mobile;

import org.eclipse.wb.internal.ercp.model.widgets.mobile.CommandInfo;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.ercp.gef.CommandGefTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for for {@link CommandInfo}.
 * 
 * @author scheglov_ke
 */
public class CommandAllTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.ercp.Command");
    suite.addTest(createSingleSuite(CommandTest.class));
    suite.addTest(createSingleSuite(CommandGefTest.class));
    //suite.addTestSuite(WaitForMemoryProfilerTest.class);
    return suite;
  }
}