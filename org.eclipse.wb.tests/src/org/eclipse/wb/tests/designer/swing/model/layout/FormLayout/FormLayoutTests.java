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
package org.eclipse.wb.tests.designer.swing.model.layout.FormLayout;

import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests {@link FormLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class FormLayoutTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.swing.FormLayout");
		suite.addTest(createSingleSuite(FormSizeInfoTest.class));
		suite.addTest(createSingleSuite(FormDimensionInfoTest.class));
		suite.addTest(createSingleSuite(FormLayoutTest.class));
		suite.addTest(createSingleSuite(CellConstraintsSupportTest.class));
		suite.addTest(createSingleSuite(FormLayoutGroupsTest.class));
		suite.addTest(createSingleSuite(FormColumnInfoTest.class));
		suite.addTest(createSingleSuite(FormRowInfoTest.class));
		suite.addTest(createSingleSuite(FormLayoutParametersTest.class));
		suite.addTest(createSingleSuite(FormLayoutConverterTest.class));
		suite.addTest(createSingleSuite(FormLayoutSelectionActionsTest.class));
		suite.addTest(createSingleSuite(FormLayoutGefTest.class));
		suite.addTest(createSingleSuite(DefaultComponentFactoryTest.class));
		return suite;
	}
}
