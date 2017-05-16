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
package org.eclipse.wb.internal.core.gef.policy.snapping;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;

/**
 * Utility class for working with SWT FormLayout.
 *
 * @author mitin_aa
 * @coverage swt.model.layout.form
 */
public final class PlacementUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Side
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the constant which represents 'opposite' side in one dimension, i.e. for LEFT it
   *         returns RIGHT.
   */
  public static int getOppositeSide(int side) {
    switch (side) {
      case IPositionConstants.LEFT :
        return IPositionConstants.RIGHT;
      case IPositionConstants.RIGHT :
        return IPositionConstants.LEFT;
      case IPositionConstants.TOP :
        return IPositionConstants.BOTTOM;
      case IPositionConstants.BOTTOM :
        return IPositionConstants.TOP;
      default :
        throw new IllegalArgumentException("Invalid side requested: " + side);
    }
  }

  public static boolean isTrailingSide(int side) {
    return side == IPositionConstants.RIGHT || side == IPositionConstants.BOTTOM;
  }

  /**
   * @return <code>true</code> if <code>side</code> either of {@link IPositionConstants#LEFT} or
   *         {@link IPositionConstants#TOP}
   */
  public static boolean isLeadingSide(int side) {
    return side == IPositionConstants.LEFT || side == IPositionConstants.TOP;
  }

  public static boolean isHorizontalSide(int side) {
    return side == IPositionConstants.LEFT || side == IPositionConstants.RIGHT;
  }

  public static int getSide(boolean isHorizontal, boolean isLeading) {
    if (isLeading) {
      return isHorizontal ? IPositionConstants.LEFT : IPositionConstants.TOP;
    } else {
      return isHorizontal ? IPositionConstants.RIGHT : IPositionConstants.BOTTOM;
    }
  }

  /**
   * Returns either of {@link PlacementInfo#LEADING} or {@link PlacementInfo#TRAILING}
   *
   * @param side
   *          the one of the {@link IPositionConstants#LEFT}, {@link IPositionConstants#RIGHT},
   *          {@link IPositionConstants#TOP}, {@link IPositionConstants#BOTTOM}.
   */
  public static int getSidePosition(int side) {
    return PlacementUtils.isTrailingSide(side) ? PlacementInfo.TRAILING : PlacementInfo.LEADING;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  public static int extractResizingSide(boolean isHorizontal, int side) {
    if (isHorizontal) {
      if ((side & IPositionConstants.WEST) != 0) {
        return IPositionConstants.LEFT;
      } else if ((side & IPositionConstants.EAST) != 0) {
        return IPositionConstants.RIGHT;
      }
    } else {
      if ((side & IPositionConstants.NORTH) != 0) {
        return IPositionConstants.TOP;
      } else if ((side & IPositionConstants.SOUTH) != 0) {
        return IPositionConstants.BOTTOM;
      }
    }
    throw new IllegalArgumentException("Wrong side value: " + side);
  }

  public static boolean hasHorizontalResizeSide(int resizeDirection) {
    return (resizeDirection & IPositionConstants.WEST) != 0
        || (resizeDirection & IPositionConstants.EAST) != 0;
  }

  public static boolean hasVerticalResizeSide(int resizeDirection) {
    return (resizeDirection & IPositionConstants.NORTH) != 0
        || (resizeDirection & IPositionConstants.SOUTH) != 0;
  }

  public static int getSideSize(Dimension size, int side) {
    if (isHorizontalSide(side)) {
      return size.width;
    } else {
      return size.height;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  public static Rectangle getTranslatedBounds(IVisualDataProvider visualDataProvider,
      IAbstractComponentInfo widget) {
    return getTranslatedBounds(visualDataProvider, widget.getModelBounds().getCopy());
  }

  public static Rectangle getTranslatedBounds(IVisualDataProvider visualDataProvider,
      Rectangle bounds) {
    return getTranslatedBounds(visualDataProvider.getClientAreaOffset(), bounds);
  }

  public static Rectangle getTranslatedBounds(Point clientAreaOffset, IAbstractComponentInfo widget) {
    return getTranslatedBounds(clientAreaOffset, widget.getModelBounds().getCopy());
  }

  public static Rectangle getTranslatedBounds(Point clientAreaOffset, Rectangle bounds) {
    return bounds.getTranslated(clientAreaOffset.getNegated());
  }

  public static int getSize(IAbstractComponentInfo component, boolean isHorizontal) {
    Rectangle bounds = component.getModelBounds();
    return isHorizontal ? bounds.width : bounds.height;
  }
}
