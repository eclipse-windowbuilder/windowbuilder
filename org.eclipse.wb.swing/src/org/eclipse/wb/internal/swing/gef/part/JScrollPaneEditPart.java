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

import org.eclipse.wb.internal.swing.gef.policy.component.JScrollPaneLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.JScrollPaneInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;

/**
 * The {@link EditPart} for {@link JScrollPaneInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.part
 */
public final class JScrollPaneEditPart extends ComponentEditPart {
	private final JScrollPaneInfo m_component;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JScrollPaneEditPart(JScrollPaneInfo component) {
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
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new JScrollPaneLayoutEditPolicy(m_component));
	}
}
