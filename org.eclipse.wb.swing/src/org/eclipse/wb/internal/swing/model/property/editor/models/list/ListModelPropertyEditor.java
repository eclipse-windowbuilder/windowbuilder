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
package org.eclipse.wb.internal.swing.model.property.editor.models.list;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.utils.ui.dialogs.StringsDialog;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.jface.window.Window;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import javax.swing.ListModel;

/**
 * {@link PropertyEditor} for {@link ListModel}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class ListModelPropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new ListModelPropertyEditor();

  private ListModelPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
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
    StringsDialog itemsDialog =
        new StringsDialog(DesignerPlugin.getShell(),
            Activator.getDefault(),
            property.getTitle(),
            ModelMessages.ListModelPropertyEditor_itemsDialogTitle,
            ModelMessages.ListModelPropertyEditor_itemsDialogMessage);
    itemsDialog.setItems(getItems(property));
    // open dialog
    if (itemsDialog.open() == Window.OK) {
      String[] items = itemsDialog.getItems();
      setItems(property, items);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the items specified in value of given {@link Property}.
   */
  public static String[] getItems(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof ListModel) {
      List<String> items = Lists.newArrayList();
      ListModel model = (ListModel) value;
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

  /**
   * Sets the items for given {@link ListModel} {@link Property}.
   */
  public static void setItems(Property property, String[] items) throws Exception {
    if (property instanceof GenericProperty) {
      GenericProperty genericProperty = (GenericProperty) property;
      JavaInfo javaInfo = genericProperty.getJavaInfo();
      // prepare source lines
      String[] lines =
          new String[]{
              "new javax.swing.AbstractListModel() {",
              "\tString[] values = new String[] {",
              "\tpublic int getSize() {",
              "\t\treturn values.length;",
              "\t}",
              "\tpublic Object getElementAt(int index) {",
              "\t\treturn values[index];",
              "\t}",
              "}",};
      // append items
      {
        String valuesLine = lines[1];
        //
        for (int i = 0; i < items.length; i++) {
          String item = items[i];
          if (i != 0) {
            valuesLine += ", ";
          }
          valuesLine += StringConverter.INSTANCE.toJavaSource(javaInfo, item);
        }
        // close items
        valuesLine += "};";
        lines[1] = valuesLine;
      }
      // set source as single String
      String source = StringUtils.join(lines, "\n");
      genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
    }
  }
}
