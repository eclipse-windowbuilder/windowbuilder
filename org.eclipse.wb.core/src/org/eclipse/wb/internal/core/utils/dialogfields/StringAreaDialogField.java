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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog field containing a label and a multi line text control.
 *
 * @author scheglov_ke
 */
public class StringAreaDialogField extends DialogField {
  private final int fRows;
  private String fText;
  private Text fTextControl;
  private ModifyListener fModifyListener;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StringAreaDialogField(int rows) {
    fRows = rows;
    fText = ""; //$NON-NLS-1$
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout helpers
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Control[] doFillIntoGrid(Composite parent, int nColumns) {
    assertEnoughColumns(nColumns);
    //
    Label label = getLabelControl(parent);
    label.setLayoutData(gridDataForLabel(1));
    Text text = getTextControl(parent);
    GridDataFactory.create(text).spanH(nColumns - 1).hintVC(fRows).fill();
    //
    return new Control[]{label, text};
  }

  @Override
  public int getNumberOfControls() {
    return 2;
  }

  protected static GridData gridDataForText(int span) {
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
    if (isOkToUse(fTextControl)) {
      fTextControl.setFocus();
      fTextControl.setSelection(0, fTextControl.getText().length());
    }
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // UI creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates or returns the created text control.
   *
   * @param parent
   *          The parent composite or <code>null</code> when the widget has already been created.
   */
  public Text getTextControl(Composite parent) {
    if (fTextControl == null) {
      assertCompositeNotNull(parent);
      fModifyListener = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          doModifyText(e);
        }
      };
      //
      fTextControl = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.BORDER);
      fTextControl.setText(fText);
      fTextControl.setFont(parent.getFont());
      fTextControl.addModifyListener(fModifyListener);
      //
      fTextControl.setEnabled(isEnabled());
    }
    return fTextControl;
  }

  private void doModifyText(ModifyEvent e) {
    if (isOkToUse(fTextControl)) {
      fText = fTextControl.getText();
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
    if (isOkToUse(fTextControl)) {
      fTextControl.setEnabled(isEnabled());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Text access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Gets the text. Can not be <code>null</code>
   */
  public String getText() {
    return fText;
  }

  /**
   * Sets the text. Triggers a dialog-changed event.
   */
  public void setText(String text) {
    if (text == null) {
      text = "";
    }
    fText = text;
    if (isOkToUse(fTextControl)) {
      fTextControl.setText(text);
    } else {
      dialogFieldChanged();
    }
  }

  /**
   * Sets the text without triggering a dialog-changed event.
   */
  public void setTextWithoutUpdate(String text) {
    fText = text;
    if (isOkToUse(fTextControl)) {
      fTextControl.removeModifyListener(fModifyListener);
      fTextControl.setText(text);
      fTextControl.addModifyListener(fModifyListener);
    }
  }

  @Override
  public void refresh() {
    super.refresh();
    if (isOkToUse(fTextControl)) {
      setTextWithoutUpdate(fText);
    }
  }
}
