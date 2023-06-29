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
package org.eclipse.wb.tests.designer.core.model.util;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.core.model.util.generic.GenericTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author scheglov_ke
 */
public class UtilTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.core.model.util");
		suite.addTest(createSingleSuite(ExposeComponentSupportTest.class));
		suite.addTest(FactoryActionsTests.suite());
		suite.addTest(createSingleSuite(JavaInfoUtilsTest.class));
		suite.addTest(createSingleSuite(ObjectInfoUtilsTest.class));
		suite.addTest(createSingleSuite(GenericTypeResolverJavaInfoTest.class));
		suite.addTest(createSingleSuite(TemplateUtilsTest.class));
		suite.addTest(createSingleSuite(ScriptUtilsTest.class));
		suite.addTest(createSingleSuite(MethodOrderTest.class));
		suite.addTest(createSingleSuite(ComponentOrderTest.class));
		suite.addTest(createSingleSuite(MorphingSupportTest.class));
		suite.addTest(createSingleSuite(ObjectsLabelProviderTest.class));
		suite.addTest(createSingleSuite(ObjectsTreeContentProviderTest.class));
		suite.addTest(createSingleSuite(RenameConvertSupportTest.class));
		suite.addTest(createSingleSuite(PredicatesTest.class));
		suite.addTest(createSingleSuite(StackContainerSupportTest.class));
		suite.addTest(GenericTests.suite());
		return suite;
	}
}
