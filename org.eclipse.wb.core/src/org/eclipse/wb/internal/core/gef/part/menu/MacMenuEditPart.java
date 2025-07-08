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

import org.eclipse.wb.core.gef.part.menu.MenuEditPartFactory;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.LayerManager;

/**
 * {@link MenuEditPart} for MacOSX which does special handling for OSX menu bar.
 *
 * @author mitin_aa
 * @coverage core.gef.menu
 */
public final class MacMenuEditPart extends MenuEditPart {
	private final IMenuInfo m_menu;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MacMenuEditPart(Object toolkitModel, IMenuInfo menu) {
		super(toolkitModel, menu);
		m_menu = menu;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IFigure createFigure() {
		return new MacMenuImageFigure(m_menu);
	}
	@Override
	protected void refreshVisuals() {
		if (!isSubMenu()) {
			Rectangle bounds = m_menu.getBounds();
			getFigure().setBounds(
					new Rectangle(TOP_LOCATION.x,
							MenuEditPartFactory.MENU_Y_LOCATION,
							bounds.width,
							bounds.height));
		} else {
			super.refreshVisuals();
		}
	}
	private boolean m_addedSelf = false;
	private IFigure m_fakeFigure;
	@Override
	protected boolean addSelfVisual(int index) {
		if (!isSubMenu()) {
			LayerManager.Helper.find(getViewer()).getLayer(IEditPartViewer.PRIMARY_LAYER).add(getFigure());
			m_addedSelf = true;
			// add invisible fake figure to the content pane to keep index right
			GraphicalEditPart parent = (GraphicalEditPart) getParent();
			parent.getContentPane().add(getFakeFigure(), index);
			return true;
		}
		return false;
	}
	@Override
	protected boolean removeSelfVisual() {
		if (m_addedSelf) {
			LayerManager.Helper.find(getViewer()).getLayer(IEditPartViewer.PRIMARY_LAYER).remove(getFigure());
			FigureUtils.removeFigure(getFakeFigure());
			m_addedSelf = false;
			return true;
		}
		return false;
	}
	private IFigure getFakeFigure() {
		if (m_fakeFigure == null) {
			m_fakeFigure = new Figure();
			m_fakeFigure.setVisible(false);
		}
		return m_fakeFigure;
	}
}
