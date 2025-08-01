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
package org.eclipse.wb.gef.graphical.tools;

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.tools.TargetingTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.internal.gef.core.EditDomain;
import org.eclipse.wb.internal.gef.graphical.GraphicalViewer;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.EditPartViewer.Conditional;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Event;

import java.util.List;

/**
 * Tool to select and manipulate figures. A selection tool is in one of three states, e.g.,
 * background selection, figure selection, handle manipulation. The different states are handled by
 * different child tools.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class SelectionTool extends TargetingTool {
	private DragTracker m_dragTracker;

	////////////////////////////////////////////////////////////////////////////
	//
	// DragTracker
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the drag tracker {@link Tool} for this {@link SelectionTool}. If the current drag tracker
	 * is not <code>null</code>, this method deactivates it. If the new drag tracker is not
	 * <code>null</code>, this method will activate it and set the {@link EditDomain} and
	 * {@link IEditPartViewer}.
	 */
	public void setDragTracker(DragTracker dragTracker) {
		if (m_dragTracker != dragTracker) {
			if (m_dragTracker != null) {
				m_dragTracker.deactivate();
			}
			//
			m_dragTracker = dragTracker;
			refreshCursor();
			//
			if (m_dragTracker != null) {
				m_dragTracker.setEditDomain(getDomain());
				m_dragTracker.setViewer(getCurrentViewer());
				m_dragTracker.activate();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tool
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Deactivates the tool. This method is called whenever the user switches to another tool. Use
	 * this method to do some clean-up when the tool is switched. Sets the drag tracker to
	 * <code>null</code>.
	 */
	@Override
	public void deactivate() {
		if (m_dragTracker != null) {
			m_dragTracker.deactivate();
			m_dragTracker = null;
		}
		super.deactivate();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Cursor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * If there is a drag tracker, this method does nothing so that the drag tracker can take care of
	 * the cursor. Otherwise, calls <code>super</code>.
	 */
	@Override
	public void refreshCursor() {
		// If we have a DragTracker, let it control the Cursor
		if (m_dragTracker == null) {
			super.refreshCursor();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// High-Level handle MouseEvent
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean handleButtonDown(int button) {
		if (m_state == STATE_INITIAL) {
			m_state = STATE_DRAG;
			//
			if (m_dragTracker != null) {
				m_dragTracker.deactivate();
			}
			//
			if ((m_stateMask & SWT.ALT) != 0) {
				setDragTracker(new MarqueeDragTracker());
				return true;
			}
			//
			Point current = getLocation();
			if (getCurrentViewer() instanceof GraphicalViewer gv) {
				Handle handle = (Handle) gv.findHandleAt(current);
				if (handle != null) {
					setDragTracker(handle.getDragTracker());
					return true;
				}
			}
			//
			updateTargetRequest();
			((SelectionRequest) getTargetRequest()).setLastButtonPressed(button);
			updateTargetUnderMouse();
			//
			EditPart editPart = getTargetEditPart();
			if (editPart == null) {
				setDragTracker(null);
				getCurrentViewer().deselectAll();
			} else {
				setDragTracker(editPart.getDragTracker(getTargetRequest()));
				lockTargetEditPart(editPart);
			}
		}
		return true;
	}

	@Override
	protected boolean handleButtonUp(int button) {
		((SelectionRequest) getTargetRequest()).setLastButtonPressed(0);
		setDragTracker(null);
		m_state = STATE_INITIAL;
		unlockTargetEditPart();
		return true;
	}

	@Override
	protected boolean handleMove() {
		if (m_state == STATE_DRAG) {
			m_state = STATE_INITIAL;
			setDragTracker(null);
		}
		if (m_state == STATE_INITIAL) {
			updateTargetRequest();
			updateTargetUnderMouse();
			showTargetFeedback();
		}
		return true;
	}

	/**
	 * If there's a drag tracker, sets it to <code>null</code> and then sets this tool's state to the
	 * initial state.
	 */
	@Override
	protected boolean handleViewerExited() {
		if (m_state == STATE_DRAG || m_state == STATE_DRAG_IN_PROGRESS) {
			// send low level event to give current tracker a chance to process 'mouse up' event.
			Event event = new Event();
			event.x = getLocation().x;
			event.y = getLocation().y;
			event.stateMask = m_stateMask;
			event.button = m_button;
			event.widget = getCurrentViewer().getControl();
			mouseUp(new MouseEvent(event), getCurrentViewer());
		}
		super.handleViewerExited();
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Request
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a {@link SelectionRequest} for the target request.
	 */
	@Override
	protected Request createTargetRequest() {
		Request request = new SelectionRequest();
		request.setType(RequestConstants.REQ_SELECTION);
		return request;
	}

	/**
	 * Sets the statemask and location of the target request (which is a {@link SelectionRequest}).
	 */
	@Override
	protected void updateTargetRequest() {
		super.updateTargetRequest();
		SelectionRequest request = (SelectionRequest) getTargetRequest();
		request.setLocation(getAbsoluteLocation());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handling Operations
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a new {@link Conditional} that evaluates to <code>true</code> if the queried edit
	 * part's {@link EditPart#isSelectable()} method returns <code>true</code>.
	 */
	@Override
	protected Conditional getTargetingConditional() {
		return editPart -> editPart.isSelectable();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Low-Level handle MouseEvent
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Forwards the mouse down event to the drag tracker, if one exists.
	 */
	@Override
	public void mouseDown(MouseEvent event, EditPartViewer viewer) {
		super.mouseDown(event, viewer);
		if (m_dragTracker != null) {
			m_dragTracker.mouseDown(event, viewer);
		}
	}

	/**
	 * Forwards the mouse up event to the drag tracker, if one exists.
	 */
	@Override
	public void mouseUp(MouseEvent event, EditPartViewer viewer) {
		if (m_dragTracker != null) {
			m_dragTracker.mouseUp(event, viewer);
		}
		super.mouseUp(event, viewer);
	}

	/**
	 * Forwards the mouse drag event to the drag tracker, if one exists.
	 */
	@Override
	public void mouseDrag(MouseEvent event, EditPartViewer viewer) {
		if (m_dragTracker != null) {
			m_dragTracker.mouseDrag(event, viewer);
		}
		super.mouseDrag(event, viewer);
	}

	/**
	 * Forwards the mouse move event to the drag tracker, if one exists.
	 */
	@Override
	public void mouseMove(MouseEvent event, EditPartViewer viewer) {
		if (m_dragTracker != null) {
			m_dragTracker.mouseMove(event, viewer);
		}
		super.mouseMove(event, viewer);
	}

	/**
	 * Forwards the mouse double clicked event to the drag tracker, if one exists.
	 */
	@Override
	public void mouseDoubleClick(MouseEvent event, EditPartViewer viewer) {
		super.mouseDoubleClick(event, viewer);
		if (m_dragTracker != null) {
			m_dragTracker.mouseDoubleClick(event, viewer);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handle KeyEvent
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void keyDown(KeyEvent event, EditPartViewer viewer) {
		if (m_dragTracker != null) {
			m_dragTracker.keyDown(event, viewer);
		} else {
			List<? extends EditPart> selection = viewer.getSelectedEditParts();
			//
			if (event.keyCode == SWT.ESC) {
				if (!selection.isEmpty()) {
					EditPart part = selection.get(0);
					EditPart parent = part.getParent();
					//
					if (parent != null && !(parent instanceof RootEditPart)) {
						viewer.select(parent);
					}
				}
			} else {
				handleKeyEvent(true, event, selection);
			}
		}
	}

	@Override
	public void keyUp(KeyEvent event, EditPartViewer viewer) {
		if (m_dragTracker != null) {
			m_dragTracker.keyUp(event, viewer);
		} else if (event.keyCode != SWT.ESC) {
			handleKeyEvent(false, event, viewer.getSelectedEditParts());
		}
	}

	private static void handleKeyEvent(boolean pressed, KeyEvent event, List<? extends EditPart> selection) {
		KeyRequest request = new KeyRequest(pressed, event);
		for (EditPart part : selection) {
			part.performRequest(request);
		}
	}
}