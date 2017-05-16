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
package org.eclipse.wb.internal.core.editor.palette.dialogs;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.dialogfields.AbstractValidationTitleAreaDialog;
import org.eclipse.wb.internal.core.utils.dialogfields.ComboDialogField;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Abstract {@link TitleAreaDialog} for palette.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public abstract class AbstractPaletteDialog extends AbstractValidationTitleAreaDialog {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractPaletteDialog(Shell parentShell,
      String shellText,
      String titleText,
      Image titleImage,
      String titleMessage) {
    super(parentShell, DesignerPlugin.getDefault(), shellText, titleText, titleImage, titleMessage);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ComboDialogField} for selecting {@link CategoryInfo}.
   */
  protected static ComboDialogField createCategoryField(PaletteInfo palette,
      CategoryInfo initialCategory) {
    final ComboDialogField categoryField = new ComboDialogField(SWT.READ_ONLY);
    // add categories
    boolean categorySelected = false;
    for (CategoryInfo category : palette.getCategories()) {
      categoryField.addItem(category.getName());
      if (category == initialCategory) {
        categoryField.selectItem(categoryField.getItemCount() - 1);
        categorySelected = true;
      }
    }
    // select default category
    if (!categorySelected) {
      categoryField.selectItem(categoryField.getItemCount() - 1);
    }
    // show all items
    Display.getCurrent().asyncExec(new Runnable() {
      public void run() {
        UiUtils.setVisibleItemCount(
            categoryField.getComboControl(null),
            categoryField.getItemCount());
      }
    });
    //
    return categoryField;
  }
}
