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
package org.eclipse.wb.internal.swing.model.property.editor.border.fields;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * {@link AbstractBorderField} that allows to select one field from many.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class ComboField extends AbstractBorderField {
	private final Class<?> m_clazz;
	private final String[] m_fields;
	private final Combo m_combo;
	private String m_source;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ComboField(Composite parent,
			String labelText,
			Class<?> clazz,
			String[] fields,
			String[] titles) {
		super(parent, 1, labelText);
		Assert.equals(fields.length, titles.length);
		m_clazz = clazz;
		m_fields = fields;
		// create Combo
		m_combo = new Combo(this, SWT.READ_ONLY);
		GridDataFactory.create(m_combo).grabH().fillH();
		m_combo.setItems(titles);
		m_combo.setVisibleItemCount(titles.length);
		m_combo.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				int index = m_combo.getSelectionIndex();
				m_source = m_clazz.getName() + "." + m_fields[index];
				notifyListeners(SWT.Selection, new Event());
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the value, that should correspond to the one of the field values.
	 */
	public void setValue(Object value) throws Exception {
		for (int i = 0; i < m_fields.length; i++) {
			String fieldName = m_fields[i];
			Field field = m_clazz.getField(fieldName);
			if (Objects.equals(field.get(null), value)) {
				m_source = m_clazz.getName() + "." + m_fields[i];
				m_combo.select(i);
				break;
			}
		}
	}

	@Override
	public String getSource() throws Exception {
		return m_source;
	}
}
