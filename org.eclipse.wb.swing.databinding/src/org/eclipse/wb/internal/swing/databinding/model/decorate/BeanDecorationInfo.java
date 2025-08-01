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
package org.eclipse.wb.internal.swing.databinding.model.decorate;

import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * Mode for describe properties as {@code preferred}, {@code advanced} and {@code hidden}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public final class BeanDecorationInfo {
	private final BeanDecorationInfo m_parent;
	private Set<String> m_preferred = Collections.emptySet();
	private Set<String> m_advanced = Collections.emptySet();
	private Set<String> m_hidden = Collections.emptySet();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BeanDecorationInfo(BeanDecorationInfo parent) {
		m_parent = parent;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	void setPreferredProperties(String[] properties) {
		if (!ArrayUtils.isEmpty(properties)) {
			m_preferred = Set.copyOf(Arrays.asList(properties));
		}
	}

	void setAdvancedProperties(String[] properties) {
		if (!ArrayUtils.isEmpty(properties)) {
			m_advanced = Set.copyOf(Arrays.asList(properties));
		}
	}

	void setHiddenProperties(String[] properties) {
		if (!ArrayUtils.isEmpty(properties)) {
			m_hidden = Set.copyOf(Arrays.asList(properties));
		}
	}

	/**
	 * @return {@link IObserveDecorator} for given {@code propertyName}.
	 */
	public IObserveDecorator getDecorator(String propertyName) {
		// preferred
		if (m_preferred.contains(propertyName)) {
			return IObserveDecorator.BOLD;
		}
		// advanced
		if (m_advanced.contains(propertyName)) {
			return IObserveDecorator.ITALIC;
		}
		// hidden
		if (m_hidden.contains(propertyName)) {
			return IObserveDecorator.HIDDEN;
		}
		// route to parent
		if (m_parent != null) {
			return m_parent.getDecorator(propertyName);
		}
		// default
		return IObserveDecorator.DEFAULT;
	}
}