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
	@Override
	public Object getValue(boolean def) {
		boolean value = def ? m_store.getDefaultBoolean(m_key) : m_store.getBoolean(m_key);
		return ValueUtils.booleanToObject(value);
	}

	@Override
	public void setValue(Object value) {
		m_store.setValue(m_key, ValueUtils.objectToBoolean(value));
	}
}