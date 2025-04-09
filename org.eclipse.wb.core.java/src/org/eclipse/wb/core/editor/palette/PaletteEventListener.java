/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.core.editor.palette;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;

import java.util.List;

/**
 * Listener for palette events.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public abstract class PaletteEventListener {
	/**
	 * Listener can update list of {@link CategoryInfo}'s.
	 */
	public void categories(List<CategoryInfo> categories) throws Exception {
	}

	/**
	 * Listener can update list of {@link CategoryInfo}'s.<br>
	 * This method is invoked after {@link #categories(List)}. Sometimes one method is not enough, for
	 * example there may be two listeners, both add categories, order of these listeners is not known,
	 * but one of them knows, that it wants to place its categories after others.
	 */
	public void categories2(List<CategoryInfo> categories) throws Exception {
	}

	/**
	 * Listener can update list of {@link EntryInfo}'s.<br>
	 * This event is useful, if you want to add new {@link EntryInfo}'s into existing
	 * {@link CategoryInfo}'s. If to want to add new {@link CategoryInfo} with {@link EntryInfo}'s, it
	 * is better to use {@link #categories(List)}.
	 */
	public void entries(CategoryInfo category, List<EntryInfo> entries) throws Exception {
	}

	/**
	 * @param canEdit
	 *          the single element array that specifies if palette can be edited, subscriber may set
	 *          it to <code>false</code>.
	 */
	public void canEdit(boolean[] canEdit) {
	}
}
