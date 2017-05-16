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

import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.palette.command.Command;
import org.eclipse.wb.internal.core.editor.palette.command.ComponentEditCommand;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for editing {@link ComponentEntryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public final class ComponentEditDialog extends ComponentAbstractDialog {
  private final ComponentEntryInfo m_component;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentEditDialog(Shell parentShell, AstEditor editor, ComponentEntryInfo component) {
    super(parentShell,
        editor,
        Messages.ComponentEditDialog_title,
        Messages.ComponentEditDialog_message);
    m_component = component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createControls(Composite container) {
    super.createControls(container);
    m_idField.setText(m_component.getId());
    m_nameField.setText(m_component.getName());
    m_classField.setText(m_component.getClassName());
    m_descriptionField.setText(m_component.getDescription());
    m_visibleField.setSelection(m_component.isVisible());
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
    boolean hidden = m_visibleField.getSelection();
    String className = m_classField.getText();
    return new ComponentEditCommand(m_component.getId(), name, description, hidden, className);
  }
}
