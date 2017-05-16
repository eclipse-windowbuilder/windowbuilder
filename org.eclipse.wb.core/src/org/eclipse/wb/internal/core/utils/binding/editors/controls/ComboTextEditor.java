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
 * Implementation of {@link IDataEditor} for text in {@link Combo}.
 *
 * @author scheglov_ke
 */
public final class ComboTextEditor implements IDataEditor {
  private final Combo m_combo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComboTextEditor(Combo combo) {
    m_combo = combo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDataEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getValue() {
    return m_combo.getText();
  }

  public void setValue(Object value) {
    m_combo.setText((String) value);
  }
}
