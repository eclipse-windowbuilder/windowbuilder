/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
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

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.gef.ui.palette.DefaultPaletteViewerPreferences;
import org.eclipse.gef.ui.palette.PaletteViewerPreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.FontDescriptor;

/**
 * The default implementation of {@link IPalettePreferences}.
 */
@SuppressWarnings("removal")
public class DesignerPaletteViewerPreferences extends DefaultPaletteViewerPreferences implements IPalettePreferences {

	public DesignerPaletteViewerPreferences() {
		this(DesignerPlugin.getPreferences());
	}

	public DesignerPaletteViewerPreferences(IPreferenceStore store) {
		super(store);
		super.setAutoCollapseSetting(COLLAPSE_NEVER);
	}

	@Override
	public FontDescriptor getCategoryFontDescriptor() {
		return null;
	}

	@Override
	public FontDescriptor getEntryFontDescriptor() {
		return null;
	}

	@Override
	public boolean isOnlyIcons() {
		return getLayoutSetting() == PaletteViewerPreferences.LAYOUT_ICONS;
	}

	@Override
	public int getMinColumns() {
		return 1;
	}

	@Override
	public int getLayoutType() {
		return getLayoutSetting();
	}
}
