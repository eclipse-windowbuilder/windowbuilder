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

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Abstract composite for {@link DialogField} based editing.
 *
 * @author scheglov_ke
 */
public class AbstractValidationComposite extends Composite {
  protected final IMessageContainer m_messageContainer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractValidationComposite(Composite parent, int style, IMessageContainer messageContainer) {
    super(parent, style);
    m_messageContainer = messageContainer;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final IDialogFieldListener m_validateListener = new IDialogFieldListener() {
    public void dialogFieldChanged(DialogField field) {
      validateAll();
    }
  };

  /**
   * Validate all and update {@link IMessageContainer}.
   */
  public final void validateAll() {
    String errorMessage = validate();
    m_messageContainer.setErrorMessage(errorMessage);
  }

  /**
   * Validate fields and returns error message or <code>null</code>.
   */
  protected String validate() {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures given {@link DialogField} and creates corresponding {@link Control}'s.
   */
  protected final void doCreateField(DialogField dialogField, String labelText, int charsWidth) {
    dialogField.setLabelText(labelText);
    dialogField.setDialogFieldListener(m_validateListener);
    // fill controls
    int columns = ((GridLayout) getLayout()).numColumns;
    DialogFieldUtils.fillControls(this, dialogField, columns, charsWidth);
  }
}
