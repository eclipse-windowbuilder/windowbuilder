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

import org.eclipse.gef.palette.PaletteDrawer;

import java.util.List;

/**
 * Category - collection of {@link DesignerEntry}.
 */
@SuppressWarnings("removal")
public abstract class DesignerContainer extends PaletteDrawer implements ICategory {

	protected DesignerContainer(String label, String desc) {
		super(label, null);
		setDescription(desc);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by
	 *             clients.
	 * @deprecated Use {@link #getLabel()} instead.
	 */
	@Override
	@Deprecated(since = "1.17.0", forRemoval = true)
	public final String getText() {
		return getLabel();
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by
	 *             clients.
	 * @deprecated Use {@link #getDescription()} instead.
	 */
	@Override
	@Deprecated(since = "1.17.0", forRemoval = true)
	public final String getToolTipText() {
		return getDescription();
	}

	/**
	 * @return <code>true</code> if this category is open.
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by
	 *             clients.
	 * @deprecated Set in the {@code DrawerFigure}.
	 */
	@Override
	@Deprecated(since = "1.17.0", forRemoval = true)
	public abstract boolean isOpen();

	/**
	 * Sets if this category is open.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by
	 *             clients.
	 * @deprecated Set in the {@code DrawerFigure}.
	 */
	@Override
	@Deprecated(since = "1.17.0", forRemoval = true)
	public abstract void setOpen(boolean b);

	@Override
	@SuppressWarnings("unchecked")
	public List<DesignerEntry> getEntries() {
		return (List<DesignerEntry>) getChildren();
	}

}
