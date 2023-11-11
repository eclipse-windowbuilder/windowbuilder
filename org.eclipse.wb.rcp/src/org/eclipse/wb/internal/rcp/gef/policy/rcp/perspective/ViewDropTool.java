/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.tools.AbstractCreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewInfo;

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
			EditPart editPart = (EditPart) viewer.getEditPartRegistry().get(component);
			if (editPart != null) {
				viewer.select(editPart);
			}
		}
	}
}
