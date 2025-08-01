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
package org.eclipse.wb.internal.core.gef.policy.layout.absolute;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Figure which draws dots upon host figure
 *
 * @author mitin_aa
 * @author lobas_av
 * @coverage core.gef.policy
 */
public class DotsFeedback<C extends IAbstractComponentInfo> extends Figure {
	private final AbsoluteBasedLayoutEditPolicy<C> m_layoutEditPolicy;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DotsFeedback(AbsoluteBasedLayoutEditPolicy<C> layoutEditPolicy, IFigure hostFigure) {
		m_layoutEditPolicy = layoutEditPolicy;
		// prepare bounds to draw on client-area only
		Rectangle bounds = hostFigure.getBounds().getCopy();
		FigureUtils.translateFigureToAbsolute(hostFigure, bounds);
		IAbstractComponentInfo container =
				(IAbstractComponentInfo) layoutEditPolicy.getHost().getModel();
		bounds.shrink(container.getClientAreaInsets());
		// set bounds
		setBounds(bounds);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Paint
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void paintClientArea(Graphics graphics) {
		// paint dots if needed
		if (m_layoutEditPolicy.isShowGridFeedback()) {
			Rectangle r = getClientArea();
			int gridStepX = m_layoutEditPolicy.getGridStepX();
			int gridStepY = m_layoutEditPolicy.getGridStepY();
			// paint dots
			int x = r.x;
			for (int i = 0; i < r.width / gridStepX; i++) {
				int y = r.y;
				for (int j = 0; j < r.height / gridStepY; j++) {
					graphics.drawPoint(x, y);
					y += gridStepY;
				}
				x += gridStepX;
			}
		}
	}
}