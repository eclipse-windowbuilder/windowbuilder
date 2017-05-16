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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstParser;

import org.apache.commons.lang.ObjectUtils;

/**
 * Description for single parameter of {@link MethodDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ParameterDescription extends AbstractDescription {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("{");
    sb.append(m_type.getName());
    // parent/child
    if (m_parent) {
      sb.append(",parent");
    }
    if (m_child) {
      sb.append(",child");
    }
    // parent2/child2
    if (m_parent2) {
      sb.append(",parent2");
    }
    if (m_child2) {
      sb.append(",child2");
    }
    // close
    sb.append("}");
    return sb.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Join
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Joins this {@link ParameterDescription} with given one.
   */
  public void join(ParameterDescription description) {
    m_name = (String) ObjectUtils.defaultIfNull(m_name, description.m_name);
    // editor/converter
    if (description.m_editor != null) {
      m_converter = description.m_converter;
      m_editor = description.m_editor;
    }
    // tags
    putTags(description.getTags());
    // other
    m_property = description.m_property;
    m_defaultSource = description.m_defaultSource;
    {
      m_parent = description.m_parent;
      m_child = description.m_child;
    }
    {
      m_parent2 = description.m_parent2;
      m_child2 = description.m_child2;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Index
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_index;

  /**
   * @return the index of this parameter.
   */
  public int getIndex() {
    return m_index;
  }

  /**
   * Sets the index of this parameter.
   */
  public void setIndex(int index) {
    m_index = index;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_name;

  /**
   * @return the name of parameter.
   */
  public String getName() {
    return m_name;
  }

  /**
   * Sets the name of parameter.
   */
  public void setName(String name) {
    m_name = name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type
  //
  ////////////////////////////////////////////////////////////////////////////
  private Class<?> m_type;

  /**
   * @return the type of this parameter.
   */
  public Class<?> getType() {
    return m_type;
  }

  /**
   * Sets the type of this parameter.
   */
  public void setType(Class<?> type) throws Exception {
    m_type = type;
    m_converterDefault = DescriptionPropertiesHelper.getConverterForType(m_type);
    m_editorDefault = DescriptionPropertiesHelper.getEditorForType(m_type);
    m_defaultSource = AstParser.getDefaultValue(m_type.getName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parent flag
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_parent;

  /**
   * @return <code>true</code> if this parameter contains reference on parent {@link JavaInfo}.
   */
  public boolean isParent() {
    return m_parent;
  }

  /**
   * Marks this parameter as passing parent of this component.
   */
  public void setParent(boolean parent) {
    m_parent = parent;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Child flag
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_child;

  /**
   * @return <code>true</code> if this parameter contains reference on child {@link JavaInfo}.
   */
  public boolean isChild() {
    return m_child;
  }

  /**
   * Marks this parameter as passing child of this component.
   */
  public void setChild(boolean child) {
    m_child = child;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "parent2" flag
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_parent2;

  /**
   * @return <code>true</code> if this parameter contains (optional) reference of parent
   *         {@link JavaInfo}.
   *
   *         We use {@link #isParent2()} and {@link #isChild2()} to create parent/child link between
   *         components because they appear as parameters of this method invocation.
   */
  public boolean isParent2() {
    return m_parent2;
  }

  /**
   * Marks this parameter as optional secondary parent.
   */
  public void setParent2(boolean parent2) {
    m_parent2 = parent2;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "child2" flag
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_child2;

  /**
   * @return <code>true</code> if this parameter contains (optional) reference of child
   *         {@link JavaInfo}.
   *
   *         We use {@link #isParent2()} and {@link #isChild2()} to create parent/child link between
   *         components because they appear as parameters of this method invocation.
   */
  public boolean isChild2() {
    return m_child2;
  }

  /**
   * Marks this parameter as optional secondary child.
   */
  public void setChild2(boolean child2) {
    m_child2 = child2;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_property;

  /**
   * @return the id of property to which this parameter should be bound, may be <code>null</code>.
   */
  public String getProperty() {
    return m_property;
  }

  /**
   * Sets the id of property to which this parameter should be bound.
   */
  public void setProperty(String property) {
    m_property = property;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Converter
  //
  ////////////////////////////////////////////////////////////////////////////
  private ExpressionConverter m_converter;
  private ExpressionConverter m_converterDefault;

  /**
   * @return the {@link ExpressionConverter} for this parameter.
   */
  public ExpressionConverter getConverter() {
    return m_converter != null ? m_converter : m_converterDefault;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editor
  //
  ////////////////////////////////////////////////////////////////////////////
  private PropertyEditor m_editor;
  private PropertyEditor m_editorDefault;

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
    return m_editor != null ? m_editor : m_editorDefault;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default source
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_defaultSource;

  /**
   * @return the default source for this parameter that should be used when user asks for
   *         "cleaning". Can return <code>null</code> that means that parameter can not be
   *         "cleaned".
   */
  public String getDefaultSource() {
    return m_defaultSource;
  }

  /**
   * Sets the default source for this parameter.
   */
  public void setDefaultSource(String defaultSource) {
    m_defaultSource = defaultSource;
  }
}
