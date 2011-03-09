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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class may be freely distributed as part of any application or plugin.
 * 
 * @author lobas_av
 */
/*package*/final class ListenerSupport {
	private final PropertyChangeListener m_listener;
	private final List<String> m_properties;
	private final Set<IdentityWrapper> m_elementsListenedTo = new HashSet<IdentityWrapper>();
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ListenerSupport(final PropertyChangeListener listener, List<String> properties) {
		m_properties = properties;
		m_listener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (m_properties.contains(event.getPropertyName())) {
					listener.propertyChange(event);
				}
			}
		};
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void hookListener(Object addedElement) {
		if (processListener("addPropertyChangeListener", addedElement)) {
			m_elementsListenedTo.add(new IdentityWrapper(addedElement));
		}
	}
	public void unhookListener(Object removedElement) {
		if (removedElement.getClass() == IdentityWrapper.class) {
			IdentityWrapper wrapper = (IdentityWrapper) removedElement;
			removedElement = wrapper.unwrap();
		}
		if (processListener("removePropertyChangeListener", removedElement)) {
			m_elementsListenedTo.remove(new IdentityWrapper(removedElement));
		}
	}
	public void dispose() {
		for (IdentityWrapper wrapper : m_elementsListenedTo) {
			if (processListener("removePropertyChangeListener", wrapper.unwrap())) {
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
	private boolean processListener(String methodName, Object target) {
		Method method = null;
		int parameters = 0;
		try {
			try {
				method =
						target.getClass().getMethod(
							methodName,
							new Class[]{String.class, PropertyChangeListener.class});
				parameters = 2;
			} catch (NoSuchMethodException e) {
				method = target.getClass().getMethod(methodName, new Class[]{PropertyChangeListener.class});
				parameters = 1;
			}
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
		if (method != null && parameters != 0) {
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			try {
				if (parameters == 1) {
					method.invoke(target, new Object[]{m_listener});
				} else {
					for (String propertyName : m_properties) {
						method.invoke(target, new Object[]{propertyName, m_listener});
					}
				}
				return true;
			} catch (Exception e) {
			}
		}
		return false;
	}
}