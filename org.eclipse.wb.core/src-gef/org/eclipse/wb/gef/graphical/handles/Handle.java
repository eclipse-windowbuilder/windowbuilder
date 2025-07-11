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
package org.eclipse.wb.gef.graphical.handles;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

import org.eclipse.draw2d.AncestorListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.geometry.Point;

/**
 * {@link Handle} will add an {@link IAncestorListener} to the owner's figure, and will
 * automatically revalidate this handle whenever the owner's figure moves.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public abstract class Handle extends Figure implements AncestorListener, org.eclipse.gef.Handle {
	private final GraphicalEditPart m_owner;
	private final Locator m_locator;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a handle for the given <code>{@link GraphicalEditPart}</code> using the given
	 * <code>{@link Locator}</code>.
	 */
	public Handle(GraphicalEditPart owner, Locator locator) {
		m_owner = owner;
		m_locator = locator;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addNotify() {
		super.addNotify();
		getOwnerFigure().addAncestorListener(this);
		revalidate();
	}

	@Override
	public void removeNotify() {
		getOwnerFigure().removeAncestorListener(this);
		super.removeNotify();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IAncestorListener
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void ancestorMoved(IFigure ancestor) {
		revalidate();
	}

	@Override
	public void ancestorAdded(IFigure ancestor) {
		// unsupported
	}

	@Override
	public void ancestorRemoved(IFigure ancestor) {
		// unsupported
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Revalidate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void revalidate() {
		getLocator().relocate(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the <code>{@link GraphicalEditPart}</code> associated with this handle.
	 */
	protected final GraphicalEditPart getOwner() {
		return m_owner;
	}

	/**
	 * Convenience method to return the owner's figure.
	 */
	protected final IFigure getOwnerFigure() {
		return getOwner().getFigure();
	}

	/**
	 * Returns the <code>{@link Locator}</code> used to position this handle.
	 */
	protected final Locator getLocator() {
		return m_locator;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DragTracker
	//
	////////////////////////////////////////////////////////////////////////////
	private Tool m_dragTracker;

	/**
	 * Returns the drag tracker {@link Tool} to use when the user clicks on this handle. If the drag
	 * tracker has not been set, it will be lazily created by calling {@link #createDragTracker()}.
	 */
	@Override
	public Tool getDragTracker() {
		if (m_dragTracker == null) {
			m_dragTracker = createDragTrackerTool();
		}
		return m_dragTracker;
	}

	/**
	 * Sets the drag tracker {@link Tool} for this handle.
	 */
	public void setDragTracker(Tool dragTracker) {
		m_dragTracker = dragTracker;
	}

	/**
	 * Creates a new drag tracker {@link Tool} to be returned by {@link #getDragTracker()}.
	 */
	protected final Tool createDragTrackerTool() {
		return null;
	}

	/**
	 * By default, the center of the handle is returned.
	 *
	 * @see org.eclipse.gef.Handle#getAccessibleLocation()
	 */
	@Override
	public Point getAccessibleLocation() {
		Point p = getBounds().getCenter();
		translateToAbsolute(p);
		return p;
	}

	@Override
	public String toString() {
		return "[%s] %s".formatted(getClass().getSimpleName(), getBounds());
	}
}