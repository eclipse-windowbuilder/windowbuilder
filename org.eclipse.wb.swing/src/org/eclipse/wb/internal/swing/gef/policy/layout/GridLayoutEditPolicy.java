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
package org.eclipse.wb.internal.swing.gef.policy.layout;

import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.layout.GridLayoutInfo;

import org.eclipse.gef.Request;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link GridLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class GridLayoutEditPolicy extends GenericFlowLayoutEditPolicy {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridLayoutEditPolicy(GridLayoutInfo layout) {
		super(layout);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractFlowLayoutEditPolicy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isHorizontal(Request request) {
		return true;
	}
}
