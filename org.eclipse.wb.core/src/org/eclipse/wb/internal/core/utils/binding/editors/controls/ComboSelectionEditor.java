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
package org.eclipse.wb.internal.core.utils.binding.editors.controls;

import org.eclipse.wb.internal.core.utils.binding.IDataEditor;

import org.eclipse.swt.widgets.Combo;

/**
 * Implementation of {@link IDataEditor} for selection index in {@link Combo}.
 *
 * @author scheglov_ke
 */
public final class ComboSelectionEditor implements IDataEditor {
  private final Combo m_combo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComboSelectionEditor(Combo combo) {
    m_combo = combo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDataEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getValue() {
    return new Integer(m_combo.getSelectionIndex());
  }

  public void setValue(Object value) {
    int index = ((Integer) value).intValue();
    m_combo.select(index);
  }
}
