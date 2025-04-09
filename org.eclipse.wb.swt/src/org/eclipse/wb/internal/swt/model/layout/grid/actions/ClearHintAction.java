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
package org.eclipse.wb.internal.swt.model.layout.grid.actions;

import org.eclipse.wb.internal.swt.model.layout.grid.IGridDataInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

/**
 * {@link Action} for clearing horizontal/vertical hint.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class ClearHintAction extends AbstractAction {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ClearHintAction(IGridDataInfo gridData, String text, boolean horizontal) {
		super(gridData, text, AS_PUSH_BUTTON, null, horizontal);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void runEx() throws Exception {
		if (m_horizontal) {
			m_gridData.setWidthHint(SWT.DEFAULT);
		} else {
			m_gridData.setHeightHint(SWT.DEFAULT);
		}
	}
}