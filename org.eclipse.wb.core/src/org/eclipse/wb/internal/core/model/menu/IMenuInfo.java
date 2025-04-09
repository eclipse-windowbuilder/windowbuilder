/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
