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
import org.eclipse.wb.internal.core.utils.binding.ValueUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.BooleanDialogField;

/**
 * Implementation of {@link IDataEditor} for {@link BooleanDialogField}.
 *
 * @author scheglov_ke
 */
public class BooleanEditor implements IDataEditor {
  private final BooleanDialogField m_field;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BooleanEditor(BooleanDialogField field) {
    m_field = field;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDataEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getValue() {
    return ValueUtils.booleanToObject(m_field.getSelection());
  }

  public void setValue(Object value) {
    m_field.setSelection(ValueUtils.objectToBoolean(value));
  }
}