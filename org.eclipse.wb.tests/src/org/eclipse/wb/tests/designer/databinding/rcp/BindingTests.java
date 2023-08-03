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
package org.eclipse.wb.tests.designer.databinding.rcp;

import org.eclipse.wb.tests.designer.databinding.rcp.model.CodeGenerationTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.DatabindingsProviderTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.ReferenceProvidersTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.UiConfigurationTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.beans.BeanBindableTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.beans.BeanObservableTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.context.BindListTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.context.BindSetTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.context.BindValueTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.widgets.ViewerObservableTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.widgets.WidgetBindableTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.widgets.WidgetObservableTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author lobas_av
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
		AstModelSupportTest.class,
		UtilsTest.class,
		BeanBindableTest.class,
		WidgetBindableTest.class,
		BeanObservableTest.class,
		WidgetObservableTest.class,
		ViewerObservableTest.class,
		BindValueTest.class,
		BindListTest.class,
		BindSetTest.class,
		DatabindingsProviderTest.class,
		ReferenceProvidersTest.class,
		CodeGenerationTest.class,
		UiConfigurationTest.class,
		JFaceDatabindingsFactoryTestRcp.class,
		JFaceDatabindingsFactoryTestSwing.class,
		org.eclipse.wb.tests.designer.databinding.swing.BindingTests.class
})
public class BindingTests {
}