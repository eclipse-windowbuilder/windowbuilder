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
package org.eclipse.wb.internal.rcp.databinding.model.beans.observables;

import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.CollectionPropertyBindableInfo;

/**
 * Model for observable object {@link org.eclipse.core.databinding.observable.set.WritableSet}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class WritableSetBeanObservableInfo extends CollectionObservableInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WritableSetBeanObservableInfo(BeanBindableInfo bindableObject,
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
		return "WritableSet";
	}
}