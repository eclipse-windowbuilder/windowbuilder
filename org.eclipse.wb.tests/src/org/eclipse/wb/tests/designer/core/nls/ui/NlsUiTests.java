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
package org.eclipse.wb.tests.designer.core.nls.ui;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author scheglov_ke
 */
public class NlsUiTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.core.nls.ui");
		suite.addTest(createSingleSuite(FlagRepositoryTest.class));
		suite.addTest(createSingleSuite(LocaleUtilsTest.class));
		suite.addTest(createSingleSuite(ContributionItemTest.class));
		suite.addTest(createSingleSuite(NlsDialogTest.class));
		suite.addTest(createSingleSuite(SourceCompositeTest.class));
		suite.addTest(createSingleSuite(PropertiesCompositeTest.class));
		suite.addTest(createSingleSuite(NewSourceDialogTest.class));
		return suite;
	}
}
