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
package org.eclipse.wb.internal.swing.preferences.laf;

import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.laf.LafSupport;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializer for Swing LAF preferences.
 * 
 * @author mitin_aa
 * @coverage swing.preferences.laf
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
    IPreferenceStore preferenceStore = ToolkitProvider.DESCRIPTION.getPreferences();
    preferenceStore.setDefault(P_APPLY_IN_MAIN, false);
    // This system LAF is default
    preferenceStore.setDefault(P_DEFAULT_LAF, LafSupport.getSystemDefaultLAF().getID());
  }
}
