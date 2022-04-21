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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.GenericPropertyGetValue;
import org.eclipse.wb.internal.core.xml.model.broadcast.GenericPropertySetExpression;
import org.eclipse.wb.internal.core.xml.model.broadcast.GenericPropertySetValue;
import org.eclipse.wb.internal.core.xml.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ContentExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.converter.ExpressionConverter;

import org.apache.commons.lang.ObjectUtils;

import java.util.List;

/**
 * {@link Property} for {@link XmlObjectInfo}, based of {@link GenericPropertyDescription}.
 *
 * @author scheglov_ke
 * @coverage XML.model.property
 */
public final class GenericPropertyImpl extends GenericProperty {
  private final GenericPropertyDescription m_description;
  private final ExpressionConverter m_converter;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GenericPropertyImpl(XmlObjectInfo object, GenericPropertyDescription description) {
    this(object, description, description.getTitle());
  }

  /**
   * Creates identical copy of given {@link GenericPropertyImpl}, but with different title.
   */
  public GenericPropertyImpl(GenericPropertyImpl property, String title) {
    this(property.getObject(), property.getDescription(), title);
  }

  private GenericPropertyImpl(XmlObjectInfo object,
      GenericPropertyDescription description,
      String title) {
    super(object, title, description.getEditor());
    m_description = description;
    setCategory(description.getCategory());
    m_converter = m_description.getConverter();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the underlying {@link GenericPropertyDescription}.
   */
  public GenericPropertyDescription getDescription() {
    return m_description;
  }

  @Override
  public Class<?> getType() {
    return m_description.getType();
  }

  /**
   * @return <code>true</code> if this {@link GenericPropertyImpl} has given tag with value
   *         <code>"true"</code>.
   */
  @Override
  public boolean hasTrueTag(String tag) {
    return m_description.hasTrueTag(tag);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isModified() throws Exception {
    for (ExpressionAccessor accessor : getAccessors()) {
      if (accessor.isModified(m_object)) {
        return true;
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getValue() throws Exception {
    // allow broadcast listeners to set value
    {
      Object[] valueArray = new Object[]{UNKNOWN_VALUE};
      m_object.getBroadcast(GenericPropertyGetValue.class).invoke(this, valueArray);
      if (valueArray[0] != UNKNOWN_VALUE) {
        return valueArray[0];
      }
    }
    // get value from accessor
    {
      Object value = m_description.getAccessor().getValue(m_object);
      if (value != UNKNOWN_VALUE) {
        return value;
      }
    }
    // default value
    return getDefaultValue();
  }

  @Override
  protected void setValueEx(Object value) throws Exception {
    // validate value
    {
      Object[] valueToValidate = new Object[]{value};
      boolean[] validationStatus = new boolean[]{true};
      m_object.getBroadcast(GenericPropertySetValue.class).invoke(
          this,
          valueToValidate,
          validationStatus);
      if (!validationStatus[0]) {
        return;
      }
      value = valueToValidate[0];
    }
    // may be editor wants to handle it
    if (m_editor instanceof ISetValuePropertyEditor) {
      ((ISetValuePropertyEditor) m_editor).setValue(this, value);
      return;
    }
    // prepare expression
    String expression;
    Object defaultValue = getDefaultValue();
    if (value == UNKNOWN_VALUE || ObjectUtils.equals(value, defaultValue)) {
      if (hasTrueTag("x-keepDefault")) {
        expression = m_converter.toSource(m_object, defaultValue);
      } else {
        expression = null;
      }
    } else if (m_editor instanceof IExpressionPropertyEditor) {
      expression = ((IExpressionPropertyEditor) m_editor).getValueExpression(this, value);
    } else {
      expression = m_converter.toSource(m_object, value);
    }
    // set expression
    setExpression(expression, value);
  }

  @Override
  public String getExpression() {
    for (ExpressionAccessor accessor : getAccessors()) {
      String expression = accessor.getExpression(m_object);
      if (expression != null) {
        return expression;
      }
    }
    return null;
  }

  @Override
  public void setExpression(String expression, Object value) throws Exception {
    // validate expression
    {
      String[] expressionToValidate = new String[]{expression};
      Object[] valueToValidate = new Object[]{value};
      boolean[] validationStatus = new boolean[]{true};
      m_object.getBroadcast(GenericPropertySetExpression.class).invoke(
          this,
          expressionToValidate,
          valueToValidate,
          validationStatus);
      if (!validationStatus[0]) {
        return;
      }
      expression = expressionToValidate[0];
      value = valueToValidate[0];
    }
    // set expression
    if (value != UNKNOWN_VALUE
        && !hasTrueTag("x-keepDefault")
        && ObjectUtils.equals(value, getDefaultValue())) {
      expression = null;
    }
    setExpression0(expression);
  }

  /**
   * Sets attribute {@link String} value.
   */
  private void setExpression0(String expression) throws Exception {
    List<ExpressionAccessor> accessors = getAccessors();
    // try to use modified accessor
    for (ExpressionAccessor accessor : accessors) {
      if (accessor.isModified(m_object)) {
        accessor.setExpression(m_object, expression);
        return;
      }
    }
    // use first accessor
    accessors.get(0).setExpression(m_object, expression);
  }

  /**
   * @return all {@link ExpressionAccessor} which can be used.
   */
  public List<ExpressionAccessor> getAccessors() {
    List<ExpressionAccessor> accessors = Lists.newArrayList();
    // content accessor first
    if (m_description.hasTrueTag("isContent")) {
      accessors.add(ContentExpressionAccessor.INSTANCE);
    }
    // default accessor
    accessors.add(m_description.getAccessor());
    // done
    return accessors;
  }

  /**
   * @return the default value of this {@link GenericPropertyImpl}.
   */
  private Object getDefaultValue() throws Exception {
    // if has forced default value in description, use it
    {
      Object defaultValue = m_description.getDefaultValue();
      if (defaultValue != UNKNOWN_VALUE) {
        return defaultValue;
      }
    }
    // use default value from accessor
    return m_description.getAccessor().getDefaultValue(m_object);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Composite
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Property getComposite(Property[] properties) {
    return GenericPropertyComposite.create(properties);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public <T> T getAdapter(Class<T> adapter) {
    if (adapter == PropertyTooltipProvider.class) {
      return m_description.getAccessor().getAdapter(adapter);
    }
    return super.getAdapter(adapter);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the value of XML attribute that has same value as current value of this
   *         {@link GenericPropertyImpl}, or <code>null</code> if no such value can be provided.
   */
  public String getClipboardSource() throws Exception {
    if (!isModified()) {
      return null;
    }
    if (m_editor instanceof IClipboardSourceProvider) {
      return ((IClipboardSourceProvider) m_editor).getClipboardSource(this);
    }
    if (m_converter != null) {
      return m_converter.toSource(m_object, getValue());
    }
    // no clipboard source
    return null;
  }
}
