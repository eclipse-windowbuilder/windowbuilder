/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.gef.core;

import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.ICommandExceptionHandler;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.tools.SelectionTool;
import org.eclipse.wb.internal.draw2d.events.EventTable;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

/**
 * @author lobas_av
 * @coverage gef.core
 */
public class EditDomain {
  private Tool m_activeTool;
  private Tool m_defaultTool;
  private IEditPartViewer m_currentViewer;
  private MouseEvent m_currentMouseEvent;
  private ICommandExceptionHandler m_exceptionHandler;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditDomain() {
    loadDefaultTool();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands/Exceptions
  //
  ////////////////////////////////////////////////////////////////////////////
  private Tool m_inCommandTool;

  /**
   * Execute given {@link Command} and handle all exceptions.
   */
  public void executeCommand(Command command) {
    clearToolDuringCommandExecution();
    try {
      if (System.getProperty("wbp.EditDomain.simulateCommandException") != null) {
        throw new Error("Simulated exception.");
      }
      command.execute();
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
  private final EventTable m_eventTable = new EventTable();

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
    m_eventTable.addListener(IActiveToolListener.class, listener);
  }

  /**
   * Removes {@link IActiveToolListener}.
   */
  public void removeActiveToolListener(IActiveToolListener listener) {
    m_eventTable.removeListener(IActiveToolListener.class, listener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tool
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Loads the default {@link Tool}.
   */
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
  public Tool getDefaultTool() {
    if (m_defaultTool == null) {
      m_defaultTool = new SelectionTool();
    }
    return m_defaultTool;
  }

  /**
   * Sets the default {@link Tool}.
   */
  public void setDefaultTool(Tool defaultTool) {
    m_defaultTool = defaultTool;
  }

  /**
   * Returns the active {@link Tool}.
   */
  public Tool getActiveTool() {
    return m_activeTool;
  }

  /**
   * Sets the active {@link Tool} for this {@link EditDomain}. If a current {@link Tool} is active,
   * it is deactivated. The new {@link Tool} is told its {@link EditDomain}, and is activated.
   */
  public void setActiveTool(Tool activeTool) {
    if (m_activeTool != null) {
      m_activeTool.deactivate();
    }
    //
    m_activeTool = activeTool;
    //
    if (m_activeTool != null) {
      m_activeTool.setDomain(this);
      m_activeTool.activate();
      // notify listeners
      for (IActiveToolListener listener : m_eventTable.getListeners(IActiveToolListener.class)) {
        listener.toolActivated(m_activeTool);
      }
      // handle auto reload tool and update cursor
      if (m_currentViewer != null) {
        m_activeTool.setViewer(m_currentViewer);
        m_activeTool.refreshCursor();
        m_activeTool.mouseMove(m_currentMouseEvent, m_currentViewer);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle key events
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Called when the key has been pressed on a viewer.
   */
  public void keyPressed(KeyEvent event, IEditPartViewer viewer) {
    Tool tool = getActiveTool();
    if (tool != null) {
      tool.keyPressed(event, viewer);
    }
  }

  /**
   * Called when the key has been released on a viewer.
   */
  public void keyReleased(KeyEvent event, IEditPartViewer viewer) {
    Tool tool = getActiveTool();
    if (tool != null) {
      tool.keyReleased(event, viewer);
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
  public void mouseDoubleClick(MouseEvent event, IEditPartViewer viewer) {
    m_currentMouseEvent = event;
    Tool tool = getActiveTool();
    if (tool != null) {
      tool.mouseDoubleClick(event, viewer);
    }
  }

  /**
   * Called when the mouse button has been pressed on a viewer.
   */
  public void mouseDown(MouseEvent event, IEditPartViewer viewer) {
    m_currentMouseEvent = event;
    Tool tool = getActiveTool();
    if (tool != null) {
      tool.mouseDown(event, viewer);
    }
  }

  /**
   * Called when the mouse button has been released on a viewer.
   */
  public void mouseUp(MouseEvent event, IEditPartViewer viewer) {
    m_currentMouseEvent = event;
    Tool tool = getActiveTool();
    if (tool != null) {
      tool.mouseUp(event, viewer);
    }
  }

  /**
   * Called when the mouse has been moved on a viewer.
   */
  public void mouseMove(MouseEvent event, IEditPartViewer viewer) {
    m_currentMouseEvent = event;
    Tool tool = getActiveTool();
    if (tool != null) {
      tool.mouseMove(event, viewer);
    }
  }

  /**
   * Called when the mouse has been dragged within a viewer.
   */
  public void mouseDrag(MouseEvent event, IEditPartViewer viewer) {
    m_currentMouseEvent = event;
    Tool tool = getActiveTool();
    if (tool != null) {
      tool.mouseDrag(event, viewer);
    }
  }

  /**
   * Called when the mouse enters a viewer.
   */
  public void viewerEntered(MouseEvent event, IEditPartViewer viewer) {
    m_currentMouseEvent = event;
    m_currentViewer = viewer;
    Tool tool = getActiveTool();
    if (tool != null) {
      tool.viewerEntered(event, viewer);
    }
  }

  /**
   * Called when the mouse exits a viewer.
   */
  public void viewerExited(MouseEvent event, IEditPartViewer viewer) {
    m_currentMouseEvent = event;
    m_currentViewer = null;
    Tool tool = getActiveTool();
    if (tool != null) {
      tool.viewerExited(event, viewer);
    }
  }
}