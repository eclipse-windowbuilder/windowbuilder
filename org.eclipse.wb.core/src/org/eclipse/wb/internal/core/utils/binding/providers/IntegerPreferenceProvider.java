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

/**
 * Implementation of {@link AbstractPreferenceProvider} for integer values.
 *
 * @author scheglov_ke
 */
public final class IntegerPreferenceProvider extends AbstractPreferenceProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public IntegerPreferenceProvider(IPreferenceStore store, String key) {
    super(store, key);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDataProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getValue(boolean def) {
    int value = def ? m_store.getDefaultInt(m_key) : m_store.getInt(m_key);
    return new Integer(value);
  }

  public void setValue(Object value) {
    int intValue;
    if (value instanceof Integer) {
      intValue = ((Integer) value).intValue();
    } else {
      intValue = Integer.parseInt(value.toString());
    }
    m_store.setValue(m_key, intValue);
  }
}