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

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.databinding.rcp.model.CodeGenerationTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.DatabindingsProviderTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.UiConfigurationTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.ViewerCodeGenerationTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.beans.BeanBindableTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.beans.BeanObservableTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.context.BindListTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.context.BindSetTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.context.BindValueTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.widgets.ViewerObservableTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.widgets.WidgetBindableTest;
import org.eclipse.wb.tests.designer.databinding.rcp.model.widgets.WidgetObservableTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author lobas_av
 *
 */
public class BindingTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.rcp.databinding");
    suite.addTest(createSingleSuite(AstModelSupportTest.class));
    suite.addTest(createSingleSuite(UtilsTest.class));
    suite.addTest(createSingleSuite(BeanBindableTest.class));
    suite.addTest(createSingleSuite(WidgetBindableTest.class));
    suite.addTest(createSingleSuite(BeanObservableTest.class));
    suite.addTest(createSingleSuite(WidgetObservableTest.class));
    suite.addTest(createSingleSuite(ViewerObservableTest.class));
    suite.addTest(createSingleSuite(BindValueTest.class));
    suite.addTest(createSingleSuite(BindListTest.class));
    suite.addTest(createSingleSuite(BindSetTest.class));
    suite.addTest(createSingleSuite(DatabindingsProviderTest.class));
    suite.addTest(createSingleSuite(CodeGenerationTest.class));
    suite.addTest(createSingleSuite(ViewerCodeGenerationTest.class));
    suite.addTest(createSingleSuite(UiConfigurationTest.class));
    suite.addTest(createSingleSuite(JFaceDatabindingsFactoryTestRcp.class));
    suite.addTest(createSingleSuite(JFaceDatabindingsFactoryTestSwing.class));
    return suite;
  }
}