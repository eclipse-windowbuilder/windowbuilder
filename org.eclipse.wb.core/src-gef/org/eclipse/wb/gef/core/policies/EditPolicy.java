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
package org.eclipse.wb.gef.core.policies;

import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.events.IEditPolicyListener;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.internal.draw2d.events.EventTable;

import java.util.List;

/**
 * @author lobas_av
 * @coverage gef.core
 */
public abstract class EditPolicy {
  /**
   * The key used to install a <i>selection</i> {@link EditPolicy}.
   */
  public static final String SELECTION_ROLE = "Selection Feedback";
  /**
   * The key used to install a <i>layout</i> {@link EditPolicy}.
   */
  public static final String LAYOUT_ROLE = "LayoutEditPolicy";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private EditPart m_host;
  private boolean m_isActive;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Activates this {@link EditPolicy}. The {@link EditPolicy} might need to hook listeners. These
   * listeners should be unhooked in <code>deactivate()</code>. The {@link EditPolicy} might also
   * contribute feedback/visuals immediately, such as <i>selection handles</i> if the
   * {@link EditPart} was selected at the time of activation.
   * <P>
   * Activate is called after the <i>host</i> has been set, and that host has been activated.
   */
  public void activate() {
    m_isActive = true;
    fireActivate();
  }

  /**
   * Deactivates the {@link EditPolicy}, the inverse of {@link #activate()}. Deactivate is called
   * when the <i>host</i> is deactivated, or when the {@link EditPolicy} is uninstalled from an
   * active host. Deactivate unhooks any listeners, and removes all feedback.
   */
  public void deactivate() {
    fireDeactivate();
    m_isActive = false;
  }

  /**
   * Returns <code>true</code> if the {@link EditPolicy} is active. {@link EditPolicy} are active
   * after {@link #activate()} is called, and until {@link #deactivate()} is called.
   */
  public boolean isActive() {
    return m_isActive;
  }

  /**
   * Method called after this {@link EditPolicy} deleted from host {@link EditPart}.
   */
  public void dispose() {
  }

  /**
   * @return the <i>host</i> {@link EditPart} on which this policy is installed.
   */
  public EditPart getHost() {
    return m_host;
  }

  /**
   * Sets the host in which this {@link EditPolicy} is installed.
   *
   * @param editpart
   *          the host {@link EditPart}
   */
  public void setHost(EditPart host) {
    m_host = host;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request/Command
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the <code>{@link Command}</code> contribution for the given
   * <code>{@link Request}</code>, or <code>null</code>. <code>null</code> is treated as a no-op by
   * the caller, or an empty contribution.
   */
  public Command getCommand(Request request) {
    return null;
  }

  /**
   * Returns <code>null</code> or the appropriate <code>{@link EditPart}</code> for the specified
   * <code>{@link Request}</code>. In general, this {@link EditPolicy} will return its <i>host</i>
   * {@link EditPart} if it understands the {@link Request}. Otherwise, it will return
   * <code>null</code>.
   * <P>
   * This method is declared on {@link EditPart#getTargetEditPart(Request) EditPart}, and is
   * redeclared here so that {@link EditPart} can delegate its implementation to each of its
   * EditPolicies. The first non- <code>null</code> result returned by an {@link EditPolicy} is
   * returned by the {@link EditPart}.
   */
  public EditPart getTargetEditPart(Request request) {
    return null;
  }

  /**
   * Returns <code>true</code> if this {@link EditPolicy} understand the specified request.
   */
  public boolean understandsRequest(Request request) {
    return false;
  }

  /**
   * Performs the specified Request. This method can be used to send a generic message to an
   * EditPolicy. Subclasses should extend this method to handle Requests. For now, the default
   * implementation does not handle any requests.
   */
  public void performRequest(Request request) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Shows or updates <i>source feedback</i> for the specified <code>{@link Request}</code>. This
   * method may be called repeatedly for the purpose of updating feedback based on changes to the
   * {@link Request}.
   * <P>
   * Does nothing if the EditPolicy does not recognize the given {@link Request}.
   * <P>
   * This method is declared on {@link EditPart#showSourceFeedback(Request) EditPart}, and is
   * redeclared here so that EditPart can delegate its implementation to each of its EditPolicies.
   */
  public void showSourceFeedback(Request request) {
  }

  /**
   * Erases source feedback based on the given <code>{@link Request}</code>. Does nothing if the
   * {@link EditPolicy} does not apply to the given {@link Request}.
   * <P>
   * This method is declared on {@link EditPart#eraseSourceFeedback(Request) EditPart}, and is
   * redeclared here so that {@link EditPart} can delegate its implementation to each of its
   * EditPolicies.
   */
  public void eraseSourceFeedback(Request request) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Shows or updates <i>target feedback</i> for the specified <code>{@link Request}</code>. This
   * method may be called repeatedly for the purpose of updating feedback based on changes to the
   * {@link Request}.
   * <P>
   * Does nothing if the EditPolicy does not recognize the given request.
   * <P>
   * This method is declared on {@link EditPart#showTargetFeedback(Request) EditPart}, and is
   * redeclared here so that {@link EditPart} can delegate its implementation to each of its
   * EditPolicies.
   */
  public void showTargetFeedback(Request request) {
  }

  /**
   * Erases target feedback based on the given <code>{@link Request}</code>. Does nothing if the
   * {@link EditPolicy} does not apply to the given {@link Request}.
   * <P>
   * This method is declared on {@link EditPart#eraseTargetFeedback(Request) EditPart}, and is
   * redeclared here so that {@link EditPart} can delegate its implementation to each of its
   * EditPolicies.
   */
  public void eraseTargetFeedback(Request request) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  private EventTable m_eventTable;

  /**
   * Adds a listener to the {@link EditPolicy}.
   */
  public void addEditPolicyListener(IEditPolicyListener listener) {
    getEnsureEventTable().addListener(IEditPolicyListener.class, listener);
  }

  /**
   * Removes the first occurrence of the specified listener from the list of listeners. Does nothing
   * if the listener was not present.
   */
  public void removeEditPolicyListener(IEditPolicyListener listener) {
    getEnsureEventTable().removeListener(IEditPolicyListener.class, listener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return all registers listeners for given class or <code>null</code>.
   */
  public <L extends Object> List<L> getListeners(Class<L> listenerClass) {
    return m_eventTable == null ? null : m_eventTable.getListeners(listenerClass);
  }

  /**
   * Access to <code>{@link EventTable}</code> use lazy creation mechanism.
   */
  protected EventTable getEnsureEventTable() {
    if (m_eventTable == null) {
      m_eventTable = new EventTable();
    }
    return m_eventTable;
  }

  private void fireActivate() {
    List<IEditPolicyListener> listeners = getListeners(IEditPolicyListener.class);
    if (listeners != null && !listeners.isEmpty()) {
      for (IEditPolicyListener listener : listeners) {
        listener.activatePolicy(this);
      }
    }
  }

  private void fireDeactivate() {
    List<IEditPolicyListener> listeners = getListeners(IEditPolicyListener.class);
    if (listeners != null && !listeners.isEmpty()) {
      for (IEditPolicyListener listener : listeners) {
        listener.deactivatePolicy(this);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Model Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Convenience method to return the host's model.
   */
  protected final Object getHostModel() {
    return getHost().getModel();
  }
}