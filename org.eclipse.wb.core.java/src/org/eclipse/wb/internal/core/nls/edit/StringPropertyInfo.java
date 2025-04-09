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
package org.eclipse.wb.internal.core.nls.edit;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;

/**
 * Container class for StringProperty information - property itself, its component and value.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class StringPropertyInfo {
	private final GenericProperty m_property;
	private final String m_value;
	private final String m_title;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public StringPropertyInfo(GenericProperty property) throws Exception {
		m_property = property;
		m_value = (String) m_property.getValue();
		m_title = m_property.getTitle() + ": " + m_value;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the component of this property.
	 */
	public JavaInfo getComponent() {
		return m_property.getJavaInfo();
	}

	/**
	 * @return the {@link GenericProperty} itself.
	 */
	public GenericProperty getProperty() {
		return m_property;
	}

	/**
	 * @return the value of property.
	 */
	public String getValue() {
		return m_value;
	}

	/**
	 * @return the title to display in UI.
	 */
	public String getTitle() {
		return m_title;
	}
}
