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
package org.eclipse.wb.internal.rcp.support;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Shell for {@link Font} preview.
 *
 * @author lobas_av
 * @coverage rcp.support
 */
public class FontPreviewShell extends Shell {
  private final Label m_label;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FontPreviewShell() {
    super(SWT.NONE);
    setLayout(new FillLayout());
    m_label = new Label(this, SWT.CENTER);
    setSize(450, 50);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Widget
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void checkSubclass() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Font
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFont(Font font) {
    m_label.setFont(font);
    FontData data = font.getFontData()[0];
    m_label.setText("" + data.getName() + " " + data.getHeight());
  }
}