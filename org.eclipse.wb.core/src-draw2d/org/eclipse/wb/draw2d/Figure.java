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
package org.eclipse.wb.draw2d;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

import java.util.Iterator;
import java.util.List;

/**
 * A lightweight graphical representation. Figures are rendered to a {@link Graphics} object.
 * Figures can be composed to create complex renderings.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class Figure extends org.eclipse.draw2d.Figure {

	////////////////////////////////////////////////////////////////////////////
	//
	// Events support
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Return all registers listeners for given class or <code>null</code>.
	 */
	@Override
	public <T extends Object> Iterator<T> getListeners(Class<T> listenerClass) {
		return super.getListeners(listenerClass);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Paints this Figure and its children.
	 *
	 * @noreference @nooverride
	 */
	@Override
	public final void paint(Graphics graphics) {
		// set figure state
		if (getBackgroundColor() != null) {
			graphics.setBackgroundColor(getBackgroundColor());
		}
		if (getForegroundColor() != null) {
			graphics.setForegroundColor(getForegroundColor());
		}
		if (getFont() != null) {
			graphics.setFont(getFont());
		}
		graphics.pushState();
		//
		try {
			// paint figure
			paintFigure(graphics);
			// paint all children
			paintChildren(graphics);
			// paint border
			paintBorder(graphics);
		} finally {
			graphics.popState();
		}
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		// fill all figure before any painting clientArea, clilds, and border.
		if (isOpaque()) {
			Rectangle bounds = getBounds();
			graphics.fillRectangle(0, 0, bounds.width, bounds.height);
		}
		//
		Insets insets = getInsets();
		graphics.translate(insets.left, insets.top);
		paintClientArea(graphics);
		graphics.restoreState();
	}

	@Override
	protected void paintChildren(Graphics graphics) {
		List<? extends IFigure> children = getChildren();
		if (children.isEmpty()) {
			return;
		}
		// set children state
		Insets insets = getInsets();
		graphics.translate(insets.left, insets.top);
		graphics.pushState();
		// paint all
		for (IFigure childFigure : children) {
			if (childFigure.isVisible() && childFigure.intersects(graphics.getClip(new Rectangle()))) {
				Rectangle childBounds = childFigure.getBounds();
				graphics.clipRect(childBounds);
				if (childFigure instanceof Figure f && f.useLocalCoordinates()) {
					graphics.translate(childBounds.x, childBounds.y);
				}
				childFigure.paint(graphics);
				graphics.restoreState();
			}
		}
		// reset state
		graphics.popState();
		graphics.restoreState();
	}

	/**
	 * Paints this Figure's primary representation.
	 */
	@Override
	protected void paintClientArea(Graphics graphics) {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	protected boolean useLocalCoordinates() {
		return true;
	}
}