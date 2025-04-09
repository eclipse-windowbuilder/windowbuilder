/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.internal.draw2d.IRootFigure;
import org.eclipse.wb.internal.gef.core.EditDomain;

import org.eclipse.draw2d.geometry.Point;
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
	 * Identifies the layer containing the primary pieces of the application.
	 */
	String PRIMARY_LAYER = "Primary Layer";
	/**
	 * The layer directly below {@link #HANDLE_LAYER}.
	 */
	String HANDLE_LAYER_SUB_1 = "Handle Layer Sub 1";
	/**
	 * The layer directly below {@link #HANDLE_LAYER}.
	 */
	String HANDLE_LAYER_SUB_2 = "Handle Layer Sub 2";
	/**
	 * Identifies the layer containing handles, which are typically editing decorations that appear on
	 * top of any model representations.
	 */
	String HANDLE_LAYER = "Handle Layer";
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
	 * The layer containing feedback, which generally temporary visuals that appear on top of all
	 * other visuals.
	 */
	String FEEDBACK_LAYER = "Feedback Layer";
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
	 * Returns root {@link Figure} use for access to {@link Layer}'s.
	 */
	IRootFigure getRootFigure();

	/**
	 * Returns the layer identified by the <code>name</code> given in the input.
	 */
	Layer getLayer(String name);

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
	void deselect(List<? extends org.eclipse.gef.EditPart> editParts);

	////////////////////////////////////////////////////////////////////////////
	//
	// Finding
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns <code>null</code> or the <code>{@link EditPart}</code> at the specified location, using
	 * the given exclusion set and conditional.
	 */
	EditPart findTargetEditPart(int x,
			int y,
			final Collection<? extends org.eclipse.gef.EditPart> exclude,
			final Conditional conditional);

	/**
	 * Returns <code>null</code> or the <code>{@link EditPart}</code> at the specified location on
	 * specified given layer, using the given exclusion set and conditional.
	 */
	EditPart findTargetEditPart(int x,
			int y,
			final Collection<? extends org.eclipse.gef.EditPart> exclude,
			final Conditional conditional,
			String layer);

	/**
	 * @return the <code>{@link Handle}</code> at the specified location.
	 */
	Handle findTargetHandle(Point location);

	/**
	 * Returns the <code>{@link Handle}</code> at the specified location <code>(x, y)</code>. Returns
	 * <code>null</code> if no handle exists at the given location <code>(x, y)</code>.
	 */
	Handle findTargetHandle(int x, int y);
}