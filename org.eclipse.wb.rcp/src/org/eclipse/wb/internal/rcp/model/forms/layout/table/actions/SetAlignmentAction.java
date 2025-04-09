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
package org.eclipse.wb.internal.rcp.model.forms.layout.table.actions;

import org.eclipse.wb.internal.rcp.model.forms.layout.table.ITableWrapDataInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * {@link Action} for modifying horizontal/vertical alignment.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class SetAlignmentAction extends AbstractAction {
	private final int m_alignment;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetAlignmentAction(ITableWrapDataInfo layoutData,
			String text,
			ImageDescriptor icon,
			boolean horizontal,
			int alignment) {
		super(layoutData, text, AS_RADIO_BUTTON, icon, horizontal);
		m_alignment = alignment;
		// set check for current alignment
		int currentAlignment =
				horizontal ? layoutData.getHorizontalAlignment() : layoutData.getVerticalAlignment();
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
			m_layoutData.setHorizontalAlignment(m_alignment);
		} else {
			m_layoutData.setVerticalAlignment(m_alignment);
		}
	}
}