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
package org.eclipse.wb.internal.rcp.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.AbstractDescriptor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * SWT widget descriptor.
 *
 * @author lobas_av
 * @coverage bindings.rcp.wizard.auto
 */
public final class SwtWidgetDescriptor extends AbstractDescriptor {
	private String m_fullClassName;
	private String m_className;
	private String m_createCode;
	private String m_bindingCode;
	private String[] m_classes;

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the widget short class name.
	 */
	public String getClassName() {
		return m_className;
	}

	/**
	 * @return the widget class name.
	 */
	public String getFullClassName() {
		return m_fullClassName;
	}

	/**
	 * Sets widget class name.
	 */
	public void setFullClassName(String className) {
		m_fullClassName = className;
		m_className = ClassUtils.getShortClassName(m_fullClassName);
	}

	/**
	 * @return the widget creation code.
	 */
	public String getCreateCode(String parent) {
		return m_createCode.replace("%parent%", parent);
	}

	/**
	 * Sets the widget creation code.
	 */
	public void setCreateCode(String code) {
		m_createCode = code;
	}

	/**
	 * @return the widget binding code.
	 */
	public String getBindingCode(String widget) {
		return m_bindingCode.replace("%widget%", widget);
	}

	/**
	 * Sets the widget binding code.
	 */
	public void setBindingCode(String code) {
		m_bindingCode = code;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Default
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets widget binding property association type.
	 */
	public void setPropertyType(String types) {
		m_classes = StringUtils.split(types);
	}

	@Override
	public boolean isDefault(Object property) {
		PropertyAdapter propertyAdapter = (PropertyAdapter) property;
		Class<?> propertyType = propertyAdapter.getType();
		//
		if (propertyType != null) {
			return ArrayUtils.contains(m_classes, propertyType.getName());
		}
		//
		return false;
	}
}