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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

/**
 * {@link DialogField} containing a {@link Label} and a {@link Spinner}.
 *
 * @author scheglov_ke
 */
public class SpinnerDialogField extends DialogField {
  private int m_selection;
  private Spinner m_spinner;
  private ModifyListener m_modifyListener;
  private int m_minimum;
  private int m_maximum = 100;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SpinnerDialogField() {
    super();
    m_selection = 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout helpers
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Control[] doFillIntoGrid(Composite parent, int nColumns) {
    assertEnoughColumns(nColumns);
    // label
    Label label;
    {
      label = getLabelControl(parent);
      label.setLayoutData(gridDataForLabel(1));
    }
    // spinner
    Control spinner;
    {
      spinner = getSpinnerControl(parent);
      spinner.setLayoutData(gridDataForSpinner(nColumns - 1));
    }
    // controls
    return new Control[]{label, spinner};
  }

  @Override
  public int getNumberOfControls() {
    return 2;
  }

  protected static GridData gridDataForSpinner(int span) {
    GridData gd = new GridData();
    gd.horizontalAlignment = GridData.FILL;
    gd.grabExcessHorizontalSpace = false;
    gd.horizontalSpan = span;
    return gd;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Focus methods
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean setFocus() {
    if (isOkToUse(m_spinner)) {
      m_spinner.setFocus();
    }
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // UI creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates or returns the created {@link Spinner} control.
   *
   * @param parent
   *          The parent composite or <code>null</code> when the widget has already been created.
   */
  public Control getSpinnerControl(Composite parent) {
    if (m_spinner == null) {
      assertCompositeNotNull(parent);
      m_modifyListener = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          doModifyText(e);
        }
      };
      //
      m_spinner = new Spinner(parent, SWT.SINGLE | SWT.BORDER);
      m_spinner.setMinimum(m_minimum);
      m_spinner.setMaximum(m_maximum);
      m_spinner.setSelection(m_selection);
      m_spinner.addModifyListener(m_modifyListener);
      // delayed
      m_spinner.setFont(parent.getFont());
      m_spinner.setEnabled(isEnabled());
    }
    return m_spinner;
  }

  private void doModifyText(ModifyEvent e) {
    if (isOkToUse(m_spinner)) {
      m_selection = m_spinner.getSelection();
    }
    dialogFieldChanged();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Enable/disable management
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void updateEnableState() {
    super.updateEnableState();
    if (isOkToUse(m_spinner)) {
      m_spinner.setEnabled(isEnabled());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Test access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the minimum value that the receiver will allow.
   */
  public void setMinimum(int minimum) {
    m_minimum = minimum;
    if (isOkToUse(m_spinner)) {
      m_spinner.setMinimum(m_minimum);
    }
  }

  /**
   * Sets the maximum value that the receiver will allow.
   */
  public void setMaximum(int maximum) {
    m_maximum = maximum;
    if (isOkToUse(m_spinner)) {
      m_spinner.setMaximum(m_maximum);
    }
  }

  /**
   * @return the selection.
   */
  public int getSelection() {
    return m_selection;
  }

  /**
   * Sets the selection. Triggers a dialog-changed event.
   */
  public void setSelection(int selection) {
    m_selection = selection;
    if (isOkToUse(m_spinner)) {
      m_spinner.setSelection(selection);
    } else {
      dialogFieldChanged();
    }
  }

  /**
   * Sets the text without triggering a dialog-changed event.
   */
  public void setSelectionWithoutUpdate(int selection) {
    m_selection = selection;
    if (isOkToUse(m_spinner)) {
      m_spinner.removeModifyListener(m_modifyListener);
      m_spinner.setSelection(selection);
      m_spinner.addModifyListener(m_modifyListener);
    }
  }

  @Override
  public void refresh() {
    super.refresh();
    if (isOkToUse(m_spinner)) {
      setSelectionWithoutUpdate(m_selection);
    }
  }
}
