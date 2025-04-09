/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.DialogFieldUiContentProvider;
import org.eclipse.wb.internal.core.utils.dialogfields.ComboDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.swing.databinding.model.properties.BeanPropertyInfo;

import org.eclipse.swt.SWT;

/**
 * Combo editor (choose properties) for {@link BeanPropertyInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public class PropertiesUiContentProvider extends DialogFieldUiContentProvider {
	private final BeanPropertyInfo m_property;
	private final ComboDialogField m_dialogField;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PropertiesUiContentProvider(BeanPropertyInfo property, String[] items) {
		m_property = property;
		m_dialogField = new ComboDialogField(SWT.BORDER | SWT.READ_ONLY);
		m_dialogField.setItems(items);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractUIContentProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public DialogField getDialogField() {
		return m_dialogField;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
		m_dialogField.selectItem(m_property.getPath());
	}

	@Override
	public void saveToObject() throws Exception {
		m_property.setPath(m_dialogField.getText());
	}
}