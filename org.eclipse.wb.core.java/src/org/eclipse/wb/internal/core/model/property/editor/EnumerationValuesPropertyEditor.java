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
package org.eclipse.wb.internal.core.model.property.editor;

import org.eclipse.wb.core.controls.CCombo3;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;

import java.beans.PropertyDescriptor;
import java.util.Objects;

/**
 * {@link PropertyEditor} for "enumerationValues" attribute of {@link PropertyDescriptor}.
 * <p>
 * See http://javadude.com/articles/javabeanattributes.html for attributes.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public class EnumerationValuesPropertyEditor extends AbstractComboPropertyEditor
implements
IValueSourcePropertyEditor,
IClipboardSourceProvider {
	private final String[] m_names;
	private final Object[] m_values;
	private final String[] m_sources;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EnumerationValuesPropertyEditor(Object attributeValue) {
		Object[] enumElements = (Object[]) attributeValue;
		int items = enumElements.length / 3;
		m_names = new String[items];
		m_values = new Object[items];
		m_sources = new String[items];
		for (int i = 0; i < items; i++) {
			m_names[i] = (String) enumElements[3 * i + 0];
			m_values[i] = enumElements[3 * i + 1];
			m_sources[i] = (String) enumElements[3 * i + 2];
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TextDisplayPropertyEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText(Property property) throws Exception {
		Object value = property.getValue();
		// return name for value
		if (value != Property.UNKNOWN_VALUE) {
			for (int i = 0; i < m_values.length; i++) {
				if (Objects.equals(m_values[i], value)) {
					return m_names[i];
				}
			}
		}
		// unknown value
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IValueSourcePropertyEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getValueSource(Object value) throws Exception {
		if (value != Property.UNKNOWN_VALUE) {
			for (int i = 0; i < m_values.length; i++) {
				if (Objects.equals(m_values[i], value)) {
					return m_sources[i];
				}
			}
		}
		// unknown value
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IClipboardSourceProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getClipboardSource(GenericProperty property) throws Exception {
		Object value = property.getValue();
		return getValueSource(value);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Combo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addItems(Property property, CCombo3 combo) throws Exception {
		for (String title : m_names) {
			combo.add(title);
		}
	}

	@Override
	protected void selectItem(Property property, CCombo3 combo) throws Exception {
		combo.setText(getText(property));
	}

	@Override
	protected void toPropertyEx(Property property, CCombo3 combo, int index) throws Exception {
		Object value = m_values[index];
		if (property instanceof GenericProperty genericProperty) {
			String source = getValueSource(value);
			genericProperty.setExpression(source, value);
		} else {
			property.setValue(value);
		}
	}
}