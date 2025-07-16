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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.wb.draw2d.FigureUtils;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A Label which draws its text vertically.
 *
 * @author mitin_aa
 */
public final class VerticalLabel extends Label {
	private Dimension m_preferredSize;

	@Override
	protected void paintFigure(Graphics graphics) {
		graphics.fillRectangle(getBounds());
		Rectangle r = getClientArea();
		graphics.translate(r.getLocation());
		graphics.translate(r.width, 0);
		graphics.rotate(90);
		graphics.drawText(getText(), 0, 0);
	}

	/**
	 * Returns the desirable size for this label's text.
	 */
	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		if (m_preferredSize == null) {
			m_preferredSize = FigureUtils.calculateTextSize(getText(), getFont());
			Insets insets = getInsets();
			m_preferredSize.expand(insets.getWidth(), insets.getHeight());
			m_preferredSize.transpose();
		}
		return m_preferredSize;
	}

	@Override
	public void invalidate() {
		m_preferredSize = null;
		super.invalidate();
	}
}