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
package org.eclipse.wb.internal.gef.tree.tools;

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.tools.AbstractTool;

/**
 * Special {@link Tool} for handle only double-click mouse event and route it to {@link EditPart}.
 * This need for tree edit part's that it's not contains special tools for handle selection (via
 * SelectEditPartTracker).
 *
 * @author lobas_av
 * @coverage gef.tree
 */
public class DoubleClickEditPartTracker extends AbstractTool implements DragTracker {
	private final EditPart m_sourceEditPart;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DoubleClickEditPartTracker(EditPart sourceEditPart) {
		m_sourceEditPart = sourceEditPart;
	}

	@Override
	protected String getCommandName() {
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// High-Level handle MouseEvent
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean handleDoubleClick(int button) {
		if (button == 1) {
			SelectionRequest request = new SelectionRequest();
			request.setType(RequestConstants.REQ_OPEN);
			request.setLocation(getLocation());
			m_sourceEditPart.performRequest(request);
		}
		return true;
	}
}