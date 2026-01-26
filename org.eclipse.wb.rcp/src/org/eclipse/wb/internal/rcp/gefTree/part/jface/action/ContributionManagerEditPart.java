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
package org.eclipse.wb.internal.rcp.gefTree.part.jface.action;

import org.eclipse.wb.core.gefTree.part.JavaEditPart;
import org.eclipse.wb.gef.tree.policies.TerminatorLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.jface.action.ContributionManagerInfo;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for {@link ContributionManagerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gefTree.part
 */
public final class ContributionManagerEditPart extends JavaEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ContributionManagerEditPart(ContributionManagerInfo manager) {
		super(manager);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refreshEditPolicies() {
		super.refreshEditPolicies();
		installEditPolicy(new TerminatorLayoutEditPolicy());
	}
}
