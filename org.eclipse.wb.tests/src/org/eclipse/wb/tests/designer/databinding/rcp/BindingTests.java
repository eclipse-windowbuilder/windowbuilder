/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * @author lobas_av
 *
 */
@Suite
@SelectClasses({
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