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

import org.eclipse.wb.core.controls.CCombo3;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * The base {@link PropertyEditor} for selecting single value of type {@link Enum}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage core.model.property.editor
 */
public abstract class AbstractEnumPropertyEditor extends AbstractComboPropertyEditor
    implements
      IValueSourcePropertyEditor,
      ITextValuePropertyEditor,
      IClipboardSourceProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // TextDisplayPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getText(Property property) throws Exception {
    Object value = property.getValue();
    // return title for value
    if (value instanceof Enum<?>) {
      Enum<?> element = (Enum<?>) value;
      return element.toString();
    }
    // unknown value
    return null;
  }

  public void setText(Property property, String text) throws Exception {
    for (Enum<?> element : getElements(property)) {
      if (element.toString().equals(text)) {
        setPropertyValue(property, element);
        break;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Combo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addItems(Property property, CCombo3 combo) throws Exception {
    Enum<?>[] elements = getElements(property);
    for (Enum<?> element : elements) {
      combo.add(element.toString());
    }
  }

  @Override
  protected void selectItem(Property property, CCombo3 combo) throws Exception {
    combo.setText(getText(property));
  }

  @Override
  protected void toPropertyEx(Property property, CCombo3 combo, int index) throws Exception {
    Enum<?>[] elements = getElements(property);
    Enum<?> element = elements[index];
    setPropertyValue(property, element);
  }

  /**
   * @return array of available values.
   */
  abstract protected Enum<?>[] getElements(Property property) throws Exception;

  /**
   * Apply new selected value to {@link Property}.
   */
  protected void setPropertyValue(Property property, Enum<?> element) throws Exception {
    if (property instanceof GenericProperty) {
      GenericProperty genericProperty = (GenericProperty) property;
      String source = getValueSource(element);
      genericProperty.setExpression(source, element);
    } else {
      property.setValue(element);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IValueSourcePropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getValueSource(Object value) throws Exception {
    if (value instanceof Enum<?>) {
      Enum<?> element = (Enum<?>) value;
      return ReflectionUtils.getFullyQualifiedName(element.getDeclaringClass(), false)
          + "."
          + element.name();
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
}