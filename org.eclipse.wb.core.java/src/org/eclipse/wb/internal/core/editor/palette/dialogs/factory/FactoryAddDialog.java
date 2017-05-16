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
package org.eclipse.wb.internal.core.editor.palette.dialogs.factory;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.palette.command.Command;
import org.eclipse.wb.internal.core.editor.palette.command.factory.FactoryAddCommand;
import org.eclipse.wb.internal.core.editor.palette.model.entry.FactoryEntryInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.dialogfields.ComboDialogField;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for adding new {@link FactoryEntryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public final class FactoryAddDialog extends FactoryAbstractDialog {
  private final PaletteInfo m_palette;
  private final CategoryInfo m_initialCategory;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FactoryAddDialog(Shell parentShell,
      AstEditor editor,
      boolean forStatic,
      PaletteInfo palette,
      CategoryInfo initialCategory) {
    super(parentShell, editor, forStatic, forStatic
        ? Messages.FactoryAddDialog_titleStatic
        : Messages.FactoryAddDialog_titleInstance, forStatic
        ? Messages.FactoryAddDialog_messageStatic
        : Messages.FactoryAddDialog_messageInstance);
    m_palette = palette;
    m_initialCategory = initialCategory;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private ComboDialogField m_categoryField;

  @Override
  protected void createControls(Composite container) {
    super.createControls(container);
    // category
    {
      m_categoryField = createCategoryField(m_palette, m_initialCategory);
      doCreateField(m_categoryField, Messages.FactoryAddDialog_categoryLabel);
    }
    // initialize fields
    m_visibleField.setSelection(true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command createCommand() {
    String id = "custom_" + System.currentTimeMillis();
    String name = m_nameField.getText().trim();
    String description = getDescriptionText();
    String factoryClassName = m_factoryClassField.getText();
    String methodSignature = m_methodSignatureField.getText();
    CategoryInfo category = m_palette.getCategories().get(m_categoryField.getSelectionIndex());
    return new FactoryAddCommand(id,
        name,
        description,
        m_visibleField.getSelection(),
        factoryClassName,
        methodSignature,
        m_forStatic,
        category);
  }
}
