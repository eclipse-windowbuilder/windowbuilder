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
package org.eclipse.wb.draw2d;

import org.eclipse.wb.draw2d.border.Border;
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.draw2d.FigureVisitor;
import org.eclipse.wb.internal.draw2d.ICustomTooltipProvider;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Cursor;

import java.util.ArrayList;
import java.util.Collections;
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
	private Figure m_parent;
	private List<Figure> m_children;
	private Border m_border;
	private Cursor m_cursor;
	private Object m_data;
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

	/**
	 * Notifies any {@link IFigureListener IFigureListeners} listening to this {@link Figure} that it
	 * has moved.
	 */
	@Override
	protected void fireMoved() {
		Iterator<FigureListener> listeners = getListeners(FigureListener.class);
		if (listeners != null) {
			listeners.forEachRemaining(figureListener -> figureListener.figureMoved(this));
		}
	}

	/**
	 * Called after the receiver's parent has been set and it has been added to its parent.
	 */
	@Override
	public void addNotify() {
		for (Figure childFigure : getChildren()) {
			childFigure.addNotify();
		}
	}

	/**
	 * Called prior to this figure's removal from its parent.
	 */
	@Override
	public void removeNotify() {
		for (Figure childFigure : getChildren()) {
			childFigure.removeNotify();
		}
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
	 * Adds the given Figure as a child of this Figure.
	 */
	public void add(Figure childFigure) {
		add(childFigure, null, -1);
	}

	/**
	 * Adds the given Figure as a child of this Figure at the given index.
	 */
	public void add(Figure childFigure, int index) {
		add(childFigure, null, index);
	}

	/**
	 * Adds the given Figure as a child of this Figure with the given bounds.
	 */
	public void add(Figure childFigure, Rectangle bounds) {
		add(childFigure, bounds, -1);
	}

	/**
	 * Adds the child Figure using the specified index and bounds.
	 */
	public void add(Figure childFigure, Rectangle bounds, int index) {
		// check figure parent
		if (childFigure.getParent() != null) {
			throw new IllegalArgumentException("Figure.add(...) Figure already added to parent");
		}
		// check figure
		for (Figure f = this; f != null; f = f.getParent()) {
			if (childFigure == f) {
				throw new IllegalArgumentException("IWAG0002E Figure.add(...) Cycle created in figure heirarchy");
			}
		}
		// check container
		if (m_children == null) {
			m_children = new ArrayList<>();
		}
		// check index
		if (index < -1 || index > m_children.size()) {
			throw new IndexOutOfBoundsException("IWAG0001E Figure.add(...) invalid index");
		}
		// add to child list
		if (index == -1) {
			m_children.add(childFigure);
		} else {
			m_children.add(index, childFigure);
		}
		// set parent
		childFigure.setParent(this);
		// notify child of change
		childFigure.addNotify();
		// set bounds
		if (bounds != null) {
			childFigure.setBounds(bounds);
		}
		// notify of change
		resetState(childFigure);
	}

	/**
	 * @return the {@link List} of children {@link Figure}'s.
	 */
	@Override
	public List<Figure> getChildren() {
		return m_children == null ? Collections.<Figure>emptyList() : m_children;
	}

	/**
	 * Removes the given child Figure from this Figure's hierarchy.
	 */
	public void remove(Figure childFigure) {
		// check child
		if (m_children == null || m_children.isEmpty()) {
			throw new IllegalArgumentException("This parent is empty");
		}
		if (childFigure.getParent() != this || !m_children.contains(childFigure)) {
			throw new IllegalArgumentException("IWAG0003E Figure is not a child of this parent");
		}
		// notify child of change
		childFigure.removeNotify();
		// remove child
		m_children.remove(childFigure);
		childFigure.setParent(null);
		// notify of change
		resetState(childFigure);
	}

	/**
	 * Removes all children from this Figure.
	 */
	@Override
	public void removeAll() {
		// remove all children
		for (Figure childFigure : getChildren()) {
			childFigure.removeNotify();
			childFigure.setParent(null);
		}
		// notify of change
		if (m_children != null && !m_children.isEmpty() && isVisible()) {
			revalidate();
			repaint();
		}
		// reset container
		m_children = null;
	}

	/**
	 * @return the {@link FigureCanvas} that contains this {@link Figure}.
	 * @noreference @nooverride
	 */
	public FigureCanvas getFigureCanvas() {
		return m_parent.getFigureCanvas();
	}

	/**
	 * Returns the Figure that is the current parent of this Figure or <code>null</code> if there is
	 * no parent.
	 */
	@Override
	public Figure getParent() {
		return m_parent;
	}

	/**
	 * Sets this Figure's parent.
	 */
	public void setParent(Figure parent) {
		Figure oldParent = m_parent;
		m_parent = parent;
		// send reparent event
		firePropertyChange("parent", oldParent, parent);//$NON-NLS-1$
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Updates the cursor.
	 */
	protected void updateCursor() {
		Figure parent = getParent();
		if (parent != null && isVisible()) {
			parent.updateCursor();
		}
	}

	/**
	 * If the argument is <code>true</code>, causes the receiver to have all mouse events delivered to
	 * it until the method is called with <code>false</code> as the argument.
	 */
	protected final void setCapture(boolean capture) {
		if (capture) {
			setCapture(this);
		} else {
			setCapture(null);
		}
	}

	/**
	 * Sets capture to the given figure.
	 *
	 * @noreference @nooverride
	 */
	public void setCapture(Figure figure) {
		Figure parent = getParent();
		if (parent != null) {
			parent.setCapture(figure);
		}
	}

	/**
	 * Request of change bounds and repaints the rectangular area within this Figure. Rectangular area
	 * <code>childFigure</code>'s bounds. Use into {@link #add(Figure, Rectangle, int)} and
	 * {@link #remove(Figure)}.
	 */
	protected final void resetState(Figure childFigure) {
		if (isVisible() && childFigure.isVisible()) {
			Rectangle bounds = getBounds();
			Insets insets = getInsets();
			Rectangle childBounds = childFigure.getBounds();
			revalidate();
			repaint(bounds.x + insets.left + childBounds.x,
					bounds.y + insets.top + childBounds.y,
					childBounds.width,
					childBounds.height);
		}
	}

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

	/**
	 * Paints the border associated with this Figure, if one exists.
	 */
	@Override
	protected void paintBorder(Graphics graphics) {
		if (m_border != null) {
			m_border.paint(this, graphics, NO_INSETS);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Bounds
	//
	////////////////////////////////////////////////////////////////////////////


	/**
	 * Sets the bounds of this Figure to the Rectangle <i>rect</i>.
	 */
	@Override
	public void setBounds(Rectangle bounds) {
		if (!this.bounds.equals(bounds)) {
			// calc repaint rectangle
			Rectangle dirtyArea = this.bounds.getUnion(bounds);
			// change bounds
			this.bounds.setBounds(bounds);
			// send move event
			fireMoved();
			// reset state
			revalidate();
			repaint(dirtyArea);
		}
	}

	/**
	 * Returns the border's Insets if the border is set. Otherwise returns NO_INSETS, an instance of
	 * Insets with all 0s. Returns Insets by reference. DO NOT Modify returned value.
	 */
	@Override
	public Insets getInsets() {
		if (m_border == null) {
			return IFigure.NO_INSETS;
		}
		return m_border.getInsets(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Get user define data.
	 */
	public Object getData() {
		return m_data;
	}

	/**
	 * Set user define data.
	 */
	public void setData(Object data) {
		m_data = data;
	}

	/**
	 * Returns the current border by reference.
	 */
	@Override
	public Border getBorder() {
		return m_border;
	}

	/**
	 * Sets the border.
	 */
	public void setBorder(Border border) {
		if (m_border != border) {
			m_border = border;
			if (isVisible()) {
				revalidate();
				repaint();
			}
		}
	}

	/**
	 * @return The Cursor used when the mouse is over this Figure
	 */
	@Override
	public Cursor getCursor() {
		return m_cursor;
	}

	/**
	 * Sets the cursor.
	 */
	@Override
	public void setCursor(Cursor cursor) {
		if (m_cursor != cursor) {
			m_cursor = cursor;
			updateCursor();
		}
	}

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