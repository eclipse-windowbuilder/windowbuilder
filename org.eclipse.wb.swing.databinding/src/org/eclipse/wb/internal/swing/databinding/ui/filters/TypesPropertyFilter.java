/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.databinding.ui.filters;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanPropertyObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.ElPropertyObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.ObjectPropertyObserveInfo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;

/**
 * Filter for {@link ObserveInfo} properties over {@link Class} type.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
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
		ObserveInfo observe = (ObserveInfo) propertyObserve;
		if (observe.getParent() instanceof BeanPropertyObserveInfo) {
			return true;
		}
		return observe instanceof ObjectPropertyObserveInfo
				|| observe instanceof ElPropertyObserveInfo
				|| select(observe.getObjectClass());
	}
}