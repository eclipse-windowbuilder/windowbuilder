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

import org.eclipse.swt.widgets.Text;

/**
 * Implementation of {@link IDataEditor} for {@link Text} widget.
 *
 * @author scheglov_ke
 */
public final class TextSingleEditor implements IDataEditor {
  private final Text m_text;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TextSingleEditor(Text text) {
    m_text = text;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDataEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getValue() {
    return m_text.getText();
  }

  public void setValue(Object value) {
    String text;
    if (value instanceof String) {
      text = (String) value;
    } else {
      text = value.toString();
    }
    m_text.setText(text);
  }
}
