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
package org.eclipse.wb.draw2d;

import org.eclipse.draw2d.Graphics;

/**
 * Draws a rectangle whose size is determined by the bounds set to it.
 *
 * @author scheglov_ke
 * @coverage gef.draw2d
 */
public final class RectangleFigure extends Figure {
	////////////////////////////////////////////////////////////////////////////
	//
	// Paint
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void paintClientArea(Graphics graphics) {
		graphics.drawRectangle(getClientArea().getResized(-1, -1));
	}
}
