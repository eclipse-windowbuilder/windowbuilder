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
package org.eclipse.wb.gef.core.requests;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.gef.requests.GroupRequest;

/**
 * A {@link Request} to change the bounds of the {@link EditPart}(s).
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class ChangeBoundsRequest extends GroupRequest implements DropRequest {
	private static final int SNAP_TO = 16;
	private Point m_mouseLocation;
	private Point m_moveDelta = new Point();
	private Dimension m_resizeDelta = new Dimension();
	private int m_resizeDirection;
	private int m_flags = 0;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructs an empty {@link ChangeBoundsRequest}.
	 */
	public ChangeBoundsRequest() {
	}

	/**
	 * Constructs a {@link ChangeBoundsRequest} with the specified <i>type</i>.
	 */
	public ChangeBoundsRequest(Object type) {
		super(type);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the location of the mouse pointer.
	 */
	@Override
	public Point getLocation() {
		return m_mouseLocation;
	}

	/**
	 * Sets the location of the mouse pointer.
	 */
	public void setLocation(Point location) {
		m_mouseLocation = location;
	}

	/**
	 * Returns a {@link Point} representing the distance the {@link EditPart} has moved.
	 */
	public Point getMoveDelta() {
		return m_moveDelta;
	}

	/**
	 * Sets the move delta.
	 */
	public void setMoveDelta(Point moveDelta) {
		m_moveDelta = moveDelta;
	}

	/**
	 * Returns a {@link Dimension} representing how much the {@link EditPart} has been resized.
	 */
	public Dimension getSizeDelta() {
		return m_resizeDelta;
	}

	/**
	 * Sets the size delta.
	 */
	public void setSizeDelta(Dimension sizeDelta) {
		m_resizeDelta = sizeDelta;
	}

	/**
	 * Returns the direction the figure is being resized. Possible values are
	 * <ul>
	 * <li>{@link org.eclipse.draw2d.PositionConstants#EAST}
	 * <li>{@link org.eclipse.draw2d.PositionConstants#WEST}
	 * <li>{@link org.eclipse.draw2d.PositionConstants#NORTH}
	 * <li>{@link org.eclipse.draw2d.PositionConstants#SOUTH}
	 * <li>{@link org.eclipse.draw2d.PositionConstants#NORTH_EAST}
	 * <li>{@link org.eclipse.draw2d.PositionConstants#NORTH_WEST}
	 * <li>{@link org.eclipse.draw2d.PositionConstants#SOUTH_EAST}
	 * <li>{@link org.eclipse.draw2d.PositionConstants#SOUTH_WEST}
	 * </ul>
	 */
	public int getResizeDirection() {
		return m_resizeDirection;
	}

	/**
	 * Sets the direction the figure is being resized.
	 *
	 * @see #getResizeDirection()
	 */
	public void setResizeDirection(int direction) {
		m_resizeDirection = direction;
	}

	/**
	 * Transforms a copy of the passed in rectangle to account for the move and/or resize deltas and
	 * returns this copy.
	 */
	public Rectangle getTransformedRectangle(Rectangle rectangle) {
		Rectangle result = rectangle.getCopy();
		result.performTranslate(m_moveDelta);
		result.resize(m_resizeDelta);
		return result;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DND Feedback
	//
	////////////////////////////////////////////////////////////////////////////
	private int m_dndFeedback;

	/**
	 * @return additional DND feedback flags.
	 */
	public int getDNDFeedback() {
		return m_dndFeedback;
	}

	/**
	 * Sets additional DND feedback flags.
	 */
	public void setDNDFeedback(int dndFeedback) {
		m_dndFeedback = dndFeedback;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Snap to horizontal axis
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Used to set whether snap-to is being performed.
	 *
	 * @param value <code>true</code> if the request is for a creation with snap-to
	 *              enabled
	 */
	public void setSnapToEnabled(boolean value) {
		m_flags = value ? m_flags | SNAP_TO : m_flags & ~SNAP_TO;
	}

	/**
	 * Returns <code>true</code> if snap-to is enabled
	 *
	 * @return <code>true</code> if the request is for a creation with snap-to
	 *         enabled
	 */
	public boolean isSnapToEnabled() {
		return (m_flags & SNAP_TO) != 0;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("ChangeBoundsRequest(type=");
		buffer.append(getType());
		buffer.append(", editParts=");
		buffer.append(getEditParts());
		buffer.append(", m_flags=");
		buffer.append(m_flags);
		buffer.append(", location=");
		buffer.append(m_mouseLocation);
		buffer.append(", resizeDelta=");
		buffer.append(m_resizeDelta);
		buffer.append(", moveDelta=");
		buffer.append(m_moveDelta);
		buffer.append(", direction=");
		buffer.append(m_resizeDirection);
		buffer.append(")");
		return buffer.toString();
	}
}