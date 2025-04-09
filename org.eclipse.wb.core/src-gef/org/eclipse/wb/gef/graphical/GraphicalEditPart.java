/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.gef.graphical;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.tools.DragEditPartTracker;
import org.eclipse.wb.gef.core.tools.Tool;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;

/**
 * @author lobas_av
 * @coverage gef.graphical
 */
public abstract class GraphicalEditPart extends EditPart {
	private Figure m_figure;

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * The default implementation calls {@link #createFigure()} if the figure is currently
	 * <code>null</code>.
	 */
	public Figure getFigure() {
		if (m_figure == null) {
			m_figure = createFigure();
		}
		return m_figure;
	}

	/**
	 * The {@link Figure} into which childrens' {@link Figure}s will be added.
	 */
	public Figure getContentPane() {
		return getFigure();
	}

	/**
	 * Creates the <code>{@link Figure}</code> to be used as this part's <i>visuals</i>. This is
	 * called from {@link #getFigure()} if the figure has not been created.
	 */
	protected abstract Figure createFigure();

	////////////////////////////////////////////////////////////////////////////
	//
	// EditPart
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds the child's {@link Figure} to the {@link #getContentPane() contentPane}.
	 */
	@Override
	protected void addChildVisual(org.eclipse.gef.EditPart childPart, int index) {
		GraphicalEditPart graphicalChildPart = (GraphicalEditPart) childPart;
		if (!graphicalChildPart.addSelfVisual(index)) {
			getContentPane().add(graphicalChildPart.getFigure(), index);
		}
		EditPartViewer graphicalChildViewer = graphicalChildPart.getViewer();
		Figure graphicalChildFigure = graphicalChildPart.getFigure();
		graphicalChildViewer.getVisualPartMap().put(graphicalChildFigure, childPart);
	}

	/**
	 * Allows the child to manage graphical adding by itself.
	 *
	 * @return <code>true</code> if edit part added it's figure itself.
	 */
	protected boolean addSelfVisual(int index) {
		return false;
	}

	/**
	 * Remove the child's {@link Figure} to the {@link #getContentPane() contentPane}.
	 */
	@Override
	protected void removeChildVisual(org.eclipse.gef.EditPart childPart) {
		GraphicalEditPart graphicalChildPart = (GraphicalEditPart) childPart;
		if (!graphicalChildPart.removeSelfVisual()) {
			getContentPane().remove(graphicalChildPart.getFigure());
		}
		EditPartViewer graphicalChildViewer = graphicalChildPart.getViewer();
		Figure graphicalChildFigure = graphicalChildPart.getFigure();
		graphicalChildViewer.getVisualPartMap().remove(graphicalChildFigure);
	}

	/**
	 * Allows the child to manage graphical removing by itself. In the most cases, edit part which
	 * added itself should remove itself as well.
	 *
	 * @return <code>true</code> if edit part removed it's figure itself.
	 */
	protected boolean removeSelfVisual() {
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DragTracking
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a {@link Tool} for dragging this {@link EditPart}. The SelectionTool is the only
	 * {@link Tool} by default that calls this method. The SelectionTool will use a SelectionRequest
	 * to provide information such as which mouse button is down, and what modifier keys are pressed.
	 */
	@Override
	public Tool getDragTracker(Request request) {
		return new DragEditPartTracker(this);
	}
}