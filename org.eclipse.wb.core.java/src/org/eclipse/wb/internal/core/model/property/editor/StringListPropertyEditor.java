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

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.swt.custom.CCombo;

import java.util.Map;

/**
 * The {@link PropertyEditor} for selecting single string from given set.
 *
 * @author sablin_aa
 * @coverage core.model.property.editor
 */
public final class StringListPropertyEditor extends AbstractListPropertyEditor {
	private boolean m_ignoreCase;
	private String[] m_strings;

	////////////////////////////////////////////////////////////////////////////
	//
	// Combo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void toPropertyEx_simpleProperty(Property property, CCombo combo, int index)
			throws Exception {
		property.setValue(m_strings[index]);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access to list items
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected int getCount() {
		return m_strings.length;
	}

	@Override
	protected int getValueIndex(Object value) {
		if (value instanceof String string) {
			for (int i = 0; i < getCount(); i++) {
				if (m_ignoreCase) {
					if (string.equalsIgnoreCase(m_strings[i])) {
						return i;
					}
				} else {
					if (string.equals(m_strings[i])) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	@Override
	protected String getTitle(int index) {
		return m_strings[index];
	}

	@Override
	protected String getExpression(int index) throws Exception {
		return StringConverter.INSTANCE.toJavaSource(null, m_strings[index]);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IConfigurablePropertyObject
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(EditorState state, Map<String, Object> parameters) throws Exception {
		m_strings = getParameterAsArray(parameters, "strings");
		m_ignoreCase = "true".equals(parameters.get("ignoreCase"));
	}

	/**
	 * Configures this editor externally.
	 */
	public void configure(String[] strings) {
		m_strings = strings;
	}
}
