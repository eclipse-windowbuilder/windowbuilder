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
package org.eclipse.wb.internal.rcp.model.forms.layout.table.actions;

import org.eclipse.wb.internal.rcp.model.forms.layout.table.ITableWrapDataInfo;

import org.eclipse.jface.action.Action;

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
			String iconPath,
			boolean horizontal) {
		super(layoutData, text, AS_CHECK_BOX, iconPath, horizontal);
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