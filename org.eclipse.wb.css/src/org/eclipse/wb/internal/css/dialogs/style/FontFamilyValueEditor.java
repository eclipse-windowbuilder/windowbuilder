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
package org.eclipse.wb.internal.css.dialogs.style;

import org.eclipse.wb.internal.css.semantics.SimpleValue;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Button;

/**
 * Editor for {@link SimpleValue} with font family.
 * 
 * @author scheglov_ke
 * @coverage CSS.ui
 */
public final class FontFamilyValueEditor extends AbstractTextButtonValueEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FontFamilyValueEditor(StyleEditOptions options, String title, SimpleValue value) {
    super(options, title, value);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void onButtonClick(Button button) {
    FontListDialog dialog = new FontListDialog(button.getShell());
    dialog.setFontsString(m_value.get());
    // open dialog
    if (dialog.open() != Window.OK) {
      return;
    }
    // set new value
    m_value.set(dialog.getFontsString());
  }
}
