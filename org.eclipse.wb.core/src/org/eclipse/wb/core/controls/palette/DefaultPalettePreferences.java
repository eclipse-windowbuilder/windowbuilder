/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    DSA - layout type added
 *******************************************************************************/
package org.eclipse.wb.core.controls.palette;

import org.eclipse.jface.resource.FontDescriptor;

/**
 * The default implementation of {@link IPalettePreferences}.
 *
 * @author scheglov_ke
 * @coverage core.control.palette
 * @deprecated Replaced by {@link DesignPaletteViewerPreferences}.
 */
@Deprecated(forRemoval = true, since = "1.18.0")
public final class DefaultPalettePreferences implements IPalettePreferences {
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
		return false;
	}

	@Override
	public int getMinColumns() {
		return 1;
	}

	@Override
	public int getLayoutType() {
		return 0;
	}
}
