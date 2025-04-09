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
package org.eclipse.wb.tests.designer.rcp.nebula;

import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

/**
 * Superclass for Nebula tests.
 *
 * @author sablin_aa
 */
public abstract class AbstractNebulaTest extends RcpModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureNewProject() throws Exception {
		super.configureNewProject();
		m_testProject.addPlugin("org.eclipse.nebula.widgets.collapsiblebuttons");
		m_testProject.addPlugin("org.eclipse.nebula.widgets.gallery");
		m_testProject.addPlugin("org.eclipse.nebula.widgets.ganttchart");
		m_testProject.addPlugin("org.eclipse.nebula.widgets.grid");
		m_testProject.addPlugin("org.eclipse.nebula.widgets.pshelf");
	}
}