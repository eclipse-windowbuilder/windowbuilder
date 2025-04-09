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
package org.eclipse.wb.internal.swing.model.layout.gbl;

import org.eclipse.wb.internal.swing.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializer for {@link AbstractGridBagLayoutInfo} preferences.
 *
 * @author scheglov_ke
 * @coverage swing.preferences.layout
 */
public final class PreferenceInitializer extends AbstractPreferenceInitializer
implements
IPreferenceConstants {
	////////////////////////////////////////////////////////////////////////////
	//
	//	Initializing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		preferenceStore.setDefault(P_ENABLE_GRAB, true);
		preferenceStore.setDefault(P_ENABLE_RIGHT_ALIGNMENT, true);
		preferenceStore.setDefault(P_GBC_LONG, false);
		preferenceStore.setDefault(P_CHANGE_INSETS_FOR_GAPS, true);
		preferenceStore.setDefault(P_GAP_COLUMN, 5);
		preferenceStore.setDefault(P_GAP_ROW, 5);
		preferenceStore.setDefault(
				P_CONSTRAINTS_NAME_TEMPLATE,
				"${constraintsAcronym}_${componentName}");
	}
}
