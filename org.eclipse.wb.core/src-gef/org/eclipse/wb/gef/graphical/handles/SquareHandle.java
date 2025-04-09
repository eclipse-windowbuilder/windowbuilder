/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

/**
 * A small square handle approximately 7x7 pixels in size, that is either black or white.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public abstract class SquareHandle extends Handle {
	/**
	 * The default size for square handles.
	 */
	protected static final int DEFAULT_HANDLE_SIZE = 7;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a handle for the given <code>{@link GraphicalEditPart}</code> using the given
	 * <code>{@link Locator}</code>.
	 */
	public SquareHandle(GraphicalEditPart owner, Locator locator) {
		super(owner, locator);
		setSize(DEFAULT_HANDLE_SIZE, DEFAULT_HANDLE_SIZE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SquareHandle
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns <code>true</code> if the handle's owner is the primary selection.
	 */
	protected boolean isPrimary() {
		return getOwner().getSelected() == EditPart.SELECTED_PRIMARY;
	}

	/**
	 * Returns the color for the outside of the handle.
	 */
	protected Color getBorderColor() {
		return isPrimary() ? ColorConstants.white : ColorConstants.black;
	}

	/**
	 * Returns the color for the inside of the handle.
	 */
	protected Color getFillColor() {
		return isPrimary() ? ColorConstants.black : ColorConstants.white;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void paintClientArea(Graphics graphics) {
		Rectangle area = getClientArea();
		area.shrink(1, 1);
		//
		graphics.setBackgroundColor(getFillColor());
		graphics.fillRectangle(area);
		//
		graphics.setForegroundColor(getBorderColor());
		graphics.drawRectangle(area);
	}
}