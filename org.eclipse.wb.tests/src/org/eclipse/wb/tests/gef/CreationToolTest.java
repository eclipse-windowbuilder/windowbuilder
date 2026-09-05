/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.tools.CreationTool;

import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.requests.SimpleFactory;

/**
 * @author lobas_av
 *
 */
public class CreationToolTest extends AbstractCreationToolTest {

	////////////////////////////////////////////////////////////////////////////
	//
	// SetUp
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureTestCase() {
		// create test factory
		CreationFactory factory = new SimpleFactory<>(String.class) {
			@Override
			public String getNewObject() {
				return "_NewObject_";
			}

			@Override
			public String toString() {
				return "TestFactory";
			}
		};
		// set CreationTool
		m_tool = new CreationTool(factory);
		m_domain.setActiveTool(m_tool);
		// create request
		m_request = new CreateRequest(factory);
	}
}