/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.core.controls.palette;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Single entry on {@link PaletteComposite}.
 *
 * @author scheglov_ke
 * @coverage core.control.palette
 * @deprecated Use {@link DesignerEntry instead}.
 */
// TODO GEF
@Deprecated(since = "1.17.0", forRemoval = true)
public interface IEntry {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sometimes we want to show entry, but don't allow to select it.
	 */
	boolean isEnabled();

	/**
	 * @return the icon of {@link IEntry}.
	 */
	ImageDescriptor getIcon();

	/**
	 * @return the title text of {@link IEntry}.
	 */
	String getText();

	/**
	 * @return the tooltip text of {@link IEntry}.
	 */
	String getToolTipText();

	////////////////////////////////////////////////////////////////////////////
	//
	// Activation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Activates this {@link IEntry}.
	 *
	 * @param reload
	 *          is <code>true</code> if entry should be automatically reloaded after successful using.
	 *
	 * @return <code>true</code> if {@link IEntry} was successfully activated.
	 */
	boolean activate(boolean reload);
}
