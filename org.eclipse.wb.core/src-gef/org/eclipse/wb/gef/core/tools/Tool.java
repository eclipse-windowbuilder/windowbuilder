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
package org.eclipse.wb.gef.core.tools;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.gef.core.EditDomain;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
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
public abstract class Tool {
  private boolean m_active;
  private IEditPartViewer m_viewer;
  private EditDomain m_domain;
  private boolean m_canUnload = true;

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
    m_state = STATE_INIT;
    m_active = true;
  }

  /**
   * Called when another Tool becomes the active tool for the {@link EditDomain}. Implementors can
   * perform state clean-up or to free resources.
   */
  public void deactivate() {
    m_active = false;
    setCommand(null);
    m_operationSet = null;
  }

  /**
   * Returns <code>true</code> if the tool is active.
   */
  public final boolean isActive() {
    return m_active;
  }

  /**
   * Get {@link IEditPartViewer}.
   */
  public final IEditPartViewer getViewer() {
    return m_viewer;
  }

  /**
   * Set {@link IEditPartViewer} into {@link Tool}.
   */
  public final void setViewer(IEditPartViewer viewer) {
    if (m_viewer != viewer) {
      setCursor(null);
      m_viewer = viewer;
      //
      if (m_viewer != null) {
        org.eclipse.swt.graphics.Point mouseLocation =
            m_viewer.getControl().toControl(Display.getCurrent().getCursorLocation());
        m_currentScreenX = mouseLocation.x;
        m_currentScreenY = mouseLocation.y;
      }
      //
      refreshCursor();
    }
  }

  /**
   * Returns the {@link EditDomain}.
   */
  public final EditDomain getDomain() {
    return m_domain;
  }

  /**
   * Set {@link EditDomain} into {@link Tool}.
   */
  public final void setDomain(EditDomain domain) {
    m_domain = domain;
  }

  /**
   * Returns <code>true</code> if the tool is set to unload when its current operation is complete.
   */
  protected final boolean unloadWhenFinished() {
    return m_canUnload;
  }

  /**
   * Setting this to <code>true</code> will cause the tool to be unloaded after one operation has
   * completed. The default value is <code>true</code>. The tool is unloaded, and the edit domains
   * default tool will be activated.
   */
  public final void setUnloadWhenFinished(boolean value) {
    m_canUnload = value;
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
    if (m_canUnload) {
      m_domain.loadDefaultTool();
    } else {
      // create emulate event
      Event event = null;
      //
      if (m_viewer != null) {
        event = new Event();
        event.display = Display.getCurrent();
        event.widget = m_viewer.getControl();
        event.type = SWT.MouseMove;
        event.x = m_currentScreenX;
        event.y = m_currentScreenY;
        event.button = m_button;
        event.stateMask = m_stateMask;
      }
      // reload tool
      deactivate();
      activate();
      // send emulate event
      if (m_viewer != null) {
        mouseMove(new MouseEvent(event), m_viewer);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // OperationSet
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<EditPart> m_operationSet;

  /**
   * Lazily creates and returns the list of {@link EditPart}'s on which the tool operates. The list
   * is initially <code>null</code>, in which case {@link #createOperationSet()} is called, and its
   * results cached until the tool is deactivated.
   */
  protected final List<EditPart> getOperationSet() {
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
  protected List<EditPart> createOperationSet() {
    return new ArrayList<EditPart>(m_viewer.getSelectedEditParts());
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
      m_domain.executeCommand(command);
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
  private Cursor m_defaultCursor;
  private Cursor m_disabledCursor;

  /**
   * Returns the cursor used under normal conditions.
   */
  protected Cursor getDefaultCursor() {
    return m_defaultCursor;
  }

  /**
   * Sets the default cursor.
   */
  public void setDefaultCursor(Cursor cursor) {
    if (m_defaultCursor != cursor) {
      m_defaultCursor = cursor;
      refreshCursor();
    }
  }

  /**
   * Returns the cursor used under abnormal conditions.
   */
  protected Cursor getDisabledCursor() {
    return m_disabledCursor == null ? getDefaultCursor() : m_disabledCursor;
  }

  /**
   * Sets the disabled cursor.
   */
  public void setDisabledCursor(Cursor cursor) {
    if (m_disabledCursor != cursor) {
      m_disabledCursor = cursor;
      refreshCursor();
    }
  }

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
    if (m_state == STATE_NONE) {
      return null;
    }
    if (m_command == null) {
      return getDisabledCursor();
    }
    return getDefaultCursor();
  }

  /**
   * Shows the given cursor on the viewer.
   */
  protected void setCursor(Cursor cursor) {
    if (m_viewer != null) {
      m_viewer.setCursor(cursor);
    }
  }

  /**
   * Sets the cursor being displayed to the appropriate cursor. If the tool is active, the current
   * cursor being displayed is updates by calling {@link #calculateCursor()}.
   */
  public void refreshCursor() {
    if (isActive()) {
      setCursor(calculateCursor());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Low-Level handle MouseEvent
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The final state for a tool to be in. Once a tool reaches this state, it will not change states
   * until it is activated() again.
   */
  protected static final int STATE_NONE = 0;
  /**
   * The first state that a tool is in. The tool will generally be in this state immediately
   * following {@link #activate()}.
   */
  protected static final int STATE_INIT = 1;
  /**
   * The state indicating that one or more buttons is pressed, but the user has not moved past the
   * drag threshold. Many tools will do nothing during this state but wait until
   * {@link #STATE_DRAG_IN_PROGRESS} is entered.
   */
  protected static final int STATE_DRAG = 2;
  /**
   * The state indicating that the drag detection theshold has been passed, and a drag is in
   * progress.
   */
  protected static final int STATE_DRAG_IN_PROGRESS = 3;
  /**
   * The state indicating that an input event has invalidated the interaction. For example, during a
   * mouse drag, pressing additional mouse button might invalidate the drag.
   */
  protected static final int STATE_INVALID = 4;
  //
  private static final int DRAG_THRESHOLD = 5;
  // mouse event info
  protected int m_currentScreenX;
  protected int m_currentScreenY;
  protected int m_stateMask;
  protected int m_button;
  // drag info
  protected int m_startScreenX;
  protected int m_startScreenY;
  protected int m_state;
  private boolean m_canPastThreshold;

  //
  private void setEvent(MouseEvent event) {
    m_currentScreenX = event.x;
    m_currentScreenY = event.y;
    m_stateMask = event.stateMask;
    m_button = event.button;
  }

  private boolean movedPastThreshold() {
    if (!m_canPastThreshold) {
      m_canPastThreshold =
          Math.abs(m_startScreenX - m_currentScreenX) > DRAG_THRESHOLD
              || Math.abs(m_startScreenY - m_currentScreenY) > DRAG_THRESHOLD;
    }
    return m_canPastThreshold;
  }

  /**
   * Handles mouse down events within a viewer. Subclasses wanting to handle this event should
   * override {@link #handleButtonDown(int)}.
   */
  public void mouseDown(MouseEvent event, IEditPartViewer viewer) {
    setViewer(viewer);
    setEvent(event);
    m_startScreenX = event.x;
    m_startScreenY = event.y;
    handleButtonDown(event.button);
  }

  /**
   * Handles mouse up within a viewer. Subclasses wanting to handle this event should override
   * {@link #handleButtonUp(int)}.
   */
  public void mouseUp(MouseEvent event, IEditPartViewer viewer) {
    setViewer(viewer);
    setEvent(event);
    handleButtonUp(event.button);
  }

  /**
   * Handles mouse drag events within a viewer. Subclasses wanting to handle this event should
   * override {@link #handleDrag()} and/or {@link #handleDragInProgress()}.
   */
  public void mouseDrag(MouseEvent event, IEditPartViewer viewer) {
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
  public void mouseMove(MouseEvent event, IEditPartViewer viewer) {
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
  public void mouseDoubleClick(MouseEvent event, IEditPartViewer viewer) {
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
  public void viewerEntered(MouseEvent event, IEditPartViewer viewer) {
    setEvent(event);
    //
    if (m_viewer != null) {
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
  public void viewerExited(MouseEvent event, IEditPartViewer viewer) {
    if (m_viewer == viewer) {
      setEvent(event);
      handleViewerExited();
      setViewer(null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // High-Level handle MouseEvent
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Called when the mouse button has been pressed. Subclasses may override this method to interpret
   * the meaning of a mouse down.
   */
  protected void handleButtonDown(int button) {
  }

  /**
   * Called when the mouse button has been released. Subclasses may override this method to
   * interpret the mouse up.
   */
  protected void handleButtonUp(int button) {
  }

  /**
   * Handles high-level processing of a mouse move. Subclasses may extend this method to process
   * mouse moves.
   */
  protected void handleMove() {
  }

  /**
   * Called whenever the mouse is being dragged. This method continues to be called even once
   * {@link #handleDragInProgress()} starts getting called. Subclasses may override this method to
   * interpret a drag.
   */
  protected void handleDrag() {
  }

  /**
   * Called only one time during a drag when the drag threshold has been exceeded. Subclasses may
   * override to interpret the drag starting.
   */
  protected void handleDragStarted() {
  }

  /**
   * Called whenever a mouse is being dragged and the drag threshold has been exceeded. Prior to the
   * drag threshold being exceeded, only {@link #handleDrag()} is called. This method gets called
   * repeatedly for every mouse move during the drag. Subclasses may override this method to
   * interpret the drag.
   */
  protected void handleDragInProgress() {
  }

  /**
   * Called when a mouse double-click occurs. Subclasses may override this method to interpret
   * double-clicks.
   */
  protected void handleDoubleClick(int button) {
  }

  /**
   * Called when the mouse enters an {@link IEditPartViewer}. Subclasses may extend this method to
   * process the viewer enter.
   */
  protected void handleViewerEntered() {
  }

  /**
   * Called when the mouse exits an {@link IEditPartViewer}. Subclasses may extend this method to
   * process the viewer enter.
   */
  protected void handleViewerExited() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Drop Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the current x, y <b>*absolute*</b> position of the mouse cursor.
   */
  public final Point getLocation() {
    return new Point(m_currentScreenX + m_viewer.getHOffset(), m_currentScreenY
        + m_viewer.getVOffset());
  }

  /**
   * Returns the starting mouse <b>*absolute*</b> location for the current tool operation. This is
   * typically the mouse location where the user first pressed a mouse button. This is important for
   * tools that interpret mouse drags.
   */
  protected Point getStartLocation() {
    return new Point(m_startScreenX + m_viewer.getHOffset(), m_startScreenY + m_viewer.getVOffset());
  }

  /**
   * Return the number of pixels that the mouse has been moved since that drag was started. The drag
   * start is determined by where the mouse button was first pressed.
   */
  protected Dimension getDragMoveDelta() {
    return getLocation().getDifference(getStartLocation());
  }

  /**
   * Resets all state fields to default values.
   */
  protected void resetState() {
    m_currentScreenX = 0;
    m_currentScreenY = 0;
    m_stateMask = 0;
    m_button = 0;
    //
    m_startScreenX = 0;
    m_startScreenY = 0;
    //
    m_canPastThreshold = false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle KeyEvent
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Called when the key has been pressed on a viewer.
   */
  public void keyPressed(KeyEvent event, IEditPartViewer viewer) {
  }

  /**
   * Called when the key has been released on a viewer.
   */
  public void keyReleased(KeyEvent event, IEditPartViewer viewer) {
  }
}