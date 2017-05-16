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
package org.eclipse.wb.internal.core.model.property.editor;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.controls.CCombo3;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * The {@link PropertyEditor} for selecting single field of class from given set.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public class StaticFieldPropertyEditor extends AbstractComboPropertyEditor
    implements
      IConfigurablePropertyObject,
      IValueSourcePropertyEditor,
      IClipboardSourceProvider {
  private Class<?> m_class;
  private String m_classSourceName;
  private String[] m_names;
  private String[] m_titles;
  private Object[] m_values;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Accepts value as {@link Object} and sets corresponding value as static field.
   */
  public void setValue(Property property, Object value) throws Exception {
    if (property instanceof GenericProperty) {
      GenericProperty genericProperty = (GenericProperty) property;
      String source = getValueSource(value);
      genericProperty.setExpression(source, value);
    } else {
      property.setValue(value);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TextDisplayPropertyEditor
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
  // IValueSourcePropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getValueSource(Object value) throws Exception {
    if (value != Property.UNKNOWN_VALUE) {
      for (int i = 0; i < m_values.length; i++) {
        Object fieldValue = m_values[i];
        if (ObjectUtils.equals(fieldValue, value)) {
          String fieldName = m_names[i];
          if (fieldName == null) {
            return null;
          } else {
            return m_classSourceName + "." + fieldName;
          }
        }
      }
    }
    // unknown value
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardSourceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getClipboardSource(GenericProperty property) throws Exception {
    Object value = property.getValue();
    return getValueSource(value);
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
    Object value = m_values[index];
    if (property instanceof GenericProperty) {
      GenericProperty genericProperty = (GenericProperty) property;
      String source = getValueSource(value);
      genericProperty.setExpression(source, value);
    } else {
      property.setValue(value);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyObject
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures this {@link StaticFieldPropertyEditor} with class and fields. This is same as
   * {@link #configure(EditorState, Map)}, but for access from code, not from component description.
   */
  public void configure(Class<?> clazz, String[] fieldDescriptions) throws Exception {
    // prepare class
    m_class = clazz;
    m_classSourceName = m_class.getName().replace('$', '.');
    // prepare fields
    initialize(new Empty_WarningConsumer(), fieldDescriptions);
  }

  public void configure(EditorState state, Map<String, Object> parameters) throws Exception {
    // prepare class
    {
      String classBinaryName = (String) parameters.get("class");
      m_class = state.getEditorLoader().loadClass(classBinaryName);
      m_classSourceName = classBinaryName.replace('$', '.');
    }
    // prepare fields
    String[] fieldDescriptions;
    if (parameters.containsKey("field")) {
      @SuppressWarnings("unchecked")
      List<String> fieldDescriptionList = (List<String>) parameters.get("field");
      fieldDescriptions = fieldDescriptionList.toArray(new String[fieldDescriptionList.size()]);
    } else if (parameters.containsKey("fields")) {
      fieldDescriptions = StringUtils.split((String) parameters.get("fields"));
    } else {
      throw new DesignerException(ICoreExceptionConstants.DESCRIPTION_EDITOR_STATIC_FIELD,
          "No fields: " + m_classSourceName);
    }
    // set fields
    initialize(new EditorState_WarningConsumer(state), fieldDescriptions);
  }

  /**
   * Initializes {@link StaticFieldPropertyEditor} using given array of fields.
   */
  private void initialize(IWarningConsumer logger, String[] fieldDescriptions) throws Exception {
    fieldDescriptions = cleanUpFieldDescriptions(logger, m_class, fieldDescriptions);
    int count = fieldDescriptions.length;
    // prepare names/titles/values
    m_names = new String[count];
    m_titles = new String[count];
    m_values = new Object[count];
    // fill names/titles/values
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
  private static String[] cleanUpFieldDescriptions(IWarningConsumer logger,
      Class<?> m_class,
      String[] fieldDescriptions) throws Exception {
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
        String message =
            MessageFormat.format("Can not find field {0}.{1}.", m_class.getName(), name);
        logger.addWarning(message, e);
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
    String[] parts = StringUtils.split(fieldDescription, ":");
    String message =
        "Exactly one ':' expected in description name:title, but found " + fieldDescription;
    Assert.equals(2, parts.length, message);
    return parts[0];
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
    String[] parts = StringUtils.split(fieldDescription, ":");
    String message =
        "Exactly one ':' expected in description name:title, but found " + fieldDescription;
    Assert.equals(2, parts.length, message);
    return parts[1];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Warnings
  //
  ////////////////////////////////////////////////////////////////////////////
  private interface IWarningConsumer {
    void addWarning(String message, Throwable e);
  }
  /**
   * Implementation of {@link IWarningConsumer} that ignores warnings.
   */
  private static final class Empty_WarningConsumer implements IWarningConsumer {
    public void addWarning(String message, Throwable e) {
    }
  }
  /**
   * Implementation of {@link IWarningConsumer} to log warnings into {@link EditorState}.
   */
  private static final class EditorState_WarningConsumer implements IWarningConsumer {
    private final EditorState m_state;

    private EditorState_WarningConsumer(EditorState state) {
      m_state = state;
    }

    public void addWarning(String message, Throwable e) {
      m_state.addWarning(new EditorWarning(message, e));
    }
  }
}