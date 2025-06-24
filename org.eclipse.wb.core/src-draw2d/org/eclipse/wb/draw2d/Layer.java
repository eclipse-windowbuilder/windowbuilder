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
package org.eclipse.wb.draw2d;

import org.eclipse.wb.internal.draw2d.IRootFigure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A transparent figure simple figure's container using into {@link IRootFigure}.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class Layer extends Figure {
	private final String m_name;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public Layer(String name) {
		m_name = name;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Bounds
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Direct set bounds from {@link IRootFigure} without notification.
	 */
	@Override
	public void setBounds(Rectangle bounds) {
		getBounds().setBounds(bounds);
	}

	/**
	 * If children not contains given point <code>(x, y)</code> then {@link Layer} just as not
	 * contains it.
	 */
	@Override
	public boolean containsPoint(int x, int y) {
		for (IFigure childFigure : getChildren()) {
			if (childFigure.containsPoint(x, y)) {
				return true;
			}
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Return identification name.
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * For this figure opaque is missing.
	 */
	@Override
	public void setOpaque(boolean opaque) {
	}

	@Override
	public String toString() {
		return "[%s] %s".formatted(getClass().getSimpleName(), getName());
	}
}