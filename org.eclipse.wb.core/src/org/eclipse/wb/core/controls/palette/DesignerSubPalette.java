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
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.core.controls.palette;

import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Subclass of the {@link PaletteDrawer} that only supports
 * {@link PaletteDrawer}'s, as opposed to {@link ToolEntry}'s.
 */
public class DesignerSubPalette extends PaletteDrawer {
	public DesignerSubPalette(String name, String desc, ImageDescriptor icon) {
		super(name, icon);
		setDescription(desc);
	}

	@Override
	public boolean acceptsType(Object type) {
		return PaletteDrawer.PALETTE_TYPE_DRAWER.equals(type);
	}
}
