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
package org.eclipse.wb.internal.rcp.model.rcp;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;

import org.eclipse.jface.window.Window;

/**
 * {@link PropertyEditor} for "category" attribute of "view".
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class ViewCategoryPropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new ViewCategoryPropertyEditor();

  private ViewCategoryPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof String) {
      return (String) value;
    }
    // unknown value
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    ExtensionElementProperty<?> extensionProperty = (ExtensionElementProperty<?>) property;
    // prepare dialog
    CategoriesAndViewsDialog dialog =
        new CategoriesAndViewsDialog(DesignerPlugin.getShell(), extensionProperty.getUtils());
    // open dialog
    if (dialog.open() == Window.OK) {
      // TODO
      //property.setValue(localeDialog.getSelectedLocale().getLocale());
    }
  }
}