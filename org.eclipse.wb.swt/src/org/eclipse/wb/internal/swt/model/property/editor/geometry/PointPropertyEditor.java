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
import org.eclipse.wb.internal.swt.support.PointSupport;

import org.eclipse.jface.dialogs.IDialogConstants;

/**
 * Implementation of {@link PropertyEditor} for {@link org.eclipse.swt.graphics.Point}.
 * 
 * @author lobas_av
 * @coverage swt.property.editor
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
    if (value == Property.UNKNOWN_VALUE) {
      // unknown value
      return null;
    }
    //
    return PointSupport.toString(value);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    // prepare Point to edit
    Object point;
    {
      Object value = property.getValue();
      if (value == Property.UNKNOWN_VALUE) {
        point = PointSupport.newPoint(0, 0);
      } else {
        point = PointSupport.getPointCopy(value);
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
    public PointDialog(String title, Object point) {
      super(title, point);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void createEditors() {
      createEditor("&X:", "x");
      createEditor("&Y:", "y");
    }
  }
}