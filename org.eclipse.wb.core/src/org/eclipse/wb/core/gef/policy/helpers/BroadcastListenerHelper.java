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
package org.eclipse.wb.core.gef.policy.helpers;

import org.eclipse.wb.core.gef.policy.IDesignEditPolicy;
import org.eclipse.wb.core.gef.policy.IEditPolicyListener;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.BroadcastSupport;

/**
 * Helper for adding/removing listeners to {@link BroadcastSupport} on
 * {@link IDesignEditPolicy} activate/deactivate.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class BroadcastListenerHelper implements IEditPolicyListener {
	private final ObjectInfo m_object;
	private final IDesignEditPolicy m_editPolicy;
	private final Object m_listener;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @deprecated Use
	 *             {@link #BroadcastListenerHelper(ObjectInfo, IDesignEditPolicy, Object)}
	 *             instead.
	 */
	@Deprecated(since = "2026-06", forRemoval = true)
	public BroadcastListenerHelper(ObjectInfo object, @SuppressWarnings("removal") org.eclipse.wb.gef.core.policies.EditPolicy editPolicy, Object listener) {
		this(object, (IDesignEditPolicy) editPolicy, listener);
	}

	/**
	 * @since 1.24
	 */
	public BroadcastListenerHelper(ObjectInfo object, IDesignEditPolicy editPolicy, Object listener) {
		m_object = object;
		m_editPolicy = editPolicy;
		m_listener = listener;
		m_editPolicy.addEditPolicyListener(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IEditPolicyListener
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void activatePolicy(IDesignEditPolicy policy) {
		m_object.addBroadcastListener(m_listener);
	}

	@Override
	public void deactivatePolicy(IDesignEditPolicy policy) {
		m_object.removeBroadcastListener(m_listener);
		m_editPolicy.removeEditPolicyListener(this);
	}

	/**
	 * @deprecated Use {@link #activatePolicy(IDesignEditPolicy)} instead.
	 */
	@Deprecated(since = "2026-06", forRemoval = true)
	public void activatePolicy(@SuppressWarnings("removal") org.eclipse.wb.gef.core.policies.EditPolicy policy) {
		activatePolicy((IDesignEditPolicy) policy);
	}

	/**
	 * @deprecated Use {@link #deactivatePolicy(IDesignEditPolicy)} instead.
	 */
	@Deprecated(since = "2026-06", forRemoval = true)
	public void deactivatePolicy(@SuppressWarnings("removal") org.eclipse.wb.gef.core.policies.EditPolicy policy) {
		deactivatePolicy((IDesignEditPolicy) policy);
	}
}
