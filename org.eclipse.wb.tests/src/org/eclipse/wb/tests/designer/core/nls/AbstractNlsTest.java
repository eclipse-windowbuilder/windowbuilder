/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.nls;

import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.After;

/**
 * Abstract test for NLS.
 *
 * @author scheglov_ke
 */
public abstract class AbstractNlsTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@After
	public void tearDown() throws Exception {
		// process UI messages (without this we have exception from Java UI)
		waitEventLoop(1);
		//
		super.tearDown();
		if (m_testProject != null) {
			deleteFiles(m_testProject.getJavaProject().getProject().getFolder("src"));
			waitForAutoBuild();
		}
	}
}
