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

import org.eclipse.wb.draw2d.Layer;

import org.eclipse.draw2d.IFigure;

import java.util.List;

/**
 * Representation main draw2d figure, contains more layers. Layer representation simple object
 * (figure) container.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public interface IRootFigure extends IFigure {
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
}