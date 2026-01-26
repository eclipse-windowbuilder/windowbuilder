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
package org.eclipse.wb.internal.rcp.gef.part.forms;

import org.eclipse.wb.gef.graphical.policies.TerminatorLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.forms.ExpandableCompositeLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.forms.ExpandableCompositeInfo;
import org.eclipse.wb.internal.swt.gef.part.CompositeEditPart;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for {@link ExpandableCompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class ExpandableCompositeEditPart extends CompositeEditPart {
	private final ExpandableCompositeInfo m_composite;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ExpandableCompositeEditPart(ExpandableCompositeInfo composite) {
		super(composite);
		m_composite = composite;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refreshEditPolicies() {
		super.refreshEditPolicies();
		installEditPolicy(new ExpandableCompositeLayoutEditPolicy(m_composite));
		installEditPolicy(new TerminatorLayoutEditPolicy());
	}
}
