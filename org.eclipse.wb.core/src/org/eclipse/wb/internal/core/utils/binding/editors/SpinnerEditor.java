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
package org.eclipse.wb.internal.core.utils.binding.editors;

import org.eclipse.wb.internal.core.utils.binding.IDataEditor;
import org.eclipse.wb.internal.core.utils.dialogfields.SpinnerDialogField;

/**
 * {@link IDataEditor} implementation for {@link SpinnerDialogField}.
 *
 * @author scheglov_ke
 */
public class SpinnerEditor implements IDataEditor {
  private final SpinnerDialogField m_field;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SpinnerEditor(SpinnerDialogField field) {
    m_field = field;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDataEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getValue() {
    return new Integer(m_field.getSelection());
  }

  public void setValue(Object value) {
    m_field.setSelection(((Integer) value).intValue());
  }
}
