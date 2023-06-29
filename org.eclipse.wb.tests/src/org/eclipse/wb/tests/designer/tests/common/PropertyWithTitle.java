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
package org.eclipse.wb.tests.designer.tests.common;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;

/**
 * Implementation of {@link Property} that has some title.
 *
 * @author scheglov_ke
 */
public class PropertyWithTitle extends Property {
	private final String m_title;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PropertyWithTitle(String title) {
		this(StringPropertyEditor.INSTANCE, title);
	}

	public PropertyWithTitle(PropertyEditor propertyEditor, String title) {
		super(propertyEditor);
		m_title = title;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Property
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTitle() {
		return m_title;
	}

	@Override
	public boolean isModified() throws Exception {
		return false;
	}

	@Override
	public Object getValue() throws Exception {
		return UNKNOWN_VALUE;
	}

	@Override
	public void setValue(Object value) throws Exception {
	}
}
