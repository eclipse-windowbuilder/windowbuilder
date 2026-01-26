/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.gef.core;

import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.internal.gef.core.EditDomain;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.ISelectionProvider;

import java.util.Collection;
import java.util.List;

/**
 * @author lobas_av
 * @coverage gef.core
 */
public interface IEditPartViewer extends ISelectionProvider, org.eclipse.gef.EditPartViewer {
	/**
	 * The layer directly below {@link #PRIMARY_LAYER}.
	 */
	String PRIMARY_LAYER_SUB_1 = "Primary Layer Sub 1";
	/**
	 * The layer directly below {@link #HANDLE_LAYER}.
	 */
	String HANDLE_LAYER_SUB_1 = "Handle Layer Sub 1";
	/**
	 * The layer directly below {@link #HANDLE_LAYER}.
	 */
	String HANDLE_LAYER_SUB_2 = "Handle Layer Sub 2";
	/**
	 * The layer directly below {@link #HANDLE_LAYER}.
	 */
	String HANDLE_LAYER_STATIC = "Handle Layer Static";
	/**
	 * The layer directly above {@link #FEEDBACK_LAYER}.
	 */
	String FEEDBACK_LAYER_ABV_1 = "Feedback Layer Abv 1";
	/**
	 * The layer directly below {@link #FEEDBACK_LAYER}.
	 */
	String FEEDBACK_LAYER_SUB_1 = "Feedback Layer Sub 1";
	/**
	 * The layer directly below {@link #FEEDBACK_LAYER}.
	 */
	String FEEDBACK_LAYER_SUB_2 = "Feedback Layer Sub 2";
	/**
	 * The layer containing feedback figures that can accept clicks.
	 */
	String CLICKABLE_LAYER = "Clickable Layer";
	/**
	 * Identifies the layer containing the menu.
	 */
	String MENU_PRIMARY_LAYER = "Menu Primary Layer";
	/**
	 * The special layer containing {@link Handle}'s for menu.
	 */
	String MENU_HANDLE_LAYER = "Menu Handle Layer";
	/**
	 * The special layer containing static {@link Handle}'s for menu.
	 */
	String MENU_HANDLE_LAYER_STATIC = "Menu Handle Layer static";
	/**
	 * The special layer containing feedback for menu.
	 */
	String MENU_FEEDBACK_LAYER = "Menu Feedback Layer";
	/**
	 * The most top layer.
	 */
	String TOP_LAYER = "Top Layer";

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * @return viewer horizontal scroll offset.
	 */
	int getHOffset();

	/**
	 * @return viewer vertical scroll offset.
	 */
	int getVOffset();

	/**
	 * Returns the {@link EditDomain EditDomain} to which this viewer belongs.
	 */
	EditDomain getEditDomain();

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Removes the specified <code>{@link List}</code> of <code>{@link EditPart}</code>'s from the
	 * current selection. The last EditPart in the new selection is made
	 * {@link EditPart#SELECTED_PRIMARY primary}.
	 */
	void deselect(List<? extends EditPart> editParts);

	////////////////////////////////////////////////////////////////////////////
	//
	// Finding
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns <code>null</code> or the <code>{@link EditPart}</code> at the specified location on
	 * specified given layer, using the given exclusion set and conditional.
	 */
	EditPart findObjectAtExcluding(Point location,
			final Collection<IFigure> exclude,
			final Conditional conditional,
			String layer);
}