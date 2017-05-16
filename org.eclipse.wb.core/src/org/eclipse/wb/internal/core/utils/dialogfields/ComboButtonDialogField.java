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
package org.eclipse.wb.internal.core.utils.dialogfields;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Dialog field containing a label a combo and a button control.
 *
 * @author lobas_av
 */
public class ComboButtonDialogField extends ComboDialogField {
  private final IStringButtonAdapter m_browseAdapter;
  private Button m_browseButton;
  private boolean m_browseButtonEnabled = true;
  private Combo m_combo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComboButtonDialogField(IStringButtonAdapter adapter, int flags) {
    super(flags);
    m_browseAdapter = adapter;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout helpers
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Control[] doFillIntoGrid(Composite parent, int columns) {
    assertEnoughColumns(columns);
    //
    Label label = getLabelControl(parent);
    label.setLayoutData(gridDataForLabel(1));
    //
    m_combo = getComboControl(parent);
    GridDataFactory.create(m_combo).fillH().grabH().spanH(columns - 2);
    //
    Button button = getChangeControl(parent);
    //
    return new Control[]{label, m_combo, button};
  }

  public void changeControlPressed() {
    m_browseAdapter.changeControlPressed(this);
  }

  @Override
  public int getNumberOfControls() {
    return 3;
  }

  public Button getChangeControl(Composite parent) {
    if (m_browseButton == null) {
      assertCompositeNotNull(parent);
      //
      m_browseButton = new Button(parent, SWT.PUSH);
      m_browseButton.setFont(parent.getFont());
      m_browseButton.setText("...");
      m_browseButton.setEnabled(isEnabled() && m_browseButtonEnabled);
      //
      m_browseButton.addSelectionListener(new SelectionListener() {
        public void widgetDefaultSelected(SelectionEvent e) {
          changeControlPressed();
        }

        public void widgetSelected(SelectionEvent e) {
          changeControlPressed();
        }
      });
    }
    return m_browseButton;
  }

  public void enableButton(boolean enable) {
    if (isOkToUse(m_browseButton)) {
      m_browseButton.setEnabled(isEnabled() && enable);
    }
    m_browseButtonEnabled = enable;
  }

  @Override
  protected void updateEnableState() {
    super.updateEnableState();
    if (isOkToUse(m_browseButton)) {
      m_browseButton.setEnabled(isEnabled() && m_browseButtonEnabled);
    }
  }
}