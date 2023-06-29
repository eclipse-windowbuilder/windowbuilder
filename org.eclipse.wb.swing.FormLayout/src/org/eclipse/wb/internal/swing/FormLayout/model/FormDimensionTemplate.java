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
package org.eclipse.wb.internal.swing.FormLayout.model;

import org.eclipse.wb.internal.swing.FormLayout.Activator;

import org.eclipse.jface.resource.ImageDescriptor;

import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.FormSpecs;

/**
 * Description for {@link FormSpec} template.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public final class FormDimensionTemplate {
	private final String m_fieldName;
	private final boolean m_component;
	private final String m_title;
	private final ImageDescriptor m_icon;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormDimensionTemplate(String fieldName, boolean component, String title, String iconName) {
		m_fieldName = fieldName;
		m_component = component;
		m_title = title;
		m_icon = Activator.getImageDescriptor("templates/" + iconName);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the name of field in {@link FormSpecs}.
	 */
	public String getFieldName() {
		return m_fieldName;
	}

	/**
	 * @return <code>true</code> if this template is component, or <code>false</code> if it is
	 *         constant.
	 */
	public boolean isComponent() {
		return m_component;
	}

	/**
	 * @return the title to display.
	 */
	public String getTitle() {
		return m_title;
	}

	/**
	 * @return the {@link ImageDescriptor} to display.
	 */
	public ImageDescriptor getIcon() {
		return m_icon;
	}
}
