/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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

import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

/**
 * Model for observable object {@code BeanProperties.list(...).observeDetail(...)}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class DetailListBeanObservableInfo extends DetailBeanObservableInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DetailListBeanObservableInfo(ObservableInfo masterObservable,
			Class<?> detailBeanClass,
			String detailPropertyReference,
			Class<?> detailPropertyType) {
		super(masterObservable, detailBeanClass, detailPropertyReference, detailPropertyType);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getPresentationPrefix() {
		return "List";
	}
}