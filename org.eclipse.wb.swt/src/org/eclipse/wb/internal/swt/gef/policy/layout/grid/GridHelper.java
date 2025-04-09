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
package org.eclipse.wb.internal.swt.gef.policy.layout.grid;

import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridHelper;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.gef.graphical.policies.GraphicalEditPolicy;
import org.eclipse.wb.internal.swt.model.layout.grid.IGridLayoutInfo;

/**
 * Helper for displaying grid for {@link IGridLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gef.GridLayout
 */
public final class GridHelper extends AbstractGridHelper {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridHelper(GraphicalEditPolicy editPolicy, boolean forTarget) {
		super(editPolicy, forTarget);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedback
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IGridInfo getGridInfo() {
		return ((IGridLayoutInfo<?>) getAbstractLayout()).getGridInfo();
	}
}
