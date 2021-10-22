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
package org.eclipse.wb.tests.designer.rcp;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.rcp.description.DescriptionTests;
import org.eclipse.wb.tests.designer.rcp.gef.GefTests;
import org.eclipse.wb.tests.designer.rcp.model.ModelTests;
import org.eclipse.wb.tests.designer.rcp.nebula.NebulaTests;
import org.eclipse.wb.tests.designer.rcp.resource.ResourceTests;
import org.eclipse.wb.tests.designer.rcp.swing2swt.Swing2SwtTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All RCP tests.
 *
 * @author scheglov_ke
 */
public class RcpTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.rcp");
    suite.addTest(ModelTests.suite());
    suite.addTest(DescriptionTests.suite());
    suite.addTest(ResourceTests.suite());
    suite.addTest(NebulaTests.suite());
    suite.addTest(Swing2SwtTests.suite());
    suite.addTest(GefTests.suite());
    //suite.addTest(createSingleSuite(WaitForMemoryProfilerTest.class));
    return suite;
  }
}
