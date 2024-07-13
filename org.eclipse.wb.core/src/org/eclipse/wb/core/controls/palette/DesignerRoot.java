/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.core.controls.palette;

import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.palette.PaletteContextMenuProvider;
import org.eclipse.gef.ui.palette.PaletteCustomizer;
import org.eclipse.jface.action.IMenuManager;

import java.util.List;

/**
 * Serves as the root {@link org.eclipse.gef.palette.PaletteEntry} for the
 * palette model. It provides access to the {@link DesignerContainer}'s,
 * {@link DesignerEntry}'s and operations on them.
 */
@SuppressWarnings("removal")
public abstract class DesignerRoot extends PaletteRoot implements IPalette {

	@Override
	@SuppressWarnings("unchecked")
	public final List<DesignerContainer> getChildren() {
		return (List<DesignerContainer>) super.getChildren();
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by
	 *             clients.
	 * @deprecated Use {@link PaletteContextMenuProvider} instead.
	 */
	@Override
	@Deprecated(since = "1.17.0", forRemoval = true)
	public abstract void addPopupActions(IMenuManager menuManager, Object target, int iconsType);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by
	 *             clients.
	 * @deprecated Use {@link EditDomain#loadDefaultTool()}.
	 */
	@Override
	@Deprecated(since = "1.17.0", forRemoval = true)
	public abstract void selectDefault();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by
	 *             clients.
	 * @deprecated Use {@link #getChildren()} instead.
	 */
	@Override
	@Deprecated(since = "1.17.0", forRemoval = true)
	public final List<DesignerContainer> getCategories() {
		return getChildren();
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by
	 *             clients.
	 * @deprecated Use {@link PaletteCustomizer} instead.
	 */
	@Override
	@Deprecated(since = "1.17.0", forRemoval = true)
	public abstract void moveCategory(ICategory category, ICategory nextCategory);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by
	 *             clients.
	 * @deprecated Use {@link PaletteCustomizer} instead.
	 */
	@Override
	@Deprecated(since = "1.17.0", forRemoval = true)
	public abstract void moveEntry(IEntry entry, ICategory targetCategory, IEntry nextEntry);
}
