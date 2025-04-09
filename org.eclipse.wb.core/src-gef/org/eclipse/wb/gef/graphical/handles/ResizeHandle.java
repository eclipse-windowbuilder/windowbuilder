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

import org.eclipse.wb.draw2d.RelativeLocator;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Locator;

/**
 * A Handle used to resize a {@link EditPart}s.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class ResizeHandle extends SquareHandle {
	private final int m_direction;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a new ResizeHandle for the given {@link GraphicalEditPart}. The <code>direction</code>
	 * is the relative direction from the center of the owner figure.
	 */
	public ResizeHandle(GraphicalEditPart owner, int direction) {
		this(owner, direction, new RelativeLocator(owner.getFigure(), direction));
	}

	/**
	 * Creates a new ResizeHandle for the given {@link GraphicalEditPart}. The <code>direction</code>
	 * is the relative direction from the center of the owner figure.
	 */
	public ResizeHandle(GraphicalEditPart owner, int direction, Locator locator) {
		super(owner, locator);
		m_direction = direction;
		setCursor(Cursors.getDirectionalCursor(direction));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public int getDirection() {
		return m_direction;
	}
}