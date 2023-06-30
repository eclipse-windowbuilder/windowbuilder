/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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

import org.eclipse.jface.resource.FontDescriptor;

/**
 * Provider for preferences of {@link PaletteComposite}.
 *
 * @author scheglov_ke
 * @coverage core.control.palette
 */
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
	 * @return <code>true</code> if only icons should be displayed for {@link IEntry}'s.
	 */
	boolean isOnlyIcons();

	/**
	 * @return the minimal number of columns for {@link ICategory}.
	 */
	int getMinColumns();

	int getLayoutType();
}
