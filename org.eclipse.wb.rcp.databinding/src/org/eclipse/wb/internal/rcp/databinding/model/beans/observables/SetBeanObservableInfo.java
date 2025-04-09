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

import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanPropertyBindableInfo;

/**
 * Model for observable object {@code BeanProperties.set(...).observe(...)}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class SetBeanObservableInfo extends BeanObservableInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SetBeanObservableInfo(BeanBindableInfo bindableObject,
			BeanPropertyBindableInfo bindableProperty) {
		super(bindableObject, bindableProperty);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getPresentationText() throws Exception {
		return super.getPresentationText() + "(Set)";
	}
}