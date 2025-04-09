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
package org.eclipse.wb.internal.swing.FormLayout.model;

import org.eclipse.wb.internal.swing.FormLayout.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializer for {@link FormLayoutInfo} preferences.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
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
	}
}
