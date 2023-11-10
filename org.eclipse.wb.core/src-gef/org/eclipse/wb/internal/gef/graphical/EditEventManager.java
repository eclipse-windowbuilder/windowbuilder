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
package org.eclipse.wb.internal.gef.graphical;

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.draw2d.EventManager;
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.gef.core.EditDomain;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;

/**
 * A special event manager that will route events to the {@link EditDomain} when appropriate.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class EditEventManager extends EventManager {
	private final EditDomain m_domain;
	private final IEditPartViewer m_viewer;
	private boolean m_eventCapture;
	private Cursor m_overrideCursor;
	private MouseEvent m_currentMouseEvent;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EditEventManager(FigureCanvas canvas, EditDomain domain, IEditPartViewer viewer) {
		super(canvas);
		m_domain = domain;
		m_viewer = viewer;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Cursor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Set the Cursor.
	 */
	@Override
	public void setCursor(Cursor cursor) {
		if (m_overrideCursor == null) {
			super.setCursor(cursor);
		} else {
			super.setCursor(m_overrideCursor);
		}
	}

	/**
	 * Set the override Cursor.
	 */
	public void setOverrideCursor(Cursor cursor) {
		if (m_overrideCursor != cursor) {
			m_overrideCursor = cursor;
			//
			if (m_overrideCursor == null) {
				// if use figure cursor update mouse figure
				if (m_eventCapture) {
					super.setCursor(null);
				} else {
					updateFigureUnderCursor(m_currentMouseEvent);
					updateCursor();
				}
			} else {
				setCursor(m_overrideCursor);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handle KeyEvent
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void dispatchKeyPressed(KeyEvent event) {
		m_domain.keyPressed(event, m_viewer);
	}

	@Override
	public void dispatchKeyReleased(KeyEvent event) {
		m_domain.keyReleased(event, m_viewer);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handle MouseEvent
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void dispatchMouseDoubleClicked(MouseEvent event) {
		delayEvent(() -> {
			m_currentMouseEvent = event;
			//
			if (!m_eventCapture) {
				super.dispatchMouseDoubleClicked(event);
				if (isEventConsumed()) {
					return;
				}
			}
			//
			m_domain.mouseDoubleClick(event, m_viewer);
		}, event);
	}

	@Override
	public void dispatchMousePressed(MouseEvent event) {
		delayEvent(() -> {
			m_viewer.getControl().forceFocus();
			m_currentMouseEvent = event;
			//
			if (!m_eventCapture) {
				super.dispatchMousePressed(event);
				if (isEventConsumed()) {
					return;
				}
			}
			//
			m_eventCapture = true;
			m_domain.mouseDown(event, m_viewer);
		}, event);
	}

	@Override
	public void dispatchMouseReleased(MouseEvent event) {
		delayEvent(() -> {
			m_currentMouseEvent = event;
			//
			if (!m_eventCapture) {
				super.dispatchMouseReleased(event);
				if (isEventConsumed()) {
					return;
				}
			}
			//
			boolean eventCapture = m_eventCapture;
			m_eventCapture = false;
			m_domain.mouseUp(event, m_viewer);
			// after release capture update mouse figure
			if (eventCapture) {
				updateFigureUnderCursor(event);
			}
		}, event);
	}

	@Override
	public void dispatchMouseMoved(MouseEvent event) {
		delayEvent(() -> {
			m_currentMouseEvent = event;
			//
			if (!m_eventCapture) {
				super.dispatchMouseMoved(event);
				if (isEventConsumed()) {
					return;
				}
			}
			//
			if ((event.stateMask & SWT.BUTTON_MASK) != 0) {
				m_domain.mouseDrag(event, m_viewer);
			} else {
				m_domain.mouseMove(event, m_viewer);
			}
		}, event);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MouseTrackListener
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void dispatchMouseEntered(MouseEvent event) {
		delayEvent(() -> {
			m_currentMouseEvent = event;
			m_domain.viewerEntered(event, m_viewer);
			updateFigureUnderCursor(event);
		}, event);
	}

	@Override
	public void dispatchMouseExited(MouseEvent event) {
		delayEvent(() -> {
			m_eventCapture = false;
			m_currentMouseEvent = event;
			m_domain.viewerExited(event, m_viewer);
			updateFigureUnderCursor(event);
		}, event);
	}

	@Override
	public void dispatchMouseHover(MouseEvent event) {
	}
}