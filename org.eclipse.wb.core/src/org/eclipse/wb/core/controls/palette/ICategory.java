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
 *******************************************************************************/
package org.eclipse.wb.core.controls.palette;

import java.util.List;

/**
 * Category - collection of {@link IEntry}.
 *
 * @author scheglov_ke
 * @coverage core.control.palette
 * @deprecated Use {@link DesignerContainer} instead. This interface will be
 *             removed after the 2027-03 release.
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
