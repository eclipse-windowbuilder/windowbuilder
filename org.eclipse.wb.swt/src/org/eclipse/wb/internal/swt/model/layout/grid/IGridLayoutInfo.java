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
package org.eclipse.wb.internal.swt.model.layout.grid;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.swt.model.layout.ILayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.swt.layout.GridLayout;

import java.util.List;

/**
 * Interface model for SWT {@link GridLayout}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public interface IGridLayoutInfo<C extends IControlInfo> extends ILayoutInfo<C> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link IControlInfo} is filler.
   */
  boolean isFiller(C control);

  /**
   * "Fixes" grid, i.e. ensures that all cells are filled (at least with fillers), even if this is
   * not strongly required by layout itself for final cells. We do this to avoid checks for
   * <code>null</code> in many places.
   */
  void fixGrid() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dimensions access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link List} of {@link GridColumnInfo}.
   */
  List<GridColumnInfo<C>> getColumns();

  /**
   * @return the {@link List} of {@link GridRowInfo}.
   */
  List<GridRowInfo<C>> getRows();

  /**
   * @return <code>true</code> if dimensions of this layout can be changed. We can change them only
   *         if we created layout using constructor.
   */
  boolean canChangeDimensions();

  /**
   * @return <code>true</code> if row with given index consists of explicit {@link IControlInfo}s,
   *         so can be moved and used as insert target.
   */
  boolean isExplicitRow(int row);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dimensions operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Deletes column with given index and all controls that located in this column.
   */
  void command_deleteColumn(int column, boolean deleteEmptyRows) throws Exception;

  /**
   * Deletes row with given index and all controls that located in this row.
   */
  void command_deleteRow(int row, boolean deleteEmptyColumn) throws Exception;

  /**
   * Moves column from/to given index.
   */
  void command_MOVE_COLUMN(int fromIndex, int toIndex) throws Exception;

  /**
   * Moves row from/to given index.
   */
  void command_MOVE_ROW(int fromIndex, int toIndex) throws Exception;

  /**
   * If there are components that span multiple columns/rows, and no other "real" components in
   * these columns/rows, then removes these excess columns/rows.
   */
  void command_normalizeSpanning() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link IControlInfo} in given cell.
   *
   * @param newControl
   *          the new {@link IControlInfo} to create.
   * @param column
   *          the column (0 based).
   * @param row
   *          the row (0 based).
   */
  void command_CREATE(C newControl, int column, boolean insertColumn, int row, boolean insertRow)
      throws Exception;

  /**
   * Moves existing {@link IControlInfo} into new cell.
   */
  void command_MOVE(C control, int column, boolean insertColumn, int row, boolean insertRow)
      throws Exception;

  /**
   * Adds {@link IControlInfo} from other parent into cell.
   */
  void command_ADD(C control, int column, boolean insertColumn, int row, boolean insertRow)
      throws Exception;

  /**
   * Sets the cells occupied by given {@link IControlInfo}.
   *
   * @param forMove
   *          is <code>true</code> if we move control and <code>false</code> if we set cells for
   *          newly added control.
   */
  void command_setCells(C control, Rectangle cells, boolean forMove) throws Exception;

  /**
   * Sets the size hint for given {@link IControlInfo}.
   */
  void command_setSizeHint(C control, boolean horizontal, Dimension size) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGridInfo support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IGridInfo} that describes this layout.
   */
  IGridInfo getGridInfo();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout data
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link IGridDataInfo} associated with given {@link IControlInfo}.
   */
  IGridDataInfo getGridData2(C control);
}