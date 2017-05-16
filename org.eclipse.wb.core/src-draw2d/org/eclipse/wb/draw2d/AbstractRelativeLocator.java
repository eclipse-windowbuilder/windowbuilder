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
package org.eclipse.wb.draw2d;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;

/**
 * Implementation of {@link ILocator} that relocates target {@link Figure} relative to the some
 * reference {@link Rectangle} using two floating-point value indicating the horizontal and vertical
 * offset from that reference {@link Rectangle}. The values (0.0, 0.0) would indicate the top-left
 * corner, while the values (1.0, 1.0) would indicate the bottom-right corner.
 * <P>
 * Constants such as {@link IPositionConstants#NORTH NORTH} and {@link IPositionConstants#SOUTH
 * SOUTH} can be used to set the placement.
 *
 * @author scheglov_ke
 * @coverage gef.draw2d
 */
public abstract class AbstractRelativeLocator implements ILocator, IPositionConstants {
  private final double m_relativeX;
  private final double m_relativeY;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructs a {@link AbstractRelativeLocator} with the given offset ratios.
   */
  public AbstractRelativeLocator(double relativeX, double relativeY) {
    m_relativeX = relativeX;
    m_relativeY = relativeY;
  }

  /**
   * Constructs a {@link AbstractRelativeLocator} with the given relative location. The location is
   * a constant from {@link IPositionConstants} used as a convenient and readable way to set both
   * the relativeX and relativeY values.
   */
  public AbstractRelativeLocator(int location) {
    switch (location & NORTH_SOUTH) {
      case NORTH :
        m_relativeY = 0;
        break;
      case SOUTH :
        m_relativeY = 1.0;
        break;
      default :
        m_relativeY = 0.5;
    }
    //
    switch (location & EAST_WEST) {
      case WEST :
        m_relativeX = 0;
        break;
      case EAST :
        m_relativeX = 1.0;
        break;
      default :
        m_relativeX = 0.5;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ILocator
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Relocates the target using the relative offset locations.
   */
  public final void relocate(Figure target) {
    // prepare reference
    Rectangle reference = getReferenceRectangle();
    FigureUtils.translateAbsoluteToFigure(target, reference);
    // prepare target location
    Dimension targetSize = target.getSize();
    int x = reference.x + (int) (reference.width * m_relativeX) - (targetSize.width + 1) / 2;
    int y = reference.y + (int) (reference.height * m_relativeY) - (targetSize.height + 1) / 2;
    // set target location
    target.setLocation(x, y);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractRelativeLocator
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the reference {@link Rectangle} in absolute coordinates.
   */
  protected abstract Rectangle getReferenceRectangle();
}