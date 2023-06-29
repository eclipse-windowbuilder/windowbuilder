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
package org.eclipse.wb.tests.designer.core.model.generic;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for generic simple/flow containers support.
 *
 * @author scheglov_ke
 */
public class GenericTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.core.model.generic");
		suite.addTest(createSingleSuite(ContainerObjectValidatorsTest.class));
		suite.addTest(createSingleSuite(FlowContainerModelTest.class));
		suite.addTest(createSingleSuite(FlowContainerGefTest.class));
		suite.addTest(createSingleSuite(FlowContainerLayoutGefTest.class));
		suite.addTest(createSingleSuite(FlowContainerGroupGefTest.class));
		suite.addTest(createSingleSuite(SimpleContainerModelTest.class));
		suite.addTest(createSingleSuite(SimpleContainerGefTest.class));
		suite.addTest(createSingleSuite(SimpleContainerLayoutGefTest.class));
		suite.addTest(createSingleSuite(FlipBooleanPropertyGefTest.class));
		suite.addTest(createSingleSuite(DblClickRunScriptEditPolicyTest.class));
		suite.addTest(createSingleSuite(OpenListenerEditPolicyTest.class));
		return suite;
	}
}
