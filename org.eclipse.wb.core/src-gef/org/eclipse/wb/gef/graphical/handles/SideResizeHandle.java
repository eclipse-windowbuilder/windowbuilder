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
package org.eclipse.wb.gef.graphical.handles;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Resize {@link Handle} located on left/top/right/bottom sides of owner {@link GraphicalEditPart}.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class SideResizeHandle extends Handle {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SideResizeHandle(GraphicalEditPart owner, int side, int width, boolean center) {
		super(owner, new ResizeHandleLocator(owner.getFigure(), side, width, center));
		if (side == PositionConstants.LEFT || side == PositionConstants.RIGHT) {
			setCursor(Cursors.SIZEE);
		} else {
			setCursor(Cursors.SIZEN);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Locator
	//
	////////////////////////////////////////////////////////////////////////////
	private static class ResizeHandleLocator implements Locator {
		private final Figure m_reference;
		private final int m_side;
		private final int m_width;
		private final boolean m_center;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public ResizeHandleLocator(Figure reference, int side, int width, boolean center) {
			m_reference = reference;
			m_side = side;
			m_width = width;
			m_center = center;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Locator
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public void relocate(IFigure target) {
			Rectangle bounds = m_reference.getBounds().getCopy();
			FigureUtils.translateFigureToFigure(m_reference, target, bounds);
			//
			int locationOffset = m_center ? m_width / 2 : m_width;
			if (m_side == PositionConstants.LEFT) {
				bounds.x -= locationOffset;
				bounds.width = m_width;
			} else if (m_side == PositionConstants.RIGHT) {
				bounds.x = bounds.right() - locationOffset;
				bounds.width = m_width;
			} else if (m_side == PositionConstants.TOP) {
				bounds.y -= locationOffset;
				bounds.height = m_width;
			} else {
				bounds.y = bounds.bottom() - locationOffset;
				bounds.height = m_width;
			}
			target.setBounds(bounds);
		}
	}
}