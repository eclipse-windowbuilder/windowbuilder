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
package org.eclipse.wb.internal.core.model.property.editor.style;

import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Sub-property for {@link StylePropertyEditor}.
 *
 * @author lobas_av
 * @coverage core.model.property.editor
 */
public final class SubStyleProperty extends Property {
	private final Property m_mainProperty;
	private final SubStylePropertyImpl m_propertyImpl;
	private String m_overrideTitle = null;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SubStyleProperty(Property mainProperty, SubStylePropertyImpl propertyImpl) {
		super(propertyImpl.createEditor());
		m_mainProperty = mainProperty;
		m_propertyImpl = propertyImpl;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTitle() {
		if (m_overrideTitle == null) {
			return m_propertyImpl.getTitle();
		} else {
			return m_overrideTitle;
		}
	}

	public void setOverrideTitle(String title) {
		m_overrideTitle = title;
	}

	@Override
	public boolean isModified() throws Exception {
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Value
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getValue() throws Exception {
		return m_propertyImpl.getValue(m_mainProperty);
	}

	@Override
	public void setValue(Object value) throws Exception {
		m_propertyImpl.setValue(m_mainProperty, value);
	}
}