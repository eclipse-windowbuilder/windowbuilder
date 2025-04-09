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
package org.eclipse.wb.internal.core.utils.binding.editors;

import org.eclipse.wb.internal.core.utils.binding.IDataEditor;
import org.eclipse.wb.internal.core.utils.dialogfields.SpinnerDialogField;

/**
 * {@link IDataEditor} implementation for {@link SpinnerDialogField}.
 *
 * @author scheglov_ke
 */
public class SpinnerEditor implements IDataEditor {
	private final SpinnerDialogField m_field;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SpinnerEditor(SpinnerDialogField field) {
		m_field = field;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDataEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getValue() {
		return Integer.valueOf(m_field.getSelection());
	}

	@Override
	public void setValue(Object value) {
		m_field.setSelection(((Integer) value).intValue());
	}
}
