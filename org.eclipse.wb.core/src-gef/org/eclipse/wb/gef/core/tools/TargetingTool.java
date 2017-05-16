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

import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.IEditPartViewer.IConditional;
import org.eclipse.wb.gef.core.requests.Request;

import java.util.Collection;
import java.util.Collections;

/**
 * The base implementation for tools which perform targeting of editparts. Targeting tools may
 * operate using either mouse drags or just mouse moves. Targeting tools work with a <i>target</i>
 * request. This request is used along with the mouse location to obtain an active target from the
 * current {@link IEditPartViewer}. This target is then asked for the <code>{@link Command}</code>
 * that performs the given request. The target is also asked to show target feedback.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public abstract class TargetingTool extends Tool {
  private EditPart m_target;
  private Request m_request;
  private boolean m_isLockTarget;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tool
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void deactivate() {
    eraseTargetFeedback();
    m_isLockTarget = false;
    m_target = null;
    m_request = null;
    super.deactivate();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EditPart
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the current target {@link EditPart}.
   */
  protected EditPart getTargetEditPart() {
    return m_target;
  }

  /**
   * Sets the target {@link EditPart}. If the target editpart is changing, this method will call
   * {@link #handleExitingEditPart()} for the previous target if not <code>null</code>, and
   * {@link #handleEnteredEditPart()} for the new target, if not <code>null</code>.
   */
  protected void setTargetEditPart(EditPart target) {
    if (m_target != target) {
      getTargetRequest().setTarget(target);
      if (m_target != null) {
        handleExitingEditPart();
      }
      //
      m_target = target;
      handleEnteredEditPart();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handling Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Called whenever the target editpart is about to change. By default, the target being exited is
   * asked to erase its feedback.
   */
  protected void handleExitingEditPart() {
    eraseTargetFeedback();
  }

  /**
   * Called whenever the target editpart has changed. By default, the new target is asked to show
   * feedback.
   */
  protected void handleEnteredEditPart() {
    updateTargetRequest();
    showTargetFeedback();
  }

  /**
   * Called when invalid input is encountered. By default, feedback is erased, and the current
   * command is set to <code>null</code>. The state does not change, so the caller must set the
   * state to {@link Tool#STATE_INVALID}.
   */
  protected void handleInvalidInput() {
    eraseTargetFeedback();
    setCommand(null);
  }

  @Override
  protected void handleViewerExited() {
    setTargetEditPart(null);
  }

  /**
   * Locks-in the given editpart as the target. Updating of the target will not occur until
   * {@link #unlockTargetEditPart()} is called.
   */
  protected void lockTargetEditPart(EditPart editpart) {
    if (editpart == null) {
      unlockTargetEditPart();
    } else {
      m_isLockTarget = true;
      setTargetEditPart(editpart);
    }
  }

  /**
   * Releases the targeting lock, and updates the target in case the mouse is already over a new
   * target.
   */
  protected void unlockTargetEditPart() {
    m_isLockTarget = false;
    updateTargetUnderMouse();
  }

  /**
   * Returns a List of objects that should be excluded as potential targets for the operation.
   */
  protected Collection<EditPart> getExclusionSet() {
    return Collections.emptyList();
  }

  /**
   * Returns the conditional object used for obtaining the target editpart from the viewer. By
   * default, a conditional is returned that tests whether an editpart at the current mouse location
   * indicates a target for the operation's request, using
   * {@link EditPart#getTargetEditPart(Request)}. If <code>null</code> is returned, then the
   * conditional fails, and the search continues.
   */
  protected IConditional getTargetingConditional() {
    return new IConditional() {
      public boolean evaluate(EditPart target) {
        updateTargetRequest(target);
        return target.getTargetEditPart(getTargetRequest()) != null;
      }
    };
  }

  /**
   * Updates the target {@link EditPart}. The target is updated by using the target conditional and
   * the target request. If the target has been locked, this method does nothing.
   */
  protected void updateTargetUnderMouse() {
    if (!m_isLockTarget) {
      EditPart editPart =
          getViewer().findTargetEditPart(
              m_currentScreenX,
              m_currentScreenY,
              getExclusionSet(),
              getTargetingConditional());
      if (editPart != null) {
        editPart = editPart.getTargetEditPart(getTargetRequest());
      }
      setTargetEditPart(editPart);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Lazily creates and returns the request used when communicating with the target editpart.
   */
  protected Request getTargetRequest() {
    if (m_request == null) {
      m_request = createTargetRequest();
    }
    return m_request;
  }

  /**
   * Creates the target request that will be used with the target editpart. This request will be
   * cached and updated as needed.
   */
  protected Request createTargetRequest() {
    return new Request();
  }

  /**
   * Subclasses should override to update the target request.
   */
  protected void updateTargetRequest() {
    getTargetRequest().setStateMask(m_stateMask);
  }

  /**
   * Subclasses should override to update the target request depending on potential target.
   */
  protected void updateTargetRequest(EditPart target) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Queries the target editpart for a command.
   */
  @Override
  protected Command getCommand() {
    return m_target == null ? null : m_target.getCommand(getTargetRequest());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_isShowingFeedback;

  /**
   * Asks the target editpart to show target feedback and sets the target feedback flag.
   */
  protected void showTargetFeedback() {
    if (m_target != null) {
      m_target.showTargetFeedback(getTargetRequest());
    }
    //
    m_isShowingFeedback = true;
  }

  /**
   * Asks the current target editpart to erase target feedback using the target request. If target
   * feedback is not being shown, this method does nothing and returns. Otherwise, the target
   * feedback flag is reset to false, and the target editpart is asked to erase target feedback.
   * This methods should rarely be overridden.
   */
  protected void eraseTargetFeedback() {
    if (m_isShowingFeedback) {
      m_isShowingFeedback = false;
      //
      if (m_target != null) {
        Request targetRequest = getTargetRequest();
        targetRequest.setEraseFeedback(true);
        try {
          m_target.eraseTargetFeedback(targetRequest);
        } finally {
          targetRequest.setEraseFeedback(false);
        }
      }
    }
  }
}