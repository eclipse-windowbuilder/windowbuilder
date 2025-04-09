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
package org.eclipse.wb.internal.rcp.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.DialogFieldUiContentProvider;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.SpinnerDialogField;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.IDelayValueProvider;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SwtObservableInfo;

/**
 * Content provider for edit (delay value over spinner) {@link SwtObservableInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class SwtDelayUiContentProvider extends DialogFieldUiContentProvider {
	private final IDelayValueProvider m_delayValueProvider;
	private final SpinnerDialogField m_dialogField;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SwtDelayUiContentProvider(IDelayValueProvider delayValueProvider, String name) {
		m_delayValueProvider = delayValueProvider;
		m_dialogField = new SpinnerDialogField();
		m_dialogField.setMinimum(0);
		m_dialogField.setMaximum(Integer.MAX_VALUE);
		m_dialogField.setLabelText(name);
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
	public void updateFromObject() {
		m_dialogField.setSelection(m_delayValueProvider.getDelayValue());
	}

	@Override
	public void saveToObject() {
		m_delayValueProvider.setDelayValue(m_dialogField.getSelection());
	}
}