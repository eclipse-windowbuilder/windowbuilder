/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.property.editor.presentation;

import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

import org.eclipse.draw2d.ButtonModel;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * Class representing flat push button as it looks in Mac OSX.
 *
 * It doesn't draw text, not need for now. ;-)
 *
 * @author mitin_aa
 */
public final class CFlatButton extends Label {
	// colors
	private static final Color COLOR_FACE = DrawUtils.getShiftedColor(ColorConstants.button, 12);
	private static final Color COLOR_FACE_SELECTED = ColorConstants.buttonDarker;
	private static final Color COLOR_BORDER_GRADIENT1 = DrawUtils.getShiftedColor(COLOR_FACE, -12);
	private static final Color COLOR_BORDER_GRADIENT1_SELECTED = DrawUtils.getShiftedColor(
			COLOR_FACE_SELECTED,
			64);
	private static final Color COLOR_BORDER_GRADIENT2 = DrawUtils.getShiftedColor(COLOR_FACE, -8);
	private static final Color COLOR_BORDER_GRADIENT2_SELECTED = DrawUtils.getShiftedColor(
			COLOR_FACE_SELECTED,
			-8);
	// fields
	private Image m_image;
	private ButtonModel m_model;

	public CFlatButton(ButtonModel model, Image image) {
		m_model = model;
		m_image = image;
	}

	@Override
	public void paint(Graphics gc) {
		boolean isSelected = m_model.isPressed() | m_model.isSelected();
		Color faceColor = isSelected ? COLOR_FACE_SELECTED : COLOR_FACE;
		Color borderGradientColor1 = isSelected ? COLOR_BORDER_GRADIENT1_SELECTED : COLOR_BORDER_GRADIENT1;
		Color borderGradientColor2 = isSelected ? COLOR_BORDER_GRADIENT2_SELECTED : COLOR_BORDER_GRADIENT2;
		Rectangle ca = getClientArea();
		// draw client area
		// dark border
		gc.setForegroundColor(ColorConstants.buttonDarker);
		gc.drawRectangle(ca.x, ca.y, ca.width - 1, ca.height - 1);
		cropClientArea(ca);
		// gradient border
		gc.setForegroundColor(borderGradientColor1);
		gc.setBackgroundColor(borderGradientColor2);
		gc.fillGradient(ca.x, ca.y, ca.width, ca.height, true);
		cropClientArea(ca);
		// fill background
		gc.setBackgroundColor(faceColor);
		gc.fillRectangle(ca);
		// draw face upper-half gradient
		Rectangle ca1 = getClientArea();
		cropClientArea(ca1);
		gc.setForegroundColor(faceColor);
		gc.setBackgroundColor(borderGradientColor1);
		gc.fillGradient(ca1.x, ca1.y, ca1.width, ca1.height / 4, true);
		// draw face down-half gradient
		ca1.x += 1;
		ca1.width -= 2;
		gc.setForegroundColor(borderGradientColor1);
		gc.setBackgroundColor(faceColor);
		gc.fillGradient(ca1.x, ca1.y + ca1.height / 4 - 1, ca1.width, ca1.height / 2, true);
		// draw image
		if (m_image != null) {
			org.eclipse.swt.graphics.Rectangle imageBounds = m_image.getBounds();
			// center it in client area
			int x = ca.x + (ca.width - imageBounds.width) / 2;
			int y = ca.y + (ca.height - imageBounds.height) / 2;
			gc.drawImage(m_image, x, y);
		}
	}

	private void cropClientArea(Rectangle ca) {
		ca.x += 1;
		ca.y += 1;
		ca.width -= 2;
		ca.height -= 2;
	}
}
