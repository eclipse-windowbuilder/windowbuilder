/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.gef.core.tools;

import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;

import org.eclipse.gef.EditPart;

import java.util.List;

/**
 * A drag tracker that moves {@link EditPart EditParts} only inside parent.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class ParentTargetDragEditPartTracker extends DragEditPartTracker {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ParentTargetDragEditPartTracker(org.eclipse.wb.gef.core.EditPart sourceEditPart) {
		super(sourceEditPart);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// High-Level handle MouseEvent
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean handleButtonUp(int button) {
		if (m_state == STATE_DRAG_IN_PROGRESS) {
			unlockTargetEditPart();
		}
		super.handleButtonUp(button);
		return true;
	}

	@Override
	protected boolean handleDragStarted() {
		super.handleDragStarted();
		if (m_state == STATE_DRAG_IN_PROGRESS) {
			ChangeBoundsRequest request = (ChangeBoundsRequest) getTargetRequest();
			List<? extends EditPart> editParts = request.getEditParts();
			lockTargetEditPart(editParts.get(0).getParent());
		}
		return true;
	}
}