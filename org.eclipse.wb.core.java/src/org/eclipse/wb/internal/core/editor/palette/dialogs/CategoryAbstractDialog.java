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
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.wb.internal.core.utils.dialogfields.StringAreaDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Abstract dialog for {@link CategoryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public abstract class CategoryAbstractDialog extends AbstractPaletteElementDialog {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CategoryAbstractDialog(Shell parentShell, String shellText, String titleText) {
    super(parentShell, shellText, titleText, null, Messages.CategoryAbstractDialog_message);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  protected StringDialogField m_idField;
  protected StringDialogField m_nameField;
  protected StringAreaDialogField m_descriptionField;
  protected SelectionButtonDialogFieldGroup m_stateField;

  @Override
  protected void createControls(Composite container) {
    m_fieldsContainer = container;
    GridLayoutFactory.create(container).columns(2);
    // id
    {
      m_idField = new StringDialogField();
      m_idField.setEditable(false);
      doCreateField(m_idField, Messages.CategoryAbstractDialog_idLabel);
    }
    // name
    {
      m_nameField = new StringDialogField();
      doCreateField(m_nameField, Messages.CategoryAbstractDialog_nameLabel);
      m_nameField.setFocus();
    }
    // description
    {
      m_descriptionField = new StringAreaDialogField(5);
      doCreateField(m_descriptionField, Messages.CategoryAbstractDialog_descriptionLabel);
      GridDataFactory.modify(m_descriptionField.getTextControl(null)).grabV();
    }
    // state
    {
      m_stateField =
          new SelectionButtonDialogFieldGroup(SWT.CHECK, new String[]{
              Messages.CategoryAbstractDialog_stateVisible,
              Messages.CategoryAbstractDialog_stateOpen}, 1, SWT.SHADOW_ETCHED_IN);
      doCreateField(m_stateField, Messages.CategoryAbstractDialog_stateLabel);
    }
    // allow to add more controls
    createAdditionalControls(container);
  }

  /**
   * Creates additional controls.
   */
  protected void createAdditionalControls(Composite container) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String validate() {
    // validate name
    {
      String name = m_nameField.getText();
      if (name.length() == 0) {
        return Messages.CategoryAbstractDialog_validateEmptyName;
      }
    }
    // OK
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Composite m_fieldsContainer;

  /**
   * Configures given {@link DialogField} for specific of this dialog.
   */
  protected final void doCreateField(DialogField dialogField, String labelText) {
    dialogField.setLabelText(labelText);
    dialogField.setDialogFieldListener(m_validateListener);
    DialogFieldUtils.fillControls(m_fieldsContainer, dialogField, 2, 60);
  }
}
