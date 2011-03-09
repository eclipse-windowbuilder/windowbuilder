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
package org.eclipse.wb.internal.swing.MigLayout.model;

import org.eclipse.wb.internal.swing.MigLayout.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializer for {@link MigLayoutInfo} preferences.
 * 
 * @author scheglov_ke
 * @coverage swing.MigLayout.model
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
