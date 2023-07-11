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
package org.eclipse.wb.internal.swing.MigLayout.gef.header.selection;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.border.CompoundBorder;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.border.MarginBorder;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.swing.MigLayout.gef.GefMessages;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Shell;

/**
 * {@link Figure} for displaying {@link MigLayoutInfo} header resize.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.header
 */
public final class ResizeHintFigure extends Figure {
	private String m_text;

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
				new MarginBorder(5)));
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

	////////////////////////////////////////////////////////////////////////////
	//
	// Paint
	//
	////////////////////////////////////////////////////////////////////////////
	private static final Shell HIDDEN_SHELL = new Shell();

	/**
	 * @return the desirable size for this {@link ResizeHintFigure}.
	 */
	private Dimension getPreferredSize() {
		GC gc = new GC(HIDDEN_SHELL);
		try {
			Graphics graphics = new SWTGraphics(gc);
			Dimension size = doPaint(graphics);
			// full size
			Insets insets = getInsets();
			return size.getExpanded(insets.getWidth(), insets.getHeight());
		} finally {
			gc.dispose();
		}
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
	// Character to operation
	//
	////////////////////////////////////////////////////////////////////////////
	enum SizeElement {
		ALL, MIN, PREF, MAX
	}

	/**
	 * @return the new {@link SizeElement} to change, if given char if size element selection. May be
	 *         <code>null</code>, if given character does not correspond any size element.
	 */
	static SizeElement getNewSizeElement(char c) {
		c = Character.toUpperCase(c);
		if (c == 'N') {
			return SizeElement.MIN;
		} else if (c == 'P') {
			return SizeElement.PREF;
		} else if (c == 'X') {
			return SizeElement.MAX;
		}
		// no change
		return null;
	}

	/**
	 * @return the new size unit, if given char if size unit selection. May be <code>null</code>, if
	 *         given character does not correspond any size unit.
	 */
	static String getNewSizeUnit(char c) {
		c = Character.toUpperCase(c);
		if (c == '0') {
			return "";
		} else if (c == '1') {
			return "px";
		} else if (c == '2') {
			return "%";
		} else if (c == '3') {
			return "lp";
		} else if (c == '4') {
			return "pt";
		} else if (c == '5') {
			return "mm";
		} else if (c == '6') {
			return "cm";
		} else if (c == '7') {
			return "in";
		} else if (c == '8') {
			return "sp";
		}
		// no change
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Paint
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void paintClientArea(Graphics graphics) {
		doPaint(graphics);
	}

	private Dimension doPaint(Graphics graphics) {
		Font font = getFont();
		Font boldFont = DrawUtils.getBoldFont(font);
		// use fonts
		try {
			int y = drawText(graphics, m_text, 0, 0, font).height;
			// Note
			{
				int x = 0;
				Dimension size;
				//
				size = drawText(graphics, GefMessages.ResizeHintFigure_note, x, y + 1, boldFont);
				x += size.width;
				//
				size = drawText(graphics, GefMessages.ResizeHintFigure_keyHint, x, y, font);
				x += size.width;
				y += size.height;
			}
			// size element/unit
			{
				int x = 10;
				Dimension size;
				size =
						drawHintColumn(
								graphics,
								x,
								y,
								boldFont,
								font,
								new String[]{"N", "P", "X"},
								new String[]{
										GefMessages.ResizeHintFigure_minimumSize,
										GefMessages.ResizeHintFigure_preferredSize,
										GefMessages.ResizeHintFigure_maximumSize});
				x += size.width + 10;
				size =
						drawHintColumn(graphics, x, y, boldFont, font, new String[]{
								"0",
								"1",
								"2",
								"3",
								"4",
								"5",
								"6",
								"7",
								"8",}, new String[]{
										GefMessages.ResizeHintFigure_unit_default,
										GefMessages.ResizeHintFigure_unit_pixels,
										GefMessages.ResizeHintFigure_unit_percents,
										GefMessages.ResizeHintFigure_unit_logicalPixels,
										GefMessages.ResizeHintFigure_unit_points,
										GefMessages.ResizeHintFigure_unit_millimeters,
										GefMessages.ResizeHintFigure_unit_centimeters,
										GefMessages.ResizeHintFigure_unit_inches,
										GefMessages.ResizeHintFigure_unit_screenPercents});
				return new Dimension(x + size.width, y + size.height);
			}
		} finally {
			boldFont.dispose();
		}
	}

	/**
	 * Draws text at given location.
	 *
	 * @return the size of text.
	 */
	private static Dimension drawText(Graphics graphics, String text, int x, int y, Font font) {
		graphics.setFont(font);
		graphics.drawText(text, x, y);
		return FigureUtils.calculateTextSize(text, font);
	}

	private static Dimension drawHintColumn(Graphics graphics,
			int x_,
			int y_,
			Font keyFont,
			Font textFont,
			String[] keyArray,
			String[] textArray) {
		int lineWidth = 0;
		int lineHeight = 0;
		//
		graphics.pushState();
		try {
			// prepare width of key column
			int keyWidth = 0;
			{
				for (String key : keyArray) {
					Dimension size = FigureUtils.calculateTextSize(key, keyFont);
					keyWidth = Math.max(keyWidth, size.width);
					lineHeight = size.height;
				}
			}
			// draw key/text pairs
			int y = y_;
			for (int i = 0; i < keyArray.length; i++) {
				int x = x_;
				// key
				{
					String key = keyArray[i];
					graphics.setForegroundColor(IColorConstants.blue);
					drawText(graphics, key, x, y, keyFont);
					x += keyWidth;
				}
				// dash
				{
					String text = " - ";
					graphics.setForegroundColor(IColorConstants.black);
					x += drawText(graphics, text, x, y, textFont).width;
				}
				// text
				{
					String text = textArray[i];
					// optional bold part
					{
						int boldEnd = text.indexOf('|');
						if (boldEnd != -1) {
							String boldText = text.substring(0, boldEnd);
							// draw bold text
							graphics.setForegroundColor(IColorConstants.black);
							x += drawText(graphics, boldText, x, y, keyFont).width;
							// prepare rest of text
							text = text.substring(boldEnd + 1);
						}
					}
					// rest of the text
					graphics.setForegroundColor(IColorConstants.black);
					Dimension size = drawText(graphics, text, x, y, textFont);
					// update line width
					lineWidth = Math.max(lineWidth, x - x_ + size.width);
				}
				// next line
				y += lineHeight;
			}
			return new Dimension(lineWidth, y - y_);
		} finally {
			graphics.popState();
		}
	}
}
