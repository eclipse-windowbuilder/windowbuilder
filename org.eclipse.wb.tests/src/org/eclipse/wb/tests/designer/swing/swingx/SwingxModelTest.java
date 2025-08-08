/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.swing.swingx;

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

/**
 * Abstract super class for SwingX tests.
 *
 * @author sablin_aa
 */
public abstract class SwingxModelTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Configures created project.
	 */
	@Override
	protected void configureNewProject() throws Exception {
		super.configureNewProject();
		m_testProject.addBundleJars("org.eclipse.wb.tests", "/libs/swingx");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Source
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the source for Swing.
	 */
	@Override
	public String getTestSource(String... lines) {
		lines = CodeUtils.join(new String[]{// filler
		"import org.jdesktop.swingx.*;"},
				lines);
		return super.getTestSource(lines);
	}
}