/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.draw2d.border;

import org.eclipse.wb.draw2d.Figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A {@link Border} is a graphical decoration that is painted just inside the outer edge of a
 * {@link Figure}.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public abstract class Border implements org.eclipse.draw2d.Border {
	private final Insets m_insets;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor {@link Border} with border insets.
	 */
	public Border(Insets insets) {
		m_insets = insets;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Border
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Get border insets.
	 */
	@Override
	public Insets getInsets(IFigure figure) {
		return m_insets;
	}

	@Override
	public Dimension getPreferredSize(IFigure figure) {
		return figure.getPreferredSize();
	}

	@Override
	public boolean isOpaque() {
		return false;
	}

	/**
	 * Paint border for <code>owner</code> {@link Figure}.
	 */
	@Override
	public final void paint(IFigure owner, Graphics graphics, Insets insets) {
		Rectangle bounds = owner.getBounds();
		paint(bounds.width, bounds.height, graphics);
	}

	/**
	 * Paint border for {@link Figure}. Coordinate (0, 0) correspond with {@link Figure} (0, 0) and
	 * <code>onwerWidth</code>, <code>ownerHeight</code> correspond with {@link Figure}
	 * <code>width</code>, <code>height</code>.
	 */
	protected abstract void paint(int ownerWidth, int ownerHeight, Graphics graphics);
}