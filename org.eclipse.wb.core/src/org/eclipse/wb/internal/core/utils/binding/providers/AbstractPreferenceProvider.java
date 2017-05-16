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

import org.eclipse.wb.internal.core.utils.binding.IDataProvider;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author lobas_av
 *
 */
public abstract class AbstractPreferenceProvider implements IDataProvider {
  protected final IPreferenceStore m_store;
  protected final String m_key;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractPreferenceProvider(IPreferenceStore store, String key) {
    m_store = store;
    m_key = key;
  }
}