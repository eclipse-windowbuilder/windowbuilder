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
package org.eclipse.wb.rcp.databinding;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;

/**
 * This class may be freely distributed as part of any application or plugin.
 * 
 * @author lobas_av
 */
public class BeansListObservableFactory extends BeansObservableFactory {
	private final String m_propertyName;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BeansListObservableFactory(Class<?> beanClass, String propertyName) {
		super(beanClass);
		m_propertyName = propertyName;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// BeansObservableFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IObservable createBeanObservable(Object target) {
		return BeanProperties.list(m_propertyName).observe(Realm.getDefault());
	}
}