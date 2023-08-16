/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.DialogFieldUiContentProvider;
import org.eclipse.wb.internal.core.utils.dialogfields.BooleanDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.swing.databinding.Messages;

/**
 * Editor for {@link IEditableProvider}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public class EditableUiContentProvider extends DialogFieldUiContentProvider {
	private final IEditableProvider m_provider;
	private final BooleanDialogField m_dialogField;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EditableUiContentProvider(IEditableProvider provider) {
		m_provider = provider;
		m_dialogField = new BooleanDialogField();
		m_dialogField.setLabelText(Messages.EditableUiContentProvider_title);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DialogFieldUIContentProvider
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
		m_dialogField.setSelection(m_provider.isEditable());
	}

	@Override
	public void saveToObject() throws Exception {
		m_provider.setEditable(m_dialogField.getSelection());
	}
}