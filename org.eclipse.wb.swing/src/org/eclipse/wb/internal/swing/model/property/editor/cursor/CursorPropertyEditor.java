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
package org.eclipse.wb.internal.swing.model.property.editor.cursor;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.controls.CCombo3;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractComboPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

import java.awt.Cursor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * {@link PropertyEditor} for {@link Cursor}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class CursorPropertyEditor extends AbstractComboPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new CursorPropertyEditor();

  private CursorPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof Cursor) {
      Cursor cursor = (Cursor) value;
      // return the name of cursor field
      for (Field cursorField : getCursorFields()) {
        if (cursor.getType() == cursorField.getInt(null)) {
          return cursorField.getName();
        }
      }
    }
    // unknown value
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractComboPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addItems(Property property, CCombo3 combo) throws Exception {
    for (Field cursorField : getCursorFields()) {
      combo.add(cursorField.getName());
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
      // prepare source
      String source;
      {
        Field cursorField = getCursorFields()[index];
        String typeSource = "java.awt.Cursor." + cursorField.getName();
        source = "java.awt.Cursor.getPredefinedCursor(" + typeSource + ")";
      }
      // set source
      genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Field[] m_cursorFields;

  /**
   * @return the {@link Field}'s with cursor constants from {@link Cursor}.
   */
  private static Field[] getCursorFields() throws Exception {
    if (m_cursorFields == null) {
      List<Field> cursorFields = Lists.newArrayList();
      //
      Field[] declaredFields = Cursor.class.getDeclaredFields();
      for (Field field : declaredFields) {
        int modifiers = field.getModifiers();
        if (Modifier.isPublic(modifiers)
            && Modifier.isStatic(modifiers)
            && field.getType() == int.class
            && field.getName().endsWith("_CURSOR")) {
          int cursorType = field.getInt(null);
          if (cursorType != Cursor.CUSTOM_CURSOR) {
            cursorFields.add(field);
          }
        }
      }
      //
      m_cursorFields = cursorFields.toArray(new Field[cursorFields.size()]);
    }
    //
    return m_cursorFields;
  }
}
