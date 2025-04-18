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
package org.eclipse.wb.rcp.databinding;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.emf.databinding.edit.EMFEditObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.domain.EditingDomain;

/**
 * This class may be freely distributed as part of any application or plugin.
 * 
 * @author lobas_av
 */
public class EMFEditBeansListObservableFactory extends BeansObservableFactory {
	private final EditingDomain m_domain;
	private final EStructuralFeature m_eStructuralFeature;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EMFEditBeansListObservableFactory(Class<?> beanClass,
			EditingDomain domain,
			EStructuralFeature eStructuralFeature) {
		super(beanClass);
		m_domain = domain;
		m_eStructuralFeature = eStructuralFeature;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// BeansObservableFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IObservable createBeanObservable(Object target) {
		if (target instanceof EObject) {
			return EMFEditObservables.observeList(
				Realm.getDefault(),
				m_domain,
				(EObject) target,
				m_eStructuralFeature);
		}
		return null;
	}
}