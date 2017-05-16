/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.gef.core.requests;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;

/**
 * A {@link Request} to change the bounds of the {@link EditPart}(s).
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class ChangeBoundsRequest extends GroupRequest implements IDropRequest {
  private Point m_mouseLocation;
  private Point m_moveDelta = new Point();
  private Dimension m_resizeDelta = new Dimension();
  private int m_resizeDirection;

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
   * <li>{@link org.eclipse.wb.draw2d.IPositionConstants#EAST}
   * <li>{@link org.eclipse.wb.draw2d.IPositionConstants#WEST}
   * <li>{@link org.eclipse.wb.draw2d.IPositionConstants#NORTH}
   * <li>{@link org.eclipse.wb.draw2d.IPositionConstants#SOUTH}
   * <li>{@link org.eclipse.wb.draw2d.IPositionConstants#NORTH_EAST}
   * <li>{@link org.eclipse.wb.draw2d.IPositionConstants#NORTH_WEST}
   * <li>{@link org.eclipse.wb.draw2d.IPositionConstants#SOUTH_EAST}
   * <li>{@link org.eclipse.wb.draw2d.IPositionConstants#SOUTH_WEST}
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
    result.translate(m_moveDelta);
    result.resize(m_resizeDelta);
    return result;
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
    buffer.append(", stateMask=");
    buffer.append(getStateMask());
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