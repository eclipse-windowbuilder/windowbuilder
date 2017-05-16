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
package org.eclipse.wb.core.controls;

import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * JGoodies like separator control.
 *
 * @author scheglov_ke
 * @coverage core.control
 */
public final class Separator extends Composite {
  private static Font BOLD_FONT;
  private final Label m_title;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Separator(Composite parent, int style) {
    super(parent, style);
    GridLayoutFactory.create(this).noMargins().columns(2).spacingH(8);
    {
      m_title = new Label(this, SWT.NONE);
      GridDataFactory.create(m_title).alignVM();
      // set font
      if (BOLD_FONT == null) {
        BOLD_FONT = DrawUtils.getBoldFont(m_title.getFont());
      }
      m_title.setFont(BOLD_FONT);
    }
    {
      Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
      GridDataFactory.create(separator).grabH().fillH().alignVM();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the text to display.
   */
  public void setText(String text) {
    m_title.setText(text);
    layout();
  }

  @Override
  public void setForeground(Color color) {
    m_title.setForeground(color);
  }
}
