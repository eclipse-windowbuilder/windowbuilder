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

import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * {@link AbstractBorderField} that allows to enter {@link String} value.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class TextField extends AbstractBorderField {
	private final Text m_text;
	private final Listener m_modifyListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			notifyListeners(SWT.Selection, event);
		}
	};

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TextField(Composite parent, String labelText) {
		super(parent, 1, labelText);
		// create Text
		{
			m_text = new Text(this, SWT.BORDER);
			GridDataFactory.create(m_text).grabH().fill().hintHC(40);
			m_text.addListener(SWT.Modify, m_modifyListener);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the value to edit.
	 */
	public void setValue(String value) {
		m_text.removeListener(SWT.Modify, m_modifyListener);
		try {
			m_text.setText(value);
		} finally {
			m_text.addListener(SWT.Modify, m_modifyListener);
		}
	}

	@Override
	public String getSource() {
		return StringConverter.INSTANCE.toJavaSource(null, m_text.getText());
	}
}
