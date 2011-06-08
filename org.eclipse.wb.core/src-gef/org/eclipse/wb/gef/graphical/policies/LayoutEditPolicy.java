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
package org.eclipse.wb.gef.graphical.policies;

import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.events.IEditPartListener;
import org.eclipse.wb.gef.core.policies.IEditPartDecorationListener;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.GroupRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;

import java.util.List;

/**
 * @author lobas_av
 * @coverage gef.graphical
 */
public abstract class LayoutEditPolicy extends GraphicalEditPolicy {
  private final IEditPartListener m_listener = new IEditPartListener() {
    public void childAdded(EditPart child, int index) {
      decorateChild(child);
    }

    public void removingChild(EditPart child, int index) {
      undecorateChild(child);
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // EditPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void activate() {
    getHost().addEditPartListener(m_listener);
    for (EditPart child : getHost().getChildren()) {
      decorateChild(child);
    }
    super.activate();
  }

  @Override
  public void deactivate() {
    for (EditPart child : getHost().getChildren()) {
      undecorateChild(child);
    }
    getHost().removeEditPartListener(m_listener);
    eraseLayoutTargetFeedback(null);
    super.deactivate();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Decorate child
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method allows {@link LayoutEditPolicy} decorate each child {@link EditPart}, usually set
   * {@link SelectionEditPolicy}.
   */
  protected void decorateChild(EditPart child) {
    fire_decorateChild(child);
  }

  /**
   * This method allows {@link LayoutEditPolicy} undo decoration done by
   * {@link #decorateChild(EditPart)}.
   */
  protected void undecorateChild(EditPart child) {
    fire_undecorateChild(child);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request/Command
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ILayoutRequestValidator} for validating {@link Request#REQ_CREATE},
   *         {@link Request#REQ_PASTE}, {@link Request#REQ_MOVE} and {@link Request#REQ_ADD}
   *         requests.
   */
  protected ILayoutRequestValidator getRequestValidator() {
    return ILayoutRequestValidator.TRUE;
  }

  /**
   * @return <code>true</code> if the {@link Request} is an {@link Request#REQ_CREATE},
   *         {@link Request#REQ_PASTE}, {@link Request#REQ_MOVE} or {@link Request#REQ_ADD}.
   */
  protected boolean isRequestCondition(Request request) {
    Object type = request.getType();
    ILayoutRequestValidator validator = getRequestValidator();
    EditPart host = getHost();
    if (type == Request.REQ_CREATE) {
      return validator.validateCreateRequest(host, (CreateRequest) request);
    }
    if (type == Request.REQ_PASTE) {
      return validator.validatePasteRequest(host, (PasteRequest) request);
    }
    if (type == Request.REQ_MOVE) {
      return validator.validateMoveRequest(host, (ChangeBoundsRequest) request);
    }
    if (type == Request.REQ_ADD) {
      return validator.validateAddRequest(host, (ChangeBoundsRequest) request);
    }
    return false;
  }

  @Override
  public boolean understandsRequest(Request request) {
    return isRequestCondition(request);
  }

  /**
   * Returns the <i>host</i> if the {@link Request} is an {@link Request#REQ_CREATE},
   * {@link Request#REQ_PASTE}, {@link Request#REQ_MOVE} or {@link Request#REQ_ADD}.
   */
  @Override
  public EditPart getTargetEditPart(Request request) {
    return isRequestCondition(request) ? getHost() : null;
  }

  /**
   * Factors incoming requests into various specific methods.
   */
  @Override
  //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "BC_UNCONFIRMED_CAST")
  public Command getCommand(Request request) {
    Object type = request.getType();
    if (type == Request.REQ_CREATE) {
      return getCreateCommand((CreateRequest) request);
    }
    if (type == Request.REQ_PASTE) {
      return getPasteCommand((PasteRequest) request);
    }
    if (type == Request.REQ_MOVE) {
      return getMoveCommand((ChangeBoundsRequest) request);
    }
    if (type == Request.REQ_ADD) {
      return getAddCommand((ChangeBoundsRequest) request);
    }
    if (type == Request.REQ_ORPHAN) {
      return getOrphanCommand((GroupRequest) request);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Command} for {@link Request#REQ_CREATE}.
   */
  protected Command getCreateCommand(CreateRequest request) {
    return null;
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_PASTE}.
   */
  protected Command getPasteCommand(PasteRequest request) {
    return null;
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_MOVE}.
   */
  protected Command getMoveCommand(ChangeBoundsRequest request) {
    return null;
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_ADD}.
   */
  protected Command getAddCommand(ChangeBoundsRequest request) {
    return null;
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_ORPHAN}.
   */
  protected Command getOrphanCommand(GroupRequest request) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void showTargetFeedback(Request request) {
    if (isRequestCondition(request)) {
      showLayoutTargetFeedback(request);
    }
  }

  @Override
  public void eraseTargetFeedback(Request request) {
    eraseLayoutTargetFeedback(request);
  }

  /**
   * Shows target feedback for {@link Request#REQ_ADD}, {@link Request#REQ_MOVE},
   * {@link Request#REQ_CREATE} or {@link Request#REQ_PASTE}.
   */
  protected void showLayoutTargetFeedback(Request request) {
  }

  /**
   * Erases target feedback for {@link Request#REQ_ADD}, {@link Request#REQ_MOVE},
   * {@link Request#REQ_CREATE} or {@link Request#REQ_PASTE}.
   */
  protected void eraseLayoutTargetFeedback(Request request) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds a {@link IEditPartDecorationListener} to this policy.
   */
  public void addEditPartListener(IEditPartDecorationListener listener) {
    getEnsureEventTable().addListener(IEditPartDecorationListener.class, listener);
  }

  /**
   * Removes the first occurrence of the specified listener from the list of listeners.
   */
  public void removeEditPartListener(IEditPartDecorationListener listener) {
    getEnsureEventTable().removeListener(IEditPartDecorationListener.class, listener);
  }

  /**
   * Fires {@link IEditPartDecorationListener#decorate(EditPart)}.
   */
  private void fire_decorateChild(EditPart child) {
    List<IEditPartDecorationListener> listeners = getListeners(IEditPartDecorationListener.class);
    if (listeners != null && !listeners.isEmpty()) {
      for (IEditPartDecorationListener listener : listeners) {
        listener.decorate(child);
      }
    }
  }

  /**
   * Fires {@link IEditPartDecorationListener#undecorate(EditPart)}.
   */
  private void fire_undecorateChild(EditPart child) {
    List<IEditPartDecorationListener> listeners = getListeners(IEditPartDecorationListener.class);
    if (listeners != null && !listeners.isEmpty()) {
      for (IEditPartDecorationListener listener : listeners) {
        listener.undecorate(child);
      }
    }
  }
}