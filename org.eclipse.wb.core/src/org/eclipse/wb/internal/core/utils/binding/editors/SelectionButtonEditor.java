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
package org.eclipse.wb.internal.core.utils.binding.editors;

import org.eclipse.wb.internal.core.utils.binding.IDataEditor;
import org.eclipse.wb.internal.core.utils.binding.ValueUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.SelectionButtonDialogField;

/**
 * @author lobas_av
 *
 */
public class SelectionButtonEditor implements IDataEditor {
	private final SelectionButtonDialogField m_field;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SelectionButtonEditor(SelectionButtonDialogField field) {
		m_field = field;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDataEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getValue() {
		return ValueUtils.booleanToObject(m_field.isSelected());
	}

	@Override
	public void setValue(Object value) {
		m_field.setSelection(ValueUtils.objectToBoolean(value));
	}
}