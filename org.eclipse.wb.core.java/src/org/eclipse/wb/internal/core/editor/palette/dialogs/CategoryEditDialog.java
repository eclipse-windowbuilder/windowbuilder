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
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.palette.command.CategoryEditCommand;
import org.eclipse.wb.internal.core.editor.palette.command.Command;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for editing {@link CategoryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public final class CategoryEditDialog extends CategoryAbstractDialog {
  private final CategoryInfo m_category;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CategoryEditDialog(Shell parentShell, CategoryInfo category) {
    super(parentShell, Messages.CategoryEditDialog_title, Messages.CategoryEditDialog_message);
    m_category = category;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createControls(Composite container) {
    super.createControls(container);
    m_idField.setText(m_category.getId());
    m_nameField.setText(m_category.getName());
    m_descriptionField.setText(m_category.getDescription());
    m_stateField.setSelection(0, m_category.isVisible());
    m_stateField.setSelection(1, m_category.isOpen());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command createCommand() {
    String name = m_nameField.getText();
    String description = m_descriptionField.getText();
    boolean hidden = m_stateField.isSelected(0);
    boolean open = m_stateField.isSelected(1);
    return new CategoryEditCommand(m_category.getId(), name, description, hidden, open);
  }
}
