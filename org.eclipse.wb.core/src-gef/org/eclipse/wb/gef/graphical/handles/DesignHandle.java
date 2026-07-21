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
package org.eclipse.wb.gef.graphical.handles;

import org.eclipse.draw2d.Locator;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Tool;
import org.eclipse.gef.handles.AbstractHandle;

/**
 * {@link Handle} will add an {@link IAncestorListener} to the owner's figure, and will
 * automatically revalidate this handle whenever the owner's figure moves.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public abstract class DesignHandle extends AbstractHandle {

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a handle for the given <code>{@link GraphicalEditPart}</code> using the given
	 * <code>{@link Locator}</code>.
	 */
	public DesignHandle(GraphicalEditPart owner, Locator locator) {
		super(owner, locator);
	}

	/**
	 * Creates a new drag tracker {@link Tool} to be returned by {@link #getDragTracker()}.
	 */
	@Override
	protected final DragTracker createDragTracker() {
		return null;
	}
}