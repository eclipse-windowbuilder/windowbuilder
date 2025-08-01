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
package org.eclipse.wb.core.controls.palette;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.border.MarginBorder;
import org.eclipse.wb.internal.draw2d.CustomTooltipProvider;
import org.eclipse.wb.internal.draw2d.JustifyLabel;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

/**
 * Standard palette tooltip: bold header and multi line details.
 *
 * @author lobas_av
 * @coverage core.control.palette
 */
public final class JustifyPaletteTooltipProvider extends CustomTooltipProvider {
	private final String m_header;
	private final String m_details;
	private final int m_wrapChars;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JustifyPaletteTooltipProvider(String header, String details, int wrapChars) {
		m_header = header;
		m_details = details;
		m_wrapChars = wrapChars;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CustomTooltipProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IFigure createTooltipFigure(IFigure hostFigure) {
		// header figure
		Label headerFigure = new Label(m_header);
		Font boldFont = FontDescriptor.createFrom(headerFigure.getFont()) //
				.setStyle(SWT.BOLD) //
				.createFont(null);
		m_canvas.addDisposeListener(event -> boldFont.dispose());
		headerFigure.setFont(boldFont);
		// details figure
		JustifyLabel detailsFigure = new JustifyLabel();
		detailsFigure.setBorder(new MarginBorder(new Insets(0, 2, 2, 2)));
		detailsFigure.setWrapChars(m_wrapChars);
		detailsFigure.setText(m_details);
		// prepare size's
		Dimension headerSize = headerFigure.getPreferredSize();
		Dimension detailsSize = detailsFigure.getPreferredSize();
		if (headerSize.width > detailsSize.width) {
			detailsFigure.setWrapPixels(headerSize.width + 10);
			detailsSize = detailsFigure.getPreferredSize();
			if (headerSize.width > detailsSize.width) {
				detailsSize.width = headerSize.width + 10;
			}
		}
		headerFigure.setBounds(new Rectangle(detailsSize.width / 2 - headerSize.width / 2,
				0,
				headerSize.width,
				headerSize.height));
		detailsFigure.setBounds(new Rectangle(0,
				headerSize.height,
				detailsSize.width,
				detailsSize.height));
		// create container figure
		Figure tooltipFigure = new Figure();
		tooltipFigure.add(headerFigure);
		tooltipFigure.add(detailsFigure);
		tooltipFigure.setBounds(new Rectangle(0, 0, detailsSize.width, headerSize.height
				+ detailsSize.height));
		//
		return tooltipFigure;
	}
}