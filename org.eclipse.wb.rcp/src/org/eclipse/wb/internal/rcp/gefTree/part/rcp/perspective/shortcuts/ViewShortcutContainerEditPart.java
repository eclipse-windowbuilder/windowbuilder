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
package org.eclipse.wb.internal.rcp.gefTree.part.rcp.perspective.shortcuts;

import org.eclipse.wb.internal.rcp.gefTree.policy.rcp.perspective.shortcuts.ViewShortcutContainerLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.ViewShortcutContainerInfo;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for {@link ViewShortcutContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gefTree.part
 */
public final class ViewShortcutContainerEditPart extends AbstractShortcutContainerEditPart {
	private final ViewShortcutContainerInfo m_container;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewShortcutContainerEditPart(ViewShortcutContainerInfo container) {
		super(container);
		m_container = container;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(new ViewShortcutContainerLayoutEditPolicy(m_container));
	}
}
