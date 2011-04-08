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

import java.awt.Dimension;

/**
 * Implementation of {@link PropertyEditor} for {@link Dimension}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class DimensionPropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new DimensionPropertyEditor();

  private DimensionPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof Dimension) {
      Dimension dimension = (Dimension) value;
      return "(" + dimension.width + ", " + dimension.height + ")";
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
    // prepare Dimension to edit
    Dimension dimension;
    {
      Object value = property.getValue();
      if (value instanceof Dimension) {
        dimension = new Dimension((Dimension) value);
      } else {
        dimension = new Dimension();
      }
    }
    // prepare dialog
    DimensionDialog dimensionDialog = new DimensionDialog(property.getTitle(), dimension);
    // open dialog
    int result = dimensionDialog.open();
    if (result == IDialogConstants.IGNORE_ID) {
      property.setValue(Property.UNKNOWN_VALUE);
    } else if (result == IDialogConstants.OK_ID) {
      property.setValue(dimension);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DimensionDialog
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class DimensionDialog extends AbstractGeometryDialog {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public DimensionDialog(String title, Dimension dimension) {
      super(title, dimension);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void createEditors() {
      createEditor(ModelMessages.DimensionPropertyEditor_width, "width");
      createEditor(ModelMessages.DimensionPropertyEditor_height, "height");
    }
  }
}
