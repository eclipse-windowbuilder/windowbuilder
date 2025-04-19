/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.core.utils.binding.providers;

import org.eclipse.wb.internal.core.utils.binding.IDataProvider;
import org.eclipse.wb.internal.core.utils.binding.ValueUtils;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author lobas_av
 *
 */
public class BooleanArrayPreferenceProvider implements IDataProvider {
	private final IPreferenceStore m_store;
	private final String[] m_keys;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BooleanArrayPreferenceProvider(IPreferenceStore store, String[] keys) {
		m_store = store;
		m_keys = keys;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDataProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getValue(boolean def) {
		boolean[] values = new boolean[m_keys.length];
		if (def) {
			// store default values
			for (int i = 0; i < values.length; i++) {
				values[i] = m_store.getDefaultBoolean(m_keys[i]);
			}
		} else {
			// store values
			for (int i = 0; i < values.length; i++) {
				values[i] = m_store.getBoolean(m_keys[i]);
			}
		}
		return values;
	}

	@Override
	public void setValue(Object value) {
		// prepare boolean array
		boolean[] values = ValueUtils.objectToBooleanArray(value);
		// check set values
		if (values != null && values.length == m_keys.length) {
			for (int i = 0; i < values.length; i++) {
				m_store.setValue(m_keys[i], values[i]);
			}
		}
	}
}