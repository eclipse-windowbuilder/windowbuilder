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
package org.eclipse.wb.gef.graphical;

import org.eclipse.wb.gef.core.tools.DragEditPartTracker;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.draw2d.EventListenerList;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.NodeListener;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import java.util.Collections;
import java.util.List;

/**
 * @author lobas_av
 * @coverage gef.graphical
 * @deprecated Extend {@link AbstractGraphicalEditPart} directly or cast to
 *             {@link org.eclipse.gef.GraphicalEditPart GraphicalEditPart}.
 */
@SuppressWarnings("removal")
@Deprecated(since = "2026-06", forRemoval = true)
public abstract class GraphicalEditPart extends org.eclipse.wb.gef.core.EditPart implements org.eclipse.gef.GraphicalEditPart {
	private IFigure m_figure;

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * The default implementation calls {@link #createFigure()} if the figure is currently
	 * <code>null</code>.
	 */
	public IFigure getFigure() {
		if (m_figure == null) {
			m_figure = createFigure();
		}
		return m_figure;
	}

	/**
	 * The {@link IFigure} into which childrens' {@link IFigure}s will be added.
	 */
	public IFigure getContentPane() {
		return getFigure();
	}

	/**
	 * Creates the <code>{@link IFigure}</code> to be used as this part's <i>visuals</i>. This is
	 * called from {@link #getFigure()} if the figure has not been created.
	 */
	protected abstract IFigure createFigure();

	////////////////////////////////////////////////////////////////////////////
	//
	// EditPart
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds the child's {@link IFigure} to the {@link #getContentPane() contentPane}.
	 */
	@Override
	protected void addChildVisual(org.eclipse.gef.EditPart childPart, int index) {
		GraphicalEditPart graphicalChildPart = (GraphicalEditPart) childPart;
		if (!graphicalChildPart.addSelfVisual(index)) {
			getContentPane().add(graphicalChildPart.getFigure(), index);
		}
		EditPartViewer graphicalChildViewer = graphicalChildPart.getViewer();
		IFigure graphicalChildFigure = graphicalChildPart.getFigure();
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
	 * Remove the child's {@link IFigure} to the {@link #getContentPane() contentPane}.
	 */
	@Override
	protected void removeChildVisual(org.eclipse.gef.EditPart childPart) {
		GraphicalEditPart graphicalChildPart = (GraphicalEditPart) childPart;
		if (!graphicalChildPart.removeSelfVisual()) {
			getContentPane().remove(graphicalChildPart.getFigure());
		}
		EditPartViewer graphicalChildViewer = graphicalChildPart.getViewer();
		IFigure graphicalChildFigure = graphicalChildPart.getFigure();
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

	@Override
	@SuppressWarnings("unchecked")
	public List<? extends GraphicalEditPart> getChildren() {
		return (List<? extends GraphicalEditPart>) super.getChildren();
	}

	@Override
	public void addNodeListener(NodeListener listener) {
		getEventListenerList().addListener(NodeListener.class, listener);
	}

	@Override
	public void removeNodeListener(NodeListener listener) {
		getEventListenerList().removeListener(NodeListener.class, listener);
	}

	private EventListenerList getEventListenerList() {
		return (EventListenerList) ReflectionUtils.getFieldObject(this, "eventListeners");
	}

	@Override
	public List<? extends ConnectionEditPart> getSourceConnections() {
		return Collections.emptyList();
	}

	@Override
	public List<? extends ConnectionEditPart> getTargetConnections() {
		return Collections.emptyList();
	}

	@Override
	public void setLayoutConstraint(EditPart child, IFigure figure, Object constraint) {
		figure.getParent().setConstraint(figure, constraint);
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