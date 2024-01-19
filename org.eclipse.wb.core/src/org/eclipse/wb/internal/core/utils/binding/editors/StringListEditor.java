/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
import org.eclipse.wb.internal.core.utils.dialogfields.ListDialogField;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lobas_av
 *
 */
public class StringListEditor implements IDataEditor {
	private final ListDialogField<String> m_field;
	private final String m_separator;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public StringListEditor(ListDialogField<String> field, String separator) {
		m_field = field;
		m_separator = separator;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDataEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getValue() {
		StringBuffer buffer = new StringBuffer();
		int count = m_field.getSize();
		int last = count - 1;
		for (int i = 0; i < count; i++) {
			buffer.append(m_field.getElement(i));
			if (i != last) {
				buffer.append(m_separator);
			}
		}
		return buffer.toString();
	}

	@Override
	public void setValue(Object value) {
		String stringValue = ObjectUtils.toString(value);
		String[] values = StringUtils.split(stringValue, m_separator);
		List<String> elements = new ArrayList<>();
		CollectionUtils.addAll(elements, values);
		m_field.setElements(elements);
	}
}