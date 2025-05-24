/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package convert;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author lobas_av
 */
public abstract class AbstractModelObject {
	private final PropertyChangeSupport m_propertyChangeSupport = new PropertyChangeSupport(this);

	////////////////////////////////////////////////////////////////////////////
	//
	// Property Support
	//
	////////////////////////////////////////////////////////////////////////////
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		m_propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		m_propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		m_propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		m_propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		m_propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}
}