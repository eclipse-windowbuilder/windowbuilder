/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.MigLayout.gef.header.actions;

import org.eclipse.wb.internal.swing.MigLayout.gef.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo.Alignment;

import org.eclipse.jface.action.Action;

;
/**
 * {@link Action} for modifying alignment of {@link MigColumnInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.header
 */
public final class SetAlignmentColumnAction extends DimensionHeaderAction<MigColumnInfo> {
	private final Alignment m_alignment;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetAlignmentColumnAction(DimensionHeaderEditPart<MigColumnInfo> header, Alignment alignment) {
		super(header, alignment.getText(), alignment.getMenuImage(), AS_RADIO_BUTTON);
		m_alignment = alignment;
		setChecked(header.getDimension().getAlignment(false) == m_alignment);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void run(MigColumnInfo dimension, int index) throws Exception {
		if (isChecked()) {
			dimension.setAlignment(m_alignment);
		}
	}
}