/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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

import java.util.Objects;

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
	@Override
	public Object getValue(boolean def) {
		return def ? m_store.getDefaultString(m_key) : m_store.getString(m_key);
	}

	@Override
	public void setValue(Object value) {
		m_store.setValue(m_key, Objects.toString(value));
	}
}