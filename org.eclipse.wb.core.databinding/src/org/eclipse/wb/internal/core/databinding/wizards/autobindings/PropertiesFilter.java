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
package org.eclipse.wb.internal.core.databinding.wizards.autobindings;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * {@link ViewerFilter} for hide/show the properties which can't be bound.
 *
 * @author lobas_av
 * @coverage bindings.wizard.auto
 */
public final class PropertiesFilter extends ViewerFilter {
	private final DescriptorContainer m_widgetContainer;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PropertiesFilter(DescriptorContainer widgetContainer) {
		m_widgetContainer = widgetContainer;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ViewerFilter
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return m_widgetContainer.getDefaultDescriptor(element, false) != null;
	}
}