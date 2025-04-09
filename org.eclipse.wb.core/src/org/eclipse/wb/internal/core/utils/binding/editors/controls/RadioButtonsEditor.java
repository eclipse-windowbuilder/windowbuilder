/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.core.utils.binding.editors.controls;

import org.eclipse.wb.internal.core.utils.binding.IDataEditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link IDataEditor} for group of radio {@link Button} widgets.
 *
 * @author scheglov_ke
 */
public final class RadioButtonsEditor implements IDataEditor {
	private final Button[] m_buttons;
	private final int[] m_values;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RadioButtonsEditor(Button[] buttons, int[] values) {
		m_buttons = buttons;
		m_values = values;
	}

	public RadioButtonsEditor(Button[] buttons) {
		m_buttons = buttons;
		m_values = getDefaultValuesForButtons();
	}

	public RadioButtonsEditor(Composite composite) {
		List<Button> buttons = new ArrayList<>();
		//
		Control[] children = composite.getChildren();
		for (int i = 0; i < children.length; i++) {
			Control control = children[i];
			if (control instanceof Button && (control.getStyle() & SWT.RADIO) != 0) {
				buttons.add((Button) control);
			}
		}
		//
		m_buttons = buttons.toArray(new Button[buttons.size()]);
		m_values = getDefaultValuesForButtons();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDataEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getValue() {
		for (int i = 0; i < m_buttons.length; i++) {
			Button button = m_buttons[i];
			if (button.getSelection()) {
				return Integer.valueOf(m_values[i]);
			}
		}
		return Integer.valueOf(m_values[0]);
	}

	@Override
	public void setValue(Object value) {
		int intValue = ((Integer) value).intValue();
		for (int i = 0; i < m_buttons.length; i++) {
			Button button = m_buttons[i];
			button.setSelection(m_values[i] == intValue);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return default values [0, 1, ...] for current array of {@link Button}'s.
	 */
	private int[] getDefaultValuesForButtons() {
		int[] values = new int[m_buttons.length];
		for (int i = 0; i < m_buttons.length; i++) {
			values[i] = i;
		}
		return values;
	}
}
