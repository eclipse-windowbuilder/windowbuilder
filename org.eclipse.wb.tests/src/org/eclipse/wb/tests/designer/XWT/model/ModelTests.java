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
package org.eclipse.wb.tests.designer.XWT.model;

import org.eclipse.wb.tests.designer.XWT.model.forms.FormsTests;
import org.eclipse.wb.tests.designer.XWT.model.jface.JFaceTests;
import org.eclipse.wb.tests.designer.XWT.model.layout.LayoutTests;
import org.eclipse.wb.tests.designer.XWT.model.property.PropertyTests;
import org.eclipse.wb.tests.designer.XWT.model.widgets.WidgetTests;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * XWT model tests.
 *
 * @author scheglov_ke
 */
public class ModelTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.xwt.model");
		suite.addTest(createSingleSuite(XwtDescriptionProcessorTest.class));
		suite.addTest(createSingleSuite(XwtTagResolverTest.class));
		suite.addTest(createSingleSuite(XwtStringArraySupportTest.class));
		suite.addTest(createSingleSuite(XwtStaticFieldSupportTest.class));
		suite.addTest(createSingleSuite(NameSupportTest.class));
		suite.addTest(createSingleSuite(NamePropertySupportTest.class));
		suite.addTest(createSingleSuite(XwtListenerPropertiesTest.class));
		suite.addTest(PropertyTests.suite());
		suite.addTest(WidgetTests.suite());
		suite.addTest(LayoutTests.suite());
		suite.addTest(JFaceTests.suite());
		suite.addTest(FormsTests.suite());
		return suite;
	}
}