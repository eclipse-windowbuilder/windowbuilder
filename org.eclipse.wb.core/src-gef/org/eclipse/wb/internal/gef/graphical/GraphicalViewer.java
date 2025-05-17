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

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.draw2d.IRootFigure;
import org.eclipse.wb.internal.draw2d.RootFigure;
import org.eclipse.wb.internal.draw2d.TargetFigureFindVisitor;
import org.eclipse.wb.internal.gef.core.AbstractEditPartViewer;
import org.eclipse.wb.internal.gef.core.EditDomain;
import org.eclipse.wb.internal.gef.core.TargetEditPartFindVisitor;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;

import java.util.Collection;
import java.util.Objects;

/**
 * @author lobas_av
 * @coverage gef.graphical
 */
public class GraphicalViewer extends AbstractEditPartViewer {
	protected final FigureCanvas m_canvas;
	private final RootEditPart m_rootEditPart;
	private EditEventManager m_eventManager;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public GraphicalViewer(Composite parent) {
		this(parent, SWT.H_SCROLL | SWT.V_SCROLL);
	}

	public GraphicalViewer(Composite parent, int style) {
		this(new FigureCanvas(parent, style) {
			@Override
			protected void setDefaultEventManager() {
				/*
				 * EventManager set during invoke setEditDomain() because before
				 * FigureCanvas not create fully and not set EditDomain.
				 */
			}
		});
	}

	protected GraphicalViewer(FigureCanvas canvas) {
		m_canvas = canvas;
		m_rootEditPart = new RootEditPart(this, getRootFigure());
		m_rootEditPart.activate();
		setRootEditPart(m_rootEditPart);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the SWT <code>Control</code> for this viewer.
	 */
	@Override
	public FigureCanvas getControl() {
		return m_canvas;
	}

	/**
	 * @return viewer horizontal scroll offset.
	 */
	@Override
	public int getHOffset() {
		return m_canvas.getViewport().getHorizontalRangeModel().getValue();
	}

	/**
	 * @return viewer vertical scroll offset.
	 */
	@Override
	public int getVOffset() {
		return m_canvas.getViewport().getVerticalRangeModel().getValue();
	}

	/**
	 * Returns root {@link EditPart}.
	 */
	@Override
	public RootEditPart getRootEditPart() {
		return m_rootEditPart;
	}

	/**
	 * Returns root {@link Figure} use for access to {@link Layer}'s.
	 */
	@Override
	public final IRootFigure getRootFigure() {
		return getRootFigureInternal();
	}

	/**
	 * Internal access to original, internal root figure.
	 */
	protected final RootFigure getRootFigureInternal() {
		return m_canvas.getRootFigure();
	}

	/**
	 * Returns the layer identified by the <code>name</code> given in the input.
	 */
	@Override
	public Layer getLayer(String name) {
		return getRootFigure().getLayer(name);
	}

	/**
	 * Sets the <code>{@link EditDomain}</code> for this viewer. The Viewer will route all mouse and
	 * keyboard events to the {@link EditDomain}.
	 */
	@Override
	public void setEditDomain(EditDomain domain) {
		super.setEditDomain(domain);
		m_eventManager = new EditEventManager(m_canvas, domain, this);
		getRootFigureInternal().getFigureCanvas().getLightweightSystem().setEventDispatcher(m_eventManager);
	}

	/**
	 * Set the Cursor.
	 */
	@Override
	public void setCursor(Cursor cursor) {
		m_eventManager.setOverrideCursor(cursor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Finding
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns <code>null</code> or the <code>{@link EditPart}</code> at the specified location on
	 * primary layers, using the given exclusion set and conditional.
	 */
	@Override
	public EditPart findTargetEditPart(int x,
			int y,
			final Collection<? extends org.eclipse.gef.EditPart> exclude,
			final Conditional conditional) {
		EditPart editPart = findTargetEditPart(x, y, exclude, conditional, MENU_PRIMARY_LAYER);
		if (editPart == null) {
			editPart = findTargetEditPart(x, y, exclude, conditional, PRIMARY_LAYER);
		}
		return editPart;
	}

	/**
	 * Returns <code>null</code> or the <code>{@link EditPart}</code> at the specified location on
	 * specified given layer, using the given exclusion set and conditional.
	 */
	@Override
	public EditPart findTargetEditPart(int x,
			int y,
			final Collection<? extends org.eclipse.gef.EditPart> exclude,
			final Conditional conditional,
			String layer) {
		TargetEditPartFindVisitor visitor = new TargetEditPartFindVisitor(m_canvas, x, y, this) {
			@Override
			protected boolean acceptVisit(Figure figure) {
				for (org.eclipse.gef.EditPart editPart : exclude) {
					GraphicalEditPart graphicalPart = (GraphicalEditPart) editPart;
					if (Objects.equals(figure, graphicalPart.getFigure())) {
						return false;
					}
				}
				return true;
			}

			@Override
			protected boolean acceptResult(Figure figure) {
				EditPart editPart = extractEditPart(figure);
				return editPart != null && (conditional == null || conditional.evaluate(editPart));
			}
		};
		getLayer(layer).accept(visitor, false);
		return visitor.getTargetEditPart();
	}

	/**
	 * @return the <code>{@link Handle}</code> at the specified location.
	 */
	@Override
	public Handle findTargetHandle(Point location) {
		return findTargetHandle(location.x, location.y);
	}

	/**
	 * Returns the <code>{@link Handle}</code> at the specified location <code>(x, y)</code>. Returns
	 * <code>null</code> if no handle exists at the given location <code>(x, y)</code>.
	 */
	@Override
	public Handle findTargetHandle(int x, int y) {
		Handle target;
		if ((target = findTargetHandle(MENU_HANDLE_LAYER_STATIC, x, y)) != null) {
			return target;
		}
		if ((target = findTargetHandle(MENU_HANDLE_LAYER, x, y)) != null) {
			return target;
		}
		if ((target = findTargetHandle(HANDLE_LAYER_STATIC, x, y)) != null) {
			return target;
		}
		if ((target = findTargetHandle(HANDLE_LAYER, x, y)) != null) {
			return target;
		}
		return null;
	}

	/**
	 * Returns the <code>{@link Handle}</code> at the specified location <code>(x, y)</code> and
	 * location in given <code>layer</code>. Returns <code>null</code> if no handle exists at the
	 * given location <code>(x, y)</code>.
	 */
	private Handle findTargetHandle(String layer, int x, int y) {
		TargetFigureFindVisitor visitor = new TargetFigureFindVisitor(m_canvas, x, y);
		getLayer(layer).accept(visitor, false);
		Figure targetFigure = visitor.getTargetFigure();
		return targetFigure instanceof Handle ? (Handle) targetFigure : null;
	}
}