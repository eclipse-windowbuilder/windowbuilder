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
package org.eclipse.wb.internal.rcp.gef.part.rcp.perspective.shortcuts;

import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.shortcuts.PerspectiveShortcutContainerLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.PerspectiveShortcutContainerInfo;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for {@link PerspectiveShortcutContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class PerspectiveShortcutContainerEditPart extends AbstractShortcutContainerEditPart {
	private final PerspectiveShortcutContainerInfo m_container;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PerspectiveShortcutContainerEditPart(PerspectiveShortcutContainerInfo container) {
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
		installEditPolicy(new PerspectiveShortcutContainerLayoutEditPolicy(m_container));
	}
}
