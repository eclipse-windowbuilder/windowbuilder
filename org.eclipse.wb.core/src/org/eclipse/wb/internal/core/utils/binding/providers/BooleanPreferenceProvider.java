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
package org.eclipse.wb.internal.core.utils.binding.providers;

import org.eclipse.wb.internal.core.utils.binding.ValueUtils;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author lobas_av
 *
 */
public class BooleanPreferenceProvider extends AbstractPreferenceProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BooleanPreferenceProvider(IPreferenceStore store, String key) {
    super(store, key);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDataProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getValue(boolean def) {
    boolean value = def ? m_store.getDefaultBoolean(m_key) : m_store.getBoolean(m_key);
    return ValueUtils.booleanToObject(value);
  }

  public void setValue(Object value) {
    m_store.setValue(m_key, ValueUtils.objectToBoolean(value));
  }
}