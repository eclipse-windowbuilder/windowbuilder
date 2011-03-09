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
package org.eclipse.wb.internal.swing.preferences.event;

import org.eclipse.wb.internal.core.model.property.event.EventsProperty;
import org.eclipse.wb.internal.core.model.property.event.IPreferenceConstants;
import org.eclipse.wb.internal.swing.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializer for {@link EventsProperty} preferences.
 * 
 * @author scheglov_ke
 * @coverage swing.preferences
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
    IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
    // type
    preferences.setDefault(P_CODE_TYPE, V_CODE_ANONYMOUS);
    preferences.setDefault(P_INNER_POSITION, V_INNER_AFTER);
    // stub
    preferences.setDefault(P_CREATE_STUB, false);
    preferences.setDefault(P_STUB_NAME_TEMPLATE, "do_${component_name}_${event_name}");
    preferences.setDefault(P_DELETE_STUB, true);
    // inner
    preferences.setDefault(P_INNER_NAME_TEMPLATE, "${Component_name}${Listener_className}");
    // other
    preferences.setDefault(P_FINAL_PARAMETERS, false);
    preferences.setDefault(P_DECORATE_ICON, true);
  }
}
