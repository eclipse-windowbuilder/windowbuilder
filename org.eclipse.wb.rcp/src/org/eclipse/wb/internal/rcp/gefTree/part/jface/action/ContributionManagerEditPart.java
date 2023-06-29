/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.gefTree.part.jface.action;

import org.eclipse.wb.core.gefTree.part.JavaEditPart;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.tree.policies.TerminatorLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.jface.action.ContributionManagerInfo;

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
