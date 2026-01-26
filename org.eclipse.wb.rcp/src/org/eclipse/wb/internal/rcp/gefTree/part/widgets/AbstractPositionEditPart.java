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
package org.eclipse.wb.internal.rcp.gefTree.part.widgets;

import org.eclipse.wb.core.gefTree.part.ObjectEditPart;
import org.eclipse.wb.internal.rcp.gefTree.policy.AbstractPositionLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.widgets.AbstractPositionInfo;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for {@link AbstractPositionInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gefTree.part
 */
public final class AbstractPositionEditPart extends ObjectEditPart {
	private final AbstractPositionInfo m_position;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractPositionEditPart(AbstractPositionInfo position) {
		super(position);
		m_position = position;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refreshEditPolicies() {
		super.refreshEditPolicies();
		installEditPolicy(new AbstractPositionLayoutEditPolicy(m_position));
	}
}
