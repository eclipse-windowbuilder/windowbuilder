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

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

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
		case PositionConstants.LEFT :
			return PositionConstants.RIGHT;
		case PositionConstants.RIGHT :
			return PositionConstants.LEFT;
		case PositionConstants.TOP :
			return PositionConstants.BOTTOM;
		case PositionConstants.BOTTOM :
			return PositionConstants.TOP;
		default :
			throw new IllegalArgumentException("Invalid side requested: " + side);
		}
	}

	public static boolean isTrailingSide(int side) {
		return side == PositionConstants.RIGHT || side == PositionConstants.BOTTOM;
	}

	/**
	 * @return <code>true</code> if <code>side</code> either of {@link PositionConstants#LEFT} or
	 *         {@link PositionConstants#TOP}
	 */
	public static boolean isLeadingSide(int side) {
		return side == PositionConstants.LEFT || side == PositionConstants.TOP;
	}

	public static boolean isHorizontalSide(int side) {
		return side == PositionConstants.LEFT || side == PositionConstants.RIGHT;
	}

	public static int getSide(boolean isHorizontal, boolean isLeading) {
		if (isLeading) {
			return isHorizontal ? PositionConstants.LEFT : PositionConstants.TOP;
		} else {
			return isHorizontal ? PositionConstants.RIGHT : PositionConstants.BOTTOM;
		}
	}

	/**
	 * Returns either of {@link PlacementInfo#LEADING} or {@link PlacementInfo#TRAILING}
	 *
	 * @param side
	 *          the one of the {@link PositionConstants#LEFT}, {@link PositionConstants#RIGHT},
	 *          {@link PositionConstants#TOP}, {@link PositionConstants#BOTTOM}.
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
			if ((side & PositionConstants.WEST) != 0) {
				return PositionConstants.LEFT;
			} else if ((side & PositionConstants.EAST) != 0) {
				return PositionConstants.RIGHT;
			}
		} else {
			if ((side & PositionConstants.NORTH) != 0) {
				return PositionConstants.TOP;
			} else if ((side & PositionConstants.SOUTH) != 0) {
				return PositionConstants.BOTTOM;
			}
		}
		throw new IllegalArgumentException("Wrong side value: " + side);
	}

	public static boolean hasHorizontalResizeSide(int resizeDirection) {
		return (resizeDirection & PositionConstants.WEST) != 0
				|| (resizeDirection & PositionConstants.EAST) != 0;
	}

	public static boolean hasVerticalResizeSide(int resizeDirection) {
		return (resizeDirection & PositionConstants.NORTH) != 0
				|| (resizeDirection & PositionConstants.SOUTH) != 0;
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
