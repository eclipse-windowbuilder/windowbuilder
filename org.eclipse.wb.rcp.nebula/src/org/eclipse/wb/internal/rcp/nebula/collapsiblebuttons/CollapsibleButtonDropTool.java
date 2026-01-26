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
package org.eclipse.wb.internal.rcp.nebula.collapsiblebuttons;

import org.eclipse.wb.gef.core.tools.AbstractCreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;

/**
 * {@link Tool} for adding new "button" on {@link CollapsibleButtonsInfo} widget.
 *
 * @author sablin_aa
 * @coverage nebula.gef
 */
public final class CollapsibleButtonDropTool extends AbstractCreationTool {
	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractCreationTool
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Request createTargetRequest() {
		return new CollapsibleButtonDropRequest();
	}

	@Override
	protected void selectAddedObjects() {
		CollapsibleButtonDropRequest request = (CollapsibleButtonDropRequest) getTargetRequest();
		ControlInfo button = request.getButton();
		if (button != null) {
			EditPartViewer viewer = getCurrentViewer();
			EditPart editPart = viewer.getEditPartRegistry().get(button);
			if (editPart != null) {
				viewer.select(editPart);
			}
		}
	}
}
