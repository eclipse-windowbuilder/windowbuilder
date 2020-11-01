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

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper.ComponentGroup;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper.ComponentInGroup;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.swt.model.widgets.ICompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for converting coordinates of {@link ComponentInfo} children to
 * {@link ITableWrapLayoutInfo}.
 *
 * @author sablin_aa
 * @coverage rcp.model.forms
 */
public final class TableWrapLayoutConverter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private TableWrapLayoutConverter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public static <T extends IControlInfo> void convert(ICompositeInfo parent,
      ITableWrapLayoutInfo<T> layout) throws Exception {
    // prepare columns and rows and distribute controls in them
    List<ComponentGroup> columns =
        GridConvertionHelper.buildGroups(parent.getChildrenControls(), true);
    List<ComponentGroup> rows =
        GridConvertionHelper.buildGroups(parent.getChildrenControls(), false);
    // sort components in columns and rows
    GridConvertionHelper.sortGroupsByTranspose(columns, rows);
    GridConvertionHelper.sortGroupsByTranspose(rows, columns);
    // ensure that columns and rows are sorted by start coordinates
    GridConvertionHelper.sortGroups(columns);
    GridConvertionHelper.sortGroups(rows);
    // calculate begin/end for each column/row
    GridConvertionHelper.updateBoundsGaps(columns, false);
    GridConvertionHelper.updateBoundsGaps(rows, false);
    // reorder controls
    reorderControls(parent, rows);
    // set cell for controls
    int numColumns = columns.size();
    List<IControlInfo> appliedControls = new ArrayList<>();
    for (ComponentGroup column : columns) {
      for (ComponentInGroup component : column.getComponents()) {
        T control = (T) component.getComponent();
        if (appliedControls.contains(control)) {
          continue;
        }
        // layout data
        GeneralLayoutData generalLayoutData =
            GeneralLayoutData.getFromInfoEx(control.getUnderlyingModel());
        ITableWrapDataInfo gridData = layout.getTableWrapData2(control);
        // set cell
        Rectangle cells = getCells(component, generalLayoutData, columns, rows);
        gridData.setX(cells.x);
        gridData.setY(cells.y);
        gridData.setHorizontalSpan(cells.width);
        gridData.setVerticalSpan(cells.height);
        // apply general
        applyGeneralLayoutData(gridData, generalLayoutData);
        // update
        appliedControls.add(control);
        numColumns = Math.max(numColumns, cells.x + cells.width);
      }
    }
    // set "numColumns"
    if (numColumns > 0) {
      layout.getPropertyByTitle("numColumns").setValue(numColumns);
    }
    // add fillers
    layout.fixGrid();
  }

  private static Rectangle getCells(ComponentInGroup component,
      GeneralLayoutData generalLayoutData,
      List<ComponentGroup> columns,
      List<ComponentGroup> rows) {
    int x = -1;
    int y = -1;
    int w = 1;
    int h = 1;
    // prepare begin/end column/row
    ComponentGroup beginColumn = GridConvertionHelper.getBeginForComponent(columns, component);
    ComponentGroup endColumn = GridConvertionHelper.getEndForComponent(columns, component);
    ComponentGroup beginRow = GridConvertionHelper.getBeginForComponent(rows, component);
    ComponentGroup endRow = GridConvertionHelper.getEndForComponent(rows, component);
    // prepare cell
    x = columns.indexOf(beginColumn);
    y = rows.indexOf(beginRow);
    w = 1 + columns.indexOf(endColumn) - x;
    h = 1 + rows.indexOf(endRow) - y;
    // use remembered values
    {
      if (generalLayoutData.gridX != null) {
        x = generalLayoutData.gridX;
        w = 1;
      }
      if (generalLayoutData.gridY != null) {
        y = generalLayoutData.gridY;
        h = 1;
      }
      if (generalLayoutData.spanX != null) {
        w = generalLayoutData.spanX;
      }
      if (generalLayoutData.spanY != null) {
        h = generalLayoutData.spanY;
      }
    }
    //
    return new Rectangle(x, y, w, h);
  }

  private static void applyGeneralLayoutData(ITableWrapDataInfo gridData,
      GeneralLayoutData generalLayoutData) throws Exception {
    // set grab
    if (generalLayoutData.horizontalGrab != null) {
      gridData.setHorizontalGrab(generalLayoutData.horizontalGrab);
    }
    if (generalLayoutData.verticalGrab != null) {
      gridData.setVerticalGrab(generalLayoutData.verticalGrab);
    }
    // set alignments
    Integer horizontalAlignmentValue = GeneralLayoutData.getRealValue(
        TableWrapLayoutInfo.m_horizontalAlignmentMap,
        generalLayoutData.horizontalAlignment);
    if (horizontalAlignmentValue != null) {
      gridData.setHorizontalAlignment(horizontalAlignmentValue);
    }
    Integer verticalAlignmentValue = GeneralLayoutData.getRealValue(
        TableWrapLayoutInfo.m_verticalAlignmentMap,
        generalLayoutData.verticalAlignment);
    if (verticalAlignmentValue != null) {
      gridData.setVerticalAlignment(verticalAlignmentValue);
    }
  }

  private static void reorderControls(ICompositeInfo parent, List<ComponentGroup> rows)
      throws Exception {
    // prepare list of controls in reversed order
    List<IControlInfo> reversedControls = new ArrayList<>();
    for (ComponentGroup row : rows) {
      for (ComponentInGroup componentInGroup : row.getComponents()) {
        IControlInfo control = (IControlInfo) componentInGroup.getComponent();
        reversedControls.add(0, control);
      }
    }
    // do reorder
    IControlInfo nextControl = null;
    for (IControlInfo control : reversedControls) {
      if (nextControl != null) {
        if (!isAlreadyRightOrder(control, nextControl)) {
          GlobalState.getOrderProcessor().move(control, nextControl);
        }
      }
      nextControl = control;
    }
  }

  /**
   * @return <code>true</code> if "control" is directly before "nextControl", so no need to move
   *         "control".
   */
  private static boolean isAlreadyRightOrder(IControlInfo control, IControlInfo nextControl) {
    ICompositeInfo composite = (ICompositeInfo) control.getParent();
    List<? extends IControlInfo> controls = composite.getChildrenControls();
    return GenericsUtils.getNextOrNull(controls, control) == nextControl;
  }
}
