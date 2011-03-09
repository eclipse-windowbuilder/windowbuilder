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
package org.eclipse.wb.rcp.databinding;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;

/**
 * This class may be freely distributed as part of any application or plugin.
 * 
 * @author lobas_av
 */
/*package*/abstract class BeansObservableFactory implements IObservableFactory {
	private final Class<?> m_beanClass;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BeansObservableFactory(Class<?> beanClass) {
		m_beanClass = beanClass;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// IObservableFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IObservable createObservable(Object target) {
		if (target instanceof IObservable) {
			return (IObservable) target;
		}
		if (Utils.instanceOf(m_beanClass, target)) {
			return createBeanObservable(target);
		}
		return null;
	}
	/**
	 * Creates an observable for the given target object.
	 */
	protected abstract IObservable createBeanObservable(Object target);
}