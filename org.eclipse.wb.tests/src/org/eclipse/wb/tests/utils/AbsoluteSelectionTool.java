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

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.tools.DragEditPartTracker;
import org.eclipse.wb.gef.graphical.tools.SelectionTool;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DragTracker;

/**
 * Subclass of the selection tool that also supports figures outside the visible
 * area. If necessary, the absolute coordinates that are passed to this tool are
 * converted to relative coordinates and the viewer scrolled by the offset. This
 * is necessary to have the {@link GraphicalViewer#findHandleAt(Point)} behave
 * correctly.
 */
public class AbsoluteSelectionTool extends SelectionTool {

	@Override
	protected boolean handleButtonDown(int button) {
		Point absoluteLocation = getLocation();
		try (AutoScroller scroller = new AutoScroller(getCurrentViewer(), absoluteLocation.x, absoluteLocation.y)) {
			Point location = scroller.getLocation();
			getCurrentInput().setMouseLocation(location.x, location.y);
			return super.handleButtonDown(button);
		} finally {
			getCurrentInput().setMouseLocation(absoluteLocation.x, absoluteLocation.y);
		}
	}

	@Override
	public void setDragTracker(DragTracker dragTracker) {
		if (dragTracker != null && dragTracker.getClass() == DragEditPartTracker.class) {
			EditPart sourceEditPart = (EditPart) ReflectionUtils.getFieldObject(dragTracker, "m_sourceEditPart");
			super.setDragTracker(new AbsoluteDragEditPartTracker(sourceEditPart));
			return;
		}
		super.setDragTracker(dragTracker);
	}
}