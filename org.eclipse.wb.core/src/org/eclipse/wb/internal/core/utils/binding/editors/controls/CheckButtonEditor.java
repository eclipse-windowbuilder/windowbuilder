/*******************************************************************************
 * Copyright (c) 2011, 2020 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.binding.editors.controls;

import org.eclipse.wb.internal.core.utils.binding.IDataEditor;
import org.eclipse.wb.internal.core.utils.binding.ValueUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link IDataEditor} for SWT {@link Button} with {@link SWT#CHECK} style.
 *
 * @author scheglov_ke
 */
public final class CheckButtonEditor implements IDataEditor {
  private final Button m_button;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CheckButtonEditor(Button button) {
    m_button = button;
    // install listener for updating enablement state for dependent controls
    m_button.addListener(SWT.Selection, event -> changeEnablement(m_button.getSelection()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDataEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setValue(Object value) {
    boolean selection = ValueUtils.objectToBoolean(value);
    m_button.setSelection(selection);
    changeEnablement(selection);
  }

  @Override
  public Object getValue() {
    return ValueUtils.booleanToObject(m_button.getSelection());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Enablement
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<Control> m_enableControls = new ArrayList<>();

  /**
   * Add {@link Control} that should be enabled/disabled when button selected/de-selected.
   */
  public void addEnableControl(Control control) {
    m_enableControls.add(control);
    UiUtils.changeControlEnable(control, m_button.getSelection());
  }

  /**
   * Updates enablement.
   */
  private void changeEnablement(boolean enable) {
    for (Control control : m_enableControls) {
      UiUtils.changeControlEnable(control, enable);
    }
  }
}
