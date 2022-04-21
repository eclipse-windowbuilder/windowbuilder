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
package org.eclipse.wb.internal.rcp.model.jface.action;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.JavaMenuMenuObject;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;

import org.eclipse.swt.graphics.Image;

/**
 * Implementation of {@link IMenuPopupInfo} for dropping down some {@link MenuManagerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class MenuManagerPopupInfo extends JavaMenuMenuObject implements IMenuPopupInfo {
  private final MenuManagerInfo m_manager;
  private Rectangle m_bounds;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuManagerPopupInfo(MenuManagerInfo manager) {
    super(manager);
    m_manager = manager;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the bounds of popup figure.
   */
  public void setBounds(Rectangle menuToolItemBounds) {
    m_bounds = menuToolItemBounds;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Model
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getModel() {
    return m_manager;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Image getImage() {
    return null;
  }

  @Override
  public Rectangle getBounds() {
    return m_bounds;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuPopupInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IMenuInfo getMenu() {
    return MenuObjectInfoUtils.getMenuInfo(m_manager);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IMenuPolicy getPolicy() {
    return getMenu().getPolicy();
  }
}
