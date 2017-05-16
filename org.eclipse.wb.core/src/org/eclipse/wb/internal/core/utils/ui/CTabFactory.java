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
package org.eclipse.wb.internal.core.utils.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabItem;

/**
 * This class provides a convienient shorthand forcreating and initializing {@link CTabItem}.
 *
 * @author lobas_av
 */
public class CTabFactory {
  private final CTabItem m_item;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private CTabFactory(CTabItem item) {
    m_item = item;
  }

  /**
   * Create new {@link CTabFactory} with new {@link CTabItem}.
   */
  public static CTabFactory item(CTabFolder folder) {
    return item(folder, SWT.NONE);
  }

  /**
   * Create new {@link CTabFactory} with new {@link TabItem} use given <code>style</code>.
   */
  public static CTabFactory item(CTabFolder folder, int style) {
    return new CTabFactory(new CTabItem(folder, style));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets item text.
   */
  public CTabFactory name(String text) {
    m_item.setText(text);
    return this;
  }

  /**
   * Sets item tooltip text.
   */
  public CTabFactory tooltip(String text) {
    m_item.setToolTipText(text);
    return this;
  }

  /**
   * Sets item control.
   */
  public CTabFactory control(Control control) {
    m_item.setControl(control);
    return this;
  }

  /**
   * Sets item image.
   */
  public CTabFactory image(Image value) {
    m_item.setImage(value);
    return this;
  }

  /**
   * Sets item data.
   */
  public CTabFactory data(Object value) {
    m_item.setData(value);
    return this;
  }
}