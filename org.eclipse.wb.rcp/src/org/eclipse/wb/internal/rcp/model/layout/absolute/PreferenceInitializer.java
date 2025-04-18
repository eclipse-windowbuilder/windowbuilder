/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.rcp.model.layout.absolute;

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;
import org.eclipse.wb.internal.rcp.ToolkitProvider;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializer for RCP absolute-based layouts preferences.
 *
 * @author mitin_aa
 * @coverage rcp.preferences.layout
 */
public final class PreferenceInitializer extends AbstractPreferenceInitializer
implements
IPreferenceConstants {
	////////////////////////////////////////////////////////////////////////////
	//
	//	Initializing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link ToolkitDescription} of the toolkit this policy applies to.
	 */
	private ToolkitDescription getToolkit() {
		return ToolkitProvider.DESCRIPTION;
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferenceStore = getToolkit().getPreferences();
		// editing mode
		preferenceStore.setDefault(P_USE_FREE_MODE, true);
		preferenceStore.setDefault(P_USE_GRID, false);
		preferenceStore.setDefault(P_DISPLAY_GRID, false);
		preferenceStore.setDefault(P_GRID_STEP_X, 5);
		preferenceStore.setDefault(P_GRID_STEP_Y, 5);
		preferenceStore.setDefault(P_CREATION_FLOW, false);
		// gaps
		preferenceStore.setDefault(P_COMPONENT_GAP_LEFT, 6);
		preferenceStore.setDefault(P_COMPONENT_GAP_RIGHT, 6);
		preferenceStore.setDefault(P_COMPONENT_GAP_TOP, 6);
		preferenceStore.setDefault(P_COMPONENT_GAP_BOTTOM, 6);
		preferenceStore.setDefault(P_CONTAINER_GAP_LEFT, 10);
		preferenceStore.setDefault(P_CONTAINER_GAP_RIGHT, 10);
		preferenceStore.setDefault(P_CONTAINER_GAP_TOP, 10);
		preferenceStore.setDefault(P_CONTAINER_GAP_BOTTOM, 10);
		// misc
		preferenceStore.setDefault(P_DISPLAY_LOCATION_SIZE_HINTS, true);
		preferenceStore.setDefault(P_AUTOSIZE_ON_PROPERTY_CHANGE, false);
	}
}
