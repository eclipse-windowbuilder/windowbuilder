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
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;

/**
 * {@link Tool} to drop new view.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class ViewDropTool extends AbstractCreationTool {
	private final ViewInfo m_view;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewDropTool(ViewInfo view) {
		m_view = view;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractCreationTool
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Request createTargetRequest() {
		return new ViewDropRequest(m_view);
	}

	@Override
	protected void selectAddedObjects() {
		ViewDropRequest request = (ViewDropRequest) getTargetRequest();
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
