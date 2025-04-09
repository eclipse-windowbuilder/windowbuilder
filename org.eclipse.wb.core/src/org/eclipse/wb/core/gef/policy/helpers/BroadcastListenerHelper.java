/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.BroadcastSupport;
import org.eclipse.wb.gef.core.events.IEditPolicyListener;
import org.eclipse.wb.gef.core.policies.EditPolicy;

/**
 * Helper for adding/removing listeners to {@link BroadcastSupport} on {@link EditPolicy}
 * activate/deactivate.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class BroadcastListenerHelper implements IEditPolicyListener {
	private final ObjectInfo m_object;
	private final EditPolicy m_editPolicy;
	private final Object m_listener;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BroadcastListenerHelper(ObjectInfo object, EditPolicy editPolicy, Object listener) {
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
	public void activatePolicy(EditPolicy policy) {
		m_object.addBroadcastListener(m_listener);
	}

	@Override
	public void deactivatePolicy(EditPolicy policy) {
		m_object.removeBroadcastListener(m_listener);
		m_editPolicy.removeEditPolicyListener(this);
	}
}
