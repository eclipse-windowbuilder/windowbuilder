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
package org.eclipse.wb.internal.rcp.model.property;

import org.eclipse.wb.core.controls.CCombo3;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractComboPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.swt.support.SwtSupport;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link PropertyEditor} for {@link Cursor}.
 *
 * @author scheglov_ke
 * @coverage rcp.property.editor
 */
public final class CursorPropertyEditor extends AbstractComboPropertyEditor
    implements
      IClipboardSourceProvider {
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
    if (property instanceof GenericProperty) {
      Expression expression = ((GenericProperty) property).getExpression();
      // new Cursor(device,style)
      if (AstNodeUtils.isCreation(
          expression,
          "org.eclipse.swt.graphics.Cursor",
          "<init>(org.eclipse.swt.graphics.Device,int)")) {
        Expression styleExpression = DomGenerics.arguments(expression).get(1);
        return getTextForStyle(styleExpression);
      }
      // SWTResourceManager.getCursor(style)
      if (AstNodeUtils.isMethodInvocation(
          expression,
          "org.eclipse.wb.swt.SWTResourceManager",
          "getCursor(int)")) {
        Expression styleExpression = DomGenerics.arguments(expression).get(0);
        return getTextForStyle(styleExpression);
      }
    }
    // unknown value
    return null;
  }

  private String getTextForStyle(Expression styleExpression) {
    if (styleExpression instanceof QualifiedName) {
      QualifiedName qualifiedName = (QualifiedName) styleExpression;
      if (AstNodeUtils.isSuccessorOf(qualifiedName.getQualifier(), "org.eclipse.swt.SWT")) {
        return qualifiedName.getName().getIdentifier();
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardSourceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getClipboardSource(GenericProperty property) throws Exception {
    String text = getText(property);
    if (text != null) {
      return "org.eclipse.wb.swt.SWTResourceManager.getCursor(org.eclipse.swt.SWT." + text + ")";
    }
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
      ManagerUtils.ensure_SWTResourceManager(genericProperty.getJavaInfo());
      // prepare source
      String source;
      {
        Field cursorField = getCursorFields().get(index);
        source = "org.eclipse.wb.swt.SWTResourceManager.getCursor(org.eclipse.swt.SWT."
            + cursorField.getName()
            + ")";
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
  /**
   * @return the {@link Field}'s with cursor constants from {@link SWT}.
   */
  private static List<Field> getCursorFields() throws Exception {
    List<Field> cursorFields = new ArrayList<>();
    Class<?> class_SWT = SwtSupport.getSwtClass();
    Field[] fields = class_SWT.getFields();
    for (Field field : fields) {
      if (field.getName().startsWith("CURSOR_")) {
        cursorFields.add(field);
      }
    }
    return cursorFields;
  }
}
