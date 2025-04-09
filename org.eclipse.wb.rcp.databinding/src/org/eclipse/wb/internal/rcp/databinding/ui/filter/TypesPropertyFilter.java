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
package org.eclipse.wb.internal.rcp.databinding.ui.filter;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;

/**
 * Filter for {@link BindableInfo} properties over {@link Class} type.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class TypesPropertyFilter
extends
org.eclipse.wb.internal.core.databinding.ui.filter.TypesPropertyFilter {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TypesPropertyFilter(String name, ImageDescriptor image, Class<?>... types) {
		super(name, image, types);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PropertyFilter
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean select(Viewer viewer, IObserveInfo propertyObserve) {
		BindableInfo bindable = (BindableInfo) propertyObserve;
		return select(bindable.getObjectType());
	}
}