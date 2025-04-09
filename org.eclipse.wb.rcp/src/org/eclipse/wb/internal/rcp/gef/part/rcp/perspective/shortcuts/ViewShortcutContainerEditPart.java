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
package org.eclipse.wb.internal.rcp.gef.part.rcp.perspective.shortcuts;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.shortcuts.ViewShortcutContainerLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.ViewShortcutContainerInfo;

/**
 * {@link EditPart} for {@link ViewShortcutContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
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
