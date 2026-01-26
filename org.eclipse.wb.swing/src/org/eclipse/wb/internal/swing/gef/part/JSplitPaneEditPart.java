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
package org.eclipse.wb.internal.swing.gef.part;

import org.eclipse.wb.internal.swing.gef.policy.component.JSplitPaneLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.JSplitPaneInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;

/**
 * The {@link EditPart} for {@link JSplitPaneInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.part
 */
public final class JSplitPaneEditPart extends ComponentEditPart {
	private final JSplitPaneInfo m_component;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JSplitPaneEditPart(JSplitPaneInfo component) {
		super(component);
		m_component = component;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new JSplitPaneLayoutEditPolicy(m_component));
	}
}
