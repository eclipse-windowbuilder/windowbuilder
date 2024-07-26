/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.layout.gbl.actions;

import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagConstraintsInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * {@link Action} for modifying horizontal alignment.
 *
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class SetAlignmentHorizontalAction extends AbstractAction {
	private final ColumnInfo.Alignment m_alignment;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetAlignmentHorizontalAction(AbstractGridBagConstraintsInfo constraints,
			String text,
			ImageDescriptor icon,
			ColumnInfo.Alignment alignment) {
		super(constraints, text, AS_RADIO_BUTTON, icon, true);
		m_alignment = alignment;
		// set check for current alignment
		setChecked(constraints.getHorizontalAlignment() == m_alignment);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void runEx() throws Exception {
		m_constraints.setHorizontalAlignment(m_alignment);
	}
}