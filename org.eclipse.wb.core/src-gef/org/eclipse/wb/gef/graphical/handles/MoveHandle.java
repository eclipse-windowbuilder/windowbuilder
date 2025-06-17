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
package org.eclipse.wb.gef.graphical.handles;

import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.tools.DragEditPartTracker;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Locator;

/**
 * A Handle used for moving {@link EditPart}s.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class MoveHandle extends Handle {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a handle for the given <code>{@link GraphicalEditPart}</code> using
	 * <code>{@link MoveHandleLocator}</code>.
	 */
	public MoveHandle(GraphicalEditPart owner) {
		this(owner, new MoveHandleLocator(owner.getFigure()));
	}

	/**
	 * Creates a handle for the given <code>{@link EditPart}</code> using the given
	 * <code>{@link Locator}</code>.
	 */
	public MoveHandle(GraphicalEditPart owner, Locator locator) {
		super(owner, locator);
		setBorder(new LineBorder(1));
		setCursor(Cursors.SIZEALL);
		// set drag tracker
		{
			Tool tracker = new DragEditPartTracker(owner);
			tracker.setDefaultCursor(getCursor());
			setDragTracker(tracker);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean containsPoint(int x, int y) {
		if (!super.containsPoint(x, y)) {
			return false;
		}
		return !getBounds().getCopy().shrink(2, 2).contains(x, y);
	}
}