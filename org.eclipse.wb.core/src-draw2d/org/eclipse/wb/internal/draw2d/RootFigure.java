/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.draw2d;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;

import java.util.List;
import java.util.Map;

/**
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class RootFigure extends Figure implements IRootFigure {
  private final FigureCanvas m_figureCanvas;
  private final RefreshManager m_refreshManager;
  private EventManager m_eventManager;
  private Dimension m_preferredSize;
  private Map<String, Layer> m_nameToLayer = Maps.newHashMap();
  private IPreferredSizeProvider m_preferredSizeProvider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RootFigure(FigureCanvas figureCanvas, RefreshManager refreshManager) {
    m_figureCanvas = figureCanvas;
    m_refreshManager = refreshManager;
    setOpaque(true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets events handler.
   */
  public void setEventManager(EventManager eventManager) {
    m_eventManager = eventManager;
  }

  /**
   * Sets figure preferred size provider.
   */
  public void setPreferredSizeProvider(IPreferredSizeProvider provider) {
    m_preferredSizeProvider = provider;
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

  /**
   * Returns the desirable size for this container figure.
   */
  public Dimension getPreferredSize() {
    // check preferred size
    if (m_preferredSize == null) {
      // calculate preferred size
      Rectangle preferred = new Rectangle();
      // layer's loop
      for (Layer layer : getLayers()) {
        // figure's loop
        for (Figure figure : layer.getChildren()) {
          if (figure.isVisible()) {
            if (figure instanceof IPreferredSizeProvider) {
              IPreferredSizeProvider provider = (IPreferredSizeProvider) figure;
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
  protected void repaint(boolean reset, int x, int y, int width, int height) {
    if (reset) {
      m_preferredSize = null;
    }
    m_refreshManager.refreshRequest(x, y, width, height);
  }

  /**
   * Updates the cursor over {@link EventManager}.
   */
  @Override
  protected void updateCursor() {
    m_eventManager.updateCursor();
  }

  /**
   * Sets capture figure over {@link EventManager}.
   */
  @Override
  public void setCapture(Figure figure) {
    m_eventManager.setCapture(figure);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layer's
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds the given layer as a child of this {@link IRootFigure}.
   */
  public void addLayer(Layer layer) {
    m_nameToLayer.put(layer.getName(), layer);
    add(layer, null, -1);
  }

  /**
   * Adds the given layer as a child of this {@link IRootFigure} with given index.
   */
  public void addLayer(Layer layer, int index) {
    m_nameToLayer.put(layer.getName(), layer);
    add(layer, null, index);
  }

  /**
   * Returns the layer identified by the <code>name</code> given in the input.
   */
  public Layer getLayer(String name) {
    return m_nameToLayer.get(name);
  }

  /**
   * Return all layer's from this {@link IRootFigure}.
   */
  public List<Layer> getLayers() {
    List<Layer> layers = Lists.newArrayList();
    for (Figure childFigure : getChildren()) {
      layers.add((Layer) childFigure);
    }
    return layers;
  }

  /**
   * Removes the layer identified by the given key from this {@link IRootFigure}.
   */
  public void removeLayer(String name) {
    removeLayer(getLayer(name));
  }

  /**
   * Removes the given layer from this {@link IRootFigure}.
   */
  public void removeLayer(Layer layer) {
    m_nameToLayer.remove(layer.getName());
    remove(layer);
  }

  /**
   * Remove all layer's from this {@link IRootFigure}.
   */
  @Override
  public void removeAll() {
    m_nameToLayer = Maps.newHashMap();
    super.removeAll();
  }
}