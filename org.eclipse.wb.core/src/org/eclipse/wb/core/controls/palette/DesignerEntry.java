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

import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Single GEF-compatible entry on {@link PaletteComposite}.
 */
@SuppressWarnings("removal")
public abstract class DesignerEntry extends ToolEntry implements IEntry {
	public DesignerEntry(String label, String shortDescription, ImageDescriptor iconSmall) {
		super(label, shortDescription, iconSmall, null);
	}

	/**
	 * Sometimes we want to show entry, but don't allow to select it.
	 */
	@Override
	public abstract boolean isEnabled();

	/**
	 * Activates this {@link DesignerEntry}.
	 *
	 * @param reload is <code>true</code> if entry should be automatically reloaded
	 *               after successful using.
	 *
	 * @return <code>true</code> if {@link DesignerEntry} was successfully
	 *         activated.
	 */
	@Override
	public abstract boolean activate(boolean reload);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by
	 *             clients.
	 * @deprecated Use {@link #getSmallIcon()} instead.
	 */
	@Override
	@Deprecated(since = "1.17.0", forRemoval = true)
	public final ImageDescriptor getIcon() {
		return getSmallIcon();
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
}
