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
import org.eclipse.wb.tests.designer.databinding.xwt.BindingTests;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * All XWT tests.
 *
 * @author scheglov_ke
 */
@Ignore
@RunWith(Suite.class)
@SuiteClasses({
		ActivatorTest.class,
		GefTests.class,
		RefactoringTest.class,
		SupportTests.class,
		ModelTests.class,
		WizardTests.class,
		BindingTests.class
})
public class XwtTests {
}
