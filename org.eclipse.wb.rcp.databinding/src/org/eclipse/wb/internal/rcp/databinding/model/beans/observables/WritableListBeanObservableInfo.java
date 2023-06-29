/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.databinding.model.beans.observables;

import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.CollectionPropertyBindableInfo;

/**
 * Model for observable object {@link org.eclipse.core.databinding.observable.list.WritableList}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class WritableListBeanObservableInfo extends CollectionObservableInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WritableListBeanObservableInfo(BeanBindableInfo bindableObject,
			CollectionPropertyBindableInfo bindableProperty,
			Class<?> elementType) {
		super(bindableObject, bindableProperty, elementType);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getPresentationPrefix() {
		return "WritableList";
	}
}