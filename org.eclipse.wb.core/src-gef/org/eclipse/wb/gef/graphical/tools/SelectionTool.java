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
package org.eclipse.wb.gef.graphical.tools;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.IEditPartViewer.IConditional;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.requests.SelectionRequest;
import org.eclipse.wb.gef.core.tools.TargetingTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.internal.gef.core.EditDomain;
import org.eclipse.wb.internal.gef.core.IRootContainer;

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
  private Tool m_dragTracker;

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
  public void setDragTrackerTool(Tool dragTracker) {
    if (m_dragTracker != dragTracker) {
      if (m_dragTracker != null) {
        m_dragTracker.deactivate();
      }
      //
      m_dragTracker = dragTracker;
      refreshCursor();
      //
      if (m_dragTracker != null) {
        m_dragTracker.setDomain(getDomain());
        m_dragTracker.setViewer(getViewer());
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
  protected void handleButtonDown(int button) {
    if (m_state == STATE_INIT) {
      m_state = STATE_DRAG;
      //
      if (m_dragTracker != null) {
        m_dragTracker.deactivate();
      }
      //
      if ((m_stateMask & SWT.ALT) != 0) {
        setDragTrackerTool(new MarqueeDragTracker());
        return;
      }
      //
      Handle handle = getViewer().findTargetHandle(m_currentScreenX, m_currentScreenY);
      if (handle != null) {
        setDragTrackerTool(handle.getDragTrackerTool());
        return;
      }
      //
      updateTargetRequest();
      ((SelectionRequest) getTargetRequest()).setLastButtonPressed(button);
      updateTargetUnderMouse();
      //
      EditPart editPart = getTargetEditPart();
      if (editPart == null) {
        setDragTrackerTool(null);
        getViewer().deselectAll();
      } else {
        setDragTrackerTool(editPart.getDragTrackerTool(getTargetRequest()));
        lockTargetEditPart(editPart);
      }
    }
  }

  @Override
  protected void handleButtonUp(int button) {
    ((SelectionRequest) getTargetRequest()).setLastButtonPressed(0);
    setDragTrackerTool(null);
    m_state = STATE_INIT;
    unlockTargetEditPart();
  }

  @Override
  protected void handleMove() {
    if (m_state == STATE_DRAG) {
      m_state = STATE_INIT;
      setDragTrackerTool(null);
    }
    if (m_state == STATE_INIT) {
      updateTargetRequest();
      updateTargetUnderMouse();
      showTargetFeedback();
    }
  }

  /**
   * If there's a drag tracker, sets it to <code>null</code> and then sets this tool's state to the
   * initial state.
   */
  @Override
  protected void handleViewerExited() {
    if (m_state == STATE_DRAG || m_state == STATE_DRAG_IN_PROGRESS) {
      // send low level event to give current tracker a chance to process 'mouse up' event.
      Event event = new Event();
      event.x = m_currentScreenX;
      event.y = m_currentScreenY;
      event.stateMask = m_stateMask;
      event.button = m_button;
      event.widget = getViewer().getControl();
      mouseUp(new MouseEvent(event), getViewer());
    }
    super.handleViewerExited();
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
    return new SelectionRequest(Request.REQ_SELECTION);
  }

  /**
   * Sets the statemask and location of the target request (which is a {@link SelectionRequest}).
   */
  @Override
  protected void updateTargetRequest() {
    super.updateTargetRequest();
    SelectionRequest request = (SelectionRequest) getTargetRequest();
    request.setLocation(getLocation());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handling Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns a new {@link IConditional} that evaluates to <code>true</code> if the queried edit
   * part's {@link EditPart#isSelectable()} method returns <code>true</code>.
   */
  @Override
  protected IConditional getTargetingConditional() {
    return new IConditional() {
      public boolean evaluate(EditPart editPart) {
        return editPart.isSelectable();
      }
    };
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
  public void mouseDown(MouseEvent event, IEditPartViewer viewer) {
    super.mouseDown(event, viewer);
    if (m_dragTracker != null) {
      m_dragTracker.mouseDown(event, viewer);
    }
  }

  /**
   * Forwards the mouse up event to the drag tracker, if one exists.
   */
  @Override
  public void mouseUp(MouseEvent event, IEditPartViewer viewer) {
    if (m_dragTracker != null) {
      m_dragTracker.mouseUp(event, viewer);
    }
    super.mouseUp(event, viewer);
  }

  /**
   * Forwards the mouse drag event to the drag tracker, if one exists.
   */
  @Override
  public void mouseDrag(MouseEvent event, IEditPartViewer viewer) {
    if (m_dragTracker != null) {
      m_dragTracker.mouseDrag(event, viewer);
    }
    super.mouseDrag(event, viewer);
  }

  /**
   * Forwards the mouse move event to the drag tracker, if one exists.
   */
  @Override
  public void mouseMove(MouseEvent event, IEditPartViewer viewer) {
    if (m_dragTracker != null) {
      m_dragTracker.mouseMove(event, viewer);
    }
    super.mouseMove(event, viewer);
  }

  /**
   * Forwards the mouse double clicked event to the drag tracker, if one exists.
   */
  @Override
  public void mouseDoubleClick(MouseEvent event, IEditPartViewer viewer) {
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
  public void keyPressed(KeyEvent event, IEditPartViewer viewer) {
    if (m_dragTracker != null) {
      m_dragTracker.keyPressed(event, viewer);
    } else {
      List<EditPart> selection = viewer.getSelectedEditParts();
      //
      if (event.keyCode == SWT.ESC) {
        if (!selection.isEmpty()) {
          EditPart part = selection.get(0);
          EditPart parent = part.getParent();
          //
          if (parent != null && !(parent instanceof IRootContainer)) {
            viewer.select(parent);
          }
        }
      } else {
        handleKeyEvent(true, event, selection);
      }
    }
  }

  @Override
  public void keyReleased(KeyEvent event, IEditPartViewer viewer) {
    if (m_dragTracker != null) {
      m_dragTracker.keyReleased(event, viewer);
    } else if (event.keyCode != SWT.ESC) {
      handleKeyEvent(false, event, viewer.getSelectedEditParts());
    }
  }

  private static void handleKeyEvent(boolean pressed, KeyEvent event, List<EditPart> selection) {
    KeyRequest request = new KeyRequest(pressed, event);
    for (EditPart part : selection) {
      part.performRequest(request);
    }
  }
}