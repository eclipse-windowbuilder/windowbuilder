/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.FormLayout.gef.header.selection;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.border.CompoundBorder;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.border.MarginBorder;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.swing.FormLayout.gef.GefMessages;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.graphics.Font;

import java.text.MessageFormat;

/**
 * {@link Figure} for displaying {@link FormLayoutInfo} header resize.
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
		setBackground(IColorConstants.tooltipBackground);
		setForeground(IColorConstants.tooltipForeground);
		setBorder(new CompoundBorder(new LineBorder(IColorConstants.tooltipForeground),
				new MarginBorder(2)));
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
	private Dimension getPreferredSize() {
		Font font = getFont();
		int width = 0;
		int height = 0;
		// text
		{
			Dimension size = FigureUtils.calculateTextSize(m_text, font);
			width = Math.max(width, size.width);
			height += size.height;
		}
		// hint
		if (m_showSizeHint) {
			int hintWidth = 0;
			Font boldFont = DrawUtils.getBoldFont(font);
			try {
				hintWidth +=
						FigureUtils.calculateTextSize(GefMessages.ResizeHintFigure_hint, boldFont).width;
				hintWidth += FigureUtils.calculateTextSize(GefMessages.ResizeHintFigure_press, font).width;
				hintWidth += FigureUtils.calculateTextSize("Ctrl", boldFont).width;
				hintWidth +=
						FigureUtils.calculateTextSize(
								MessageFormat.format(GefMessages.ResizeHintFigure_toSetSize, m_sizeHint),
								font).width;
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
	protected void paintClientArea(Graphics graphics) {
		graphics.drawText(m_text, 0, 0);
		// draw hint
		if (m_showSizeHint) {
			int y = getClientArea().height / 2;
			//
			Font font = getFont();
			Font boldFont = DrawUtils.getBoldFont(font);
			try {
				int x = 0;
				x = drawText(graphics, x, y + 1, GefMessages.ResizeHintFigure_hint, boldFont);
				x = drawText(graphics, x, y, GefMessages.ResizeHintFigure_press, font);
				//
				graphics.pushState();
				graphics.setForegroundColor(IColorConstants.lightBlue);
				x = drawText(graphics, x, y + 1, "Ctrl", boldFont);
				graphics.popState();
				//
				x =
						drawText(
								graphics,
								x,
								y,
								MessageFormat.format(GefMessages.ResizeHintFigure_toSetSize, m_sizeHint),
								font);
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
	private int drawText(Graphics graphics, int x, int y, String text, Font font) {
		graphics.setFont(font);
		graphics.drawText(text, x, y);
		return x + FigureUtils.calculateTextSize(text, font).width;
	}
}
