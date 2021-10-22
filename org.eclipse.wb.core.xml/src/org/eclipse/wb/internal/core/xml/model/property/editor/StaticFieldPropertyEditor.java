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
package org.eclipse.wb.internal.core.xml.model.property.editor;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.controls.CCombo3;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractComboPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.xml.IExceptionConstants;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.core.xml.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.xml.model.property.IExpressionPropertyEditor;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

/**
 * {@link PropertyEditor} for selecting single field of class from given set.
 *
 * @author scheglov_ke
 * @coverage XML.model.property.editor
 */
public final class StaticFieldPropertyEditor extends AbstractComboPropertyEditor
    implements
      IConfigurablePropertyObject,
      IExpressionPropertyEditor,
      IClipboardSourceProvider {
  private Class<?> m_class;
  private String[] m_names;
  private String[] m_titles;
  private Object[] m_values;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getText(Property property) throws Exception {
    Object value = property.getValue();
    // return title for value
    if (value != Property.UNKNOWN_VALUE) {
      for (int i = 0; i < m_values.length; i++) {
        Object fieldValue = m_values[i];
        if (ObjectUtils.equals(fieldValue, value)) {
          return m_titles[i];
        }
      }
    }
    // unknown value
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getValueExpression(GenericProperty property, Object value) throws Exception {
    if (value != Property.UNKNOWN_VALUE) {
      for (int i = 0; i < m_values.length; i++) {
        if (ObjectUtils.equals(m_values[i], value)) {
          return getExpression(property, i);
        }
      }
    }
    return null;
  }

  /**
   * @return the attribute {@link String} to use for field with given index.
   */
  private String getExpression(GenericProperty property, int index) throws Exception {
    String fieldName = m_names[index];
    // support for *remove
    if (fieldName == null) {
      return null;
    }
    // get expression from toolkit
    String[] expression = {null};
    property.getObject().getBroadcast(StaticFieldPropertyEditorGetExpression.class).invoke(
        m_class,
        fieldName,
        expression);
    Assert.isNotNull2(expression[0], "Can not resolve {0} {1}", m_class, fieldName);
    return expression[0];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardSourceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getClipboardSource(GenericProperty property) throws Exception {
    Object value = property.getValue();
    return getValueExpression(property, value);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Combo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addItems(Property property, CCombo3 combo) throws Exception {
    for (String title : m_titles) {
      combo.add(title);
    }
  }

  @Override
  protected void selectItem(Property property, CCombo3 combo) throws Exception {
    combo.setText(getText(property));
  }

  @Override
  protected void toPropertyEx(Property property, CCombo3 combo, int index) throws Exception {
    if (property instanceof GenericProperty) {
      GenericProperty genericProperty = (GenericProperty) property;
      String expression = getExpression(genericProperty, index);
      Object value = m_values[index];
      genericProperty.setExpression(expression, value);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyObject
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures this {@link StaticFieldPropertyEditor} with class and fields. This is same as
   * {@link #configure(EditorContext, Map)}, but for access from code, not from component
   * description.
   */
  public void configure(Class<?> clazz, String[] fieldDescriptions) throws Exception {
    m_class = clazz;
    initialize(fieldDescriptions);
  }

  public void configure(EditorContext context, Map<String, Object> parameters) throws Exception {
    // prepare class
    {
      String classBinaryName = (String) parameters.get("class");
      if (classBinaryName == null) {
        throw new DesignerException(IExceptionConstants.DESCRIPTION_EDITOR_STATIC_FIELD,
            "No class.");
      }
      m_class = context.getClassLoader().loadClass(classBinaryName);
    }
    // prepare fields
    String[] fieldDescriptions;
    if (parameters.containsKey("fields")) {
      Object fieldsObject = parameters.get("fields");
      if (fieldsObject instanceof List<?>) {
        @SuppressWarnings("unchecked")
        List<String> fieldDescriptionList = (List<String>) fieldsObject;
        fieldDescriptions = fieldDescriptionList.toArray(new String[fieldDescriptionList.size()]);
      } else {
        fieldDescriptions = StringUtils.split((String) fieldsObject);
      }
    } else {
      throw new DesignerException(IExceptionConstants.DESCRIPTION_EDITOR_STATIC_FIELD, "No fields.");
    }
    // set fields
    initialize(fieldDescriptions);
  }

  /**
   * Fills arrays using given field descriptions.
   */
  private void initialize(String[] fieldDescriptions) throws Exception {
    fieldDescriptions = cleanUpFieldDescriptions(m_class, fieldDescriptions);
    int count = fieldDescriptions.length;
    // prepare arrays
    m_names = new String[count];
    m_titles = new String[count];
    m_values = new Object[count];
    // fill arrays
    for (int i = 0; i < count; i++) {
      String fieldDescription = fieldDescriptions[i];
      // special cases
      if ("*remove".equals(fieldDescription)) {
        m_titles[i] = "";
        continue;
      }
      // fill name/title
      m_names[i] = getFieldName(fieldDescription);
      m_titles[i] = getFieldTitle(fieldDescription);
      // fill value
      {
        Field field = m_class.getField(m_names[i]);
        Assert.isTrue(Modifier.isStatic(field.getModifiers()), "Field %s is not static.", field);
        m_values[i] = field.get(null);
      }
    }
  }

  /**
   * Check field's, if field does not exist, then remove it from array.
   */
  private static String[] cleanUpFieldDescriptions(Class<?> m_class, String[] fieldDescriptions)
      throws Exception {
    List<String> newFieldDescriptions = Lists.newArrayList();
    // check all fields
    for (String fieldDescription : fieldDescriptions) {
      // skip special cases
      if ("*remove".equals(fieldDescription)) {
        newFieldDescriptions.add(fieldDescription);
        continue;
      }
      // prepare field name
      String name = getFieldName(fieldDescription);
      // check exist field
      try {
        m_class.getField(name);
      } catch (NoSuchFieldException e) {
        continue;
      }
      // OK, valid field
      newFieldDescriptions.add(fieldDescription);
    }
    // return as array
    return newFieldDescriptions.toArray(new String[newFieldDescriptions.size()]);
  }

  /**
   * @return the name of field from given field description.
   */
  private static String getFieldName(String fieldDescription) {
    // simple case - just name of field
    if (!fieldDescription.contains(":")) {
      return fieldDescription;
    }
    // name:title
    return getTwoParts(fieldDescription)[0];
  }

  /**
   * @return the title of field from given field description.
   */
  private static String getFieldTitle(String fieldDescription) {
    // simple case - use name of field
    if (!fieldDescription.contains(":")) {
      return fieldDescription;
    }
    // name:title
    return getTwoParts(fieldDescription)[1];
  }

  /**
   * Splits field description into exactly two parts - "name:title".
   */
  private static String[] getTwoParts(String fieldDescription) {
    String[] parts = StringUtils.split(fieldDescription, ":");
    String message =
        "Exactly one ':' expected in description name:title, but found " + fieldDescription;
    Assert.equals(2, parts.length, message);
    return parts;
  }
}
