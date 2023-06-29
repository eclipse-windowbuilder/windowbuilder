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
package org.eclipse.wb.internal.core.utils.binding.editors;

import org.eclipse.wb.internal.core.utils.binding.IDataEditor;
import org.eclipse.wb.internal.core.utils.dialogfields.ComboDialogField;

import org.apache.commons.lang.ObjectUtils;

/**
 * @author lobas_av
 *
 */
public class StringComboEditor implements IDataEditor {
	private final ComboDialogField m_field;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public StringComboEditor(ComboDialogField field) {
		m_field = field;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDataEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getValue() {
		return m_field.getText();
	}

	@Override
	public void setValue(Object value) {
		m_field.setText(ObjectUtils.toString(value));
	}
}