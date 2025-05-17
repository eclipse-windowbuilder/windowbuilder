/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
import org.eclipse.wb.internal.core.utils.dialogfields.ListDialogField;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
		String stringValue = Objects.toString(value);
		String[] values = StringUtils.split(stringValue, m_separator);
		List<String> elements = new ArrayList<>();
		CollectionUtils.addAll(elements, values);
		m_field.setElements(elements);
	}
}