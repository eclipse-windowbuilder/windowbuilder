/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.core.gef.policy.layout.absolute;

import org.eclipse.wb.draw2d.Figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * Figure to draw half-arc at the end of the drawn line. The default figure size is (7, 7).
 *
 * @author mitin_aa
 * @coverage core.gef.policy
 */
public class LineEndFigure extends Figure {
	public static final int RADIUS = 3;
	private static final int FIGURE_SIZE = RADIUS * 2 + 1;
	private final Color m_color;
	private int m_startAngle;
	private int m_lengthAngle;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LineEndFigure(int alignment, Color color) {
		m_color = color;
		if (alignment == PositionConstants.LEFT || alignment == PositionConstants.RIGHT) {
			m_startAngle = 90;
		} else if (alignment == PositionConstants.TOP || alignment == PositionConstants.BOTTOM) {
			m_startAngle = 0;
		}
		if (alignment == PositionConstants.LEFT || alignment == PositionConstants.TOP) {
			m_lengthAngle = -180;
		} else if (alignment == PositionConstants.RIGHT || alignment == PositionConstants.BOTTOM) {
			m_lengthAngle = 180;
		}
		setSize(FIGURE_SIZE, FIGURE_SIZE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Paint
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void paintClientArea(Graphics graphics) {
		int oldAntialias = graphics.getAntialias();
		try {
			graphics.setAntialias(SWT.ON);
			graphics.setForegroundColor(m_color);
			graphics.setBackgroundColor(m_color);
			graphics.setLineStyle(SWT.LINE_SOLID);
			Rectangle clientArea = getClientArea();
			graphics.fillArc(0, 0, clientArea.width, clientArea.height, m_startAngle, m_lengthAngle);
		} finally {
			graphics.setAntialias(oldAntialias);
		}
	}
}