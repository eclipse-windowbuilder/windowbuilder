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
package org.eclipse.wb.internal.core.nls.ui;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.nls.Messages;
import org.eclipse.wb.internal.core.utils.dialogfields.AbstractValidationTitleAreaDialog;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for adding new key with value.
 *
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public final class AddKeyValueDialog extends AbstractValidationTitleAreaDialog {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AddKeyValueDialog(Shell parentShell) {
    super(parentShell,
        DesignerPlugin.getDefault(),
        Messages.AddKeyValueDialog_title,
        Messages.AddKeyValueDialog_message,
        null,
        null);
    setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private StringDialogField m_keyField;
  private StringDialogField m_valueField;

  @Override
  protected void createControls(Composite container) {
    m_fieldsContainer = container;
    GridLayoutFactory.create(container).columns(2);
    // key
    {
      m_keyField = new StringDialogField();
      doCreateField(m_keyField, Messages.AddKeyValueDialog_keyLabel);
    }
    // value
    {
      m_valueField = new StringDialogField();
      doCreateField(m_valueField, Messages.AddKeyValueDialog_valueLabel);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final String validate() {
    // validate key
    {
      String key = m_keyField.getText().trim();
      if (key.length() == 0) {
        return Messages.AddKeyValueDialog_validateEmptyKey;
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getKey() {
    return m_keyField.getText();
  }

  public String getValue() {
    return m_valueField.getText();
  }
}
