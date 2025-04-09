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
package org.eclipse.wb.tests.designer.databinding.rcp.model;

import org.eclipse.wb.internal.core.databinding.parser.DatabindingRootProcessor;
import org.eclipse.wb.internal.core.databinding.parser.ParseState;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.tests.designer.databinding.rcp.DatabindingTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

/**
 *
 * @author lobas_av
 *
 */
public abstract class AbstractBindingTest extends RcpModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureNewProject() throws Exception {
		super.configureNewProject();
		DatabindingTestUtils.configure(m_testProject);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	protected final DatabindingsProvider getDatabindingsProvider() throws Exception {
		ParseState parseState = DatabindingRootProcessor.STATES.get(m_lastEditor.getModelUnit());
		assertNotNull(parseState);
		assertNotNull(parseState.databindingsProvider);
		assertInstanceOf(DatabindingsProvider.class, parseState.databindingsProvider);
		return (DatabindingsProvider) parseState.databindingsProvider;
	}
}