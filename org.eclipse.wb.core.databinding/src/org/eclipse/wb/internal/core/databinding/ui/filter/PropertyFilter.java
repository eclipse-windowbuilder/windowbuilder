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
import org.eclipse.jface.viewers.Viewer;

/**
 * Filter for {@link IObserveInfo} properties.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public abstract class PropertyFilter {
	private final String m_name;
	private final ImageDescriptor m_image;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PropertyFilter(String name, ImageDescriptor image) {
		m_name = name;
		m_image = image;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the name to display for user.
	 */
	public final String getName() {
		return m_name;
	}

	/**
	 * @return the image to display for user.
	 */
	public final ImageDescriptor getImageDescriptor() {
		return m_image;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Filter
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns whether the given {@link IObserveInfo} element makes it through this filter.
	 */
	public abstract boolean select(Viewer viewer, IObserveInfo propertyObserve);
}