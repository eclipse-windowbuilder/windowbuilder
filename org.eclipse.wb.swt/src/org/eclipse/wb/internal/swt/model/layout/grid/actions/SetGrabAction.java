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
 * {@link Action} for modifying horizontal/vertical grab.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class SetGrabAction extends AbstractAction {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetGrabAction(IGridDataInfo gridData, String text, ImageDescriptor icon, boolean horizontal) {
		super(gridData, text, AS_CHECK_BOX, icon, horizontal);
		setChecked(horizontal ? gridData.getHorizontalGrab() : gridData.getVerticalGrab());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void runEx() throws Exception {
		if (m_horizontal) {
			m_gridData.setHorizontalGrab(!m_gridData.getHorizontalGrab());
		} else {
			m_gridData.setVerticalGrab(!m_gridData.getVerticalGrab());
		}
	}
}