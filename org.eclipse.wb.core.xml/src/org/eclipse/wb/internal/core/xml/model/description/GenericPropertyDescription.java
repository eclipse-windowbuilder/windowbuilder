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
package org.eclipse.wb.internal.core.xml.model.description;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.converter.ExpressionConverter;

/**
 * Description of single property of {@link XmlObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage XML.model.description
 */
public final class GenericPropertyDescription extends AbstractDescription {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GenericPropertyDescription(String id,
			String name,
			Class<?> type,
			ExpressionAccessor accessor) {
		m_id = id;
		m_name = name;
		m_title = name;
		m_type = type;
		m_accessor = accessor;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Id
	//
	////////////////////////////////////////////////////////////////////////////
	private final String m_id;

	/**
	 * @return the id of this property.
	 */
	public String getId() {
		return m_id;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Name
	//
	////////////////////////////////////////////////////////////////////////////
	private final String m_name;

	/**
	 * @return the name of this property.
	 */
	public String getName() {
		return m_name;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Title
	//
	////////////////////////////////////////////////////////////////////////////
	private final String m_title;

	/**
	 * @return the title of this property.
	 */
	public String getTitle() {
		// try to find "title" tag
		{
			String title = getTag("title");
			if (title != null) {
				return title;
			}
		}
		// use default title
		return m_title;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Type
	//
	////////////////////////////////////////////////////////////////////////////
	private final Class<?> m_type;

	/**
	 * @return the type of this property.
	 */
	public Class<?> getType() {
		return m_type;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Category
	//
	////////////////////////////////////////////////////////////////////////////
	private PropertyCategory m_category = PropertyCategory.NORMAL;

	/**
	 * Sets the {@link PropertyCategory} for this property.
	 */
	public void setCategory(PropertyCategory category) {
		m_category = category;
	}

	/**
	 * @return the {@link PropertyCategory} of this property.
	 */
	public PropertyCategory getCategory() {
		return m_category;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Default value
	//
	////////////////////////////////////////////////////////////////////////////
	private Object m_defaultValue = Property.UNKNOWN_VALUE;

	/**
	 * @return the forced default value.
	 */
	public Object getDefaultValue() {
		return m_defaultValue;
	}

	/**
	 * Sets the forced default value.
	 */
	public void setDefaultValue(Object defaultValue) {
		m_defaultValue = defaultValue;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Accessor
	//
	////////////////////////////////////////////////////////////////////////////
	private final ExpressionAccessor m_accessor;

	/**
	 * @return the {@link ExpressionAccessor} for this property.
	 */
	public ExpressionAccessor getAccessor() {
		return m_accessor;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Converter
	//
	////////////////////////////////////////////////////////////////////////////
	private ExpressionConverter m_converter;

	/**
	 * Sets the {@link ExpressionConverter} for this property.
	 */
	public void setConverter(ExpressionConverter converter) {
		m_converter = converter;
	}

	/**
	 * @return the {@link ExpressionConverter} for this property.
	 */
	public ExpressionConverter getConverter() {
		return m_converter;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editor
	//
	////////////////////////////////////////////////////////////////////////////
	private PropertyEditor m_editor;

	/**
	 * Sets the {@link PropertyEditor} for this property.
	 */
	public void setEditor(PropertyEditor editor) {
		m_editor = editor;
	}

	/**
	 * @return the {@link PropertyEditor} for this property.
	 */
	public PropertyEditor getEditor() {
		return m_editor;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void visit(XmlObjectInfo object, int state) throws Exception {
		super.visit(object, state);
		m_accessor.visit(object, state);
	}
}
