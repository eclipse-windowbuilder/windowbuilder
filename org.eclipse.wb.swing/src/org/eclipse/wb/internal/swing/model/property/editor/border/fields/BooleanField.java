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

import java.util.Objects;

/**
 * {@link AbstractBorderField} that allows to select {@link Boolean} value.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class BooleanField extends AbstractBorderField {
	private final Button[] m_buttons;
	private String m_source;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BooleanField(Composite parent, String labelText, String[] titles) {
		super(parent, 2, labelText);
		Assert.equals(2, titles.length);
		// create radio buttons
		m_buttons = new Button[2];
		m_buttons[0] = createRadioButton(titles[0], "false");
		m_buttons[1] = createRadioButton(titles[1], "true");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates single radio {@link Button}.
	 */
	private Button createRadioButton(String title, final String source) {
		Button button = new Button(this, SWT.RADIO);
		button.setText(title);
		button.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				m_source = source;
				notifyListeners(SWT.Selection, new Event());
			}
		});
		return button;
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
		if (Objects.equals(Boolean.FALSE, value)) {
			m_source = "false";
			m_buttons[0].setSelection(true);
			m_buttons[1].setSelection(false);
		} else {
			m_source = "true";
			m_buttons[0].setSelection(false);
			m_buttons[1].setSelection(true);
		}
	}

	@Override
	public String getSource() throws Exception {
		return m_source;
	}
}
