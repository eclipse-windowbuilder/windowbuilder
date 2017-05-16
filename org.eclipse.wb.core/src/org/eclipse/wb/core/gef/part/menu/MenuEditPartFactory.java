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
package org.eclipse.wb.core.gef.part.menu;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.core.gef.part.menu.MacMenuEditPart;
import org.eclipse.wb.internal.core.gef.part.menu.MenuEditPart;
import org.eclipse.wb.internal.core.gef.part.menu.MenuItemEditPart;
import org.eclipse.wb.internal.core.gef.part.menu.MenuPopupEditPart;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;

/**
 * Factory for "menu" {@link EditPart}s.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public final class MenuEditPartFactory {
  /**
   * @return the {@link EditPart} for {@link IMenuInfo}.
   */
  public static EditPart createPopupMenu(Object toolkitModel, IMenuPopupInfo popup) {
    return new MenuPopupEditPart(toolkitModel, popup);
  }

  /**
   * @return the {@link EditPart} for {@link IMenuInfo}.
   */
  public static EditPart createMenu(Object toolkitModel, IMenuInfo menu) {
    return new MenuEditPart(toolkitModel, menu);
  }

  /**
   * @return the {@link EditPart} for {@link IMenuInfo}, used on Mac.
   */
  public static EditPart createMenuMac(Object toolkitModel, IMenuInfo menu) {
    return new MacMenuEditPart(toolkitModel, menu);
  }

  /**
   * @return the {@link EditPart} for {@link IMenuItemInfo}.
   */
  public static EditPart createMenuItem(Object toolkitModel, IMenuItemInfo item) {
    return new MenuItemEditPart(toolkitModel, item);
  }

  public static final int MENU_Y_LOCATION = 3;
}