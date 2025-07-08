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

import org.eclipse.wb.draw2d.border.MarginBorder;
import org.eclipse.wb.internal.draw2d.CustomTooltipProvider;
import org.eclipse.wb.internal.draw2d.Label;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Simple Draw2d tooltip represented single line of text.
 *
 * @author lobas_av
 * @coverage core.control.palette
 */
public final class SimplePaletteTooltipProvider extends CustomTooltipProvider {
	private final String m_details;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SimplePaletteTooltipProvider(String details) {
		m_details = details;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CustomTooltipProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IFigure createTooltipFigure(IFigure hostFigure) {
		// create tooltip figure
		Label tooltipFigure = new Label(m_details);
		tooltipFigure.setBorder(new MarginBorder(2));
		// configure bounds
		Dimension preferredSize = tooltipFigure.getPreferredSize();
		tooltipFigure.setBounds(new Rectangle(0, 0, preferredSize.width, preferredSize.height));
		return tooltipFigure;
	}
}