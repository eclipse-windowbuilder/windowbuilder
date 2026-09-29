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
package org.eclipse.wb.internal.gef.graphical;

import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.DesignEditPart;
import org.eclipse.wb.internal.draw2d.RootFigure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.tools.MarqueeDragTracker;

/**
 * A {@link RootEditPart} is the <i>root</i> of an {@link IEditPartViewer}. It bridges the gap
 * between the {@link IEditPartViewer} and its contents. It does not correspond to anything in the
 * model, and typically can not be interacted with by the User. The Root provides a homogeneous
 * context for the applications "real" EditParts.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class RootEditPart extends DesignEditPart implements org.eclipse.gef.RootEditPart, LayerManager {
	private IEditPartViewer m_viewer;
	private EditPart m_contentEditPart;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RootEditPart() {
		createLayers();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Layer's
	//
	////////////////////////////////////////////////////////////////////////////
	private void createLayers() {
		getFigure().add(new Layer(), IEditPartViewer.PRIMARY_LAYER_SUB_1);
		getFigure().add(new Layer(), LayerConstants.PRIMARY_LAYER);
		getFigure().add(new Layer(), IEditPartViewer.HANDLE_LAYER_SUB_1);
		getFigure().add(new Layer(), IEditPartViewer.HANDLE_LAYER_SUB_2);
		getFigure().add(new Layer(), LayerConstants.HANDLE_LAYER);
		getFigure().add(new Layer(), IEditPartViewer.HANDLE_LAYER_STATIC);
		getFigure().add(new Layer(), IEditPartViewer.FEEDBACK_LAYER_SUB_1);
		getFigure().add(new Layer(), IEditPartViewer.FEEDBACK_LAYER_SUB_2);
		getFigure().add(new Layer(), LayerConstants.FEEDBACK_LAYER);
		getFigure().add(new Layer(), IEditPartViewer.FEEDBACK_LAYER_ABV_1);
		getFigure().add(new Layer(), IEditPartViewer.CLICKABLE_LAYER);
		getFigure().add(new Layer(), IEditPartViewer.MENU_PRIMARY_LAYER);
		getFigure().add(new Layer(), IEditPartViewer.MENU_HANDLE_LAYER);
		getFigure().add(new Layer(), IEditPartViewer.MENU_HANDLE_LAYER_STATIC);
		getFigure().add(new Layer(), IEditPartViewer.MENU_FEEDBACK_LAYER);
		getFigure().add(new Layer(), IEditPartViewer.TOP_LAYER);
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
		return getFigure().getLayer(LayerConstants.PRIMARY_LAYER);
	}

	@Override
	protected IFigure createFigure() {
		return new RootFigure();
	}

	@Override
	public RootFigure getFigure() {
		return (RootFigure) super.getFigure();
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
			for (Layer layer : getFigure().getLayers()) {
				layer.removeAll();
			}
		}
		//
		m_contentEditPart = contentEditPart;
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
	public DragTracker getDragTracker(Request request) {
		return new MarqueeDragTracker();
	}

	@Override
	public IFigure getLayer(Object key) {
		if (key instanceof String name) {
			return getFigure().getLayer(name);
		}
		return null;
	}
}