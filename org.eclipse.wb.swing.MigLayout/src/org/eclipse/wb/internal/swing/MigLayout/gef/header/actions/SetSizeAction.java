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
package org.eclipse.wb.internal.swing.MigLayout.gef.header.actions;

import org.eclipse.wb.internal.swing.MigLayout.gef.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swing.MigLayout.model.MigDimensionInfo;

import org.eclipse.jface.action.Action;

/**
 * {@link Action} for modifying grow of {@link MigDimensionInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.header
 */
public final class SetSizeAction<T extends MigDimensionInfo> extends DimensionHeaderAction<T> {
	private final String m_sizeString;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetSizeAction(DimensionHeaderEditPart<T> header, String text, String sizeString) {
		super(header, text);
		m_sizeString = sizeString;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Run
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void run(T dimension, int index) throws Exception {
		dimension.setSize(m_sizeString);
	}
}