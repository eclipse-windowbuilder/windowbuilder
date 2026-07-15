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
package org.eclipse.wb.gef.core.tools;

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.swt.graphics.Cursor;

/**
 * A drag tracker used to select {@link EditPart EditParts}.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class SelectEditPartTracker extends TargetingTool implements DragTracker {
	private final EditPart m_sourceEditPart;
	private boolean m_isSelected;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SelectEditPartTracker(EditPart sourceEditPart) {
		m_sourceEditPart = sourceEditPart;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Drop Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void resetFlags() {
		super.resetFlags();
		m_isSelected = false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Cursor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Cursor calculateCursor() {
		return isInState(STATE_INITIAL) || isInState(STATE_DRAG)
				? getDefaultCursor()
						: super.calculateCursor();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// High-Level handle MouseEvent
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean handleButtonDown(int button) {
		if ((button == 1 || button == 3)
				&& isInState(STATE_INITIAL)
				&& m_sourceEditPart.getSelected() == EditPart.SELECTED_NONE) {
			performSelection();
		}
		if (button == 1) {
			if (isInState(STATE_INITIAL)) {
				setState(STATE_DRAG);
			}
		} else {
			if (button == 3) {
				setState(STATE_TERMINAL);
			} else {
				setState(STATE_INVALID);
			}
			handleInvalidInput();
		}
		return true;
	}

	@Override
	protected boolean handleButtonUp(int button) {
		if (isInState(STATE_DRAG)) {
			performSelection();
			setState(STATE_TERMINAL);
		}
		return true;
	}

	@Override
	protected boolean handleDragStarted() {
		if (isInState(STATE_DRAG)) {
			setState(STATE_DRAG_IN_PROGRESS);
		}
		return true;
	}

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

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Performs the appropriate selection action based on the selection state of the source and the
	 * modifiers (CTRL and SHIFT). If no modifier key is pressed, the source will be set as the only
	 * selection. If the CTRL key is pressed and the edit part is already selected, it will be
	 * deselected. If the CTRL key is pressed and the edit part is not selected, it will be appended
	 * to the selection set. If the SHIFT key is pressed, the source will be appended to the
	 * selection.
	 */
	private void performSelection() {
		if (!m_isSelected) {
			m_isSelected = true;
			EditPartViewer viewer = getCurrentViewer();
			//
			if (getCurrentInput().isControlKeyDown()) {
				if (viewer.getSelectedEditParts().contains(m_sourceEditPart)) {
					viewer.deselect(m_sourceEditPart);
				} else {
					viewer.appendSelection(m_sourceEditPart);
				}
			} else if (getCurrentInput().isShiftKeyDown()) {
				viewer.appendSelection(m_sourceEditPart);
			} else {
				viewer.select(m_sourceEditPart);
			}
		}
	}
}