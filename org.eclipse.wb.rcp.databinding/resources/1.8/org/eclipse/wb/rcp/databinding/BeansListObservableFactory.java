/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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