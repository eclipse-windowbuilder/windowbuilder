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
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanPropertyBindableInfo;

/**
 * Model for observable object <code>BeansObservables.observeValue(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class ValueBeanObservableInfo extends BeanObservableInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ValueBeanObservableInfo(BeanBindableInfo bindableObject,
			BeanPropertyBindableInfo bindableProperty) {
		super(bindableObject, bindableProperty);
	}
}