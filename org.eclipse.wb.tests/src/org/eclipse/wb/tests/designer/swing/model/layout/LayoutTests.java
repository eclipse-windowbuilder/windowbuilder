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
package org.eclipse.wb.tests.designer.swing.model.layout;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.swing.model.layout.FormLayout.FormLayoutTests;
import org.eclipse.wb.tests.designer.swing.model.layout.MigLayout.MigLayoutTests;
import org.eclipse.wb.tests.designer.swing.model.layout.gbl.GridBagLayoutTests;
import org.eclipse.wb.tests.designer.swing.model.layout.group.GroupLayoutTests;
import org.eclipse.wb.tests.designer.swing.model.layout.model.BorderLayoutTest;
import org.eclipse.wb.tests.designer.swing.model.layout.model.BoxLayoutTest;
import org.eclipse.wb.tests.designer.swing.model.layout.model.CardLayoutGefTest;
import org.eclipse.wb.tests.designer.swing.model.layout.model.CardLayoutTest;
import org.eclipse.wb.tests.designer.swing.model.layout.model.FlowLayoutGefTest;
import org.eclipse.wb.tests.designer.swing.model.layout.model.FlowLayoutTest;
import org.eclipse.wb.tests.designer.swing.model.layout.model.GridLayoutTest;
import org.eclipse.wb.tests.designer.swing.model.layout.spring.SpringLayoutTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for Swing layouts.
 *
 * @author scheglov_ke
 */
public class LayoutTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.swing.model.layout");
		suite.addTest(createSingleSuite(LayoutManagersTest.class));
		suite.addTest(createSingleSuite(ImplicitLayoutTest.class));
		suite.addTest(createSingleSuite(AbsoluteLayoutTest.class));
		suite.addTest(createSingleSuite(AbsoluteLayoutSelectionActionsTest.class));
		suite.addTest(createSingleSuite(AbsoluteLayoutGefTest.class));
		//suite.addTest(createSingleSuite(ConstraintsAbsoluteLayoutTest.class));
		suite.addTest(createSingleSuite(BorderLayoutTest.class));
		suite.addTest(createSingleSuite(FlowLayoutTest.class));
		suite.addTest(createSingleSuite(FlowLayoutGefTest.class));
		suite.addTest(createSingleSuite(GridLayoutTest.class));
		suite.addTest(createSingleSuite(BoxLayoutTest.class));
		suite.addTest(createSingleSuite(CardLayoutTest.class));
		suite.addTest(createSingleSuite(CardLayoutGefTest.class));
		suite.addTest(FormLayoutTests.suite());
		suite.addTest(MigLayoutTests.suite());
		suite.addTest(GridBagLayoutTests.suite());
		suite.addTest(SpringLayoutTests.suite());
		suite.addTest(GroupLayoutTests.suite());
		//suite.addTest(GEFLayoutTests.suite());
		suite.addTest(createSingleSuite(LayoutGefTest.class));
		return suite;
	}
}
