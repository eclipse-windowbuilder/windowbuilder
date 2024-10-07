/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    DSA - Added getLayoutType method
 *******************************************************************************/
package org.eclipse.wb.core.controls.palette;

import org.eclipse.gef.ui.palette.PaletteViewerPreferences;
import org.eclipse.jface.resource.FontDescriptor;

/**
 * Provider for preferences of {@link PaletteComposite}.
 *
 * @author scheglov_ke
 * @coverage core.control.palette
 * @deprecated Replaced by {@link DesignPaletteViewerPreferences}.
 */
@Deprecated(forRemoval = true, since = "1.18.0")
public interface IPalettePreferences {
	/**
	 * @return the {@link FontDescriptor} for {@link ICategory}.
	 */
	FontDescriptor getCategoryFontDescriptor();

	/**
	 * @return the {@link FontDescriptor} for {@link IEntry}.
	 */
	FontDescriptor getEntryFontDescriptor();

	/**
	 * @return {@code true} if only icons should be displayed for {@link IEntry}'s.
	 * @deprecated Use {@link PaletteViewerPreferences#getLayoutSetting} instead and
	 *             compare with {@link PaletteViewerPreferences#LAYOUT_ICONS}.
	 */
	@Deprecated(forRemoval = true, since = "1.18.0")
	boolean isOnlyIcons();

	/**
	 * @return the minimal number of columns for {@link ICategory}.
	 */
	int getMinColumns();

	/**
	 * @deprecated Use {@link PaletteViewerPreferences#getLayoutSetting} instead.
	 */
	@Deprecated(forRemoval = true, since = "1.18.0")
	int getLayoutType();
}
