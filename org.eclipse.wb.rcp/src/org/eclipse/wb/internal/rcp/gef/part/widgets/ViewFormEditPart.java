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
package org.eclipse.wb.internal.rcp.gef.part.widgets;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.policies.TerminatorLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.widgets.ViewFormLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.widgets.ViewFormInfo;
import org.eclipse.wb.internal.swt.gef.part.CompositeEditPart;

/**
 * {@link EditPart} for {@link ViewFormInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class ViewFormEditPart extends CompositeEditPart {
	private final ViewFormInfo m_composite;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewFormEditPart(ViewFormInfo composite) {
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
		installEditPolicy(new ViewFormLayoutEditPolicy(m_composite));
		installEditPolicy(new TerminatorLayoutEditPolicy());
	}
}
