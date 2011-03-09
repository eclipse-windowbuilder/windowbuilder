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
package org.eclipse.wb.internal.rcp.databinding.xwt.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.DialogFieldUiContentProvider;
import org.eclipse.wb.internal.core.utils.dialogfields.ComboDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.BindingInfo;

import org.eclipse.swt.SWT;

/**
 * 
 * @author lobas_av
 * 
 */
public class ModeContentProvider extends DialogFieldUiContentProvider {
  private final ComboDialogField m_dialogField;
  private final BindingInfo m_binding;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ModeContentProvider(BindingInfo binding) {
    m_binding = binding;
    m_dialogField = new ComboDialogField(SWT.BORDER | SWT.READ_ONLY);
    m_dialogField.setLabelText("Mode:");
    m_dialogField.setItems(BindingInfo.MODES);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractUIContentProvider
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
    m_dialogField.selectItem(m_binding.getMode());
  }

  public void saveToObject() throws Exception {
    m_binding.setMode(m_dialogField.getSelectionIndex());
  }
}