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
package org.eclipse.wb.internal.swing.model.component.menu;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.swt.graphics.Image;

import javax.swing.JMenuItem;

/**
 * Model for {@link JMenuItem}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.menu
 */
public class JMenuItemInfo extends ContainerInfo implements IAdaptable {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JMenuItemInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IMenuItemInfo m_itemImpl = new MenuItemImpl();

  public <T> T getAdapter(Class<T> adapter) {
    if (adapter.isAssignableFrom(IMenuItemInfo.class)) {
      return adapter.cast(m_itemImpl);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    // item bounds fetches by using SwingImageUtils.fetchMenuVisualData()
    if (getParent() instanceof JMenuInfo || getParent() instanceof JPopupMenuInfo) {
      refresh_fetch_super();
      return;
    }
    // other container
    super.refresh_fetch();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuItemInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link IMenuItemInfo}.
   * 
   * @author scheglov_ke
   */
  private final class MenuItemImpl extends AbstractMenuObject implements IMenuItemInfo {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MenuItemImpl() {
      super(JMenuItemInfo.this);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Model
    //
    ////////////////////////////////////////////////////////////////////////////
    public Object getModel() {
      return JMenuItemInfo.this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    public Image getImage() {
      return null;
    }

    public Rectangle getBounds() {
      return JMenuItemInfo.this.getBounds();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMenuItemInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuInfo getMenu() {
      return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Policy
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuPolicy getPolicy() {
      return IMenuPolicy.NOOP;
    }
  }
}
