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
 * {@link Action} for modifying horizontal/vertical grab.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class SetGrabAction extends AbstractAction {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetGrabAction(ITableWrapDataInfo layoutData,
			String text,
			ImageDescriptor icon,
			boolean horizontal) {
		super(layoutData, text, AS_CHECK_BOX, icon, horizontal);
		setChecked(horizontal ? layoutData.getHorizontalGrab() : layoutData.getVerticalGrab());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void runEx() throws Exception {
		if (m_horizontal) {
			m_layoutData.setHorizontalGrab(!m_layoutData.getHorizontalGrab());
		} else {
			m_layoutData.setVerticalGrab(!m_layoutData.getVerticalGrab());
		}
	}
}