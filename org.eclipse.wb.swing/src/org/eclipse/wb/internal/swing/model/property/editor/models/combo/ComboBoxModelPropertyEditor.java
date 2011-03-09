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
package org.eclipse.wb.internal.swing.model.property.editor.models.combo;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.swing.Activator;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.window.Window;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

/**
 * {@link PropertyEditor} for {@link ComboBoxModel}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class ComboBoxModelPropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new ComboBoxModelPropertyEditor();

  private ComboBoxModelPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    if (property instanceof GenericProperty) {
      GenericProperty genericProperty = (GenericProperty) property;
      ITypeBinding enumTypeBinding = extractEnumTypeBinding(genericProperty.getExpression());
      if (enumTypeBinding != null) {
        return enumTypeBinding.getName();
      }
    }
    String[] items = getItems(property);
    return "[" + StringUtils.join(items, ", ") + "]";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    if (property instanceof GenericProperty) {
      GenericProperty genericProperty = (GenericProperty) property;
      ComboBoxModelDialog dialog =
          new ComboBoxModelDialog(DesignerPlugin.getShell(),
              Activator.getDefault(),
              genericProperty.getJavaInfo().getEditor().getJavaProject(),
              property.getTitle());
      ITypeBinding enumTypeBinding = extractEnumTypeBinding(genericProperty.getExpression());
      if (enumTypeBinding != null) {
        dialog.setEnumTypeName(AstNodeUtils.getFullyQualifiedName(enumTypeBinding, true));
      } else {
        dialog.setItems(getItems(genericProperty));
      }
      // open dialog
      if (dialog.open() == Window.OK) {
        JavaInfo javaInfo = genericProperty.getJavaInfo();
        // prepare model source
        String source = null;
        if (dialog.isEnumSelected()) {
          String enumTypeName = dialog.getEnumTypeName();
          if (!StringUtils.isEmpty(enumTypeName)) {
            source = "new javax.swing.DefaultComboBoxModel(" + enumTypeName + ".values())";
          }
        } else {
          String[] items = dialog.getItems();
          if (items != null && items.length > 0) {
            source = "new javax.swing.DefaultComboBoxModel(new String[] {";
            // append items
            for (int i = 0; i < items.length; i++) {
              String item = items[i];
              if (i != 0) {
                source += ", ";
              }
              source += StringConverter.INSTANCE.toJavaSource(javaInfo, item);
            }
            // close model
            source += "})";
          }
        }
        // set source
        if (StringUtils.isEmpty(source)) {
          genericProperty.setValue(Property.UNKNOWN_VALUE);
        } else {
          genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  private static ITypeBinding extractEnumTypeBinding(Expression expression) {
    if (expression != null) {
      expression = AstNodeUtils.getActualVariableExpression(expression);
      if (AstNodeUtils.isSuccessorOf(expression, DefaultComboBoxModel.class)) {
        if (expression instanceof ClassInstanceCreation) {
          ClassInstanceCreation creation = (ClassInstanceCreation) expression;
          if ("<init>(java.lang.Object[])".equals(AstNodeUtils.getCreationSignature(creation))) {
            Expression argumentExpression =
                AstNodeUtils.getActualVariableExpression(DomGenerics.arguments(creation).get(0));
            if (argumentExpression instanceof MethodInvocation) {
              MethodInvocation argumentInvocation = (MethodInvocation) argumentExpression;
              IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(argumentInvocation);
              ITypeBinding methodDeclaringClassBinding = methodBinding.getDeclaringClass();
              if (methodDeclaringClassBinding.isEnum()
                  && "values()".equals(AstNodeUtils.getMethodSignature(argumentInvocation))) {
                return methodDeclaringClassBinding;
              }
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * @return the items specified in value of given {@link Property}.
   */
  private static String[] getItems(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof ComboBoxModel) {
      List<String> items = Lists.newArrayList();
      ComboBoxModel model = (ComboBoxModel) value;
      for (int i = 0; i < model.getSize(); i++) {
        Object element = model.getElementAt(i);
        if (element instanceof String) {
          items.add((String) element);
        }
      }
      return items.toArray(new String[items.size()]);
    }
    // no items
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }
}
