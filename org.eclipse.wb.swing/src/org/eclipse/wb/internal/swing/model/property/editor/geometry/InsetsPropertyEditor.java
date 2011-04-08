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
package org.eclipse.wb.internal.swing.model.property.editor.geometry;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.geometry.AbstractGeometryDialog;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.jface.dialogs.IDialogConstants;

import java.awt.Insets;

/**
 * Implementation of {@link PropertyEditor} for {@link Insets}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class InsetsPropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new InsetsPropertyEditor();

  private InsetsPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof Insets) {
      Insets insets = (Insets) value;
      return "("
          + insets.top
          + ", "
          + insets.left
          + ", "
          + insets.bottom
          + ", "
          + insets.right
          + ")";
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
    // prepare Insets to edit
    Insets insets;
    {
      Object value = property.getValue();
      if (value instanceof Insets) {
        Insets insetsValue = (Insets) value;
        insets =
            new Insets(insetsValue.top, insetsValue.left, insetsValue.bottom, insetsValue.right);
      } else {
        insets = new Insets(0, 0, 0, 0);
      }
    }
    // prepare dialog
    InsetsDialog insetsDialog = new InsetsDialog(property.getTitle(), insets);
    // open dialog
    int result = insetsDialog.open();
    if (result == IDialogConstants.IGNORE_ID) {
      property.setValue(Property.UNKNOWN_VALUE);
    } else if (result == IDialogConstants.OK_ID) {
      property.setValue(insets);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // InsetsDialog
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class InsetsDialog extends AbstractGeometryDialog {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public InsetsDialog(String title, Insets insets) {
      super(title, insets);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void createEditors() {
      createEditor(ModelMessages.InsetsPropertyEditor_top, "top");
      createEditor(ModelMessages.InsetsPropertyEditor_left, "left");
      createEditor(ModelMessages.InsetsPropertyEditor_bottom, "bottom");
      createEditor(ModelMessages.InsetsPropertyEditor_right, "right");
    }
  }
}
