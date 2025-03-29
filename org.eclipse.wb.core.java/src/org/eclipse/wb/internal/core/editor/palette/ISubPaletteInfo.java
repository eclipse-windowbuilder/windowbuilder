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
package org.eclipse.wb.internal.core.editor.palette;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;

import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.ToolEntry;

import java.util.List;

/**
 * Interface that may be implemented by {@link EntryInfo}'s when they shouldn't
 * be realized by a {@link ToolEntry}, but rather a {@link PaletteDrawer}.
 */
public interface ISubPaletteInfo {

	/**
	 * Returns the list of elements contained by this entry info.
	 *
	 * @return the stack elements contained by this entry.
	 */
	public List<CategoryInfo> getSubCategories();
}
