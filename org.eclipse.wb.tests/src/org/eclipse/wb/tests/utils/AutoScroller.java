/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.utils;

import org.eclipse.wb.internal.draw2d.FigureCanvas;

import org.eclipse.draw2d.RangeModel;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPartViewer;

import java.io.Closeable;

/**
 * This class scrolls the viewer so that the absolute coordinates that are
 * passed as constructor arguments are within the visible area. This is required
 * because GEF does not support selecting edit parts/figures that are currently
 * invisible. The relative coordinates can then be accessed via
 * {@link #scrolledX} and {@link #scrolledY}.
 */
public class AutoScroller implements Closeable {
	private final Point location;
	private RangeModel m_horizontalRange;
	private RangeModel m_verticalRange;

	public AutoScroller(EditPartViewer viewer, int x, int y) {
		if (viewer.getControl() instanceof FigureCanvas canvas) {
			Viewport viewport = canvas.getViewport();
			m_horizontalRange = viewport.getHorizontalRangeModel();
			m_verticalRange = viewport.getVerticalRangeModel();
			int offX = Math.max(x - m_horizontalRange.getExtent() + 8, 0);
			int offY = Math.max(y - m_verticalRange.getExtent() + 8, 0);
			m_horizontalRange.setValue(offX);
			m_verticalRange.setValue(offY);
			int scrolledX = x - m_horizontalRange.getValue();
			int scrolledY = y - m_verticalRange.getValue();
			location = new Point(scrolledX, scrolledY);
		} else {
			location = new Point(x, y);
		}
	}

	public Point getLocation() {
		return location;
	}

	@Override
	public void close() {
		if (m_horizontalRange != null) {
			m_horizontalRange.setValue(0);
		}
		if (m_verticalRange != null) {
			m_verticalRange.setValue(0);
		}
	}
}