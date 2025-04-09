/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.rcp.gef.policy.jface.action;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.tools.AbstractCreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContributionItemInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;

/**
 * {@link Tool} for adding new {@link ActionInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class ActionDropTool extends AbstractCreationTool {
	private final ActionInfo m_action;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ActionDropTool(ActionInfo action) {
		m_action = action;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractCreationTool
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Request createTargetRequest() {
		return new ActionDropRequest(m_action);
	}

	@Override
	protected void selectAddedObjects() {
		ActionDropRequest request = (ActionDropRequest) getTargetRequest();
		ActionContributionItemInfo item = request.getItem();
		if (item != null) {
			EditPartViewer viewer = getCurrentViewer();
			EditPart editPart = (EditPart) viewer.getEditPartRegistry().get(item);
			if (editPart != null) {
				viewer.select(editPart);
			}
		}
	}
}
