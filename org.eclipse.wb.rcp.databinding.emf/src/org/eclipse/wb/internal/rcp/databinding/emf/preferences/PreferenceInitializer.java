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
package org.eclipse.wb.internal.rcp.databinding.emf.preferences;

import org.eclipse.wb.internal.rcp.databinding.emf.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * 
 * @author lobas_av
 * 
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractPreferenceInitializer
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore store = Activator.getStore();
    store.setDefault(IPreferenceConstants.GENERATE_CODE_FOR_VERSION_2_5, false);
  }
}