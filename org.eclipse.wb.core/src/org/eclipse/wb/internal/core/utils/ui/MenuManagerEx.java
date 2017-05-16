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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Extended {@link MenuManager} that particularly supports image for this {@link MenuItem}.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public class MenuManagerEx extends MenuManager {
  private Image m_image;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuManagerEx() {
  }

  public MenuManagerEx(String text) {
    super(text);
  }

  public MenuManagerEx(String text, String id) {
    super(text, id);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Image} for this {@link MenuItem}.
   */
  public Image getImage() {
    return m_image;
  }

  /**
   * Sets the {@link Image} for this {@link MenuItem}.
   */
  public void setImage(Image image) {
    m_image = image;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Fill
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void fill(Menu parent, int index) {
    super.fill(parent, index);
    // set image for menu manager's item
    index = parent.getItemCount() - 1;
    MenuItem menuItem = parent.getItem(index);
    menuItem.setImage(m_image);
  }
}
