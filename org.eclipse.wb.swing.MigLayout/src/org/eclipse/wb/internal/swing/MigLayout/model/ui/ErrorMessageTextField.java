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
package org.eclipse.wb.internal.swing.MigLayout.model.ui;

import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Special {@link org.eclipse.jface.fieldassist.DecoratedField} that can show optional error message
 * in top-right corner.
 * 
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
@SuppressWarnings("deprecation")
public class ErrorMessageTextField extends org.eclipse.jface.fieldassist.DecoratedField {
  private final FieldDecoration m_fieldDecoration;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ErrorMessageTextField(Composite parent, int style) {
    super(parent, style, new org.eclipse.jface.fieldassist.TextControlCreator());
    // prepare decoration
    {
      FieldDecoration standardDecoration =
          FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
      m_fieldDecoration = new FieldDecoration(standardDecoration.getImage(), ""); //$NON-NLS-1$
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Control getLayoutControl() {
    return super.getLayoutControl();
  }

  @Override
  public Control getControl() {
    return super.getControl();
  }

  /**
   * Shows error message.
   * 
   * @param message
   *          the message to show as decorator, or <code>null</code> to hide error message.
   */
  public void setErrorMessage(String message) {
    if (message != null) {
      addFieldDecoration(m_fieldDecoration, SWT.TOP | SWT.RIGHT, false);
      m_fieldDecoration.setDescription(message);
    } else {
      hideDecoration(m_fieldDecoration);
      // note, that we use knowledge about implementation of DecoratedField (that it uses FormLayout)
      {
        FormData data = (FormData) getControl().getLayoutData();
        data.right = new FormAttachment(100, 0);
      }
      getControl().getParent().layout();
    }
  }
}
