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

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.requests.SelectionRequest;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;

/**
 * A drag tracker used to select {@link EditPart EditParts}.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class SelectEditPartTracker extends TargetingTool {
  private final EditPart m_sourceEditPart;
  private boolean m_isSelected;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SelectEditPartTracker(EditPart sourceEditPart) {
    m_sourceEditPart = sourceEditPart;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Drop Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void resetState() {
    super.resetState();
    m_isSelected = false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cursor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Cursor calculateCursor() {
    return m_state == STATE_INIT || m_state == STATE_DRAG
        ? getDefaultCursor()
        : super.calculateCursor();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // High-Level handle MouseEvent
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void handleButtonDown(int button) {
    if ((button == 1 || button == 3)
        && m_state == STATE_INIT
        && m_sourceEditPart.getSelected() == EditPart.SELECTED_NONE) {
      performSelection();
    }
    if (button == 1) {
      if (m_state == STATE_INIT) {
        m_state = STATE_DRAG;
      }
    } else {
      if (button == 3) {
        m_state = STATE_NONE;
      } else {
        m_state = STATE_INVALID;
      }
      handleInvalidInput();
    }
  }

  @Override
  protected void handleButtonUp(int button) {
    if (m_state == STATE_DRAG) {
      performSelection();
      performClick();
      m_state = STATE_NONE;
    }
  }

  @Override
  protected void handleDragStarted() {
    if (m_state == STATE_DRAG) {
      m_state = STATE_DRAG_IN_PROGRESS;
    }
  }

  @Override
  protected void handleDoubleClick(int button) {
    if (button == 1) {
      SelectionRequest request = new SelectionRequest(Request.REQ_OPEN);
      request.setLocation(getLocation());
      m_sourceEditPart.performRequest(request);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Performs the appropriate selection action based on the selection state of the source and the
   * modifiers (CTRL and SHIFT). If no modifier key is pressed, the source will be set as the only
   * selection. If the CTRL key is pressed and the edit part is already selected, it will be
   * deselected. If the CTRL key is pressed and the edit part is not selected, it will be appended
   * to the selection set. If the SHIFT key is pressed, the source will be appended to the
   * selection.
   */
  private void performSelection() {
    if (!m_isSelected) {
      m_isSelected = true;
      IEditPartViewer viewer = getViewer();
      //
      if ((m_stateMask & SWT.CONTROL) != 0) {
        if (viewer.getSelectedEditParts().contains(m_sourceEditPart)) {
          viewer.deselect(m_sourceEditPart);
        } else {
          viewer.appendSelection(m_sourceEditPart);
        }
      } else if ((m_stateMask & SWT.SHIFT) != 0) {
        viewer.appendSelection(m_sourceEditPart);
      } else {
        viewer.select(m_sourceEditPart);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Click
  //
  ////////////////////////////////////////////////////////////////////////////
  private void performClick() {
    if (m_sourceEditPart.getSelected() != EditPart.SELECTED_NONE) {
      getViewer().fireEditPartClick(m_sourceEditPart);
    }
  }
}