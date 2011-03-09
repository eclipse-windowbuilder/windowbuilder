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
package org.eclipse.wb.internal.swing.model.property.editor.border.fields;

import org.eclipse.wb.core.controls.CSpinner;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * {@link AbstractBorderField} that allows to enter integer value.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class IntegerField extends AbstractBorderField {
  private CSpinner m_spinner;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public IntegerField(Composite parent, String labelText) {
    super(parent, 2, labelText);
    // create spinner
    {
      m_spinner = new CSpinner(this, SWT.BORDER);
      GridDataFactory.create(m_spinner).hintHC(15);
      m_spinner.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          notifyListeners(SWT.Selection, event);
        }
      });
    }
    new Label(this, SWT.NONE).setText("pixel");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the value, that should correspond to the one of the field values.
   */
  public void setValue(int value) throws Exception {
    m_spinner.setSelection(value);
  }

  @Override
  public String getSource() throws Exception {
    int value = m_spinner.getSelection();
    return Integer.toString(value);
  }
}
