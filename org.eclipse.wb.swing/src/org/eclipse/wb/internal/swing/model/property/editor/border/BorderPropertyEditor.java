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
package org.eclipse.wb.internal.swing.model.property.editor.border;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.jface.window.Window;

import javax.swing.border.Border;

/**
 * {@link PropertyEditor} for {@link Border}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class BorderPropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new BorderPropertyEditor();

  private BorderPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof Border) {
      return CodeUtils.getShortClass(value.getClass().getName());
    }
    if (property.isModified() && value == null) {
      return "(no border)";
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    BorderDialog m_fontDialog =
        new BorderDialog(DesignerPlugin.getShell(), genericProperty.getJavaInfo().getEditor());
    // set "modified" flag
    m_fontDialog.setBorderModified(property.isModified());
    // set initial value
    {
      Object value = property.getValue();
      if (value instanceof Border) {
        Border border = (Border) value;
        m_fontDialog.setBorder(border);
      }
    }
    // open dialog
    if (m_fontDialog.open() == Window.OK) {
      String source = m_fontDialog.getBorderSource();
      genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
    }
  }
}
