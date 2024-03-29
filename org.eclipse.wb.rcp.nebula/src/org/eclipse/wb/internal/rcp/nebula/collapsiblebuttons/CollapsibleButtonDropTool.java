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
package org.eclipse.wb.internal.rcp.nebula.collapsiblebuttons;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.tools.AbstractCreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

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
			EditPart editPart = (EditPart) viewer.getEditPartRegistry().get(button);
			if (editPart != null) {
				viewer.select(editPart);
			}
		}
	}
}
