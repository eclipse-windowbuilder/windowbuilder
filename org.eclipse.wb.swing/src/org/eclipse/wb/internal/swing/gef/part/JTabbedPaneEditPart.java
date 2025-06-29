/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.swing.gef.policy.component.JTabbedPaneLayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.policy.component.JTabbedPaneTabLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.JTabbedPaneInfo;

import org.eclipse.gef.EditPolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link EditPart} for {@link JTabbedPaneInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.part
 */
public final class JTabbedPaneEditPart extends ComponentEditPart {
	private final JTabbedPaneInfo m_component;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JTabbedPaneEditPart(JTabbedPaneInfo component) {
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
		installEditPolicy(new JTabbedPaneTabLayoutEditPolicy(m_component));
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new JTabbedPaneLayoutEditPolicy(m_component));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Object> getModelChildren() {
		List<Object> children = new ArrayList<>();
		children.addAll(super.getModelChildren());
		children.addAll(m_component.getTabs());
		return children;
	}
}
