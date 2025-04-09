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
package org.eclipse.wb.draw2d.border;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

/**
 * Provides for a line border with sides of equal widths.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class LineBorder extends Border {
	private final Color m_color;
	private final int m_width;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructs a {@link LineBorder} with the specified color and of the specified width.
	 */
	public LineBorder(Color color, int width) {
		super(new Insets(width));
		m_color = color;
		m_width = width;
	}

	/**
	 * Constructs a default black {@link LineBorder} with a width of one pixel.
	 */
	public LineBorder() {
		this(null, 1);
	}

	/**
	 * Constructs a black {@link LineBorder} with the specified width.
	 */
	public LineBorder(int width) {
		this(null, width);
	}

	/**
	 * Constructs a {@link LineBorder} with the specified color and a width of 1 pixel.
	 */
	public LineBorder(Color color) {
		this(color, 1);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the line color of this border.
	 */
	public Color getColor() {
		return m_color;
	}

	/**
	 * Returns the line width of this border.
	 */
	public int getWidth() {
		return m_width;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Border
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void paint(int ownerWidth, int ownerHeight, Graphics graphics) {
		// prepare border rectangle
		Rectangle paintBorderRectangle = new Rectangle(0, 0, ownerWidth, ownerHeight);
		if (m_width % 2 != 0) {
			paintBorderRectangle.width--;
			paintBorderRectangle.height--;
		}
		paintBorderRectangle.shrink(m_width / 2, m_width / 2);
		// draw border
		graphics.setLineWidth(m_width);
		if (m_color != null) {
			graphics.setForegroundColor(m_color);
		}
		graphics.drawRectangle(paintBorderRectangle);
	}
}