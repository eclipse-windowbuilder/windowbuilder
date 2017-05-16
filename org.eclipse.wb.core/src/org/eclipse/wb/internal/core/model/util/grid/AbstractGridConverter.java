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
package org.eclipse.wb.internal.core.model.util.grid;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData.HorizontalAlignment;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData.VerticalAlignment;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper.ComponentGroup;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper.ComponentInGroup;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import java.util.List;

/**
 * Helper for converting coordinates of {@link IAbstractComponentInfo} children to grid-based
 * layout.
 *
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage core.model.util
 */
public abstract class AbstractGridConverter {
  /**
   * Layout container.
   */
  public interface IGridLayoutContainer {
    IAbstractComponentInfo getComponent();

    List<IAbstractComponentInfo> getControls();
  }
  /**
   * Layout.
   */
  public interface IGridLayoutInstance {
    IGridLayoutContainer getContainer();

    IGridLayoutData getLayoutData(IAbstractComponentInfo control);

    void setColumnCount(int value) throws Exception;

    void applyChanges() throws Exception;
  }
  /**
   * Layout data.
   */
  public interface IGridLayoutData {
    IAbstractComponentInfo getComponent();

    IGridLayoutInstance getLayout();

    // cell
    void setGridX(int value) throws Exception;

    void setGridY(int value) throws Exception;

    // size
    void setSpanX(int value) throws Exception;

    void setSpanY(int value) throws Exception;

    // grab
    void setHorizontalGrab(boolean value) throws Exception;

    void setVerticalGrab(boolean value) throws Exception;

    // alignment
    void setHorizontalAlignment(HorizontalAlignment value) throws Exception;

    void setVerticalAlignment(VerticalAlignment value) throws Exception;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void convert(IGridLayoutContainer container, IGridLayoutInstance layout)
      throws Exception {
    // prepare columns and rows and distribute controls in them
    List<ComponentGroup> columns = GridConvertionHelper.buildGroups(container.getControls(), true);
    List<ComponentGroup> rows = GridConvertionHelper.buildGroups(container.getControls(), false);
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
    reorderControls(container, rows);
    // set cell for controls
    int numColumns = columns.size();
    List<IAbstractComponentInfo> appliedControls = Lists.newArrayList();
    for (ComponentGroup column : columns) {
      for (ComponentInGroup component : column.getComponents()) {
        IAbstractComponentInfo control = component.getComponent();
        if (appliedControls.contains(control)) {
          continue;
        }
        // layout data
        GeneralLayoutData generalLayoutData =
            GeneralLayoutData.getFromInfoEx(control.getUnderlyingModel());
        IGridLayoutData gridData = layout.getLayoutData(control);
        // set cell
        Rectangle cells = getCells(component, generalLayoutData, columns, rows);
        gridData.setGridX(cells.x);
        gridData.setGridY(cells.y);
        gridData.setSpanX(cells.width);
        gridData.setSpanY(cells.height);
        // apply general
        applyGeneralLayoutData(gridData, generalLayoutData);
        // update
        appliedControls.add(control);
        numColumns = Math.max(numColumns, cells.x + cells.width);
      }
    }
    // set "numColumns"
    if (numColumns > 0) {
      layout.setColumnCount(numColumns);
    }
    // add fillers
    layout.applyChanges();
  }

  protected static Rectangle getCells(ComponentInGroup component,
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

  protected static void applyGeneralLayoutData(IGridLayoutData gridData,
      GeneralLayoutData generalLayoutData) throws Exception {
    // set grab
    if (generalLayoutData.horizontalGrab != null) {
      gridData.setHorizontalGrab(generalLayoutData.horizontalGrab);
    }
    if (generalLayoutData.verticalGrab != null) {
      gridData.setVerticalGrab(generalLayoutData.verticalGrab);
    }
    // set alignments
    if (generalLayoutData.horizontalAlignment != null) {
      gridData.setHorizontalAlignment(generalLayoutData.horizontalAlignment);
    }
    if (generalLayoutData.verticalAlignment != null) {
      gridData.setVerticalAlignment(generalLayoutData.verticalAlignment);
    }
  }

  protected static void reorderControls(IGridLayoutContainer container, List<ComponentGroup> rows)
      throws Exception {
    // prepare list of controls in reversed order
    List<IAbstractComponentInfo> reversedControls = Lists.newArrayList();
    for (ComponentGroup row : rows) {
      for (ComponentInGroup componentInGroup : row.getComponents()) {
        IAbstractComponentInfo control = componentInGroup.getComponent();
        reversedControls.add(0, control);
      }
    }
    // do reorder
    IAbstractComponentInfo nextControl = null;
    for (IAbstractComponentInfo control : reversedControls) {
      if (nextControl != null) {
        boolean alreadyRightOrder =
            GenericsUtils.getNextOrNull(container.getControls(), control) == nextControl;
        if (!alreadyRightOrder) {
          GlobalState.getOrderProcessor().move(control, nextControl);
        }
      }
      nextControl = control;
    }
  }
}
