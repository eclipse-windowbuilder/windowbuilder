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
package org.eclipse.wb.internal.core.gef.policy.layout.absolute;

import org.eclipse.wb.core.gef.command.CompoundEditCommand;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.tools.ToolUtilities;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Abstract super class for policies supporting children moving with keyboard arrow keys.
 *
 * @author mitin_aa
 * @author lobas_av
 * @coverage core.gef.policy
 */
public abstract class KeyboardMovingLayoutEditPolicy extends LayoutEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // KeyRequest
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performRequest(Request request) {
    if (request instanceof KeyRequest) {
      KeyRequest keyRequest = (KeyRequest) request;
      // check input
      switch (keyRequest.getKeyCode()) {
        case SWT.ARROW_UP :
        case SWT.ARROW_DOWN :
        case SWT.ARROW_LEFT :
        case SWT.ARROW_RIGHT :
          break;
        default :
          return;
      }
      List<EditPart> editParts = ToolUtilities.getSelectionWithoutDependants(getViewer());
      // check selection
      if (!editParts.isEmpty()) {
        for (EditPart editPart : editParts) {
          if (getHost() == editPart) {
            return;
          }
        }
        if (keyRequest.isPressed()) {
          handleKeyPressed(keyRequest, editParts);
        } else {
          handleKeyReleased();
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // High-Level handle KeyEvent
  //
  ////////////////////////////////////////////////////////////////////////////
  private final ChangeBoundsRequest m_changeBoundsRequest = new ChangeBoundsRequest();
  private Timer m_keyDragTimer;
  private boolean m_isKeyboardMoving;

  /**
   * Key pressed handler, generating GEF change bounds request
   */
  private void handleKeyPressed(KeyRequest request, List<EditPart> editParts) {
    synchronized (m_changeBoundsRequest) {
      if (m_keyDragTimer != null) {
        return;
      }
      m_isKeyboardMoving = true;
      // fill request
      boolean isResizing = request.isControlKeyPressed();
      m_changeBoundsRequest.setEditParts(editParts);
      m_changeBoundsRequest.setType(isResizing ? getResizeRequestType() : Request.REQ_MOVE);
      //
      if (isResizing) {
        int resizeDirection = m_changeBoundsRequest.getResizeDirection();
        Dimension sizeDelta = m_changeBoundsRequest.getSizeDelta();
        // calc resize delta and direction
        switch (request.getKeyCode()) {
          case SWT.ARROW_UP :
            sizeDelta.height--;
            resizeDirection |= IPositionConstants.NORTH_SOUTH;
            break;
          case SWT.ARROW_DOWN :
            sizeDelta.height++;
            resizeDirection |= IPositionConstants.NORTH_SOUTH;
            break;
          case SWT.ARROW_LEFT :
            sizeDelta.width--;
            resizeDirection |= IPositionConstants.EAST_WEST;
            break;
          case SWT.ARROW_RIGHT :
            sizeDelta.width++;
            resizeDirection |= IPositionConstants.EAST_WEST;
            break;
        }
        m_changeBoundsRequest.setResizeDirection(resizeDirection);
        // handle feedback
        for (EditPart resizePart : editParts) {
          resizePart.showSourceFeedback(m_changeBoundsRequest);
        }
      } else {
        Point moveDelta = m_changeBoundsRequest.getMoveDelta();
        // calc move delta
        switch (request.getKeyCode()) {
          case SWT.ARROW_UP :
            moveDelta.y--;
            break;
          case SWT.ARROW_DOWN :
            moveDelta.y++;
            break;
          case SWT.ARROW_LEFT :
            moveDelta.x--;
            break;
          case SWT.ARROW_RIGHT :
            moveDelta.x++;
            break;
        }
        // handle feedback
        showLayoutTargetFeedback(m_changeBoundsRequest);
      }
    }
  }

  /**
   * Key released handler, which generates command after 300ms of key released
   */
  private void handleKeyReleased() {
    if (!m_isKeyboardMoving) {
      // previous 'keypressed' request was cancelled, so cancel 'release' too.
      return;
    }
    // start drag timer
    if (m_keyDragTimer == null) {
      m_keyDragTimer = new Timer();
      m_keyDragTimer.schedule(new TimerTask() {
        @Override
        public void run() {
          Display display = getViewer().getControl().getDisplay();
          display.syncExec(new Runnable() {
            public void run() {
              synchronized (m_changeBoundsRequest) {
                try {
                  List<EditPart> editParts = m_changeBoundsRequest.getEditParts();
                  //
                  if (editParts != null && !editParts.isEmpty()) {
                    // Create command
                    CompoundEditCommand command =
                        new CompoundEditCommand((ObjectInfo) getHost().getModel());
                    //
                    if (Request.REQ_MOVE.equals(m_changeBoundsRequest.getType())) {
                      eraseLayoutTargetFeedback(m_changeBoundsRequest);
                      command.add(getCommand(m_changeBoundsRequest));
                    } else {
                      for (EditPart part : editParts) {
                        part.eraseSourceFeedback(m_changeBoundsRequest);
                        command.add(part.getCommand(m_changeBoundsRequest));
                      }
                    }
                    // run command
                    getViewer().getEditDomain().executeCommand(command);
                  }
                } finally {
                  m_isKeyboardMoving = false;
                  m_keyDragTimer = null;
                  // clear request
                  m_changeBoundsRequest.setMoveDelta(new Point());
                  m_changeBoundsRequest.setSizeDelta(new Dimension());
                  m_changeBoundsRequest.setResizeDirection(IPositionConstants.NONE);
                }
              }
            }
          });
        }
      },
          300);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Convenience method for returning the <code>{@link IEditPartViewer}</code> for this host.
   */
  protected final IEditPartViewer getViewer() {
    return getHost().getViewer();
  }

  /**
   * @return <code>true</code> if work key move/resize process.
   */
  protected final boolean isKeyboardMoving() {
    return m_isKeyboardMoving;
  }

  /**
   * @return {@link Request} type association with resize.
   */
  protected abstract String getResizeRequestType();
}