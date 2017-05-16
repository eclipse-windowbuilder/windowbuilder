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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Dialog field containing a single check box control.
 *
 * @author lobas_av
 */
public class CheckDialogField extends DialogField {
  private boolean m_selection;
  private Button m_buttonControl;
  private SelectionListener m_selectionListener;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout helpers
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Control[] doFillIntoGrid(Composite parent, int columns) {
    assertEnoughColumns(columns);
    // create controls and add layout data
    Button button = getButtonControl(parent);
    GridDataFactory.create(button).alignHF().spanH(columns);
    //
    return new Control[]{button};
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // UI creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates or returns the created check box control.
   *
   * @param parent
   *          The parent composite or <code>null</code> when the widget has already been created.
   */
  public Button getButtonControl(Composite parent) {
    if (m_buttonControl == null) {
      assertCompositeNotNull(parent);
      m_selectionListener = new SelectionListener() {
        public void widgetSelected(SelectionEvent e) {
          doModifySelection(e);
        }

        public void widgetDefaultSelected(SelectionEvent e) {
          doModifySelection(e);
        }
      };
      //
      m_buttonControl = new Button(parent, SWT.CHECK);
      // initialize
      if (fLabelText != null) {
        m_buttonControl.setText(fLabelText);
      }
      m_buttonControl.setSelection(m_selection);
      m_buttonControl.addSelectionListener(m_selectionListener);
      m_buttonControl.setFont(parent.getFont());
      m_buttonControl.setEnabled(isEnabled());
    }
    return m_buttonControl;
  }

  private void doModifySelection(SelectionEvent e) {
    if (isOkToUse(m_buttonControl)) {
      m_selection = m_buttonControl.getSelection();
    }
    dialogFieldChanged();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Enable / disable management
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void updateEnableState() {
    super.updateEnableState();
    if (isOkToUse(m_buttonControl)) {
      m_buttonControl.setEnabled(isEnabled());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Gets the selection.
   */
  public boolean getSelection() {
    return m_selection;
  }

  /**
   * Sets the selection. Triggers a dialog-changed event.
   */
  public void setSelection(boolean selection) {
    m_selection = selection;
    if (isOkToUse(m_buttonControl)) {
      m_buttonControl.setSelection(m_selection);
    } else {
      dialogFieldChanged();
    }
  }

  /**
   * Sets the selection without triggering a dialog-changed event.
   */
  public void setSelectionWithoutUpdate(boolean selection) {
    m_selection = selection;
    if (isOkToUse(m_buttonControl)) {
      m_buttonControl.removeSelectionListener(m_selectionListener);
      m_buttonControl.setSelection(selection);
      m_buttonControl.addSelectionListener(m_selectionListener);
    }
  }

  @Override
  public void refresh() {
    super.refresh();
    if (isOkToUse(m_buttonControl)) {
      setSelectionWithoutUpdate(m_selection);
    }
  }
}