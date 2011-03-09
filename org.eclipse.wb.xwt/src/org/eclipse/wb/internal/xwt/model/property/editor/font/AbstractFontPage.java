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
package org.eclipse.wb.internal.xwt.model.property.editor.font;

import org.eclipse.swt.widgets.Composite;

/**
 * Abstract page for editing {@link FontInfo} in {@link FontDialog}.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.property.editor
 */
public abstract class AbstractFontPage extends Composite {
  protected final FontDialog m_fontDialog;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractFontPage(Composite parent, int style, FontDialog fontDialog) {
    super(parent, style);
    m_fontDialog = fontDialog;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link FontInfo} selected in {@link FontDialog}.
   */
  public abstract void setFont(FontInfo fontInfo);
}