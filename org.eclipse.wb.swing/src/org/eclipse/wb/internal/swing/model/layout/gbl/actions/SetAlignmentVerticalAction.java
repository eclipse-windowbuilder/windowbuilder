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
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * {@link Action} for modifying vertical alignment.
 *
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class SetAlignmentVerticalAction extends AbstractAction {
	private final RowInfo.Alignment m_alignment;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetAlignmentVerticalAction(AbstractGridBagConstraintsInfo constraints,
			String text,
			ImageDescriptor icon,
			RowInfo.Alignment alignment) {
		super(constraints, text, AS_RADIO_BUTTON, icon, false);
		m_alignment = alignment;
		// set check for current alignment
		setChecked(constraints.getVerticalAlignment() == m_alignment);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void runEx() throws Exception {
		m_constraints.setVerticalAlignment(m_alignment);
	}
}