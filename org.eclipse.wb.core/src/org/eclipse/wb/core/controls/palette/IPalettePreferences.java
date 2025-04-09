/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * @deprecated Replaced by {@link DesignerPaletteViewerPreferences}. This
 *             interface will be removed after the 2027-03 release.
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
