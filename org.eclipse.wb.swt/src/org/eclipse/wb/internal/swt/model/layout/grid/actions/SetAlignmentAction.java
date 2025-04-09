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
package org.eclipse.wb.internal.swt.model.layout.grid.actions;

import org.eclipse.wb.internal.swt.model.layout.grid.IGridDataInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * {@link Action} for modifying horizontal/vertical alignment.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class SetAlignmentAction extends AbstractAction {
	private final int m_alignment;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetAlignmentAction(IGridDataInfo gridData,
			String text,
			ImageDescriptor icon,
			boolean horizontal,
			int alignment) {
		super(gridData, text, AS_RADIO_BUTTON, icon, horizontal);
		m_alignment = alignment;
		// set check for current alignment
		int currentAlignment =
				horizontal ? gridData.getHorizontalAlignment() : gridData.getVerticalAlignment();
		setChecked(currentAlignment == alignment);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void runEx() throws Exception {
		if (m_horizontal) {
			m_gridData.setHorizontalAlignment(m_alignment);
		} else {
			m_gridData.setVerticalAlignment(m_alignment);
		}
	}
}