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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Helper structure containing visual menu data: menu image, menu bounds, items bounds.
 *
 * @author mitin_aa
 * @coverage core.model.menu
 */
public final class MenuVisualData {
	/**
	 * An image representing popup menu for menu bar m_menuImage == null because we have a shell
	 * window as menu image
	 */
	public Image m_menuImage;
	/**
	 * A rectangle of menu bounds. For popup menu it is menu image size, for menu bar it is bounds in
	 * shell coordinates
	 */
	public Rectangle m_menuBounds;
	/**
	 * An ArrayList of Rectangles of menu item bounds in menu window coordinates.
	 */
	public List<Rectangle> m_itemBounds;
}