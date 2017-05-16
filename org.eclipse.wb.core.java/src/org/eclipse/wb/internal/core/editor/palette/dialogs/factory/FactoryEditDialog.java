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

import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.palette.command.Command;
import org.eclipse.wb.internal.core.editor.palette.command.factory.FactoryEditCommand;
import org.eclipse.wb.internal.core.editor.palette.model.entry.FactoryEntryInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.StaticFactoryEntryInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for editing {@link StaticFactoryEntryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public final class FactoryEditDialog extends FactoryAbstractDialog {
  private final FactoryEntryInfo m_entry;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FactoryEditDialog(Shell parentShell,
      AstEditor editor,
      boolean forStatic,
      FactoryEntryInfo entry) {
    super(parentShell, editor, forStatic, forStatic
        ? Messages.FactoryEditDialog_titleStatic
        : Messages.FactoryEditDialog_titleInstance, forStatic
        ? Messages.FactoryEditDialog_messageStatic
        : Messages.FactoryEditDialog_messageInstance);
    m_entry = entry;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createControls(Composite container) {
    super.createControls(container);
    m_nameField.setText(m_entry.getName());
    m_factoryClassField.setText(m_entry.getFactoryClassName());
    m_methodSignatureField.setText(m_entry.getMethodSignature());
    m_descriptionField.setText(m_entry.getDescription());
    m_visibleField.setSelection(m_entry.isVisible());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command createCommand() {
    String name = m_nameField.getText().trim();
    String description = getDescriptionText();
    String factoryClassName = m_factoryClassField.getText();
    String methodSignature = m_methodSignatureField.getText();
    return new FactoryEditCommand(m_entry.getId(),
        name,
        description,
        m_visibleField.getSelection(),
        factoryClassName,
        methodSignature,
        m_forStatic);
  }
}
