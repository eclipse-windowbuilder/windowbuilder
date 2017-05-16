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
package org.eclipse.wb.internal.core.utils.dialogfields;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;

/**
 * Dialog field for selecting directory in file system.
 *
 * @author scheglov_ke
 */
public class StringButtonDirectoryDialogField extends StringButtonDialogField {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StringButtonDirectoryDialogField() {
    super(new Adapter());
    setButtonLabel("...");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // UI creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Control[] doFillIntoGrid(Composite parent, int columns) {
    Control[] controls = super.doFillIntoGrid(parent, columns);
    GridDataFactory.create(getChangeControl(null));
    return controls;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adapter
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class Adapter implements IStringButtonAdapter {
    ////////////////////////////////////////////////////////////////////////////
    //
    // IStringButtonAdapter
    //
    ////////////////////////////////////////////////////////////////////////////
    public void changeControlPressed(DialogField field) {
      StringButtonDirectoryDialogField directoryField = (StringButtonDirectoryDialogField) field;
      DirectoryDialog directoryDialog =
          new DirectoryDialog(directoryField.getLabelControl(null).getShell());
      directoryDialog.setFilterPath(directoryField.getText());
      String newDirectory = directoryDialog.open();
      if (newDirectory != null) {
        directoryField.setText(newDirectory);
      }
    }
  }
}
