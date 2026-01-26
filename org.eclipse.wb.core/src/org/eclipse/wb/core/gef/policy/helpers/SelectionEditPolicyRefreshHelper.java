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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.eclipse.gef.EditPart;

/**
 * Helper for re-displaying selection of {@link SelectionEditPolicy} on refresh broadcast in model.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public class SelectionEditPolicyRefreshHelper {
	private final SelectionEditPolicy m_policy;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SelectionEditPolicyRefreshHelper(SelectionEditPolicy policy) {
		m_policy = policy;
		ObjectInfo hierarchyObject = GlobalState.getActiveObject();
		new BroadcastListenerHelper(hierarchyObject, policy, new ObjectEventListener() {
			@Override
			public void refreshed() throws Exception {
				boolean isActivePolicy = m_policy.isActive();
				boolean isSelectedHost = m_policy.getHost().getSelected() != EditPart.SELECTED_NONE;
				if (isActivePolicy && isSelectedHost) {
					m_policy.refreshSelection();
				}
			}
		});
	}
}
