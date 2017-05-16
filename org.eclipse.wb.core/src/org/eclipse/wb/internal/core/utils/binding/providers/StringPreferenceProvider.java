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

import org.eclipse.jface.preference.IPreferenceStore;

import org.apache.commons.lang.ObjectUtils;

/**
 * @author lobas_av
 *
 */
public class StringPreferenceProvider extends AbstractPreferenceProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StringPreferenceProvider(IPreferenceStore store, String key) {
    super(store, key);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDataProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getValue(boolean def) {
    return def ? m_store.getDefaultString(m_key) : m_store.getString(m_key);
  }

  public void setValue(Object value) {
    m_store.setValue(m_key, ObjectUtils.toString(value));
  }
}