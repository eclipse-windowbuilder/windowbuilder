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

import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;
import org.eclipse.wb.internal.css.dialogs.color.ColorDialog;
import org.eclipse.wb.internal.css.semantics.SimpleValue;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Button;

/**
 * Editor for {@link SimpleValue} with color.
 * 
 * @author scheglov_ke
 * @coverage CSS.ui
 */
public final class ColorValueEditor extends AbstractTextButtonValueEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColorValueEditor(StyleEditOptions options, String title, SimpleValue value) {
    super(options, title, value);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void onButtonClick(Button button) {
    ColorDialog colorDialog = new ColorDialog(button.getShell());
    // try to set initial value
    {
      // TODO
    }
    // open dialog
    if (colorDialog.open() != Window.OK) {
      return;
    }
    // set new value
    ColorInfo color = colorDialog.getColorInfo();
    if (m_options.useNamedColors && color.m_name != null) {
      m_value.set(color.m_name);
    } else {
      m_value.set("#" + color.getHexRGB());
    }
  }
}
