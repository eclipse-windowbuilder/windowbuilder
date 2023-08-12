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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class EventManager implements MouseListener, MouseMoveListener, MouseTrackListener {
	public static final int ANY_BUTTON = SWT.BUTTON1 | SWT.BUTTON2 | SWT.BUTTON3;
	//
	private final FigureCanvas m_canvas;
	private final RootFigure m_root;
	private MouseEvent m_currentEvent;
	private Figure m_cursorFigure;
	private Figure m_captureFigure;
	private Figure m_targetFigure;
	private Cursor m_cursor;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EventManager(FigureCanvas canvas) {
		m_canvas = canvas;
		m_root = m_canvas.getRootFigure();
		// custom tooltip
		new CustomTooltipManager(canvas, this);
		// add listeners
		m_canvas.addMouseListener(this);
		m_canvas.addMouseMoveListener(this);
		m_canvas.addMouseTrackListener(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Cursor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Updates the Cursor.
	 */
	public void updateCursor() {
		if (m_cursorFigure == null) {
			setCursor(null);
		} else {
			setCursor(m_cursorFigure.getCursor());
		}
	}

	/**
	 * Set the Cursor.
	 */
	public void setCursor(Cursor cursor) {
		if (m_cursor == null) {
			if (cursor == null) {
				return;
			}
		} else if (m_cursor == cursor || m_cursor.equals(cursor)) {
			return;
		}
		//
		m_cursor = cursor;
		m_canvas.setCursor(m_cursor);
	}

	protected void updateFigureToolTipText() {
		if (m_cursorFigure == null) {
			m_canvas.setToolTipText(null);
		} else {
			m_canvas.setToolTipText(m_cursorFigure.getToolTipText());
		}
	}

	private void setFigureUnderCursor(Figure figure, org.eclipse.swt.events.MouseEvent event) {
		if (m_cursorFigure != figure) {
			sendEvent(() -> m_targetFigure.handleMouseExited(m_currentEvent), event);
			//
			m_cursorFigure = figure;
			sendEvent(() -> m_targetFigure.handleMouseEntered(m_currentEvent), event);
			// finish
			updateCursor();
			updateFigureToolTipText();
		}
	}

	public final Figure getCursorFigure() {
		return m_cursorFigure;
	}

	/**
	 * Update the {@link Figure} located at the given location which will accept mouse events.
	 */
	protected final void updateFigureUnderCursor(org.eclipse.swt.events.MouseEvent event) {
		TargetFigureFindVisitor visitor = new TargetFigureFindVisitor(m_canvas, event.x, event.y);
		m_root.accept(visitor, false);
		setFigureUnderCursor(visitor.getTargetFigure(), event);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Capture
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets capture to the given figure. All subsequent events will be sent to the given figure until
	 * {@link #setCapture(null)} is called.
	 */
	public void setCapture(Figure captureFigure) {
		m_captureFigure = captureFigure;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Consume
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Return whether this event has been consumed.
	 */
	protected boolean isEventConsumed() {
		return m_currentEvent != null && m_currentEvent.isConsumed();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MouseEvent listener's
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent event) {
		updateFigureUnderCursor(event);
		sendEvent(() -> m_targetFigure.handleMouseDoubleClicked(m_currentEvent), event);
	}

	@Override
	public void mouseDown(org.eclipse.swt.events.MouseEvent event) {
		if (m_canvas.getToolTipText() != null) {
			m_canvas.setToolTipText(null);
		}
		updateFigureUnderCursor(event);
		sendEvent(() -> m_targetFigure.handleMousePressed(m_currentEvent), event);
	}

	@Override
	public void mouseUp(org.eclipse.swt.events.MouseEvent event) {
		updateFigureUnderCursor(event);
		sendEvent(() -> m_targetFigure.handleMouseReleased(m_currentEvent), event);
	}

	@Override
	public void mouseMove(org.eclipse.swt.events.MouseEvent event) {
		updateFigureUnderCursor(event);
		sendEvent(() -> m_targetFigure.handleMouseMoved(m_currentEvent), event);
	}

	private <T extends Object> void sendEvent(Runnable delayEvent,
			org.eclipse.swt.events.MouseEvent e) {
		m_currentEvent = null;
		m_targetFigure = m_captureFigure == null ? m_cursorFigure : m_captureFigure;
		//
		if (m_targetFigure != null) {
			m_currentEvent = new MouseEvent(null, m_targetFigure, e);
			//
			Rectangle bounds = m_targetFigure.getBounds();
			Point location = new Point(m_currentEvent.x - bounds.x, m_currentEvent.y - bounds.y);
			location.x += m_canvas.getHorizontalScrollModel().getSelection();
			location.y += m_canvas.getVerticalScrollModel().getSelection();
			FigureUtils.translateAbsoluteToFigure(m_targetFigure, location);
			//
			m_currentEvent.x = location.x;
			m_currentEvent.y = location.y;
			//
			delayEvent(e.widget, delayEvent);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MouseTrackListener
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void mouseEnter(org.eclipse.swt.events.MouseEvent event) {
		updateFigureUnderCursor(event);
		sendEvent(() -> m_targetFigure.handleMouseEntered(m_currentEvent), event);
	}

	@Override
	public void mouseExit(org.eclipse.swt.events.MouseEvent event) {
		updateFigureUnderCursor(event);
		sendEvent(() -> m_targetFigure.handleMouseExited(m_currentEvent), event);
	}

	@Override
	public void mouseHover(org.eclipse.swt.events.MouseEvent event) {
		updateFigureUnderCursor(event);
		sendEvent(() -> m_targetFigure.handleMouseHover(m_currentEvent), event);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events queue
	//
	////////////////////////////////////////////////////////////////////////////
	private static String FLAG_DELAY_EVENTS = "Flag that events to this Control should be delayed";
	private static String KEY_DELAYED_EVENTS = "List of delayed events";

	/**
	 * Specifies if events to given {@link Control} should be delayed or not.
	 */
	public static void delayEvents(Control control, boolean delay) {
		if (delay) {
			control.setData(FLAG_DELAY_EVENTS, Boolean.TRUE);
		} else {
			control.setData(FLAG_DELAY_EVENTS, null);
		}
	}

	/**
	 * If arguments contain {@link TypedEvent} and target {@link Control} is disabled, then puts this
	 * event into {@link List} with key {@link #KEY_DELAYED_EVENTS}.
	 */
	private static void delayEvent(Widget widget, Runnable delayEvent) {
		if (widget.isDisposed() || widget.getData(FLAG_DELAY_EVENTS) == null) {
			// execute immediately
			delayEvent.run();
		} else {
			// prepare delay queue
			@SuppressWarnings("unchecked")
			List<Runnable> eventQueue = (List<Runnable>) widget.getData(KEY_DELAYED_EVENTS);
			if (eventQueue == null) {
				eventQueue = new ArrayList<>();
				widget.setData(KEY_DELAYED_EVENTS, eventQueue);
			}
			// put event into queue
			eventQueue.add(delayEvent);
		}
	}

	/**
	 * Runs events delayed before because given {@link Control} was disabled.
	 */
	public static void runDelayedEvents(Control control) {
		// prepare delay queue
		@SuppressWarnings("unchecked")
		List<Runnable> eventQueue = (List<Runnable>) control.getData(KEY_DELAYED_EVENTS);
		control.setData(KEY_DELAYED_EVENTS, null);
		// run all events
		if (eventQueue != null) {
			for (Runnable event : eventQueue) {
				event.run();
			}
		}
	}
}