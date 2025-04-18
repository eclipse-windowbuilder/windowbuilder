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
package org.eclipse.wb.core.gef.policy.layout.grid;

import org.eclipse.wb.core.model.IAbstractComponentInfo;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * This interface provides information about grid.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy.grid
 */
public interface IGridInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Dimensions
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the count of columns.
	 */
	int getColumnCount();

	/**
	 * @return the count of rows.
	 */
	int getRowCount();

	////////////////////////////////////////////////////////////////////////////
	//
	// Intervals
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the array of {@link Interval} for each column in grid.
	 */
	Interval[] getColumnIntervals();

	/**
	 * @return the array of {@link Interval} for each row in grid.
	 */
	Interval[] getRowIntervals();

	////////////////////////////////////////////////////////////////////////////
	//
	// Cells
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the cells {@link Rectangle} occupied by given {@link IAbstractComponentInfo}.
	 */
	Rectangle getComponentCells(IAbstractComponentInfo component);

	/**
	 * @return the pixels {@link Rectangle} for given cells {@link Rectangle}.
	 */
	Rectangle getCellsRectangle(Rectangle cells);

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedback
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if container uses right-to-left orientation.
	 */
	boolean isRTL();

	/**
	 * Container's client area may be only partly accessible for grid (for example in Swing "border"
	 * can consume some insets inside of container), so this methods returns the {@link Insets} to
	 * crop client area of container.
	 *
	 * @return the {@link Insets} of container.
	 */
	Insets getInsets();

	////////////////////////////////////////////////////////////////////////////
	//
	// Virtual columns
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this {@link IGridInfo} supports virtual columns. For example in
	 *         GWT widget HTMLTable always filled with columns/rows, so virtual columns/rows don't
	 *         exist.
	 */
	boolean hasVirtualColumns();

	/**
	 * @return the size of virtual column.
	 */
	int getVirtualColumnSize();

	/**
	 * @return the size of virtual column gap.
	 */
	int getVirtualColumnGap();

	////////////////////////////////////////////////////////////////////////////
	//
	// Virtual rows
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this {@link IGridInfo} supports virtual rows. For example in GWT
	 *         widget HTMLTable always filled with columns/rows, so virtual columns/rows don't exist.
	 */
	boolean hasVirtualRows();

	/**
	 * @return the size of virtual row.
	 */
	int getVirtualRowSize();

	/**
	 * @return the size of virtual row gap.
	 */
	int getVirtualRowGap();

	////////////////////////////////////////////////////////////////////////////
	//
	// Checks
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>AbstractComponentInfo</code> that occupies cell with given column/row.
	 */
	IAbstractComponentInfo getOccupied(int column, int row);
}
