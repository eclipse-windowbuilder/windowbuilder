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

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.gef.core.EditDomain;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lobas_av
 * @coverage gef.core
 */
public abstract class Tool extends org.eclipse.gef.tools.AbstractTool implements DragTracker {
	private static final int FLAG_ACTIVE = 8;
	private static final int FLAG_PAST_THRESHOLD = 1;

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Called when this tool becomes the active tool for the {@link EditDomain}. Implementors can
	 * perform any necessary initialization here.
	 */
	public void activate() {
		resetState();
		m_state = STATE_INITIAL;
		setFlag(FLAG_ACTIVE, true);
	}

	/**
	 * Called when another Tool becomes the active tool for the {@link EditDomain}. Implementors can
	 * perform state clean-up or to free resources.
	 */
	public void deactivate() {
		setFlag(FLAG_ACTIVE, false);
		setCommand(null);
		m_operationSet = null;
	}

	/**
	 * Returns <code>true</code> if the tool is active.
	 */
	public final boolean isActive() {
		return getFlag(FLAG_ACTIVE);
	}

	/**
	 * Get {@link IEditPartViewer}.
	 */
	public final IEditPartViewer getCurrentViewer() {
		return (IEditPartViewer) super.getCurrentViewer();
	}

	/**
	 * Returns the {@link EditDomain}.
	 */
	public final EditDomain getDomain() {
		return (EditDomain) super.getDomain();
	}

	/**
	 * Called when the current tool operation is to be completed. In other words, the "state machine"
	 * and has accepted the sequence of input (i.e. the mouse gesture). By default, the tool will
	 * either reactivate itself, or ask the edit domain to load the default tool.
	 * <P>
	 * Subclasses should extend this method to first do whatever it is that the tool does, and then
	 * call <code>super</code>.
	 *
	 * @see #unloadWhenFinished()
	 */
	protected void handleFinished() {
		if (unloadWhenFinished()) {
			getDomain().loadDefaultTool();
		} else {
			// create emulate event
			Event event = null;
			//
			if (getCurrentViewer() != null) {
				event = new Event();
				event.display = Display.getCurrent();
				event.widget = getCurrentViewer().getControl();
				event.type = SWT.MouseMove;
				event.x = getLocation().x;
				event.y = getLocation().y;
				event.button = m_button;
				event.stateMask = m_stateMask;
			}
			// reload tool
			deactivate();
			activate();
			// send emulate event
			if (getCurrentViewer() != null) {
				mouseMove(new MouseEvent(event), getCurrentViewer());
			}
		}
	}

	@Override
	protected String getCommandName() {
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// OperationSet
	//
	////////////////////////////////////////////////////////////////////////////
	private List<? extends EditPart> m_operationSet;

	/**
	 * Lazily creates and returns the list of {@link EditPart}'s on which the tool operates. The list
	 * is initially <code>null</code>, in which case {@link #createOperationSet()} is called, and its
	 * results cached until the tool is deactivated.
	 */
	protected final List<? extends EditPart> getOperationSet() {
		if (m_operationSet == null) {
			m_operationSet = createOperationSet();
		}
		return m_operationSet;
	}

	/**
	 * Returns a new List of {@link EditPart}'s that this tool is operating on. This method is called
	 * once during {@link #getOperationSet()}, and its result is cached.
	 * <P>
	 * By default, the operations set is the current viewer's entire selection. Subclasses may
	 * override this method to filter or alter the operation set as necessary.
	 */
	protected List<? extends EditPart> createOperationSet() {
		return new ArrayList<>(getCurrentViewer().getSelectedEditParts());
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
	protected final void executeCommand() {
		if (m_command != null) {
			Command command = m_command;
			setCommand(null);
			getDomain().getCommandStack().execute(command);
		}
	}

	/**
	 * Sets the currently active command.
	 */
	protected final void setCommand(Command command) {
		m_command = command;
		refreshCursor();
	}

	/**
	 * Returns a new, updated command based on the tools current properties.
	 */
	protected Command getCommand() {
		return null;
	}

	/**
	 * Updates currently command.
	 */
	protected final void updateCommand() {
		setCommand(getCommand());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Cursor
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the appropriate cursor for the tools current state. If the tool is in its terminal
	 * state, <code>null</code> is returned. Otherwise, either the default or disabled cursor is
	 * returned, based on the existence of a current command, and whether that current command is
	 * executable.
	 * <P>
	 * Subclasses may override or extend this method to calculate the appropriate cursor based on
	 * other conditions.
	 */
	protected Cursor calculateCursor() {
		if (m_state == STATE_TERMINAL) {
			return null;
		}
		if (m_command == null) {
			return getDisabledCursor();
		}
		return getDefaultCursor();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Low-Level handle MouseEvent
	//
	////////////////////////////////////////////////////////////////////////////
	//
	private static final int DRAG_THRESHOLD = 5;
	// mouse event info
	protected int m_stateMask;
	protected int m_button;
	// drag info
	protected int m_state;

	//
	private void setEvent(MouseEvent event) {
		getCurrentInput().setInput(event);
		m_stateMask = event.stateMask;
		m_button = event.button;
	}

	protected boolean movedPastThreshold() {
		if (!getFlag(FLAG_PAST_THRESHOLD)) {
			Point start = getAbsoluteStartLocation();
			Point end = getAbsoluteLocation();
			setFlag(FLAG_PAST_THRESHOLD, Math.abs(start.x - end.x) > DRAG_THRESHOLD || Math.abs(start.y - end.y) > DRAG_THRESHOLD);
		}
		return getFlag(FLAG_PAST_THRESHOLD);
	}

	/**
	 * Handles mouse down events within a viewer. Subclasses wanting to handle this event should
	 * override {@link #handleButtonDown(int)}.
	 */
	public void mouseDown(MouseEvent event, EditPartViewer viewer) {
		setViewer(viewer);
		setEvent(event);
		setStartLocation(new Point(event.x, event.y));
		handleButtonDown(event.button);
	}

	/**
	 * Handles mouse up within a viewer. Subclasses wanting to handle this event should override
	 * {@link #handleButtonUp(int)}.
	 */
	public void mouseUp(MouseEvent event, EditPartViewer viewer) {
		setViewer(viewer);
		setEvent(event);
		handleButtonUp(event.button);
	}

	/**
	 * Handles mouse drag events within a viewer. Subclasses wanting to handle this event should
	 * override {@link #handleDrag()} and/or {@link #handleDragInProgress()}.
	 */
	public void mouseDrag(MouseEvent event, EditPartViewer viewer) {
		setViewer(viewer);
		boolean wasDragging = movedPastThreshold();
		setEvent(event);
		handleDrag();
		//
		if (movedPastThreshold()) {
			if (!wasDragging) {
				handleDragStarted();
			}
			handleDragInProgress();
		}
	}

	/**
	 * Handles mouse moves (if the mouse button is up) within a viewer. Subclasses wanting to handle
	 * this event should override {@link #handleMove()}.
	 */
	public void mouseMove(MouseEvent event, EditPartViewer viewer) {
		setViewer(viewer);
		setEvent(event);
		if (m_state == STATE_DRAG_IN_PROGRESS) {
			handleDragInProgress();
		} else {
			handleMove();
		}
	}

	/**
	 * Handles mouse double click events within a viewer. Subclasses wanting to handle this event
	 * should override {@link #handleDoubleClick(int)}.
	 */
	public void mouseDoubleClick(MouseEvent event, EditPartViewer viewer) {
		setViewer(viewer);
		setEvent(event);
		handleDoubleClick(event.button);
	}

	/**
	 * Receives the mouse entered event.
	 * <p>
	 * FEATURE in SWT: mouseExit comes after mouseEntered on the new . Therefore, if the current
	 * viewer is not <code>null</code>, it means the exit has not been sent yet by SWT. To maintain
	 * proper ordering, GEF fakes the exit and calls {@link #handleViewerExited()}. The real exit will
	 * then be ignored.
	 */
	public void viewerEntered(MouseEvent event, EditPartViewer viewer) {
		setEvent(event);
		//
		if (getCurrentViewer() != null) {
			handleViewerExited();
		}
		//
		setViewer(viewer);
		handleViewerEntered();
	}

	/**
	 * Handles the mouse exited event. Subclasses wanting to handle this event should override
	 * {@link #handleViewerExited()}.
	 */
	public void viewerExited(MouseEvent event, EditPartViewer viewer) {
		if (getCurrentViewer() == viewer) {
			setEvent(event);
			handleViewerExited();
			setViewer((IEditPartViewer) null);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Drop Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the current x, y <b>*absolute*</b> position of the mouse cursor.
	 */
	public final Point getAbsoluteLocation() {
		return new Point(getLocation().x + getCurrentViewer().getHOffset(),
				getLocation().y + getCurrentViewer().getVOffset());
	}

	/**
	 * Returns the starting mouse <b>*absolute*</b> location for the current tool operation. This is
	 * typically the mouse location where the user first pressed a mouse button. This is important for
	 * tools that interpret mouse drags.
	 */
	protected Point getAbsoluteStartLocation() {
		return new Point(getStartLocation().x + getCurrentViewer().getHOffset(),
				getStartLocation().y + getCurrentViewer().getVOffset());
	}

	/**
	 * Resets all state fields to default values.
	 */
	protected void resetState() {
		getCurrentInput().setMouseLocation(0, 0);
		m_stateMask = 0;
		m_button = 0;
		//
		setStartLocation(new Point(0, 0));
		//
		setFlag(FLAG_PAST_THRESHOLD, false);
	}
}