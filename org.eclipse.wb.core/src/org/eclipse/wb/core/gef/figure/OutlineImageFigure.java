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
package org.eclipse.wb.core.gef.figure;

import org.eclipse.wb.draw2d.Figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * Figure which draws associated {@link Image} and outlines it with border.
 *
 * @author mitin_aa
 * @coverage core.gef.policy
 */
public final class OutlineImageFigure extends Figure {
	private final Image m_image;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public OutlineImageFigure() {
		this(null);
	}

	public OutlineImageFigure(Image image) {
		this(image, ColorConstants.orange);
	}

	public OutlineImageFigure(Image image, Color borderColor) {
		this(image, borderColor, null);
	}

	public OutlineImageFigure(Image image, Color borderColor, Rectangle bounds) {
		m_image = image;
		setForegroundColor(borderColor);
		if (bounds != null) {
			setBounds(bounds);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Paint
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void paintClientArea(Graphics graphics) {
		if (m_image != null) {
			graphics.drawImage(m_image, 0, 0);
		}
		graphics.drawRectangle(getClientArea().getResized(-1, -1));
	}
}