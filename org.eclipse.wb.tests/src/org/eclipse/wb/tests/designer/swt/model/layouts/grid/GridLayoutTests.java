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
package org.eclipse.wb.tests.designer.swt.model.layouts.grid;

import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link GridLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class GridLayoutTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.swt.model.layout.grid");
		suite.addTest(createSingleSuite(GridDataTest.class));
		suite.addTest(createSingleSuite(GridLayoutSelectionActionsTest.class));
		suite.addTest(createSingleSuite(GridLayoutTest.class));
		suite.addTest(createSingleSuite(GridLayoutParametersTest.class));
		suite.addTest(createSingleSuite(GridLayoutConverterTest.class));
		suite.addTest(createSingleSuite(GridLayoutExposedTest.class));
		suite.addTest(createSingleSuite(GridLayoutGefTest.class));
		return suite;
	}
}