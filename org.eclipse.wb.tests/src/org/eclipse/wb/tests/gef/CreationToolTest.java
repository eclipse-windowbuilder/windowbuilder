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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;

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
		ICreationFactory factory = new ICreationFactory() {
			@Override
			public void activate() {
			}

			@Override
			public Object getNewObject() {
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