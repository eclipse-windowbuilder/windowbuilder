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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * XWT model tests.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		XwtDescriptionProcessorTest.class,
		XwtTagResolverTest.class,
		XwtStringArraySupportTest.class,
		XwtStaticFieldSupportTest.class,
		NameSupportTest.class,
		NamePropertySupportTest.class,
		XwtListenerPropertiesTest.class,
		PropertyTests.class,
		WidgetTests.class,
		LayoutTests.class,
		JFaceTests.class,
		FormsTests.class
})
public class ModelTests {
}