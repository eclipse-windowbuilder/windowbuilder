/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.draw2d.events;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.internal.draw2d.FigureCanvas;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Instances of this class are sent whenever mouse related actions occur. This includes mouse
 * buttons being pressed and released, the mouse pointer being moved and the mouse pointer crossing
 * widget boundaries.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public final class MouseEvent extends org.eclipse.draw2d.MouseEvent {
	private static final long serialVersionUID = 1L;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MouseEvent(FigureCanvas canvas, org.eclipse.swt.events.MouseEvent event, Figure source) {
		super(null, source, event);
		//
		Rectangle bounds = source.getBounds();
		Point location = new Point(event.x - bounds.x, event.y - bounds.y);
		location.x += canvas.getHorizontalScrollModel().getSelection();
		location.y += canvas.getVerticalScrollModel().getSelection();
		FigureUtils.translateAbsoluteToFigure(source, location);
		//
		x = location.x;
		y = location.y;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("MouseEvent{source=");
		buffer.append(source);
		buffer.append(" button=");
		buffer.append(button);
		buffer.append(" stateMask=");
		buffer.append(getState());
		buffer.append(" x=");
		buffer.append(x);
		buffer.append(" y=");
		buffer.append(y);
		buffer.append('}');
		return buffer.toString();
	}
}