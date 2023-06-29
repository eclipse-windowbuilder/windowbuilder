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
package org.eclipse.wb.tests.designer.swing.model;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.swing.model.bean.BeanTests;
import org.eclipse.wb.tests.designer.swing.model.component.ComponentTests;
import org.eclipse.wb.tests.designer.swing.model.layout.LayoutTests;
import org.eclipse.wb.tests.designer.swing.model.property.PropertiesTests;
import org.eclipse.wb.tests.designer.swing.model.top.TopLevelTests;
import org.eclipse.wb.tests.designer.swing.model.util.UtilTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for Swing objects models.
 *
 * @author scheglov_ke
 */
public class ModelTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.swing.model");
		suite.addTest(createSingleSuite(CoordinateUtilsTest.class));
		suite.addTest(LayoutTests.suite());
		suite.addTest(ComponentTests.suite());
		suite.addTest(BeanTests.suite());
		suite.addTest(UtilTests.suite());
		suite.addTest(PropertiesTests.suite());
		suite.addTest(createSingleSuite(ClipboardTest.class));
		suite.addTest(TopLevelTests.suite());
		return suite;
	}
}
