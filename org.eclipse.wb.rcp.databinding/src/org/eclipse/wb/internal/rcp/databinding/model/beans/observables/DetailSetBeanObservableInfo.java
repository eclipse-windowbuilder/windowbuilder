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

import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

/**
 * Model for observable object <code>BeansObservables.observeDetailSet(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class DetailSetBeanObservableInfo extends DetailBeanObservableInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DetailSetBeanObservableInfo(ObservableInfo masterObservable,
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
		return "Set";
	}
}