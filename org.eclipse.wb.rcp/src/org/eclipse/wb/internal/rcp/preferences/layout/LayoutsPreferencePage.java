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
package org.eclipse.wb.internal.rcp.preferences.layout;

import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.rcp.preferences.PreferencesMessages;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataNameSupport;
import org.eclipse.wb.internal.swt.model.layout.LayoutNameSupport;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Main {@link PreferencePage} for Layout Support.
 *
 * @author scheglov_ke
 * @coverage rcp.preferences.ui
 */
public final class LayoutsPreferencePage
extends
org.eclipse.wb.internal.core.preferences.LayoutsPreferencePage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutsPreferencePage() {
		super(ToolkitProvider.DESCRIPTION);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected AbstractBindingComposite createBindingComposite(Composite parent) {
		return new ContentsCompositeEx(parent, m_bindManager, m_preferences);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Contents
	//
	////////////////////////////////////////////////////////////////////////////
	protected class ContentsCompositeEx extends ContentsComposite {
		public ContentsCompositeEx(Composite parent,
				DataBindManager bindManager,
				IPreferenceStore preferences) {
			super(parent, bindManager, preferences);
			// layout template
			{
				new Label(this, SWT.NONE).setText(PreferencesMessages.LayoutsPreferencePage_layoutVariablePattern);
				// control
				Combo templateCombo = new Combo(this, SWT.READ_ONLY);
				templateCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				templateCombo.setItems(LayoutNameSupport.TEMPLATES);
				// bind
				bindString(templateCombo, IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE);
			}
			// layout data template
			{
				new Label(this, SWT.NONE).setText(PreferencesMessages.LayoutsPreferencePage_dataVariablePattern);
				// control
				Combo templateCombo = new Combo(this, SWT.READ_ONLY);
				templateCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				templateCombo.setItems(LayoutDataNameSupport.TEMPLATES);
				// bind
				bindString(templateCombo, IPreferenceConstants.P_LAYOUT_DATA_NAME_TEMPLATE);
			}
		}
	}
}