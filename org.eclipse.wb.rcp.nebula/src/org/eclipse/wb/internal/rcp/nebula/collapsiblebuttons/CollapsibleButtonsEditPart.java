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
package org.eclipse.wb.internal.rcp.nebula.collapsiblebuttons;

import org.eclipse.wb.gef.graphical.policies.TerminatorLayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.part.CompositeEditPart;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for {@link CollapsibleButtonsInfo}.
 *
 * @author sablin_aa
 * @coverage nebula.gef
 */
public final class CollapsibleButtonsEditPart extends CompositeEditPart {
	private final CollapsibleButtonsInfo m_collButtons;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CollapsibleButtonsEditPart(CollapsibleButtonsInfo collButtons) {
		super(collButtons);
		m_collButtons = collButtons;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refreshEditPolicies() {
		super.refreshEditPolicies();
		installEditPolicy(new CollapsibleButtonsLayoutEditPolicy(m_collButtons));
		installEditPolicy(new TerminatorLayoutEditPolicy());
	}
}
