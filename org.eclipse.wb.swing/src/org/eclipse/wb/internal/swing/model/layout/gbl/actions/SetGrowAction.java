/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.model.layout.gbl.actions;

import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagConstraintsInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.DimensionInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * {@link Action} for that sets weight for {@link DimensionInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class SetGrowAction extends AbstractAction {
	private final DimensionInfo m_dimension;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetGrowAction(AbstractGridBagConstraintsInfo constraints,
			String text,
			ImageDescriptor icon,
			boolean horizontal) {
		super(constraints, text, AS_CHECK_BOX, icon, horizontal);
		m_dimension = horizontal ? constraints.getColumn() : constraints.getRow();
		setChecked(m_dimension.hasWeight());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void runEx() throws Exception {
		m_dimension.setWeight(m_dimension.hasWeight() ? 0.0 : 1.0);
	}
}