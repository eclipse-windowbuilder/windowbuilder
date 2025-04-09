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
package org.eclipse.wb.internal.rcp.nebula.collapsiblebuttons;

import org.eclipse.wb.gef.core.requests.AbstractCreateRequest;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.gef.Request;

/**
 * A {@link Request} for adding new "button" on {@link CollapsibleButtonsInfo} widget.
 *
 * @author sablin_aa
 * @coverage nebula.gef
 */
public final class CollapsibleButtonDropRequest extends AbstractCreateRequest {
	public static final String TYPE = "drop Collapsible button";

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CollapsibleButtonDropRequest() {
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
