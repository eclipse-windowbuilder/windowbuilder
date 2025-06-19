/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;

import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;

/**
 * A drag tracker used to select {@link EditPart EditParts}.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class SelectEditPartTracker extends TargetingTool {
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
	protected void resetState() {
		super.resetState();
		m_isSelected = false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Cursor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Cursor calculateCursor() {
		return m_state == STATE_INITIAL || m_state == STATE_DRAG
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
				&& m_state == STATE_INITIAL
				&& m_sourceEditPart.getSelected() == EditPart.SELECTED_NONE) {
			performSelection();
		}
		if (button == 1) {
			if (m_state == STATE_INITIAL) {
				m_state = STATE_DRAG;
			}
		} else {
			if (button == 3) {
				m_state = STATE_TERMINAL;
			} else {
				m_state = STATE_INVALID;
			}
			handleInvalidInput();
		}
		return true;
	}

	@Override
	protected boolean handleButtonUp(int button) {
		if (m_state == STATE_DRAG) {
			performSelection();
			m_state = STATE_TERMINAL;
		}
		return true;
	}

	@Override
	protected boolean handleDragStarted() {
		if (m_state == STATE_DRAG) {
			m_state = STATE_DRAG_IN_PROGRESS;
		}
		return true;
	}

	@Override
	protected boolean handleDoubleClick(int button) {
		if (button == 1) {
			SelectionRequest request = new SelectionRequest();
			request.setType(RequestConstants.REQ_OPEN);
			request.setLocation(getAbsoluteLocation());
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
			IEditPartViewer viewer = getCurrentViewer();
			//
			if ((m_stateMask & SWT.CONTROL) != 0) {
				if (viewer.getSelectedEditParts().contains(m_sourceEditPart)) {
					viewer.deselect(m_sourceEditPart);
				} else {
					viewer.appendSelection(m_sourceEditPart);
				}
			} else if ((m_stateMask & SWT.SHIFT) != 0) {
				viewer.appendSelection(m_sourceEditPart);
			} else {
				viewer.select(m_sourceEditPart);
			}
		}
	}
}