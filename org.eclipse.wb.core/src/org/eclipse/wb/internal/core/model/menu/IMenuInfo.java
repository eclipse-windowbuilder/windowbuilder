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
package org.eclipse.wb.internal.core.model.menu;

import java.util.List;

/**
 * Interface for menu containers: drop-down, popup or bar menus.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage core.model.menu
 */
public interface IMenuInfo extends IMenuObjectInfo {
  String NO_ITEMS_TEXT = "(Add items here)";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if {@link IMenuItemInfo}'s are located horizontally, and
   *         <code>false</code> if vertically.
   */
  boolean isHorizontal();

  /**
   * @return {@link IMenuItemInfo}'s that this menu contains.
   */
  List<IMenuItemInfo> getItems();
}
