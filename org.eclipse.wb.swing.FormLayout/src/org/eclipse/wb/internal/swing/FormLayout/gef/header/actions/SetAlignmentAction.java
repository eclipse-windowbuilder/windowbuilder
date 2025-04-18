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
package org.eclipse.wb.internal.swing.FormLayout.gef.header.actions;

import org.eclipse.wb.internal.swing.FormLayout.gef.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swing.FormLayout.model.FormDimensionInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.jgoodies.forms.layout.FormSpec.DefaultAlignment;

/**
 * {@link Action} for modifying alignment of {@link FormDimensionInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.header
 */
public final class SetAlignmentAction<T extends FormDimensionInfo> extends DimensionHeaderAction<T> {
	private final DefaultAlignment m_alignment;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetAlignmentAction(DimensionHeaderEditPart<T> header,
			String text,
			ImageDescriptor imageDescriptor,
			DefaultAlignment alignment) {
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
	protected void run(T dimension) throws Exception {
		if (isChecked()) {
			dimension.setAlignment(m_alignment);
		}
	}
}