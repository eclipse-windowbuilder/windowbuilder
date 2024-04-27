/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.gef.tree.dnd;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPartViewer.Conditional;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lobas_av
 * @coverage gef.tree
 */
public class TreeDropListener implements DropTargetListener {
	private final IEditPartViewer m_viewer;
	private DropTargetEvent m_currentEvent;
	private EditPart m_target;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TreeDropListener(IEditPartViewer viewer) {
		m_viewer = viewer;
		// add DND listener
		DropTarget target = new DropTarget(m_viewer.getControl(), DND.DROP_MOVE);
		target.setTransfer(new Transfer[]{TreeTransfer.INSTANCE});
		target.addDropListener(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DropTargetListener
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void dragEnter(DropTargetEvent event) {
		m_currentEvent = event;
	}

	@Override
	public void dropAccept(DropTargetEvent event) {
		m_currentEvent = event;
	}

	@Override
	public void dragLeave(DropTargetEvent event) {
		m_currentEvent = event;
		clearState();
	}

	@Override
	public void dragOperationChanged(DropTargetEvent event) {
		m_currentEvent = event;
		eraseTargetFeedback();
		updateTargetRequest();
		updateTargetEditPart();
		updateTargetRequestAfter();
		updateCommand();
	}

	@Override
	public void dragOver(DropTargetEvent event) {
		boolean needUpdateFeedback =
				!m_isShowingFeedback || event.x != m_currentEvent.x || event.y != m_currentEvent.y;
		m_currentEvent = event;
		updateTargetRequest();
		updateTargetEditPart();
		updateTargetRequestAfter();
		updateCommand();
		if (needUpdateFeedback) {
			showTargetFeedback();
		}
		m_currentEvent.feedback =
				DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL | getTargetRequest().getDNDFeedback();
	}

	@Override
	public void drop(DropTargetEvent event) {
		m_currentEvent = event;
		List<Object> models = getModels(getDragSource());
		eraseTargetFeedback();
		updateTargetRequest();
		updateTargetEditPart();
		updateTargetRequestAfter();
		try {
			executeCommand();
		} finally {
			clearState();
		}
		resetSelectionFromModels(models);
	}

	private void clearState() {
		eraseTargetFeedback();
		m_currentEvent = null;
		m_request = null;
		m_target = null;
	}

	private List<EditPart> getDragSource() {
		return m_viewer.getSelectedEditParts();
	}

	private List<Object> getModels(List<EditPart> editParts) {
		List<Object> models = new ArrayList<>();
		for (EditPart editPart : editParts) {
			models.add(editPart.getModel());
		}
		return models;
	}

	/**
	 * Selection {@link EditPart} by model's.
	 */
	private void resetSelectionFromModels(List<Object> models) {
		if (!models.isEmpty()) {
			// prepare new EditPart's
			List<EditPart> newEditParts = new ArrayList<>();
			for (Object model : models) {
				EditPart newEditPart = (EditPart) m_viewer.getEditPartRegistry().get(model);
				newEditParts.add(newEditPart);
			}
			// set new selection
			m_viewer.setSelection(newEditParts);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Request
	//
	////////////////////////////////////////////////////////////////////////////
	private ChangeBoundsRequest m_request;

	/**
	 * Lazily creates and returns the request used when communicating with the target editpart.
	 */
	private ChangeBoundsRequest getTargetRequest() {
		if (m_request == null) {
			m_request = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
			m_request.setEditParts(getDragSource());
		}
		return m_request;
	}

	/**
	 * Sets the location of the request.
	 */
	private void updateTargetRequest() {
		ChangeBoundsRequest request = getTargetRequest();
		request.setLocation(getDropLocation());
	}

	/**
	 * Sets the type of the request.
	 */
	private void updateTargetRequestAfter() {
		ChangeBoundsRequest request = getTargetRequest();
		if (getDragSource().get(0).getParent() == m_target) {
			request.setType(RequestConstants.REQ_MOVE);
		} else {
			request.setType(RequestConstants.REQ_ADD);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Target
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the target {@link EditPart}.
	 */
	private void setTargetEditPart(EditPart target) {
		if (m_target != target) {
			if (m_target != null) {
				eraseTargetFeedback();
			}
			m_target = target;
		}
	}

	/**
	 * Updates the target {@link EditPart}. The target is updated by using the target conditional and
	 * the target request.
	 */
	private void updateTargetEditPart() {
		Point location = getDropLocation();
		EditPart editPart =
				m_viewer.findTargetEditPart(
						location.x,
						location.y,
						includeChildren(getDragSource()),
						getTargetingConditional());
		if (editPart != null) {
			editPart = editPart.getTargetEditPart(getTargetRequest());
		}
		setTargetEditPart(editPart);
	}

	/**
	 * Returns the current x, y <b>*absolute*</b> position of the mouse cursor.
	 */
	private Point getDropLocation() {
		DropTarget target = (DropTarget) m_currentEvent.widget;
		org.eclipse.swt.graphics.Point location =
				target.getControl().toControl(m_currentEvent.x, m_currentEvent.y);
		return new Point(location);
	}

	/**
	 * Returns the conditional object used for obtaining the target editpart from the viewer. By
	 * default, a conditional is returned that tests whether an editpart at the current mouse location
	 * indicates a target for the operation's request, using
	 * {@link EditPart#getTargetEditPart(Request)}. If <code>null</code> is returned, then the
	 * conditional fails, and the search continues.
	 */
	private Conditional getTargetingConditional() {
		return editPart -> editPart.getTargetEditPart(getTargetRequest()) != null;
	}

	private static List<EditPart> includeChildren(List<EditPart> parts) {
		List<EditPart> result = new ArrayList<>();
		for (EditPart editPart : parts) {
			result.add(editPart);
			result.addAll(includeChildren(editPart.getChildren()));
		}
		return result;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Command
	//
	////////////////////////////////////////////////////////////////////////////
	private Command m_command;

	/**
	 * Execute the currently active command.
	 */
	private void executeCommand() {
		if (m_command != null) {
			try {
				m_viewer.getEditDomain().executeCommand(m_command);
			} finally {
				setCommand(null);
			}
		}
	}

	/**
	 * Sets the currently active command.
	 */
	private void setCommand(Command command) {
		m_command = command;
		m_currentEvent.detail = m_command == null ? DND.DROP_NONE : DND.DROP_MOVE;
	}

	/**
	 * Updates currently command.
	 */
	private void updateCommand() {
		setCommand(getCommand());
	}

	/**
	 * Returns a new, updated command based on the tools current properties.
	 */
	private Command getCommand() {
		if (m_target != null) {
			return m_target.getCommand(getTargetRequest());
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedback
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean m_isShowingFeedback;

	/**
	 * Asks the target editpart to show target feedback and sets the target feedback flag.
	 */
	private void showTargetFeedback() {
		if (m_target != null) {
			m_target.showTargetFeedback(getTargetRequest());
		}
		//
		m_isShowingFeedback = true;
	}

	/**
	 * Asks the current target editpart to erase target feedback using the target request. If target
	 * feedback is not being shown, this method does nothing and returns. Otherwise, the target
	 * feedback flag is reset to false, and the target editpart is asked to erase target feedback.
	 * This methods should rarely be overridden.
	 */
	private void eraseTargetFeedback() {
		if (m_isShowingFeedback) {
			m_isShowingFeedback = false;
			//
			if (m_target != null) {
				m_target.eraseTargetFeedback(getTargetRequest());
			}
		}
	}
}