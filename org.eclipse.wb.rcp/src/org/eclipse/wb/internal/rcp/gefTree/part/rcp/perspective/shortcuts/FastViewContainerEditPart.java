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

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.rcp.gefTree.policy.rcp.perspective.shortcuts.FastViewContainerLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.FastViewContainerInfo;

/**
 * {@link EditPart} for {@link FastViewContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gefTree.part
 */
@Deprecated(since = "1.9.1400", forRemoval = true)
public final class FastViewContainerEditPart extends AbstractShortcutContainerEditPart {
	@SuppressWarnings("removal")
	private final FastViewContainerInfo m_container;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	@Deprecated
	@SuppressWarnings("removal")
	public FastViewContainerEditPart(FastViewContainerInfo container) {
		super(container);
		m_container = container;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Deprecated
	@SuppressWarnings("removal")
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(new FastViewContainerLayoutEditPolicy(m_container));
	}
}
