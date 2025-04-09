/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.internal.swing.MigLayout.gef.header.actions;

import org.eclipse.wb.internal.swing.MigLayout.gef.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo.Alignment;

import org.eclipse.jface.action.Action;

/**
 * {@link Action} for modifying alignment of {@link MigRowInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.header
 */
public final class SetAlignmentRowAction extends DimensionHeaderAction<MigRowInfo> {
	private final Alignment m_alignment;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetAlignmentRowAction(DimensionHeaderEditPart<MigRowInfo> header, Alignment alignment) {
		super(header, alignment.getText(), alignment.getMenuImageDescriptor(), AS_RADIO_BUTTON);
		m_alignment = alignment;
		setChecked(header.getDimension().getAlignment(false) == m_alignment);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void run(MigRowInfo dimension, int index) throws Exception {
		if (isChecked()) {
			dimension.setAlignment(m_alignment);
		}
	}
}