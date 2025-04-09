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
package org.eclipse.wb.internal.swing.FormLayout.gef;

import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridHelper;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.gef.graphical.policies.GraphicalEditPolicy;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;

/**
 * Helper for displaying grid for {@link FormLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.policy
 */
public final class FormGridHelper extends AbstractGridHelper {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormGridHelper(GraphicalEditPolicy editPolicy, boolean forTarget) {
		super(editPolicy, forTarget);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedback
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IGridInfo getGridInfo() {
		return ((FormLayoutInfo) getAbstractLayout()).getGridInfo();
	}
}
