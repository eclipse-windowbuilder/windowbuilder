/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.events.IEditPolicyListener;
import org.eclipse.wb.gef.core.policies.EditPolicy;

import org.eclipse.gef.EditPartListener;

/**
 * Helper for adding/removing {@link EditPartListener} for host {@link EditPart} on
 * {@link EditPolicy} activate/deactivate.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class SelectionListenerHelper implements IEditPolicyListener {
	private final EditPolicy m_editPolicy;
	private final EditPartListener m_listener;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SelectionListenerHelper(EditPolicy editPolicy, EditPartListener listener) {
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
		m_editPolicy.getHost().addEditPartListener(m_listener);
	}

	@Override
	public void deactivatePolicy(EditPolicy policy) {
		m_editPolicy.getHost().removeEditPartListener(m_listener);
		m_editPolicy.removeEditPolicyListener(this);
	}
}
