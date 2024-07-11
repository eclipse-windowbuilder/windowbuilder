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

import java.util.List;

/**
 * Category - collection of {@link IEntry}.
 *
 * @author scheglov_ke
 * @coverage core.control.palette
 * @deprecated Use {@link DesignerContainer} instead.
 */
//TODO GEF
@Deprecated(since = "1.17.0", forRemoval = true)
public interface ICategory {
	/**
	 * @return the title text of category.
	 */
	String getText();

	/**
	 * @return the tooltip text of {@link ICategory}.
	 */
	String getToolTipText();

	/**
	 * @return <code>true</code> if this category is open.
	 */
	boolean isOpen();

	/**
	 * Sets if this category is open.
	 */
	void setOpen(boolean b);

	/**
	 * @return the {@link List} of {@link IEntry}'s.
	 */
	List<? extends IEntry> getEntries();
}
