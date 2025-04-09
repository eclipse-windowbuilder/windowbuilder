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
package org.eclipse.wb.internal.core.databinding.ui.filter;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;

import org.eclipse.jface.resource.ImageDescriptor;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Filter for {@link IObserveInfo} properties over {@link Class} type.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public abstract class TypesPropertyFilter extends PropertyFilter {
	private final Class<?>[] m_types;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TypesPropertyFilter(String name, ImageDescriptor image, Class<?>... types) {
		super(name, image);
		m_types = types;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Filter
	//
	////////////////////////////////////////////////////////////////////////////
	protected final boolean select(Class<?> type) {
		return ArrayUtils.contains(m_types, type);
	}
}