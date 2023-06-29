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
package org.eclipse.wb.tests.designer.swt.model.layouts;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.swt.model.layouts.grid.GridLayoutTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for SWT layouts models.
 *
 * @author lobas_av
 */
public class LayoutTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.swt.model.layout");
		suite.addTest(createSingleSuite(LayoutTest.class));
		suite.addTest(createSingleSuite(LayoutDataTest.class));
		suite.addTest(createSingleSuite(VirtualLayoutDataTest.class));
		suite.addTest(createSingleSuite(ImplicitLayoutDataTest.class));
		suite.addTest(createSingleSuite(AbsoluteLayoutTest.class));
		suite.addTest(createSingleSuite(AbsoluteLayoutSelectionActionsTest.class));
		suite.addTest(createSingleSuite(AbsoluteLayoutOrderingTest.class));
		suite.addTest(createSingleSuite(FillLayoutTest.class));
		suite.addTest(createSingleSuite(RowLayoutTest.class));
		suite.addTest(createSingleSuite(LayoutLayoutDataCompatibilityTest.class));
		suite.addTest(GridLayoutTests.suite());
		return suite;
	}
}