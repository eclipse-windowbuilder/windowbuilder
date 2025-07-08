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
package org.eclipse.wb.internal.gef.graphical;

import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.tools.MarqueeSelectionTool;
import org.eclipse.wb.internal.draw2d.IRootFigure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.LayerManager;

/**
 * A {@link RootEditPart} is the <i>root</i> of an {@link IEditPartViewer}. It bridges the gap
 * between the {@link IEditPartViewer} and its contents. It does not correspond to anything in the
 * model, and typically can not be interacted with by the User. The Root provides a homogeneous
 * context for the applications "real" EditParts.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class RootEditPart extends GraphicalEditPart implements org.eclipse.gef.RootEditPart, LayerManager {
	private IEditPartViewer m_viewer;
	private final IRootFigure m_rootFigure;
	private EditPart m_contentEditPart;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RootEditPart(IRootFigure rootFigure) {
		m_rootFigure = rootFigure;
		createLayers();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Layer's
	//
	////////////////////////////////////////////////////////////////////////////
	private void createLayers() {
		m_rootFigure.addLayer(new Layer(IEditPartViewer.PRIMARY_LAYER_SUB_1));
		m_rootFigure.addLayer(new Layer(IEditPartViewer.PRIMARY_LAYER));
		m_rootFigure.addLayer(new Layer(IEditPartViewer.HANDLE_LAYER_SUB_1));
		m_rootFigure.addLayer(new Layer(IEditPartViewer.HANDLE_LAYER_SUB_2));
		m_rootFigure.addLayer(new Layer(IEditPartViewer.HANDLE_LAYER));
		m_rootFigure.addLayer(new Layer(IEditPartViewer.HANDLE_LAYER_STATIC));
		m_rootFigure.addLayer(new Layer(IEditPartViewer.FEEDBACK_LAYER_SUB_1));
		m_rootFigure.addLayer(new Layer(IEditPartViewer.FEEDBACK_LAYER_SUB_2));
		m_rootFigure.addLayer(new Layer(IEditPartViewer.FEEDBACK_LAYER));
		m_rootFigure.addLayer(new Layer(IEditPartViewer.FEEDBACK_LAYER_ABV_1));
		m_rootFigure.addLayer(new Layer(IEditPartViewer.CLICKABLE_LAYER));
		m_rootFigure.addLayer(new Layer(IEditPartViewer.MENU_PRIMARY_LAYER));
		m_rootFigure.addLayer(new Layer(IEditPartViewer.MENU_HANDLE_LAYER));
		m_rootFigure.addLayer(new Layer(IEditPartViewer.MENU_HANDLE_LAYER_STATIC));
		m_rootFigure.addLayer(new Layer(IEditPartViewer.MENU_FEEDBACK_LAYER));
		m_rootFigure.addLayer(new Layer(IEditPartViewer.TOP_LAYER));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EditPart
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the root's {@link EditPartViewer}.
	 */
	@Override
	public IEditPartViewer getViewer() {
		return m_viewer;
	}

	@Override
	public void setViewer(EditPartViewer viewer) {
		if (m_viewer == viewer) {
			return;
		}
		if (m_viewer != null) {
			unregister();
		}
		m_viewer = (IEditPartViewer) viewer;
		if (m_viewer != null) {
			register();
		}
	}

	/**
	 * Return root {@link IFigure} for all {@link EditPart} {@link IFigure}'s.
	 */
	@Override
	public IFigure getContentPane() {
		return m_rootFigure.getLayer(IEditPartViewer.PRIMARY_LAYER);
	}

	/**
	 * This {@link EditPart} not contains itself {@link IFigure}.
	 */
	@Override
	protected IFigure createFigure() {
		return null;
	}

	@Override
	public Object getModel() {
		return LayerManager.ID;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IRootEditPart
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the <i>content</i> {@link EditPart}.
	 */
	@Override
	public EditPart getContents() {
		return m_contentEditPart;
	}

	/**
	 * Sets the <i>content</i> {@link EditPart}. A IRootEditPart only has a single child, called its
	 * <i>contents</i>.
	 */
	@Override
	public void setContents(org.eclipse.gef.EditPart contentEditPart) {
		if (m_contentEditPart != null) {
			// remove content
			removeChild(m_contentEditPart);
			// clear all layers
			for (Layer layer : m_rootFigure.getLayers()) {
				layer.removeAll();
			}
		}
		//
		m_contentEditPart = (EditPart) contentEditPart;
		//
		if (m_contentEditPart != null) {
			addChild(m_contentEditPart, -1);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DragTracking
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Tool getDragTracker(Request request) {
		return new MarqueeSelectionTool();
	}

	@Override
	public IFigure getLayer(Object key) {
		if (key instanceof String name) {
			return m_rootFigure.getLayer(name);
		}
		return null;
	}
}