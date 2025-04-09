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
package org.eclipse.wb.internal.swing.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.AbstractDescriptor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Swing component descriptor.
 *
 * @author lobas_av
 * @coverage bindings.swing.wizard.auto
 */
public final class SwingComponentDescriptor extends AbstractDescriptor {
	private String m_componentClassName;
	private String[] m_propertyClasses;

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public String getComponentClass() {
		return m_componentClassName;
	}

	public void setComponentClass(String className) {
		m_componentClassName = className;
	}

	public String getPropertyClass() {
		return m_propertyClasses[m_propertyClasses.length - 1];
	}

	public void setPropertyClass(String classes) {
		m_propertyClasses = StringUtils.split(classes);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Default
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isDefault(Object property) {
		PropertyAdapter propertyAdapter = (PropertyAdapter) property;
		Class<?> propertyType = propertyAdapter.getType();
		if (propertyType != null) {
			return ArrayUtils.contains(m_propertyClasses, propertyType.getName());
		}
		return false;
	}
}