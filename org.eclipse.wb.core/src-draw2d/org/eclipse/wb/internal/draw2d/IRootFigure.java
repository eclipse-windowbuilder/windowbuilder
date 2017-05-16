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

import org.eclipse.wb.draw2d.Layer;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import java.util.List;

/**
 * Representation main draw2d figure, contains more layers. Layer representation simple object
 * (figure) container.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public interface IRootFigure {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Layer's
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds the given layer as a child of this {@link IRootFigure}.
   */
  void addLayer(Layer layer);

  /**
   * Adds the given layer as a child of this {@link IRootFigure} with given index.
   */
  void addLayer(Layer layer, int index);

  /**
   * Returns the layer identified by the <code>name</code> given in the input.
   */
  Layer getLayer(String name);

  /**
   * Return all layer's from this {@link IRootFigure}.
   */
  List<Layer> getLayers();

  /**
   * Removes the layer identified by the given key from this {@link IRootFigure}.
   */
  void removeLayer(String name);

  /**
   * Removes the given layer from this {@link IRootFigure}.
   */
  void removeLayer(Layer layer);

  /**
   * Remove all layer's from this {@link IRootFigure}.
   */
  void removeAll();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the background Color of this Figure.
   */
  Color getBackground();

  /**
   * Sets the background color.
   */
  void setBackground(Color background);

  /**
   * Returns the local foreground Color of this Figure.
   */
  Color getForeground();

  /**
   * Sets the foreground color.
   */
  void setForeground(Color foreground);

  /**
   * Returns the current Font by reference.
   */
  Font getFont();

  /**
   * Sets the font.
   */
  void setFont(Font font);
}