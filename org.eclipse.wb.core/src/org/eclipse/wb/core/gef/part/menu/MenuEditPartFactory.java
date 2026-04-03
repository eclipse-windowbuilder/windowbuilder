/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.core.gef.part.menu;

import org.eclipse.wb.internal.core.gef.part.menu.MacMenuEditPart;
import org.eclipse.wb.internal.core.gef.part.menu.MenuEditPart;
import org.eclipse.wb.internal.core.gef.part.menu.MenuItemEditPart;
import org.eclipse.wb.internal.core.gef.part.menu.MenuPopupEditPart;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;

import org.eclipse.gef.EditPart;

/**
 * Factory for "menu" {@link EditPart}s.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public final class MenuEditPartFactory {
	/**
	 * @return the {@link EditPart} for {@link IMenuInfo}.
	 * @since 1.24
	 */
	public static EditPart createPopupMenu(Object toolkitModel, IMenuPopupInfo popup) {
		return new MenuPopupEditPart(toolkitModel, popup);
	}

	/**
	 * @return the {@link EditPart} for {@link IMenuInfo}.
	 * @since 1.24
	 */
	public static EditPart createMenu(Object toolkitModel, IMenuInfo menu) {
		return new MenuEditPart(toolkitModel, menu);
	}

	/**
	 * @return the {@link EditPart} for {@link IMenuInfo}, used on Mac.
	 * @since 1.24
	 */
	public static EditPart createMenuMac(Object toolkitModel, IMenuInfo menu) {
		return new MacMenuEditPart(toolkitModel, menu);
	}

	/**
	 * @return the {@link EditPart} for {@link IMenuItemInfo}.
	 * @since 1.24
	 */
	public static EditPart createMenuItem(Object toolkitModel, IMenuItemInfo item) {
		return new MenuItemEditPart(toolkitModel, item);
	}

	public static final int MENU_Y_LOCATION = 3;
}