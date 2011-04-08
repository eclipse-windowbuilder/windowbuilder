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

import java.awt.Point;

/**
 * Implementation of {@link PropertyEditor} for {@link Point}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class PointPropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new PointPropertyEditor();

  private PointPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof Point) {
      Point point = (Point) value;
      return "(" + point.x + ", " + point.y + ")";
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
    // prepare Point to edit
    Point point;
    {
      Object value = property.getValue();
      if (value instanceof Point) {
        point = new Point((Point) value);
      } else {
        point = new Point();
      }
    }
    // prepare dialog
    PointDialog pointDialog = new PointDialog(property.getTitle(), point);
    // open dialog
    int result = pointDialog.open();
    if (result == IDialogConstants.IGNORE_ID) {
      property.setValue(Property.UNKNOWN_VALUE);
    } else if (result == IDialogConstants.OK_ID) {
      property.setValue(point);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PointDialog
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class PointDialog extends AbstractGeometryDialog {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public PointDialog(String title, Point point) {
      super(title, point);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void createEditors() {
      createEditor(ModelMessages.PointPropertyEditor_x, "x");
      createEditor(ModelMessages.PointPropertyEditor_y, "y");
    }
  }
}
