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
package org.eclipse.wb.internal.rcp.gefTree.part.rcp.perspective.shortcuts;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.rcp.gefTree.policy.rcp.perspective.shortcuts.PerspectiveShortcutContainerLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.PerspectiveShortcutContainerInfo;

/**
 * {@link EditPart} for {@link PerspectiveShortcutContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gefTree.part
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
