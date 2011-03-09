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

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;

/**
 * Implementation of {@link GenericProperty} for composite property.
 * 
 * @author scheglov_ke
 * @coverage XML.model.property
 */
public final class GenericPropertyComposite extends GenericProperty {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  public static GenericPropertyComposite create(Property... properties) {
    // prepare GenericProperty's array
    GenericProperty[] genericProperties = new GenericProperty[properties.length];
    for (int i = 0; i < properties.length; i++) {
      Property property = properties[i];
      genericProperties[i] = (GenericProperty) property;
    }
    // create composite
    GenericPropertyComposite composite = new GenericPropertyComposite(genericProperties);
    composite.setCategory(genericProperties[0].getCategory());
    return composite;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final GenericProperty[] m_properties;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GenericPropertyComposite(GenericProperty[] properties) {
    super(properties[0].getObject(), properties[0].getTitle(), properties[0].getEditor());
    m_properties = properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int hashCode() {
    return m_properties.length;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    //
    if (obj instanceof GenericPropertyComposite) {
      GenericPropertyComposite property = (GenericPropertyComposite) obj;
      return ArrayUtils.isEquals(m_properties, property.m_properties);
    }
    //
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Object NO_VALUE = new Object();

  @Override
  public boolean isModified() throws Exception {
    for (GenericProperty property : m_properties) {
      if (property.isModified()) {
        return true;
      }
    }
    // no modified properties
    return false;
  }

  @Override
  public Object getValue() throws Exception {
    Object value = NO_VALUE;
    for (GenericProperty property : m_properties) {
      Object propertyValue = property.getValue();
      if (value == NO_VALUE) {
        value = propertyValue;
      } else if (!ObjectUtils.equals(value, propertyValue)) {
        return UNKNOWN_VALUE;
      }
    }
    // return common value
    return value;
  }

  @Override
  public void setValue(final Object value) throws Exception {
    ExecutionUtils.run(m_object, new RunnableEx() {
      public void run() throws Exception {
        for (GenericProperty property : m_properties) {
          property.setValue(value);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Class<?> getType() {
    Class<?> commonType = null;
    for (GenericProperty property : m_properties) {
      Class<?> type = property.getType();
      // check that type is same
      if (commonType == null) {
        commonType = type;
      } else if (type != commonType) {
        return null;
      }
    }
    // return type
    return commonType;
  }

  @Override
  public boolean hasTrueTag(String tag) {
    for (GenericProperty property : m_properties) {
      if (!property.hasTrueTag(tag)) {
        return false;
      }
    }
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expression
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getExpression() {
    String commonExpression = null;
    for (GenericProperty property : m_properties) {
      // prepare expression
      String expression = property.getExpression();
      if (expression == null) {
        return null;
      }
      // check source
      if (commonExpression == null) {
        commonExpression = expression;
      } else if (!commonExpression.equals(expression)) {
        return null;
      }
    }
    return commonExpression;
  }

  @Override
  public void setExpression(final String expression, final Object value) throws Exception {
    ExecutionUtils.run(m_object, new RunnableEx() {
      public void run() throws Exception {
        for (GenericProperty property : m_properties) {
          property.setExpression(expression, value);
        }
      }
    });
  }
}
