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
package org.eclipse.wb.internal.swing.FormLayout.preferences;

import org.eclipse.wb.internal.core.preferences.bind.AbstractBindingPreferencesPage;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.FormLayout.Activator;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.IPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link PreferencePage} for {@link FormLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.ui
 */
public final class FormLayoutPreferencePage extends AbstractBindingPreferencesPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormLayoutPreferencePage() {
		super(Activator.getStore());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected AbstractBindingComposite createBindingComposite(Composite parent) {
		return new ContentsComposite(parent, m_bindManager, m_preferences);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Contents
	//
	////////////////////////////////////////////////////////////////////////////
	private class ContentsComposite extends AbstractBindingComposite {
		public ContentsComposite(Composite parent,
				DataBindManager bindManager,
				IPreferenceStore preferences) {
			super(parent, bindManager, preferences);
			GridLayoutFactory.create(this).noMargins().columns(2);
			// boolean preferences
			checkButton(
					this,
					2,
					PreferencesMessages.FormLayoutPreferencePage_useGrab,
					IPreferenceConstants.P_ENABLE_GRAB);
			checkButton(
					this,
					2,
					PreferencesMessages.FormLayoutPreferencePage_rightAlignment,
					IPreferenceConstants.P_ENABLE_RIGHT_ALIGNMENT);
		}
	}
}