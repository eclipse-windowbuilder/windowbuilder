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
package org.eclipse.wb.internal.rcp.databinding.model.context;

import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Context for set/get state during create provider contents.
 *
 * @see BindingInfo#createContentProviders(java.util.List, IPageListener, DatabindingsProvider)
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.context
 */
public final class BindingUiContentProviderContext {
	private final Map<String, Object> m_values = new HashMap<>();
	private String m_direction;

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the direction for current state.
	 */
	public String getDirection() {
		return m_direction;
	}

	/**
	 * Sets current direction (target, model and etc.).
	 */
	public void setDirection(String direction) {
		m_direction = direction;
	}

	/**
	 * @return the value for given name.
	 */
	public Object getValue(String name) {
		return m_values.get(name);
	}

	/**
	 * Sets value for given name.
	 */
	public void setValue(String name, Object value) {
		m_values.put(name, value);
	}
}