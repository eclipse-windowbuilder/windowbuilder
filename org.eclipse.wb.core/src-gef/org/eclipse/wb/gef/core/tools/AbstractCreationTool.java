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

import org.eclipse.wb.gef.core.requests.AbstractCreateRequest;
import org.eclipse.wb.internal.gef.core.SharedCursors;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

/**
 * @author lobas_av
 * @coverage gef.core
 */
public abstract class AbstractCreationTool extends TargetingTool {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractCreationTool() {
		setDefaultCursor(SharedCursors.CURSOR_ADD);
		setDisabledCursor(SharedCursors.CURSOR_NO);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// High-Level handle MouseEvent
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean handleButtonDown(int button) {
		if (button == 1) {
			if (m_state == STATE_INITIAL) {
				m_state = STATE_DRAG;
				((AbstractCreateRequest) getTargetRequest()).setLocation(getAbsoluteLocation());
				lockTargetEditPart(getTargetEditPart());
			}
		} else {
			m_state = STATE_INVALID;
			handleInvalidInput();
		}
		return true;
	}

	@Override
	protected boolean handleButtonUp(int button) {
		if (m_state == STATE_DRAG || m_state == STATE_DRAG_IN_PROGRESS) {
			eraseTargetFeedback();
			unlockTargetEditPart();
			executeCommand();
			selectAddedObjects();
		}
		//
		m_state = STATE_TERMINAL;
		handleFinished();
		return true;
	}

	@Override
	protected boolean handleMove() {
		updateTargetRequest();
		updateTargetUnderMouse();
		showTargetFeedback();
		updateCommand();
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
	protected boolean handleDragInProgress() {
		if (m_state == STATE_DRAG_IN_PROGRESS) {
			updateTargetRequest();
			showTargetFeedback();
			updateCommand();
		}
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Request
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a {@link ICreateRequest} and sets this tool's factory on the request.
	 */
	@Override
	protected abstract Request createTargetRequest();

	/**
	 * Sets the location (and size if the user is performing size-on-drop) of the request.
	 */
	@Override
	protected void updateTargetRequest() {
		super.updateTargetRequest();
		AbstractCreateRequest request = (AbstractCreateRequest) getTargetRequest();
		if (m_state == STATE_DRAG_IN_PROGRESS) {
			Point start = getAbsoluteStartLocation();
			Rectangle bounds = new Rectangle(start, getDragMoveDelta());
			request.setLocation(bounds.getLocation());
			request.setSize(bounds.getSize());
		} else {
			request.setSize(null);
			request.setLocation(getAbsoluteLocation());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Add the newly created object to the viewer's selected objects.
	 */
	protected abstract void selectAddedObjects();

	////////////////////////////////////////////////////////////////////////////
	//
	// Handle KeyEvent
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void keyDown(KeyEvent event, EditPartViewer viewer) {
		if (event.keyCode == SWT.ESC) {
			viewer.getEditDomain().loadDefaultTool();
		}
	}
}