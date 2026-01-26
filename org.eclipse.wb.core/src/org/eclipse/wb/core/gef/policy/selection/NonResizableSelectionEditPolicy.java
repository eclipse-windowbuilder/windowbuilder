/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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

import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.EditPart;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link SelectionEditPolicy} that shows {@link Handle}'s around {@link EditPart} but does not
 * support any resizing.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public class NonResizableSelectionEditPolicy extends SelectionEditPolicy {
	////////////////////////////////////////////////////////////////////////////
	//
	// Handles
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Handle> createSelectionHandles() {
		List<Handle> handles = new ArrayList<>();
		handles.add(new MoveHandle(getHost()));
		handles.add(createHandle(PositionConstants.SOUTH_EAST));
		handles.add(createHandle(PositionConstants.SOUTH_WEST));
		handles.add(createHandle(PositionConstants.NORTH_WEST));
		handles.add(createHandle(PositionConstants.NORTH_EAST));
		return handles;
	}

	/**
	 * @return the {@link ResizeHandle} for given direction.
	 */
	private Handle createHandle(int direction) {
		ResizeHandle handle = new ResizeHandle(getHost(), direction);
		handle.setDragTracker(new ResizeTracker(direction, null));
		return handle;
	}
}