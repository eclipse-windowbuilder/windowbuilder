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
package org.eclipse.wb.tests.designer.swing.model.layout.gbl;

import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link GridBagLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class GridBagLayoutTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.swing.GridBagLayout");
    suite.addTest(createSingleSuite(GridBagLayoutTest.class));
    suite.addTest(createSingleSuite(GridBagDimensionTest.class));
    suite.addTest(createSingleSuite(GridBagColumnTest.class));
    suite.addTest(createSingleSuite(GridBagRowTest.class));
    suite.addTest(createSingleSuite(GridBagConstraintsTest.class));
    suite.addTest(createSingleSuite(GridBagLayoutParametersTest.class));
    suite.addTest(createSingleSuite(GridBagLayoutConverterTest.class));
    suite.addTest(createSingleSuite(GridBagLayoutSelectionActionsTest.class));
    suite.addTest(createSingleSuite(GridBagLayoutSurroundSupportTest.class));
    suite.addTest(createSingleSuite(GridBagLayoutGefTest.class));
    return suite;
  }
}
