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
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.draw2d.IRootFigure;
import org.eclipse.wb.internal.draw2d.RootFigure;
import org.eclipse.wb.internal.gef.core.AbstractEditPartViewer;
import org.eclipse.wb.internal.gef.core.EditDomain;

import org.eclipse.draw2d.ExclusionSearch;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.TreeSearch;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.LayerConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;

import java.util.Collection;

/**
 * @author lobas_av
 * @coverage gef.graphical
 */
public class GraphicalViewer extends AbstractEditPartViewer implements org.eclipse.gef.GraphicalViewer {
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
		m_rootEditPart = new RootEditPart(getRootFigure());
		m_rootEditPart.setViewer(this);
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
	 * Returns root {@link IFigure} use for access to {@link Layer}'s.
	 */
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
	 * Sets the <code>{@link EditDomain}</code> for this viewer. The Viewer will route all mouse and
	 * keyboard events to the {@link EditDomain}.
	 */
	@Override
	public void setEditDomain(EditDomain domain) {
		super.setEditDomain(domain);
		m_eventManager = new EditEventManager(m_canvas, domain, this);
		m_canvas.getLightweightSystem().setEventDispatcher(m_eventManager);
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
	public EditPart findObjectAtExcluding(Point location,
			// TODO Draw2D - Typify once lower bound is 3.22
			@SuppressWarnings("rawtypes") final Collection exclude,
			final Conditional conditional) {
		EditPart editPart = findObjectAtExcluding(location, exclude, conditional, MENU_PRIMARY_LAYER);
		if (editPart == null) {
			editPart = findObjectAtExcluding(location, exclude, conditional, PRIMARY_LAYER);
		}
		return editPart;
	}

	/**
	 * Returns <code>null</code> or the <code>{@link EditPart}</code> at the specified location on
	 * specified given layer, using the given exclusion set and conditional.
	 */
	@Override
	public EditPart findObjectAtExcluding(Point location,
			final Collection<IFigure> exclude,
			final Conditional conditional,
			String layer) {
		class ConditionalTreeSearch extends ExclusionSearch {
			ConditionalTreeSearch(Collection<IFigure> coll) {
				super(coll);
			}

			@Override
			public boolean accept(IFigure figure) {
				EditPart editpart = null;
				while (editpart == null && figure != null) {
					editpart = getVisualPartMap().get(figure);
					figure = figure.getParent();
				}
				return editpart != null && (conditional == null || conditional.evaluate(editpart));
			}

			@Override
			public boolean prune(IFigure figure) {
				if (figure instanceof Layer layerFigure) {
					return !layer.equals(layerFigure.getName());
				}
				return super.prune(figure);
			}
		}
		IFigure figure = m_canvas.getLightweightSystem().getRootFigure().findFigureAt(location.x, location.y,
				new ConditionalTreeSearch(exclude));
		EditPart editPart = null;
		while (editPart == null && figure != null) {
			editPart = getVisualPartMap().get(figure);
			figure = figure.getParent();
		}
		return editPart;
	}

	/**
	 * Returns the <code>{@link Handle}</code> at the specified location <code>(x, y)</code>. Returns
	 * <code>null</code> if no handle exists at the given location <code>(x, y)</code>.
	 */
	@Override
	public Handle findHandleAt(Point p) {
		Handle target;
		if ((target = findTargetHandle(MENU_HANDLE_LAYER_STATIC, p)) != null) {
			return target;
		}
		if ((target = findTargetHandle(MENU_HANDLE_LAYER, p)) != null) {
			return target;
		}
		if ((target = findTargetHandle(HANDLE_LAYER_STATIC, p)) != null) {
			return target;
		}
		if ((target = findTargetHandle(LayerConstants.HANDLE_LAYER, p)) != null) {
			return target;
		}
		return null;
	}

	/**
	 * Returns the <code>{@link Handle}</code> at the specified location <code>(x, y)</code> and
	 * location in given <code>layer</code>. Returns <code>null</code> if no handle exists at the
	 * given location <code>(x, y)</code>.
	 */
	private Handle findTargetHandle(String layer, Point p) {
		return (Handle) m_canvas.getLightweightSystem().getRootFigure().findFigureAt(p.x, p.y,
				new TreeSearch() {
			@Override
			public boolean accept(IFigure figure) {
				return figure instanceof Handle;
			}

			@Override
			public boolean prune(IFigure figure) {
				return figure instanceof Layer layerFigure && !layer.equals(layerFigure.getName());
			}
		});
	}
}