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
package org.eclipse.wb.internal.core.utils.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for XML.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public final class Model {
	////////////////////////////////////////////////////////////////////////////
	//
	// Encoding
	//
	////////////////////////////////////////////////////////////////////////////
	private String m_charset;

	/**
	 * Sets the XML document charset.
	 */
	public void setCharset(String charset) {
		m_charset = charset;
	}

	/**
	 * @return the XML document charset, may be <code>null</code> if unknown.
	 */
	public String getCharset() {
		return m_charset;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Listeners
	//
	////////////////////////////////////////////////////////////////////////////
	private final List<IModelChangedListener> m_listeners = new ArrayList<>();

	/**
	 * Adds the listener to the list of listeners that will be notified on model changes.
	 */
	public void addModelChangedListener(IModelChangedListener listener) {
		if (!m_listeners.contains(listener)) {
			m_listeners.add(listener);
		}
	}

	/**
	 * Takes the listener off the list of registered change listeners.
	 */
	public void removeModelChangedListener(IModelChangedListener listener) {
		m_listeners.remove(listener);
	}

	/**
	 * Delivers change event to all the registered listeners.
	 */
	public void fireModelChanged(ModelChangedEvent event) {
		List<IModelChangedListener> listeners = new ArrayList<>(m_listeners);
		for (IModelChangedListener listener : listeners) {
			listener.modelChanged(event);
		}
	}
}
