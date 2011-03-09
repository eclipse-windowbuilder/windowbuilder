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
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.DialogFieldUiContentProvider;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.swing.databinding.model.bindings.ColumnBindingInfo;

/**
 * {@link ColumnBindingInfo} {@code name} attribute editor.
 * 
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public class ColumnNameUiContentProvider extends DialogFieldUiContentProvider {
  private final ColumnBindingInfo m_binding;
  private final StringDialogField m_dialogField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnNameUiContentProvider(ColumnBindingInfo binding) {
    m_binding = binding;
    m_dialogField = new StringDialogField();
    m_dialogField.setLabelText("Column name:");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DialogFieldUIContentProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public DialogField getDialogField() {
    return m_dialogField;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFromObject() throws Exception {
    m_dialogField.setText(m_binding.getColumnName());
  }

  public void saveToObject() throws Exception {
    m_binding.setColumnName(m_dialogField.getText());
  }
}