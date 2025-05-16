/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Layer;

import org.eclipse.draw2d.EventDispatcher;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Dimension;
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
	private final FigureCanvas m_figureCanvas;
	private Dimension m_preferredSize;
	private Map<String, Layer> m_nameToLayer = new HashMap<>();
	private IPreferredSizeProvider m_preferredSizeProvider;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RootFigure(FigureCanvas figureCanvas) {
		m_figureCanvas = figureCanvas;
		setOpaque(true);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Sets figure preferred size provider.
	 */
	public void setPreferredSizeProvider(IPreferredSizeProvider provider) {
		m_preferredSizeProvider = provider;
	}

	@Override
	public EventDispatcher internalGetEventDispatcher() {
		return m_figureCanvas.getLightweightSystem().getRootFigure().internalGetEventDispatcher();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// RootFigure
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @noreference @nooverride
	 */
	@Override
	public FigureCanvas getFigureCanvas() {
		return m_figureCanvas;
	}

	@Override
	public UpdateManager getUpdateManager() {
		return getFigureCanvas().getLightweightSystem().getUpdateManager();
	}

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
						if (figure instanceof IPreferredSizeProvider provider) {
							Dimension figurePreferredSize = provider.getPreferredSize(null);
							preferred.union(0, 0, figurePreferredSize.width, figurePreferredSize.height);
						} else {
							preferred.union(figure.getBounds());
						}
					}
				}
			}
			// set preferred size
			if (m_preferredSizeProvider == null) {
				m_preferredSize = preferred.getSize();
			} else {
				m_preferredSize = m_preferredSizeProvider.getPreferredSize(preferred.getSize());
			}
		}
		return m_preferredSize;
	}

	/**
	 * Sets the bounds of this Figure to the Rectangle <i>rect</i>. Bounds set of union FigureCanvas
	 * <code>bounds</code> and preferred size.
	 */
	@Override
	public void setBounds(Rectangle bounds) {
		Rectangle value = getBounds().setBounds(bounds).union(getPreferredSize());
		for (Layer layer : getLayers()) {
			layer.setBounds(value);
		}
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
	public void addLayer(Layer layer) {
		m_nameToLayer.put(layer.getName(), layer);
		add(layer, null, -1);
	}

	/**
	 * Adds the given layer as a child of this {@link IRootFigure} with given index.
	 */
	@Override
	public void addLayer(Layer layer, int index) {
		m_nameToLayer.put(layer.getName(), layer);
		add(layer, null, index);
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
	 * Removes the layer identified by the given key from this {@link IRootFigure}.
	 */
	@Override
	public void removeLayer(String name) {
		removeLayer(getLayer(name));
	}

	/**
	 * Removes the given layer from this {@link IRootFigure}.
	 */
	@Override
	public void removeLayer(Layer layer) {
		m_nameToLayer.remove(layer.getName());
		remove(layer);
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