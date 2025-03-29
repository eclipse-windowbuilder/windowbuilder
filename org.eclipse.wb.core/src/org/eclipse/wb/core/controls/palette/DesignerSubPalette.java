/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
