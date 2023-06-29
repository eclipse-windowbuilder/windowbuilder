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
package org.eclipse.wb.tests.designer.XWT;

import org.eclipse.wb.tests.designer.XWT.gef.GefTests;
import org.eclipse.wb.tests.designer.XWT.model.ModelTests;
import org.eclipse.wb.tests.designer.XWT.refactoring.RefactoringTest;
import org.eclipse.wb.tests.designer.XWT.support.SupportTests;
import org.eclipse.wb.tests.designer.XWT.wizard.WizardTests;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.databinding.xwt.BindingTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All XWT tests.
 *
 * @author scheglov_ke
 */
public class XwtTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.xwt");
		suite.addTest(createSingleSuite(ActivatorTest.class));
		suite.addTest(GefTests.suite());
		suite.addTest(createSingleSuite(RefactoringTest.class));
		suite.addTest(SupportTests.suite());
		suite.addTest(ModelTests.suite());
		suite.addTest(WizardTests.suite());
		suite.addTest(BindingTests.suite());
		return suite;
	}
}
