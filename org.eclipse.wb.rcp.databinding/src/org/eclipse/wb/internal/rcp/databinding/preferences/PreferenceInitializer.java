/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.databinding.preferences;

import org.eclipse.wb.internal.rcp.databinding.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializer for default RCP bindings preferences.
 *
 * @author lobas_av
 * @coverage bindings.rcp.preferences
 */
public final class PreferenceInitializer extends AbstractPreferenceInitializer
implements
IPreferenceConstants {
	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractPreferenceInitializer
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getStore();
		// code generation
		store.setDefault(ADD_INVOKE_INITDB_TO_GUI, true);
		store.setDefault(ADD_INITDB_TO_FIELD, true);
		store.setDefault(ADD_INVOKE_INITDB_TO_COMPOSITE_CONSTRUCTOR, true);
		store.setDefault(INITDB_TRY_CATCH, false);
		store.setDefault(GENERATE_CODE_FOR_VERSION_1_3, true);
		store.setDefault(USE_VIEWER_SUPPORT, false);
		store.setDefault(UPDATE_VALUE_STRATEGY_DEFAULT, "POLICY_UPDATE");
		store.setDefault(UPDATE_LIST_STRATEGY_DEFAULT, "POLICY_UPDATE");
		store.setDefault(UPDATE_SET_STRATEGY_DEFAULT, "POLICY_UPDATE");
		store.setDefault(INITDB_GENERATE_ACCESS, PROTECTED_ACCESS);
	}
}