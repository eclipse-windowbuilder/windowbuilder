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
package org.eclipse.wb.internal.swt.model.property.editor.geometry;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.geometry.AbstractGeometryDialog;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.support.RectangleSupport;

import org.eclipse.jface.dialogs.IDialogConstants;

/**
 * Implementation of {@link PropertyEditor} for {@link org.eclipse.swt.graphics.Rectangle}.
 * 
 * @author lobas_av
 * @coverage swing.property.editor
 */
public final class RectanglePropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new RectanglePropertyEditor();

  private RectanglePropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (value == Property.UNKNOWN_VALUE) {
      // unknown value
      return null;
    }
    return RectangleSupport.toString(value);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    // prepare Rectangle to edit
    Object rectangle;
    {
      Object value = property.getValue();
      if (value == Property.UNKNOWN_VALUE) {
        rectangle = RectangleSupport.newRectangle(0, 0, 0, 0);
      } else {
        rectangle = value;
      }
    }
    // prepare dialog
    RectangleDialog rectangleDialog = new RectangleDialog(property.getTitle(), rectangle);
    // open dialog
    int result = rectangleDialog.open();
    if (result == IDialogConstants.IGNORE_ID) {
      property.setValue(Property.UNKNOWN_VALUE);
    } else if (result == IDialogConstants.OK_ID) {
      property.setValue(rectangle);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // RectangleDialog
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class RectangleDialog extends AbstractGeometryDialog {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public RectangleDialog(String title, Object rectangle) {
      super(title, rectangle);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void createEditors() {
      createEditor(ModelMessages.RectanglePropertyEditor_xLabel, "x");
      createEditor(ModelMessages.RectanglePropertyEditor_yLabel, "y");
      createEditor(ModelMessages.RectanglePropertyEditor_widthLabel, "width");
      createEditor(ModelMessages.RectanglePropertyEditor_heightLabel, "height");
    }
  }
}