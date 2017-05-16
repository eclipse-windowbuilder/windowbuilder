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
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.palette.command.CategoryAddCommand;
import org.eclipse.wb.internal.core.editor.palette.command.Command;
import org.eclipse.wb.internal.core.utils.dialogfields.ComboDialogField;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import java.text.MessageFormat;
import java.util.List;

/**
 * Dialog for adding new {@link CategoryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public final class CategoryAddDialog extends CategoryAbstractDialog {
  private final PaletteInfo m_palette;
  private final CategoryInfo m_initialNextCategory;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CategoryAddDialog(Shell parentShell, PaletteInfo palette, CategoryInfo initialNextCategory) {
    super(parentShell, Messages.CategoryAddDialog_title, Messages.CategoryAddDialog_message);
    m_palette = palette;
    m_initialNextCategory = initialNextCategory;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private ComboDialogField m_locationField;

  @Override
  protected void createAdditionalControls(Composite container) {
    m_stateField.setSelection(0, true);
    m_stateField.setSelection(1, true);
    // id
    {
      String id = "custom_" + System.currentTimeMillis();
      m_idField.setTextWithoutUpdate(id);
    }
    // location
    {
      m_locationField = new ComboDialogField(SWT.READ_ONLY);
      doCreateField(m_locationField, Messages.CategoryAddDialog_targetCategoryLabel);
      // add categories
      boolean categorySelected = false;
      for (CategoryInfo category : m_palette.getCategories()) {
        m_locationField.addItem(MessageFormat.format(
            Messages.CategoryAddDialog_targetCategoryBefore,
            category.getName()));
        if (category == m_initialNextCategory) {
          m_locationField.selectItem(m_locationField.getItemCount() - 1);
          categorySelected = true;
        }
      }
      // add end
      m_locationField.addItem(Messages.CategoryAddDialog_targetCategoryEnd);
      if (!categorySelected) {
        m_locationField.selectItem(m_locationField.getItemCount() - 1);
      }
      // show all items
      UiUtils.setVisibleItemCount(
          m_locationField.getComboControl(null),
          m_locationField.getItemCount());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command createCommand() {
    String id = m_idField.getText();
    String name = m_nameField.getText();
    String description = m_descriptionField.getText();
    boolean hidden = m_stateField.isSelected(0);
    boolean open = m_stateField.isSelected(1);
    // prepare next category
    String nextCategoryId = null;
    {
      int index = m_locationField.getSelectionIndex();
      List<CategoryInfo> categories = m_palette.getCategories();
      if (index < categories.size()) {
        nextCategoryId = categories.get(index).getId();
      }
    }
    // create command
    return new CategoryAddCommand(id, name, description, hidden, open, nextCategoryId);
  }
}
