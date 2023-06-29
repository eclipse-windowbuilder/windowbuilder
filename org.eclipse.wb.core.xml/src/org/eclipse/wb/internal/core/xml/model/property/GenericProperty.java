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
package org.eclipse.wb.internal.core.xml.model.property;

import org.eclipse.wb.internal.core.model.property.ITypedProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;

/**
 * {@link Property} for {@link XmlObjectInfo}, based of {@link GenericPropertyDescription}.
 *
 * @author scheglov_ke
 * @coverage XML.model.property
 */
public abstract class GenericProperty extends XmlProperty implements ITypedProperty {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GenericProperty(XmlObjectInfo object, String title, PropertyEditor propertyEditor) {
		super(object, title, propertyEditor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this {@link GenericProperty} has given tag with value
	 *         <code>"true"</code>.
	 */
	public abstract boolean hasTrueTag(String tag);

	////////////////////////////////////////////////////////////////////////////
	//
	// Expression
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the attribute {@link String} expression, may be <code>null</code>.
	 */
	public abstract String getExpression();

	/**
	 * Updates attribute of {@link XmlObjectInfo} to have given string expression. If given value
	 * equals default, then attribute will be removed.
	 */
	public abstract void setExpression(String expression, Object value) throws Exception;
}
