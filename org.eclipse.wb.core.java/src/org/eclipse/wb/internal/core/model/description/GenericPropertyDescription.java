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
package org.eclipse.wb.internal.core.model.description;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SetterAccessor;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Description of single {@link GenericProperty} - its {@link ExpressionAccessor}'s,
 * {@link ExpressionConverter} and {@link PropertyEditor}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class GenericPropertyDescription extends AbstractDescription {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GenericPropertyDescription(String id, String title) {
    this(id, title, null);
  }

  public GenericPropertyDescription(String id, String title, Class<?> type) {
    m_id = id;
    m_title = title;
    m_type = type;
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
  // Title
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_title;

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
  // Accessors
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<ExpressionAccessor> m_accessors = Lists.newArrayList();

  /**
   * @return the list of {@link ExpressionAccessor}'s.
   */
  public List<ExpressionAccessor> getAccessorsList() {
    return m_accessors;
  }

  /**
   * @return the array of {@link ExpressionAccessor}'s.
   */
  public ExpressionAccessor[] getAccessorsArray() {
    return m_accessors.toArray(new ExpressionAccessor[m_accessors.size()]);
  }

  /**
   * Adds given {@link ExpressionAccessor}.
   */
  public void addAccessor(ExpressionAccessor accessor) {
    m_accessors.add(accessor);
    accessor.setPropertyDescription(this);
  }

  /**
   * @return the setter {@link Method}, if this this property is based on {@link SetterAccessor},
   *         may be <code>null</code>.
   */
  Method getSetter() {
    List<ExpressionAccessor> accessors = getAccessorsList();
    for (ExpressionAccessor accessor : accessors) {
      if (accessor instanceof SetterAccessor) {
        return ((SetterAccessor) accessor).getSetter();
      }
    }
    return null;
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
  // Join
  //
  ////////////////////////////////////////////////////////////////////////////
  public void join(GenericPropertyDescription property) {
    m_title = property.getTitle();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void visit(JavaInfo javaInfo, int state) throws Exception {
    super.visit(javaInfo, state);
    for (ExpressionAccessor accessor : m_accessors) {
      accessor.visit(javaInfo, state);
    }
  }
}
