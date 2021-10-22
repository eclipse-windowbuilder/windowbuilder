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
package org.eclipse.wb.internal.rcp.model.forms.layout.table;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.swt.model.layout.ILayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.ui.forms.widgets.TableWrapLayout;

import java.util.List;

/**
 * Interface model for {@link TableWrapLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public interface ITableWrapLayoutInfo<C extends IControlInfo> extends ILayoutInfo<C> {
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
   * @return the {@link List} of {@link TableWrapColumnInfo}.
   */
  List<TableWrapColumnInfo<C>> getColumns();

  /**
   * @return the {@link List} of {@link TableWrapRowInfo}.
   */
  List<TableWrapRowInfo<C>> getRows();

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
   * Sets the height hint for given {@link IControlInfo}.
   */
  void command_setHeightHint(C control, int size) throws Exception;

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
   * @return {@link ITableWrapDataInfo} associated with given {@link IControlInfo}.
   */
  ITableWrapDataInfo getTableWrapData2(C control);
}
