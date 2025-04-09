/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.core.utils.binding.editors.controls;

import org.eclipse.wb.internal.core.utils.binding.IDataEditor;

import org.eclipse.swt.widgets.Combo;

/**
 * Implementation of {@link IDataEditor} for selection index in {@link Combo}.
 *
 * @author scheglov_ke
 */
public final class ComboSelectionEditor implements IDataEditor {
	private final Combo m_combo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ComboSelectionEditor(Combo combo) {
		m_combo = combo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDataEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getValue() {
		return Integer.valueOf(m_combo.getSelectionIndex());
	}

	@Override
	public void setValue(Object value) {
		int index = ((Integer) value).intValue();
		m_combo.select(index);
	}
}
