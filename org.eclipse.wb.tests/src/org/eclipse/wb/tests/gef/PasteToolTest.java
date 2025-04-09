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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.tools.PasteTool;

/**
 * @author lobas_av
 *
 */
public class PasteToolTest extends AbstractCreationToolTest {

	////////////////////////////////////////////////////////////////////////////
	//
	// SetUp
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureTestCase() {
		// set PasteTool
		Object memento = "TestMemento";
		m_tool = new PasteTool(memento) {
			@Override
			protected void selectAddedObjects() {
			}
		};
		m_domain.setActiveTool(m_tool);
		// create request
		m_request = new PasteRequest(memento);
	}
}