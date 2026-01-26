/*******************************************************************************
 * Copyright (c) 2025, 2026 Patrick Ziegler and others.
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

import org.eclipse.wb.gef.core.tools.DragEditPartTracker;
import org.eclipse.wb.internal.gef.graphical.GraphicalViewer;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;

import java.util.Collection;

/**
 * Subclass of the drag tracker that also supports figures outside the visible
 * area. If necessary, the absolute coordinates that are passed to this tool are
 * converted to relative coordinates and the viewer scrolled by the offset. This
 * is necessary to have the
 * {@link GraphicalViewer#findObjectAtExcluding(Point, Collection)} behave
 * correctly.
 */
public class AbsoluteDragEditPartTracker extends DragEditPartTracker {

	public AbsoluteDragEditPartTracker(EditPart sourceEditPart) {
		super(sourceEditPart);
	}

	@Override
	protected void updateTargetUnderMouse() {
		Point absoluteLocation = getLocation();
		try (AutoScroller scroller = new AutoScroller(getCurrentViewer(), absoluteLocation.x, absoluteLocation.y)) {
			Point location = scroller.getLocation();
			getCurrentInput().setMouseLocation(location.x, location.y);
			super.updateTargetUnderMouse();
		} finally {
			getCurrentInput().setMouseLocation(absoluteLocation.x, absoluteLocation.y);
		}
	}
}
