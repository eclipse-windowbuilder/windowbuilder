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
package org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.actions;

import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * {@link Action} for modifying alignment of {@link RowInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class SetAlignmentRowAction extends DimensionHeaderAction<RowInfo> {
	private final RowInfo.Alignment m_alignment;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetAlignmentRowAction(DimensionHeaderEditPart<RowInfo> header,
			String text,
			ImageDescriptor imageDescriptor,
			RowInfo.Alignment alignment) {
		super(header, text, imageDescriptor, AS_RADIO_BUTTON);
		m_alignment = alignment;
		setChecked(header.getDimension().getAlignment() == m_alignment);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void run(RowInfo dimension) throws Exception {
		if (isChecked()) {
			dimension.setAlignment(m_alignment);
		}
	}
}