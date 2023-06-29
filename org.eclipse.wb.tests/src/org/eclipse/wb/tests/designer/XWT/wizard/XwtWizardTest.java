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
package org.eclipse.wb.tests.designer.XWT.wizard;

import org.eclipse.wb.internal.xwt.wizards.XwtWizard;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.jdt.core.IPackageFragment;

/**
 * Tests for {@link XwtWizard}.
 *
 * @author scheglov_ke
 */
public class XwtWizardTest extends XwtModelTest {
	protected IPackageFragment m_packageFragment;

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		m_packageFragment = m_testProject.getPackage("test");
	}

	@Override
	protected void tearDown() throws Exception {
		{
			waitEventLoop(0);
			TestUtils.closeAllEditors();
			waitEventLoop(0);
		}
		super.tearDown();
	}
}