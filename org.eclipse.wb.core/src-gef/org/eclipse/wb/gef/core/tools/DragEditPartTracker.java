/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.gef.core.tools;

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.DragPermissionRequest;
import org.eclipse.wb.internal.gef.core.IObjectInfoEditPart;
import org.eclipse.wb.internal.gef.core.SharedCursors;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer.Conditional;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.GroupRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A drag tracker that moves {@link EditPart EditParts}.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class DragEditPartTracker extends SelectEditPartTracker {
	private boolean m_canMove;
	private boolean m_canReparent;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DragEditPartTracker(org.eclipse.wb.gef.core.EditPart sourceEditPart) {
		super(sourceEditPart);
		setDefaultCursor(SharedCursors.CURSOR_MOVE);
		setDisabledCursor(SharedCursors.CURSOR_NO);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tool
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void deactivate() {
		super.deactivate();
		m_exclusionSet = null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// High-Level handle MouseEvent
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean handleButtonUp(int button) {
		if (m_state == STATE_DRAG_IN_PROGRESS) {
			// prepare models if restoring selection
			List<Object> models = getOperationSetModels();
			eraseTargetFeedback();
			executeCommand();
			m_state = STATE_TERMINAL;
			// restore selection
			restoreSelectionFromModels(models);
		} else {
			super.handleButtonUp(button);
		}
		return true;
	}

	@Override
	protected boolean handleDragStarted() {
		super.handleDragStarted();
		if (m_state == STATE_DRAG_IN_PROGRESS) {
			updateTargetRequest();
			updateTargetUnderMouse();
		}
		return true;
	}

	@Override
	protected boolean handleDragInProgress() {
		if (m_state == STATE_DRAG_IN_PROGRESS) {
			updateTargetRequest();
			updateTargetUnderMouse();
			showTargetFeedback();
			updateCommand();
		}
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handling operations
	//
	////////////////////////////////////////////////////////////////////////////
	private Collection<org.eclipse.wb.gef.core.EditPart> m_exclusionSet;

	/**
	 * Returns a list of all the edit parts in the {@link Tool#getOperationSet() operation set}.
	 */
	@Override
	protected Collection<org.eclipse.wb.gef.core.EditPart> getExclusionSet() {
		if (m_exclusionSet == null) {
			m_exclusionSet = new ArrayList<>(getOperationSet());
		}
		return m_exclusionSet;
	}

	/**
	 * Returns "all ignore" {@link Conditional} if <code>OperationSet</code> is empty.
	 */
	@Override
	protected Conditional getTargetingConditional() {
		if (!getCurrentViewer().getSelectedEditParts().isEmpty() && getOperationSet().isEmpty()) {
			return editPart -> false;
		}
		return super.getTargetingConditional();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// OperationSet
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a {@link List} of top-level edit parts excluding dependants that understand the current
	 * target request.
	 */
	@Override
	protected List<org.eclipse.wb.gef.core.EditPart> createOperationSet() {
		// extract OperationSet from selection
		List<org.eclipse.wb.gef.core.EditPart> operationSet = ToolUtilities.getSelectionWithoutDependants(getCurrentViewer());
		// check understandsRequest() from parent
		// FIXME Kosta.20071115 I don't understand, why we should ask parent _here_
		// Yes, if parent does not support move, we should not allow operation.
		// However even if parent does not support move, we still can _reparent_ from it on different parent.
		{
			// XXX just to set "operationSet", probably there is better place
			ChangeBoundsRequest request = (ChangeBoundsRequest) getTargetRequest();
			request.setEditParts(operationSet);
		}
		/*if (!operationSet.isEmpty()) {
    	EditPart firstPart = operationSet.get(0);
    	ChangeBoundsRequest request = (ChangeBoundsRequest) getTargetRequest();
    	request.setEditParts(operationSet);
    	//
    	if (!firstPart.getParent().understandsRequest(request)) {
    		return Collections.emptyList();
    	}
    }*/
		// check permission for move and reparenting
		{
			DragPermissionRequest request = new DragPermissionRequest();
			for (org.eclipse.wb.gef.core.EditPart editPart : operationSet) {
				editPart.performRequest(request);
			}
			m_canMove = request.canMove();
			m_canReparent = request.canReparent();
		}
		//
		return operationSet;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Request
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a {@link ChangeBoundsRequest}. The type is {@link Request#REQ_MOVE}.
	 */
	@Override
	protected Request createTargetRequest() {
		return new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
	}

	/**
	 * Updates the request with the current {@link Tool#getOperationSet() operation set}, move delta,
	 * location and type.
	 */
	@Override
	protected void updateTargetRequest() {
		super.updateTargetRequest();
		ChangeBoundsRequest request = (ChangeBoundsRequest) getTargetRequest();
		request.setEditParts(getOperationSet());
		request.setMoveDelta(new Point(getDragMoveDelta()));
		request.setLocation(getLocation());
	}

	@Override
	protected void updateTargetRequest(EditPart target) {
		super.updateTargetRequest(target);
		ChangeBoundsRequest request = (ChangeBoundsRequest) getTargetRequest();
		List<? extends EditPart> editParts = request.getEditParts();
		if (!editParts.isEmpty()) {
			if (editParts.get(0).getParent() == target) {
				request.setType(RequestConstants.REQ_MOVE);
			} else {
				request.setType(RequestConstants.REQ_ADD);
			}
		}
	}

	/**
	 * @return <code>true</code> if current {@link #getTargetRequest()} has {@link Request#REQ_MOVE}
	 *         type.
	 */
	private boolean isMove() {
		return getTargetRequest().getType() == RequestConstants.REQ_MOVE;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Command
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Return collection model's of operation set.
	 */
	private List<Object> getOperationSetModels() {
		List<Object> models = new ArrayList<>();
		for (org.eclipse.wb.gef.core.EditPart part : getOperationSet()) {
			models.add(part.getModel());
		}
		return models;
	}

	/**
	 * Select {@link EditPart} by their model's.
	 */
	private void restoreSelectionFromModels(List<Object> models) {
		if (models != null) {
			IEditPartViewer viewer = getCurrentViewer();
			// prepare new EditPart's
			List<org.eclipse.wb.gef.core.EditPart> newEditParts = new ArrayList<>();
			for (Object model : models) {
				org.eclipse.wb.gef.core.EditPart newEditPart = (org.eclipse.wb.gef.core.EditPart) viewer.getEditPartRegistry().get(model);
				if (newEditPart != null) {
					newEditParts.add(newEditPart);
				}
			}
			// set new selection
			viewer.setSelection(newEditParts);
		}
	}

	@Override
	protected Command getCommand() {
		Request request = getTargetRequest();
		List<org.eclipse.wb.gef.core.EditPart> operationSet = getOperationSet();
		//
		if (isMove()) {
			if (m_canMove && !operationSet.isEmpty()) {
				// if move get command from parent
				org.eclipse.wb.gef.core.EditPart firstPart = operationSet.get(0);
				//
				return firstPart.getParent().getCommand(request);
			}
		} else if (m_canReparent) {
			// if change parent get command from new parent and notify old parent for orphans
			EditPart targetEditPart = getTargetEditPart();
			//
			if (targetEditPart != null) {
				CompoundCommand compoundCommand = new CompoundCommand();
				if (targetEditPart instanceof IObjectInfoEditPart objectInfoEditPart) {
					compoundCommand = objectInfoEditPart.createCompoundCommand();
				}
				if (!operationSet.isEmpty()) {
					org.eclipse.wb.gef.core.EditPart firstPart = operationSet.get(0);
					GroupRequest orphanRequest = new GroupRequest(RequestConstants.REQ_ORPHAN);
					orphanRequest.setEditParts(operationSet);
					compoundCommand.add(firstPart.getParent().getCommand(orphanRequest));
				}
				Command reparentCommand = targetEditPart.getCommand(request);
				if (reparentCommand != null) {
					compoundCommand.add(reparentCommand);
					return compoundCommand;
				}
			}
		}
		return null;
	}
}