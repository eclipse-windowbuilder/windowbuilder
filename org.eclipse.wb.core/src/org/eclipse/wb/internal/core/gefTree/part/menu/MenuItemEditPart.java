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
package org.eclipse.wb.internal.core.gefTree.part.menu;

import org.eclipse.wb.core.gefTree.part.ObjectEditPart;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.gefTree.policy.menu.MenuItemLayoutEditPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;

/**
 * {@link TreeEditPart} for {@link IMenuItemInfo}.
 *
 * @author scheglov_ke
 * @coverage core.gefTree.menu
 */
public final class MenuItemEditPart extends ObjectEditPart {
  private final ObjectInfo m_itemInfo;
  private final IMenuItemInfo m_itemObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuItemEditPart(ObjectInfo itemInfo, IMenuItemInfo itemObject) {
    super(itemInfo);
    m_itemInfo = itemInfo;
    m_itemObject = itemObject;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(new MenuItemLayoutEditPolicy(m_itemInfo, m_itemObject));
  }
}
