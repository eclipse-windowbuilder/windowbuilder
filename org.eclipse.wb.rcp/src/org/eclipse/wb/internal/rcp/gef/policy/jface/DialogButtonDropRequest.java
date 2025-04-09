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
package org.eclipse.wb.internal.rcp.gef.policy.jface;

import org.eclipse.wb.gef.core.requests.AbstractCreateRequest;
import org.eclipse.wb.internal.rcp.model.jface.DialogInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.gef.Request;

/**
 * A {@link Request} for adding new "button" on {@link DialogInfo} button bar.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class DialogButtonDropRequest extends AbstractCreateRequest {
	public static final String TYPE = "drop Dialog button";

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DialogButtonDropRequest() {
		super(TYPE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Component
	//
	////////////////////////////////////////////////////////////////////////////
	private ControlInfo m_button;

	/**
	 * @return the {@link ControlInfo} button to select after drop finished.
	 */
	public ControlInfo getButton() {
		return m_button;
	}

	/**
	 * Sets the {@link ControlInfo} button to select after drop finished.
	 */
	public void setButton(ControlInfo button) {
		m_button = button;
	}
}
