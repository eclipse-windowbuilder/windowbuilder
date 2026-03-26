/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.gef.core.policies;

import org.eclipse.wb.core.gef.policy.IDesignEditPolicy;
import org.eclipse.wb.core.gef.policy.IEditPolicyListener;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.policies.GraphicalEditPolicy;

import org.eclipse.draw2d.EventListenerList;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.LayerManager;

import java.util.Iterator;

@SuppressWarnings("removal")
public class DesignEditPolicy extends GraphicalEditPolicy implements IDesignEditPolicy, IRequestEditPolicy {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
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
	@Override
	public void activate() {
		m_isActive = true;
		fireActivate();
	}

	/**
	 * Deactivates the {@link EditPolicy}, the inverse of {@link #activate()}. Deactivate is called
	 * when the <i>host</i> is deactivated, or when the {@link EditPolicy} is uninstalled from an
	 * active host. Deactivate unhooks any listeners, and removes all feedback.
	 */
	@Override
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

	////////////////////////////////////////////////////////////////////////////
	//
	// Request/Command
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Performs the specified Request. This method can be used to send a generic message to an
	 * EditPolicy. Subclasses should extend this method to handle Requests. For now, the default
	 * implementation does not handle any requests.
	 */
	@Override
	public void performRequest(Request request) {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events
	//
	////////////////////////////////////////////////////////////////////////////
	private EventListenerList m_eventTable;

	/**
	 * Adds a listener to the {@link EditPolicy}.
	 */
	@Override
	public void addEditPolicyListener(IEditPolicyListener listener) {
		getEnsureEventTable().addListener(IEditPolicyListener.class, listener);
	}

	/**
	 * Removes the first occurrence of the specified listener from the list of listeners. Does nothing
	 * if the listener was not present.
	 */
	@Override
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
	public <L extends Object> Iterator<L> getListeners(Class<L> listenerClass) {
		return m_eventTable == null ? null : m_eventTable.getListeners(listenerClass);
	}

	/**
	 * Access to <code>{@link EventTable}</code> use lazy creation mechanism.
	 */
	protected EventListenerList getEnsureEventTable() {
		if (m_eventTable == null) {
			m_eventTable = new EventListenerList();
		}
		return m_eventTable;
	}

	private void fireActivate() {
		Iterator<IEditPolicyListener> listeners = getListeners(IEditPolicyListener.class);
		if (listeners != null) {
			listeners.forEachRemaining(listener -> listener.activatePolicy(this));
		}
	}

	private void fireDeactivate() {
		Iterator<IEditPolicyListener> listeners = getListeners(IEditPolicyListener.class);
		if (listeners != null) {
			listeners.forEachRemaining(listener -> listener.deactivatePolicy(this));
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

	////////////////////////////////////////////////////////////////////////////
	//
	// Layer's
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Obtains the specified layer.
	 */
	@Override
	protected final Layer getLayer(Object layer) {
		if (isOnMenuLayer()) {
			if (LayerConstants.HANDLE_LAYER.equals(layer)) {
				layer = IEditPartViewer.MENU_HANDLE_LAYER;
			} else if (IEditPartViewer.HANDLE_LAYER_STATIC.equals(layer)) {
				layer = IEditPartViewer.MENU_HANDLE_LAYER_STATIC;
			} else if (LayerConstants.FEEDBACK_LAYER.equals(layer)) {
				layer = IEditPartViewer.MENU_FEEDBACK_LAYER;
			}
		}
		return (Layer) super.getLayer(layer);
	}

	/**
	 * @return the {@link Layer} for {@link IEditPartViewer#FEEDBACK_LAYER}.
	 */
	@Override
	protected Layer getFeedbackLayer() {
		return getLayer(LayerConstants.FEEDBACK_LAYER);
	}

	/**
	 * @return <code>true</code> if host {@link EditPart} is located on
	 *         {@link IEditPartViewer#MENU_PRIMARY_LAYER}.
	 */
	private boolean isOnMenuLayer() {
		Layer menuPrimaryLayer = (Layer) LayerManager.Helper.find(getHost()).getLayer(IEditPartViewer.MENU_PRIMARY_LAYER);
		for (IFigure figure = getHostFigure(); figure != null; figure = figure.getParent()) {
			if (figure == menuPrimaryLayer) {
				return true;
			}
		}
		// no, probably normal PRIMARY_LAYER
		return false;
	}
}
