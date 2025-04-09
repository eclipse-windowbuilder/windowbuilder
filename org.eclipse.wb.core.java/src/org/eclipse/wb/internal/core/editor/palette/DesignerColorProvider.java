/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.palette;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.ui.palette.PaletteColorProvider;
import org.eclipse.swt.graphics.Color;

/**
 * Defines arbitrary colors that distinguish themselves from the default
 * palette.
 */
public class DesignerColorProvider extends PaletteColorProvider {
	public static final Color COLOR_PALETTE_BACKGROUND = ColorConstants.button;
	public static final Color COLOR_ENTRY_SELECTED = getShiftedColor(COLOR_PALETTE_BACKGROUND, 24);
	public static final Color COLOR_DRAWER_GRAD_BEGIN = getShiftedColor(COLOR_PALETTE_BACKGROUND, -8);
	public static final Color COLOR_DRAWER_GRAD_END = getShiftedColor(COLOR_PALETTE_BACKGROUND, 16);

	@Override
	public Color getListSelectedBackgroundColor() {
		return COLOR_ENTRY_SELECTED;
	}

	@Override
	public Color getListHoverBackgroundColor() {
		return COLOR_PALETTE_BACKGROUND;
	}

	@Override
	public Color getListBackground() {
		return COLOR_PALETTE_BACKGROUND;
	}

	/**
	 * @return new {@link Color} based on given {@link Color} and shifted on given
	 *         value to make it darker or lighter.
	 */
	private static Color getShiftedColor(Color color, int delta) {
		int r = Math.max(0, Math.min(color.getRed() + delta, 255));
		int g = Math.max(0, Math.min(color.getGreen() + delta, 255));
		int b = Math.max(0, Math.min(color.getBlue() + delta, 255));
		return new Color(color.getDevice(), r, g, b);
	}
}
