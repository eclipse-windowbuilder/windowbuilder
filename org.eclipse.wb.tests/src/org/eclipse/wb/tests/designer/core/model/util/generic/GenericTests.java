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
package org.eclipse.wb.tests.designer.core.model.util.generic;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test for generic, description driven features.
 *
 * @author scheglov_ke
 */
public class GenericTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.core.model.util.generic");
		suite.addTest(createSingleSuite(CopyPropertyTopChildTest.class));
		suite.addTest(createSingleSuite(CopyPropertyTopTest.class));
		suite.addTest(createSingleSuite(ModelMethodPropertyTest.class));
		suite.addTest(createSingleSuite(ModelMethodPropertyChildTest.class));
		return suite;
	}
}
