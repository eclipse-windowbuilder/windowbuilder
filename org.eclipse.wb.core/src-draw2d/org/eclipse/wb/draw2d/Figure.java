/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.draw2d;

import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.draw2d.FigureVisitor;
import org.eclipse.wb.internal.draw2d.ICustomTooltipProvider;

import org.eclipse.draw2d.Graphics;
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
	private String m_toolTipText;
	private ICustomTooltipProvider m_customTooltipProvider;

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
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Visits this {@link Figure} and its children using given {@link FigureVisitor}.
	 */
	public final void accept(FigureVisitor visitor, boolean forward) {
		if (visitor.visit(this)) {
			List<Figure> children = getChildren();
			int size = children.size();
			//
			if (forward) {
				for (int i = 0; i < size; i++) {
					Figure childFigure = children.get(i);
					childFigure.accept(visitor, forward);
				}
			} else {
				for (int i = size - 1; i >= 0; i--) {
					Figure childFigure = children.get(i);
					childFigure.accept(visitor, forward);
				}
			}
			visitor.endVisit(this);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parent/Children
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * @return the {@link List} of children {@link Figure}'s.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Figure> getChildren() {
		return (List<Figure>) super.getChildren();
	}

	/**
	 * @return the {@link FigureCanvas} that contains this {@link Figure}.
	 * @noreference @nooverride
	 */
	public FigureCanvas getFigureCanvas() {
		return ((Figure) getParent()).getFigureCanvas();
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
		List<Figure> children = getChildren();
		if (children.isEmpty()) {
			return;
		}
		// set children state
		Insets insets = getInsets();
		graphics.translate(insets.left, insets.top);
		graphics.pushState();
		// paint all
		for (Figure childFigure : children) {
			if (childFigure.isVisible() && childFigure.intersects(graphics.getClip(new Rectangle()))) {
				Rectangle childBounds = childFigure.getBounds();
				graphics.clipRect(childBounds);
				graphics.translate(childBounds.x, childBounds.y);
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

	/**
	 * Returns the receiver's tool tip text, or <code>null</code> if it has not been set.
	 */
	public String getToolTipText() {
		return m_toolTipText;
	}

	/**
	 * Sets the receiver's tool tip text to the argument, which may be <code>null</code> indicating
	 * that no tool tip text should be shown.
	 */
	public void setToolTipText(String toolTipText) {
		m_toolTipText = toolTipText;
	}

	/**
	 * @return custom tool tip provider {@link ICustomTooltipProvider}, or <code>null</code> if it has
	 *         not been set.
	 */
	public ICustomTooltipProvider getCustomTooltipProvider() {
		return m_customTooltipProvider;
	}

	/**
	 * Sets the custom tool tip provider {@link ICustomTooltipProvider} to the argument, which may be
	 * <code>null</code> indicating that no tool tip text should be shown.
	 */
	public void setCustomTooltipProvider(ICustomTooltipProvider provider) {
		m_customTooltipProvider = provider;
	}

	@Override
	protected boolean useLocalCoordinates() {
		return true;
	}
}