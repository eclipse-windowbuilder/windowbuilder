/*******************************************************************************
 * Copyright (c) 2011, 2022 Google, Inc. and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    Daten- und Systemtechnik Aachen - Addition of LayoutDialogFieldGroup
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.dialogfields;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Utilities for dialog fields.
 *
 * @author scheglov_ke
 */
public class DialogFieldUtils {
  /**
   * Utility method for creating field control and tweaking layout properties.
   *
   * @return the created {@link Control}s.
   */
  public static Control[] fillControls(Composite parent,
      DialogField field,
      int nColumns,
      int charsWidth) {
    // create field's controls
    Control[] controls = field.doFillIntoGrid(parent, nColumns);
    // prepare main control
    Control control;
    if (field instanceof StringDialogField) {
      control = ((StringDialogField) field).getTextControl(null);
    } else if (field instanceof StringAreaDialogField) {
      control = ((StringAreaDialogField) field).getTextControl(null);
    } else if (field instanceof ComboDialogField) {
      control = ((ComboDialogField) field).getComboControl(null);
    } else if (field instanceof BooleanDialogField) {
      control = ((BooleanDialogField) field).getButtonControl(parent);
    } else if (field instanceof SelectionButtonDialogFieldGroup) {
      control = ((SelectionButtonDialogFieldGroup) field).getSelectionButtonsGroup(parent);
    } else if (field instanceof CheckedListDialogField) {
      control = ((CheckedListDialogField) field).getListControl(parent);
    } else if (field instanceof FontDialogField) {
      control = ((FontDialogField) field).getGroupControl(parent);
    } else if (field instanceof LayoutDialogFieldGroup) {
      control = ((LayoutDialogFieldGroup) field).getSelectionButtonsGroup(parent);
    } else {
      throw new IllegalArgumentException("Not supported dialog field: " + field);
    }
    // configure layout data
    GridDataFactory.modify(control).grabH().hintHC(charsWidth).fillH();
    return controls;
  }
}
