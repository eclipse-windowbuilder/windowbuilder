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
package org.eclipse.wb.core.gef.figure;

import org.eclipse.wb.draw2d.Layer;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Abstract class for displaying feedback figures on given layer.
 *
 * Usually we need also some hint that describes this position, so we keep it in this class.
 *
 * @author scheglov_ke
 * @coverage core.gef.figure
 */
public abstract class AbstractPositionFeedback {
	private final Layer m_layer;
	private final String m_hint;
	protected final IFigure m_figure;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractPositionFeedback(Layer layer, Rectangle bounds, String hint) {
		m_layer = layer;
		m_hint = hint;
		{
			m_figure = createFigure();
			m_layer.add(m_figure);
			m_figure.setBounds(bounds);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Updates state of feedback if it contains/not mouse cursor.
	 */
	public abstract void update(boolean contains);

	/**
	 * @return the {@link IFigure} for this feedback.
	 */
	protected abstract IFigure createFigure();

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Return some hint that describes this position feedback.
	 */
	public final String getHint() {
		return m_hint;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operations
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Updates state of feedback with mouse cursor in given location.
	 *
	 * @return <code>true</code> feedback contains given location
	 */
	public final boolean update(Point location) {
		boolean contains = m_figure.getBounds().contains(location);
		update(contains);
		return contains;
	}

	/**
	 * Removes feedback.
	 */
	public final void remove() {
		m_layer.remove(m_figure);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Data
	//
	////////////////////////////////////////////////////////////////////////////
	private Object m_data;

	/**
	 * @return the application defined data associated with this object.
	 */
	public final Object getData() {
		return m_data;
	}

	/**
	 * Sets the application defined data associated with this object.
	 */
	public final void setData(Object data) {
		m_data = data;
	}
}