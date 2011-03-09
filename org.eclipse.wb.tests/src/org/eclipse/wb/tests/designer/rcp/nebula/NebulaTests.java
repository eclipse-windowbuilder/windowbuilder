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
package org.eclipse.wb.tests.designer.rcp.nebula;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for Nebula widgets models.
 * 
 * @author sablin_aa
 */
public class NebulaTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.rcp.nebula");
    suite.addTest(createSingleSuite(CollapsibleButtonsTest.class));
    suite.addTest(createSingleSuite(CTableTreeTest.class));
    suite.addTest(createSingleSuite(GalleryTest.class));
    suite.addTest(createSingleSuite(GanttChartTest.class));
    suite.addTest(createSingleSuite(GridTest.class));
    suite.addTest(createSingleSuite(PShelfTest.class));
    return suite;
  }
}