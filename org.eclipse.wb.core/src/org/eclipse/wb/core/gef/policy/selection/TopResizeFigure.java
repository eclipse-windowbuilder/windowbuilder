/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.core.gef.policy.selection;

import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.internal.draw2d.SemiTransparentFigure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import org.apache.commons.lang3.StringUtils;

/**
 * Figure used as feedback while resizing top-level edit parts.
 *
 * @author mitin_aa
 */
public class TopResizeFigure extends SemiTransparentFigure {
	private String m_sizeText;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TopResizeFigure() {
		super(64);
		setBackgroundColor(ColorConstants.lightGreen);
		setForegroundColor(ColorConstants.darkGray);
		setBorder(new LineBorder(ColorConstants.darkBlue, 1));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void setSizeText(String sizeText) {
		m_sizeText = sizeText;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Paint
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void paintClientArea(Graphics graphics) {
		super.paintClientArea(graphics);
		if (!StringUtils.isEmpty(m_sizeText)) {
			Rectangle area = getClientArea();
			Font oldFont = graphics.getFont();
			Font newFont = FontDescriptor.createFrom(oldFont) //
					.setHeight(16) //
					.setStyle(SWT.NONE) //
					.createFont(null);
			graphics.setFont(newFont);
			Dimension textExtent = TextUtilities.INSTANCE.getTextExtents(m_sizeText, graphics.getFont());
			int x = area.x + (area.width - textExtent.width) / 2;
			int y = area.y + (area.height - textExtent.height) / 2;
			graphics.drawString(m_sizeText, x, y);
			graphics.setFont(oldFont);
			newFont.dispose();
		}
	}
}
