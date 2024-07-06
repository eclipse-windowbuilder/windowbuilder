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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * This class may be freely distributed as part of any application or plugin.
 * 
 * @author lobas_av
 */
/*package*/abstract class EMFListenerSupport {
	private final List<EStructuralFeature> m_properties;
	private final Set<IdentityWrapper> m_elementsListenedTo = new HashSet<IdentityWrapper>();
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EMFListenerSupport(List<EStructuralFeature> properties) {
		m_properties = properties;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void hookListener(Object addedElement) {
		if (processListener(true, addedElement)) {
			m_elementsListenedTo.add(new IdentityWrapper(addedElement));
		}
	}
	public void unhookListener(Object removedElement) {
		if (removedElement.getClass() == IdentityWrapper.class) {
			IdentityWrapper wrapper = (IdentityWrapper) removedElement;
			removedElement = wrapper.unwrap();
		}
		if (processListener(false, removedElement)) {
			m_elementsListenedTo.remove(new IdentityWrapper(removedElement));
		}
	}
	public void dispose() {
		for (IdentityWrapper wrapper : m_elementsListenedTo) {
			if (processListener(false, wrapper.unwrap())) {
				m_elementsListenedTo.remove(wrapper);
			}
		}
		m_elementsListenedTo.clear();
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Listener
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean processListener(boolean add, Object target) {
		if (target instanceof EObject) {
			EObject eObject = (EObject) target;
			EList<Adapter> eAdapters = eObject.eAdapters();
			if (add) {
				eAdapters.add(m_propertyChangeAdapter);
			} else {
				eAdapters.remove(m_propertyChangeAdapter);
			}
			return true;
		}
		return false;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	private Adapter m_propertyChangeAdapter = new AdapterImpl() {
		@Override
		public void notifyChanged(Notification notification) {
			if (notification.getEventType() == Notification.SET
				&& m_properties.contains(notification.getFeature())) {
				fireLabelPropertyChanged(notification.getNotifier());
			}
		}
	};
	protected abstract void fireLabelPropertyChanged(Object element);
}