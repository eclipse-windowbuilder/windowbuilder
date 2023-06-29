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
package org.eclipse.wb.internal.rcp.gef.policy.jface;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.tools.AbstractCreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.rcp.model.jface.DialogInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * {@link Tool} for adding new "button" on {@link DialogInfo} button bar.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class DialogButtonDropTool extends AbstractCreationTool {
	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractCreationTool
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Request createTargetRequest() {
		return new DialogButtonDropRequest();
	}

	@Override
	protected void selectAddedObjects() {
		DialogButtonDropRequest request = (DialogButtonDropRequest) getTargetRequest();
		ControlInfo button = request.getButton();
		if (button != null) {
			IEditPartViewer viewer = getViewer();
			EditPart editPart = viewer.getEditPartByModel(button);
			if (editPart != null) {
				viewer.select(editPart);
			}
		}
	}
}
