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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * This class provides a convenient shorthand for creating and initializing {@link TabItem}.
 *
 * @author lobas_av
 */
public class TabFactory {
  private final TabItem m_item;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private TabFactory(TabItem item) {
    m_item = item;
  }

  /**
   * Create new {@link TabFactory} with new {@link TabItem}.
   */
  public static TabFactory item(TabFolder folder) {
    return item(folder, SWT.NONE);
  }

  /**
   * Create new {@link TabFactory} with new {@link TabItem} use given <code>style</code>.
   */
  public static TabFactory item(TabFolder folder, int style) {
    return new TabFactory(new TabItem(folder, style));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets item text.
   */
  public TabFactory text(String text) {
    m_item.setText(text);
    return this;
  }

  /**
   * Sets item tooltip text.
   */
  public TabFactory tooltip(String text) {
    m_item.setToolTipText(text);
    return this;
  }

  /**
   * Sets item control.
   */
  public TabFactory control(Control control) {
    m_item.setControl(control);
    return this;
  }

  /**
   * Fills {@link TabItem} with new {@link Composite} and returns this {@link Composite}.
   */
  public Composite composite() {
    Composite composite = new Composite(m_item.getParent(), SWT.NONE);
    m_item.setControl(composite);
    return composite;
  }

  /**
   * Sets item image.
   */
  public TabFactory image(Image value) {
    m_item.setImage(value);
    return this;
  }

  /**
   * Sets item data.
   */
  public TabFactory data(Object value) {
    m_item.setData(value);
    return this;
  }
}