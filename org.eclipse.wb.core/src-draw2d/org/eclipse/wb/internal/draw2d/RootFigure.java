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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class RootFigure extends Figure implements IRootFigure {
	private Dimension m_preferredSize;
	private Map<Object, Layer> m_nameToLayer = new HashMap<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RootFigure() {
		setOpaque(true);
		setLayoutManager(new StackLayout());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// RootFigure
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the desirable size for this container figure.
	 */
	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		// check preferred size
		if (m_preferredSize == null) {
			// calculate preferred size
			Rectangle preferred = new Rectangle();
			// layer's loop
			for (Layer layer : getLayers()) {
				// figure's loop
				for (IFigure figure : layer.getChildren()) {
					if (figure.isVisible()) {
						Point figureLocation = figure.getLocation();
						Dimension figurePreferredSize = figure.getPreferredSize(wHint, hHint);
						preferred.union(figureLocation.x, figureLocation.y, figurePreferredSize.width, figurePreferredSize.height);
					}
				}
			}
			// set preferred size
			m_preferredSize = preferred.getSize();
		}
		return m_preferredSize;
	}

	/**
	 * Sets the bounds of this Figure to the Rectangle <i>rect</i>. Bounds set of union FigureCanvas
	 * <code>bounds</code> and preferred size.
	 */
	@Override
	public void setBounds(Rectangle bounds) {
		getBounds().setBounds(bounds).setSize(Dimension.max(bounds.getSize(), getPreferredSize()));
	}

	/**
	 * Send repaint request for <code>RefreshManager</code>. Adds a dirty region (defined by the
	 * rectangle <i>x, y, w, h</i>) to the update queue. If <code>reset</code> is <code>true</code>
	 * then <code>RootFigure</code> recalculate preferred size and <code>FigureCanvas</code> make
	 * reconfigure scrolling.
	 */
	@Override
	public void repaint(int x, int y, int width, int height) {
		getUpdateManager().addDirtyRegion(this, x, y, width, height);
	}

	@Override
	public void invalidate() {
		m_preferredSize = null;
		super.invalidate();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Layer's
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds the given layer as a child of this {@link IRootFigure}.
	 */
	@Override
	public void add(IFigure figure, Object constraints, int index) {
		if (figure instanceof Layer layerFigure) {
			m_nameToLayer.put(constraints, layerFigure);
		}
		super.add(figure, constraints, index);
	}

	/**
	 * Returns the layer identified by the <code>name</code> given in the input.
	 */
	@Override
	public Layer getLayer(String name) {
		return m_nameToLayer.get(name);
	}

	/**
	 * Return all layer's from this {@link IRootFigure}.
	 */
	@Override
	public List<Layer> getLayers() {
		List<Layer> layers = new ArrayList<>();
		for (IFigure childFigure : getChildren()) {
			layers.add((Layer) childFigure);
		}
		return layers;
	}

	/**
	 * Removes the given layer from this {@link IRootFigure}.
	 */
	@Override
	public void remove(IFigure figure) {
		m_nameToLayer.values().remove(figure);
		super.remove(figure);
	}

	/**
	 * Remove all layer's from this {@link IRootFigure}.
	 */
	@Override
	public void removeAll() {
		m_nameToLayer = new HashMap<>();
		super.removeAll();
	}
}