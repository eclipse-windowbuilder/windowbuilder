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