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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
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
public final class RadioField extends AbstractBorderField {
	private final Class<?> m_clazz;
	private final String[] m_fields;
	private final Button[] m_buttons;
	private String m_source;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RadioField(Composite parent,
			String labelText,
			Class<?> clazz,
			String[] fields,
			String[] titles) {
		super(parent, fields.length, labelText);
		Assert.equals(fields.length, titles.length);
		m_clazz = clazz;
		m_fields = fields;
		// create radio buttons
		m_buttons = new Button[fields.length];
		for (int i = 0; i < titles.length; i++) {
			final int index = i;
			m_buttons[i] = new Button(this, SWT.RADIO);
			m_buttons[i].setText(titles[i]);
			m_buttons[i].addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					m_source = m_clazz.getName() + "." + m_fields[index];
					notifyListeners(SWT.Selection, new Event());
				}
			});
		}
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
				m_buttons[i].setSelection(true);
			} else {
				m_buttons[i].setSelection(false);
			}
		}
	}

	@Override
	public String getSource() throws Exception {
		return m_source;
	}
}
