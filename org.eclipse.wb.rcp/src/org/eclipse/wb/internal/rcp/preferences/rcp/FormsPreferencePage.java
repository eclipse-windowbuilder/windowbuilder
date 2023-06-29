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
package org.eclipse.wb.internal.rcp.preferences.rcp;

import org.eclipse.wb.internal.core.preferences.bind.AbstractBindingPreferencesPage;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.rcp.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.rcp.preferences.PreferencesMessages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link PreferencePage} for Forms API.
 *
 * @author scheglov_ke
 * @coverage rcp.preferences.ui
 */
public class FormsPreferencePage extends AbstractBindingPreferencesPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormsPreferencePage() {
		super(RcpToolkitDescription.INSTANCE);
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
			GridLayoutFactory.create(this).noMargins().columns(1);
			// boolean preferences
			checkButton(
					this,
					PreferencesMessages.FormsPreferencePage_generatePaintBorders,
					IPreferenceConstants.FORMS_PAINT_BORDERS);
			checkButton(
					this,
					PreferencesMessages.FormsPreferencePage_generateAdapt,
					IPreferenceConstants.FORMS_ADAPT_CONTROL);
		}
	}
}