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
package org.eclipse.wb.internal.gef.core;

import org.eclipse.wb.gef.core.ICommandExceptionHandler;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.tools.SelectionTool;

import org.eclipse.draw2d.EventListenerList;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.events.MouseEvent;

/**
 * @author lobas_av
 * @coverage gef.core
 */
public class EditDomain extends org.eclipse.gef.EditDomain {
	private Tool m_activeTool;
	private Tool m_defaultTool;
	private EditPartViewer m_currentViewer;
	private MouseEvent m_currentMouseEvent;
	private ICommandExceptionHandler m_exceptionHandler;

	public EditDomain() {
		setCommandStack(new DesignerCommandStack());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands/Exceptions
	//
	////////////////////////////////////////////////////////////////////////////
	private Tool m_inCommandTool;

	private class DesignerCommandStack extends CommandStack {
		@Override
		public void execute(Command command) {
			clearToolDuringCommandExecution();
			try {
				if (System.getProperty("wbp.EditDomain.simulateCommandException") != null) {
					throw new Error("Simulated exception.");
				}
				super.execute(command);
			} catch (Throwable e) {
				if (m_exceptionHandler != null) {
					m_exceptionHandler.handleException(e);
					// exception handler usually recreates viewer, so we should cancel execution on current viewer
					throw new CancelOperationError();
				}
			} finally {
				restoreToolAfterCommandExecution();
			}
		}
	}

	/**
	 * Sometimes execution of {@link Command} may run SWT events loop, so user could interact with
	 * GEF. But at this time model and GEF may be in non-consistent state, so this will cause
	 * exceptions. We clear active {@link Tool}, so user events will be ignored.
	 */
	private void clearToolDuringCommandExecution() {
		if (m_activeTool != null) {
			m_inCommandTool = m_activeTool;
			m_activeTool = null;
		}
	}

	/**
	 * Restores active {@link Tool} after {@link #clearToolDuringCommandExecution()}.
	 */
	private void restoreToolAfterCommandExecution() {
		if (m_activeTool == null) {
			m_activeTool = m_inCommandTool;
		}
	}

	/**
	 * Set command exceptions handler.
	 */
	public void setExceptionHandler(ICommandExceptionHandler exceptionHandler) {
		m_exceptionHandler = exceptionHandler;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tool listeners
	//
	////////////////////////////////////////////////////////////////////////////
	private IDefaultToolProvider m_defaultToolProvider;
	private EventListenerList m_eventTable;

	private EventListenerList getEventTable() {
		if (m_eventTable == null) {
			m_eventTable = new EventListenerList();
		}
		return m_eventTable;
	}

	/**
	 * Sets the {@link IDefaultToolProvider}.
	 */
	public void setDefaultToolProvider(IDefaultToolProvider toolListener) {
		m_defaultToolProvider = toolListener;
	}

	/**
	 * Adds new {@link IActiveToolListener}.
	 */
	public void addActiveToolListener(IActiveToolListener listener) {
		getEventTable().addListener(IActiveToolListener.class, listener);
	}

	/**
	 * Removes {@link IActiveToolListener}.
	 */
	public void removeActiveToolListener(IActiveToolListener listener) {
		getEventTable().removeListener(IActiveToolListener.class, listener);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tool
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Loads the default {@link Tool}.
	 */
	@Override
	public void loadDefaultTool() {
		if (m_defaultToolProvider != null) {
			m_defaultToolProvider.loadDefaultTool();
		} else {
			setActiveTool(getDefaultTool());
		}
	}

	/**
	 * Returns the default tool for this edit domain. This will be a {@link SelectionTool} unless
	 * specifically replaced using {@link #setDefaultTool(Tool)}.
	 */
	@Override
	public Tool getDefaultTool() {
		if (m_defaultTool == null) {
			m_defaultTool = new SelectionTool();
		}
		return m_defaultTool;
	}

	/**
	 * Sets the default {@link Tool}.
	 */
	@Override
	public void setDefaultTool(org.eclipse.gef.Tool defaultTool) {
		m_defaultTool = (Tool) defaultTool;
	}

	/**
	 * Returns the active {@link Tool}.
	 */
	@Override
	public Tool getActiveTool() {
		return m_activeTool;
	}

	/**
	 * Sets the active {@link Tool} for this {@link EditDomain}. If a current {@link Tool} is active,
	 * it is deactivated. The new {@link Tool} is told its {@link EditDomain}, and is activated.
	 */
	@Override
	public void setActiveTool(org.eclipse.gef.Tool activeTool) {
		if (m_activeTool != null) {
			m_activeTool.deactivate();
		}
		//
		m_activeTool = (Tool) activeTool;
		//
		if (m_activeTool != null) {
			m_activeTool.setEditDomain(this);
			m_activeTool.activate();
			// notify listeners
			for (IActiveToolListener listener : getEventTable().getListenersIterable(IActiveToolListener.class)) {
				listener.toolActivated(m_activeTool);
			}
			// handle auto reload tool and update cursor
			if (m_currentViewer != null) {
				m_activeTool.setViewer(m_currentViewer);
				m_activeTool.mouseMove(m_currentMouseEvent, m_currentViewer);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handle mouse events
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Called when the mouse button has been double-clicked on a viewer.
	 */
	@Override
	public void mouseDoubleClick(MouseEvent event, EditPartViewer viewer) {
		m_currentMouseEvent = event;
		super.mouseDoubleClick(event, viewer);
	}

	/**
	 * Called when the mouse button has been pressed on a viewer.
	 */
	@Override
	public void mouseDown(MouseEvent event, EditPartViewer viewer) {
		m_currentMouseEvent = event;
		super.mouseDown(event, viewer);
	}

	/**
	 * Called when the mouse button has been released on a viewer.
	 */
	@Override
	public void mouseUp(MouseEvent event, EditPartViewer viewer) {
		m_currentMouseEvent = event;
		super.mouseUp(event, viewer);
	}

	/**
	 * Called when the mouse has been moved on a viewer.
	 */
	@Override
	public void mouseMove(MouseEvent event, EditPartViewer viewer) {
		m_currentMouseEvent = event;
		super.mouseMove(event, viewer);
	}

	/**
	 * Called when the mouse has been dragged within a viewer.
	 */
	@Override
	public void mouseDrag(MouseEvent event, EditPartViewer viewer) {
		m_currentMouseEvent = event;
		super.mouseDrag(event, viewer);
	}

	/**
	 * Called when the mouse enters a viewer.
	 */
	@Override
	public void viewerEntered(MouseEvent event, EditPartViewer viewer) {
		m_currentMouseEvent = event;
		m_currentViewer = viewer;
		super.viewerEntered(event, viewer);
	}

	/**
	 * Called when the mouse exits a viewer.
	 */
	@Override
	public void viewerExited(MouseEvent event, EditPartViewer viewer) {
		m_currentMouseEvent = event;
		m_currentViewer = null;
		super.viewerExited(event, viewer);
	}
}