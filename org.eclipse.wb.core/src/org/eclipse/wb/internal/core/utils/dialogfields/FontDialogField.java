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
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * Implementation of {@link DialogField} for {@link Font} selecting.
 *
 * @author scheglov_ke
 */
public class FontDialogField extends DialogField {
  private FontData[] m_fontDataArray;
  private String m_chooseButtonText = "!Choose!";
  private String m_defaultButtonText = "!Default!";
  //
  private Group m_group;
  private Label m_fontLabel;
  private Button m_chooseButton;
  private Button m_defaultButton;

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Control[] doFillIntoGrid(Composite parent, int columns) {
    assertEnoughColumns(columns);
    // create group
    getGroupControl(parent);
    GridDataFactory.create(m_group).spanH(columns).grabH().fillH();
    //
    return new Control[]{m_group};
  }

  @Override
  public int getNumberOfControls() {
    return 1;
  }

  /**
   * Returns or creates {@link Group} control.
   */
  public Group getGroupControl(Composite parent) {
    if (m_group == null) {
      m_group = new Group(parent, SWT.NONE);
      m_group.setText(fLabelText);
      //
      GridLayoutFactory.create(m_group).columns(3);
      // font label
      {
        m_fontLabel = new Label(m_group, SWT.NONE);
        GridDataFactory.create(m_fontLabel).grab().fillH().alignVM();
        updateFontLabel();
      }
      // buttons
      {
        {
          m_chooseButton = new Button(m_group, SWT.NONE);
          GridDataFactory.create(m_chooseButton);
          m_chooseButton.setText(m_chooseButtonText);
          m_chooseButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              FontDialog fontDialog = new FontDialog(m_group.getShell());
              fontDialog.setFontList(m_fontDataArray);
              if (fontDialog.open() != null) {
                setFontDataArray(fontDialog.getFontList());
              }
            }
          });
        }
        {
          m_defaultButton = new Button(m_group, SWT.NONE);
          GridDataFactory.create(m_defaultButton);
          m_defaultButton.setText(m_defaultButtonText);
          m_defaultButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              setFontDataArray(Display.getDefault().getSystemFont().getFontData());
            }
          });
        }
      }
    }
    return m_group;
  }

  /**
   * Shows current array of {@link FontData} in {@link #m_fontLabel}.
   */
  private void updateFontLabel() {
    if (isOkToUse(m_fontLabel)) {
      if (m_fontDataArray != null) {
        m_fontLabel.setFont(new Font(m_group.getDisplay(), m_fontDataArray));
        //
        FontData fontData = m_fontDataArray[0];
        //
        String styleString = "";
        {
          int style = fontData.getStyle();
          if ((style & SWT.BOLD) != 0) {
            styleString += " BOLD";
          }
          if ((style & SWT.ITALIC) != 0) {
            styleString += " ITALIC";
          }
        }
        m_fontLabel.setText(fontData.getName() + styleString + " " + fontData.getHeight());
      } else {
        m_fontLabel.setFont(null);
        m_fontLabel.setText("");
      }
      m_group.layout();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Focus methods
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean setFocus() {
    if (isOkToUse(m_chooseButton)) {
      m_chooseButton.setFocus();
    }
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the selected array of {@link FontData}.
   */
  public FontData[] getFontDataArray() {
    return m_fontDataArray;
  }

  /**
   * Sets the array of {@link FontData} to display.
   */
  public void setFontDataArray(FontData[] fontDataArray) {
    m_fontDataArray = fontDataArray;
    updateFontLabel();
  }

  /**
   * Sets the label of "choose" button.
   */
  public void setChooseButtonText(String chooseButtonText) {
    m_chooseButtonText = chooseButtonText;
    if (isOkToUse(m_chooseButton)) {
      m_chooseButton.setText(m_chooseButtonText);
    }
  }

  /**
   * Sets the label of "default" button.
   */
  public void setDefaultButtonText(String defaultButtonText) {
    m_defaultButtonText = defaultButtonText;
    if (isOkToUse(m_defaultButton)) {
      m_defaultButton.setText(m_defaultButtonText);
    }
  }
}
