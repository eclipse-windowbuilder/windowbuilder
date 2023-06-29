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
package org.eclipse.wb.tests.designer.core.util.ast;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author scheglov_ke
 */
public class AstTests extends DesignerSuiteTests {
	////////////////////////////////////////////////////////////////////////////
	//
	// Suite
	//
	////////////////////////////////////////////////////////////////////////////
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.core.utils.ast");
		suite.addTest(createSingleSuite(AstEditorTest.class));
		suite.addTest(createSingleSuite(GathererTest.class));
		suite.addTest(createSingleSuite(AstNodeUtilsTest.class));
		suite.addTest(createSingleSuite(AstReflectionUtilsTest.class));
		suite.addTest(createSingleSuite(BindingsTest.class));
		suite.addTest(createSingleSuite(AstVisitorExTest.class));
		return suite;
	}
}
