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
package org.eclipse.wb.internal.swing.FormLayout.gef.header.selection;

import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.swing.FormLayout.gef.GefMessages;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.CompoundBorder;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.graphics.Font;

import java.text.MessageFormat;

/**
 * {@link IFigure} for displaying {@link FormLayoutInfo} header resize.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.header
 */
public final class ResizeHintFigure extends Figure {
	private String m_text;
	private String m_sizeHint;
	private boolean m_showSizeHint = true;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ResizeHintFigure() {
		setOpaque(true);
		setBackgroundColor(ColorConstants.tooltipBackground);
		setForegroundColor(ColorConstants.tooltipForeground);
		setBorder(new CompoundBorder(new LineBorder(ColorConstants.tooltipForeground), new MarginBorder(2)));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the first line text.
	 */
	public void setText(String text) {
		m_text = text;
		update();
	}

	/**
	 * Sets the second line (hint) text.
	 */
	public void setSizeHint(String hint) {
		m_sizeHint = hint;
		update();
	}

	/**
	 * Sets flag if size hint should be displayed.
	 */
	public void setShowSizeHint(boolean showSizeHint) {
		m_showSizeHint = showSizeHint;
		update();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Label
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the desirable size for this {@link ResizeHintFigure}.
	 */
	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		int width = 0;
		int height = 0;
		// text
		{
			Dimension size = FigureUtilities.getTextExtents(m_text, getFont());
			width = Math.max(width, size.width);
			height += size.height;
		}
		// hint
		if (m_showSizeHint) {
			int hintWidth = 0;
			Font boldFont = DrawUtils.getBoldFont(getFont());
			try {
				hintWidth += FigureUtilities.getTextExtents(GefMessages.ResizeHintFigure_hint, boldFont).width;
				hintWidth += FigureUtilities.getTextExtents(GefMessages.ResizeHintFigure_press, getFont()).width;
				hintWidth += FigureUtilities.getTextExtents("Ctrl", boldFont).width;
				hintWidth += FigureUtilities.getTextExtents(MessageFormat.format(GefMessages.ResizeHintFigure_toSetSize, m_sizeHint), getFont()).width;
			} finally {
				boldFont.dispose();
			}
			// update size
			width = Math.max(width, hintWidth);
			height *= 2;
		}
		// full size
		Insets insets = getInsets();
		return new Dimension(width + insets.getWidth(), height + insets.getHeight());
	}

	/**
	 * Updates the size and forces repaint.
	 */
	private void update() {
		setSize(getPreferredSize());
		repaint();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Paint
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void paintFigure(Graphics graphics) {
		super.paintFigure(graphics);
		graphics.translate(getLocation().x + getInsets().left, getLocation().y + getInsets().top);
		graphics.drawText(m_text, 0, 0);
		// draw hint
		if (m_showSizeHint) {
			int y = getClientArea().height / 2;
			Font boldFont = DrawUtils.getBoldFont(getFont());
			try {
				int x = 0;
				x = drawText(graphics, x, y + 1, GefMessages.ResizeHintFigure_hint, boldFont);
				x = drawText(graphics, x, y, GefMessages.ResizeHintFigure_press, getFont());
				//
				graphics.pushState();
				graphics.setForegroundColor(ColorConstants.lightBlue);
				x = drawText(graphics, x, y + 1, "Ctrl", boldFont);
				graphics.popState();
				//
				x = drawText(graphics, x, y, MessageFormat.format(GefMessages.ResizeHintFigure_toSetSize, m_sizeHint), getFont());
			} finally {
				boldFont.dispose();
			}
		}
	}

	/**
	 * Draws text at given location.
	 *
	 * @return the new <code>x</code>.
	 */
	private static int drawText(Graphics graphics, int x, int y, String text, Font font) {
		graphics.setFont(font);
		graphics.drawText(text, x, y);
		return x + FigureUtilities.getTextExtents(text, font).width;
	}
}
