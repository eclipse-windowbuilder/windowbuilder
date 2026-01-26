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
package org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective;

import org.eclipse.wb.gef.core.tools.AbstractCreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.PerspectiveInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;

/**
 * {@link Tool} to drop new perspective.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class PerspectiveDropTool extends AbstractCreationTool {
	private final PerspectiveInfo m_perspective;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PerspectiveDropTool(PerspectiveInfo perspective) {
		m_perspective = perspective;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractCreationTool
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Request createTargetRequest() {
		return new PerspectiveDropRequest(m_perspective);
	}

	@Override
	protected void selectAddedObjects() {
		PerspectiveDropRequest request = (PerspectiveDropRequest) getTargetRequest();
		Object component = request.getComponent();
		if (component != null) {
			EditPartViewer viewer = getCurrentViewer();
			EditPart editPart = viewer.getEditPartRegistry().get(component);
			if (editPart != null) {
				viewer.select(editPart);
			}
		}
	}
}
