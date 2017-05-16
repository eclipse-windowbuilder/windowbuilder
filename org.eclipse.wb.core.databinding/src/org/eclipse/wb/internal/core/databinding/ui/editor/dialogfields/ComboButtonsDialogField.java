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
package org.eclipse.wb.internal.core.databinding.ui.editor.dialogfields;

import org.eclipse.wb.internal.core.databinding.Activator;
import org.eclipse.wb.internal.core.utils.dialogfields.ComboButtonDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Dialog field containing a label a combo and a two button controls.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class ComboButtonsDialogField extends ComboButtonDialogField {
  private static final Image CLEAR_IMAGE = Activator.getImage("clear.gif");
  private final IStringButtonAdapter m_clearAdapter;
  private final boolean m_useClearButton;
  private Button m_clearButton;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComboButtonsDialogField(IStringButtonAdapter browseAdapter,
      IStringButtonAdapter clearAdapter,
      int comboFlags,
      boolean useClearButton) {
    super(browseAdapter, comboFlags);
    m_clearAdapter = clearAdapter;
    m_useClearButton = useClearButton;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout helpers
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Control[] doFillIntoGrid(Composite parent, int columns) {
    assertEnoughColumns(columns);
    // label
    Label label = getLabelControl(parent);
    label.setLayoutData(gridDataForLabel(1));
    // combo
    Combo combo = getComboControl(parent);
    int needColumns = getNumberOfControls();
    GridDataFactory.create(combo).fillH().grabH().spanH(columns - needColumns + 1);
    // buttons
    Button browseButton = getChangeControl(parent);
    Button clearButton = m_useClearButton ? getClearControl(parent) : null;
    // result controls
    return m_useClearButton
        ? new Control[]{label, combo, browseButton, clearButton}
        : new Control[]{label, combo, browseButton};
  }

  @Override
  public int getNumberOfControls() {
    return m_useClearButton ? 4 : 3;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // UI creation
  //
  ////////////////////////////////////////////////////////////////////////////
  public Button getClearControl(Composite parent) {
    if (m_clearButton == null) {
      assertCompositeNotNull(parent);
      // create button
      m_clearButton = new Button(parent, SWT.PUSH);
      m_clearButton.setFont(parent.getFont());
      m_clearButton.setImage(CLEAR_IMAGE);
      m_clearButton.setEnabled(isEnabled());
      // configure listener
      m_clearButton.addSelectionListener(new SelectionListener() {
        public void widgetDefaultSelected(SelectionEvent e) {
          changeClearPressed();
        }

        public void widgetSelected(SelectionEvent e) {
          changeClearPressed();
        }
      });
    }
    return m_clearButton;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Enable / disable management
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void updateEnableState() {
    super.updateEnableState();
    if (isOkToUse(m_clearButton)) {
      m_clearButton.setEnabled(isEnabled());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adapter communication
  //
  ////////////////////////////////////////////////////////////////////////////
  public void changeClearPressed() {
    m_clearAdapter.changeControlPressed(this);
  }
}