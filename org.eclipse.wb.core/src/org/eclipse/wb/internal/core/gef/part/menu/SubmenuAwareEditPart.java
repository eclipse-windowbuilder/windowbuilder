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
package org.eclipse.wb.internal.core.gef.part.menu;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.gef.policy.menu.MenuSelectionEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.menu.SubmenuAwareLayoutEditPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.LayerManager;

import java.util.Collections;
import java.util.List;

/**
 * {@link EditPart} for {@link IMenuObjectInfo} which may contain {@link IMenuInfo} as child.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage core.gef.menu
 */
public abstract class SubmenuAwareEditPart extends MenuObjectEditPart {
	private final IMenuObjectInfo m_object;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SubmenuAwareEditPart(Object toolkitModel, IMenuObjectInfo menuModel) {
		super(toolkitModel, menuModel);
		m_object = menuModel;
	}

	/////////////////////////////////////////////////////////////////////
	//
	// Visuals
	//
	/////////////////////////////////////////////////////////////////////
	@Override
	public final IFigure getContentPane() {
		return LayerManager.Helper.find(getViewer()).getLayer(IEditPartViewer.MENU_PRIMARY_LAYER);
	}

	@Override
	protected void addChildVisual(org.eclipse.gef.EditPart childPart, int index) {
		// this needed because index for menu item child (cascaded menu)
		// is always zero. This leads to improper edit parts order.
		// The workaround is to override this method and forcibly
		// add figure with default index.
		GraphicalEditPart graphicalPart = (GraphicalEditPart) childPart;
		EditPartViewer graphicalViewer = graphicalPart.getViewer();
		Figure graphicalFigure = graphicalPart.getFigure();
		getContentPane().add(graphicalFigure);
		graphicalViewer.getVisualPartMap().put(graphicalFigure, childPart);
	}

	/////////////////////////////////////////////////////////////////////
	//
	// Edit policies
	//
	/////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new SubmenuAwareLayoutEditPolicy(m_object));
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new MenuSelectionEditPolicy());
	}

	/////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	/////////////////////////////////////////////////////////////////////
	@Override
	protected final List<?> getModelChildren() {
		// prepare selected object
		IMenuObjectInfo selectedObject = null;
		{
			List<? extends EditPart> selectedEditParts = getViewer().getSelectedEditParts();
			if (!selectedEditParts.isEmpty()) {
				EditPart selectedEditPart = selectedEditParts.get(selectedEditParts.size() - 1);
				MenuObjectEditPart menuObjectEditPart = getMenuObjectEditPart(selectedEditPart);
				if (menuObjectEditPart != null) {
					selectedObject = menuObjectEditPart.getMenuModel();
				}
			}
		}
		// sub-menu is visible when "selected" or "selecting" objects belong to our menu
		if (MenuObjectInfoUtils.isParentChild(m_object, selectedObject)
				|| MenuObjectInfoUtils.isParentChild(m_object, MenuObjectInfoUtils.m_selectingObject)) {
			Object childMenu = getChildMenu();
			if (childMenu != null) {
				return Collections.singletonList(childMenu);
			}
		}
		// if we are not on the path to activating menu object, so don't show sub-menu
		return Collections.EMPTY_LIST;
	}

	/**
	 * In Swing we can drop just any component on menu, for example strut/glue between items. But for
	 * these components {@link EditPart} is not {@link MenuObjectEditPart}, so, we should climb up
	 * until find {@link MenuObjectEditPart}.
	 *
	 * @return the {@link MenuObjectEditPart} that corresponds to given {@link EditPart}, or
	 *         <code>null</code>, if {@link EditPart} does not belong to any
	 *         {@link MenuObjectEditPart} hierarchy.
	 */
	private static MenuObjectEditPart getMenuObjectEditPart(EditPart editPart) {
		for (; editPart != null; editPart = editPart.getParent()) {
			if (editPart instanceof MenuObjectEditPart) {
				return (MenuObjectEditPart) editPart;
			}
		}
		// not a MenuObjectEditPart
		return null;
	}

	/**
	 * @return the toolkit model for {@link IMenuInfo}, or <code>null</code> this object has no
	 *         sub-menu.
	 */
	protected abstract Object getChildMenu();
}
