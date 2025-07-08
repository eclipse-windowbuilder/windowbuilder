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

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.border.LineBorder;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

/**
 * Feedback with opaque {@link IFigure}.
 *
 * @author scheglov_ke
 * @coverage core.gef.figure
 */
public final class SolidPositionFeedback extends AbstractPositionFeedback {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SolidPositionFeedback(Layer layer, Rectangle bounds, String hint) {
		super(layer, bounds, hint);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure methods
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IFigure createFigure() {
		IFigure figure = new Figure();
		figure.setOpaque(true);
		figure.setBorder(new LineBorder(getBorderColor()));
		return figure;
	}

	@Override
	public void update(boolean contains) {
		if (contains) {
			m_figure.setBackgroundColor(getActiveColor());
		} else {
			m_figure.setBackgroundColor(getInactiveColor());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Colors
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the border {@link Color}.
	 */
	private Color getBorderColor() {
		return ColorConstants.darkGreen;
		//return ColorConstants.orange;
	}

	/**
	 * @return the inactivate {@link Color}.
	 */
	private Color getInactiveColor() {
		//return SWTResourceManager.getColor(0x64, 0x95, 0xED);
		return ColorConstants.lightGreen;
	}

	/**
	 * @return the activate {@link Color}.
	 */
	private Color getActiveColor() {
		//return SWTResourceManager.getColor(0x1E, 0xB0, 0xFF);
		return ColorConstants.yellow;
	}
}