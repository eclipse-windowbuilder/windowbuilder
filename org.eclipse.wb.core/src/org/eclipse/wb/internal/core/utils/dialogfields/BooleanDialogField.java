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
import org.eclipse.swt.widgets.Label;

/**
 * Dialog field containing a label and a check box control.
 *
 * @author scheglov_ke
 */
public final class BooleanDialogField extends DialogField {
  private boolean m_selection;
  private Button m_buttonControl;
  private SelectionListener m_selectionListener;
  private final boolean m_leftToRight;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BooleanDialogField() {
    this(false);
  }

  /**
   * Creates boolean field editor which looks like usual check-box if <code>leftToRight</code> is
   * <code>true</code>.
   *
   * @param leftToRight
   *          if <code>true</code> creates boolean field editor which looks like usual check-box.
   */
  public BooleanDialogField(boolean leftToRight) {
    m_leftToRight = leftToRight;
    m_selection = false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout helpers
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Control[] doFillIntoGrid(Composite parent, int nColumns) {
    assertEnoughColumns(nColumns);
    // create controls and add layout data
    Button button;
    Label label;
    if (m_leftToRight) {
      button = getButtonControl(parent);
      GridDataFactory.create(button).alignHF();
      label = getLabelControl(parent);
      GridDataFactory.create(label).spanH(nColumns - 1).alignHF();
    } else {
      label = getLabelControl(parent);
      GridDataFactory.create(label).alignHF();
      button = getButtonControl(parent);
      GridDataFactory.create(button).spanH(nColumns - 1).alignHF();
    }
    //
    return new Control[]{label, button};
  }

  @Override
  public int getNumberOfControls() {
    return 2;
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
