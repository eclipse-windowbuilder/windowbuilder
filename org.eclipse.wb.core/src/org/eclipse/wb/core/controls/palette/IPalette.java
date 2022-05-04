/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    DSA - Altered add popup actions to include IconType
 *******************************************************************************/
package org.eclipse.wb.core.controls.palette;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;

import java.util.List;

/**
 * The palette model interface. It provides access to the {@link ICategory}'s, {@link IEntry}'s and
 * operations on them.
 *
 * @author scheglov_ke
 * @coverage core.control.palette
 */
public interface IPalette {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link List} of {@link ICategory}'s to display as roots of palette.
   */
  List<ICategory> getCategories();

  /**
   * Adds {@link Action}'s to the popup menu.
   *
   * @param menuManager
   *          the {@link IMenuManager} for {@link Action}'s
   * @param target
   *          the object under cursor
   */
  void addPopupActions(IMenuManager menuManager, Object target, int iconsType);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asks for selecting default {@link IEntry}.
   */
  void selectDefault();

  /**
   * Moves given <code>category</code> before given <code>nextCategory</code>.
   */
  void moveCategory(ICategory category, ICategory nextCategory);

  /**
   * Moves given <code>entry</code> into given category before <code>nextEntry</code>.
   */
  void moveEntry(IEntry entry, ICategory targetCategory, IEntry nextEntry);
}
