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
package org.eclipse.wb.internal.rcp.preferences.event;

import org.eclipse.wb.internal.core.model.property.event.EventsProperty;
import org.eclipse.wb.internal.core.model.property.event.IPreferenceConstants;
import org.eclipse.wb.internal.rcp.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializer for {@link EventsProperty} preferences.
 *
 * @author scheglov_ke
 * @coverage rcp.preferences
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
		// type
		preferenceStore.setDefault(P_CODE_TYPE, V_CODE_ANONYMOUS);
		preferenceStore.setDefault(P_INNER_POSITION, V_INNER_AFTER);
		// stub
		preferenceStore.setDefault(P_CREATE_STUB, false);
		preferenceStore.setDefault(P_STUB_NAME_TEMPLATE, "do_${component_name}_${event_name}");
		preferenceStore.setDefault(P_DELETE_STUB, true);
		// inner
		preferenceStore.setDefault(P_INNER_NAME_TEMPLATE, "${Component_name}${Listener_className}");
		// other
		preferenceStore.setDefault(P_FINAL_PARAMETERS, false);
		preferenceStore.setDefault(P_DECORATE_ICON, true);
	}
}
