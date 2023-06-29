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
package org.eclipse.wb.tests.designer.core.model.creation;

import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link FactoryDescriptionHelper} and static/instance factories.
 *
 * @author scheglov_ke
 */
public class CreationsTests extends DesignerSuiteTests {
	////////////////////////////////////////////////////////////////////////////
	//
	// Suite
	//
	////////////////////////////////////////////////////////////////////////////
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.core.model.creation");
		// creations
		suite.addTest(createSingleSuite(ThisCreationSupportTest.class));
		suite.addTest(createSingleSuite(ConstructorCreationSupportTest.class));
		suite.addTest(createSingleSuite(ExposedPropertyCreationSupportTest.class));
		suite.addTest(createSingleSuite(ExposedFieldCreationSupportTest.class));
		suite.addTest(createSingleSuite(SuperInvocationCreationSupportTest.class));
		// factories
		suite.addTest(FactoriesTests.suite());
		// other
		suite.addTest(createSingleSuite(InvocationChainCreationSupportTest.class));
		suite.addTest(createSingleSuite(ICreationSupportPermissionsTest.class));
		suite.addTest(createSingleSuite(OpaqueCreationSupportTest.class));
		return suite;
	}
}
