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
package org.eclipse.wb.internal.rcp.gef.part.jface;

import org.eclipse.wb.gef.graphical.policies.TerminatorLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.jface.DialogButtonBarLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.jface.DialogInfo;
import org.eclipse.wb.internal.swt.gef.part.CompositeEditPart;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for "buttonBar" part of {@link DialogInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class DialogButtonBarEditPart extends CompositeEditPart {
	private final CompositeInfo m_composite;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DialogButtonBarEditPart(CompositeInfo composite) {
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
		installEditPolicy(new DialogButtonBarLayoutEditPolicy(m_composite));
		installEditPolicy(new TerminatorLayoutEditPolicy());
	}
}
