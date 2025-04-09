/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
	@Override
	public Object getValue(boolean def) {
		int value = def ? m_store.getDefaultInt(m_key) : m_store.getInt(m_key);
		return Integer.valueOf(value);
	}

	@Override
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