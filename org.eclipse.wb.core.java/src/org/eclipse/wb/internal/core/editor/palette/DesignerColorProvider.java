/*******************************************************************************
 * Copyright (c) 2025, 2026 Patrick Ziegler and others.
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

import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.ui.palette.PaletteColorProvider;
import org.eclipse.swt.graphics.Color;

/**
 * Defines arbitrary colors that distinguish themselves from the default
 * palette.
 */
public class DesignerColorProvider extends PaletteColorProvider {
	public static final Color COLOR_PALETTE_BACKGROUND = ColorConstants.button;
	public static final Color COLOR_ENTRY_SELECTED = DrawUtils.getShiftedColor(COLOR_PALETTE_BACKGROUND, 24);
	public static final Color COLOR_DRAWER_GRAD_BEGIN = DrawUtils.getShiftedColor(COLOR_PALETTE_BACKGROUND, -8);
	public static final Color COLOR_DRAWER_GRAD_END = DrawUtils.getShiftedColor(COLOR_PALETTE_BACKGROUND, 16);

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
}
