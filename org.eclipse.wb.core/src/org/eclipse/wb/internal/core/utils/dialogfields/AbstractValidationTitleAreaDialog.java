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
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableTitleAreaDialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Abstract {@link TitleAreaDialog} with {@link DialogField}'s validation.
 *
 * @author scheglov_ke
 */
public abstract class AbstractValidationTitleAreaDialog extends ResizableTitleAreaDialog {
  private final String m_shellText;
  private final String m_titleText;
  private final Image m_titleImage;
  private final String m_titleMessage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractValidationTitleAreaDialog(Shell parentShell,
      AbstractUIPlugin plugin,
      String shellText,
      String titleText,
      Image titleImage,
      String titleMessage) {
    super(parentShell, plugin);
    m_shellText = shellText;
    m_titleText = titleText;
    m_titleImage = titleImage;
    m_titleMessage = titleMessage;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Messages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final Control createContents(Composite parent) {
    Control control = super.createContents(parent);
    // configure Shell and title area
    {
      getShell().setText(m_shellText);
      setTitle(m_titleText);
      if (m_titleImage != null) {
        setTitleImage(m_titleImage);
      }
      setMessage(m_titleMessage);
    }
    // do validation first time
    {
      m_validationEnabled = true;
      validateAll();
    }
    //
    return control;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    //
    Composite container = new Composite(area, SWT.NONE);
    GridDataFactory.create(container).grab().fill();
    createControls(container);
    //
    return area;
  }

  /**
   * Creates controls on this {@link TitleAreaDialog}.
   */
  protected abstract void createControls(Composite container);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_validationEnabled;
  /**
   * Implementation of {@link IDialogFieldListener} for {@link DialogField}'s validation.
   */
  protected final IDialogFieldListener m_validateListener = new IDialogFieldListener() {
    public void dialogFieldChanged(DialogField field) {
      validateAll();
    }
  };

  /**
   * Validate all and disable/enable "OK" button.
   */
  public final void validateAll() {
    if (m_validationEnabled) {
      // prepare error message
      String errorMessage;
      try {
        errorMessage = validate();
      } catch (Throwable e) {
        errorMessage = e.getMessage();
      }
      // apply error message
      setErrorMessage(errorMessage);
      setValid(errorMessage == null);
    }
  }

  /**
   * Notifies that dialog is valid or not.
   */
  protected void setValid(boolean enabled) {
    // enable/disable OK
    {
      Button button = getButton(IDialogConstants.OK_ID);
      if (button != null) {
        button.setEnabled(enabled);
      }
    }
  }

  /**
   * Validate fields and returns error message or <code>null</code>.
   */
  protected String validate() throws Exception {
    return null;
  }
}
