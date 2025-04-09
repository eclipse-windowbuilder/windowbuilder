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
package org.eclipse.wb.internal.rcp.gef.policy.jface.action;

import org.eclipse.wb.gef.core.requests.AbstractCreateRequest;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContributionItemInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;

import org.eclipse.gef.Request;

/**
 * A {@link Request} for adding new {@link ActionInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class ActionDropRequest extends AbstractCreateRequest {
	public static final String TYPE = "drop JFace Action";
	private final ActionInfo m_action;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ActionDropRequest(ActionInfo action) {
		super(TYPE);
		m_action = action;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link ActionInfo} to drop.
	 */
	public ActionInfo getAction() {
		return m_action;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Item
	//
	////////////////////////////////////////////////////////////////////////////
	private ActionContributionItemInfo m_item;

	/**
	 * @return the {@link ActionContributionItemInfo} to select after drop finished.
	 */
	public ActionContributionItemInfo getItem() {
		return m_item;
	}

	/**
	 * Sets the {@link ActionContributionItemInfo} to select after drop finished.
	 */
	public void setItem(ActionContributionItemInfo item) {
		m_item = item;
	}
}
