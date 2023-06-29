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
package org.eclipse.wb.tests.designer.core.model.variables;

import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link VariableSupport}.
 *
 * @author scheglov_ke
 */
public class VariablesTests extends DesignerSuiteTests {
	////////////////////////////////////////////////////////////////////////////
	//
	// Suite
	//
	////////////////////////////////////////////////////////////////////////////
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.core.model.variable");
		suite.addTest(createSingleSuite(AbstractVariableSupportTest.class));
		suite.addTest(createSingleSuite(ThisTest.class));
		suite.addTest(createSingleSuite(ThisForcedMethodTest.class));
		suite.addTest(createSingleSuite(AbstractNamedTest.class));
		suite.addTest(createSingleSuite(AbstractSimpleTest.class));
		suite.addTest(createSingleSuite(LocalUniqueTest.class));
		suite.addTest(createSingleSuite(LocalReuseTest.class));
		suite.addTest(createSingleSuite(FieldUniqueTest.class));
		suite.addTest(createSingleSuite(FieldInitializerTest.class));
		suite.addTest(createSingleSuite(FieldReuseTest.class));
		suite.addTest(createSingleSuite(EmptyTest.class));
		suite.addTest(createSingleSuite(EmptyPureTest.class));
		suite.addTest(createSingleSuite(EmptyInvocationTest.class));
		suite.addTest(createSingleSuite(VoidInvocationTest.class));
		suite.addTest(createSingleSuite(ExposedPropertyTest.class));
		suite.addTest(createSingleSuite(ExposedFieldTest.class));
		suite.addTest(createSingleSuite(LazyTest.class));
		suite.addTest(createSingleSuite(MethodParameterTest.class));
		suite.addTest(createSingleSuite(NamesManagerTest.class));
		suite.addTest(createSingleSuite(TextPropertyRenameTest.class));
		return suite;
	}
}
