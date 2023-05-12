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
package org.eclipse.wb.internal.swing.MigLayout.model;

import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper.ComponentGroup;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper.ComponentInGroup;
import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo.Alignment;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.Set;

/**
 * Helper for converting coordinates of {@link ComponentInfo} children to {@link MigLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.model
 */
public final class MigLayoutConverter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private MigLayoutConverter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void convert(ContainerInfo parent, MigLayoutInfo layout) throws Exception {
    // prepare columns and rows and distribute controls in them
    List<ComponentGroup> columns =
        GridConvertionHelper.buildGroups(parent.getChildrenComponents(), true);
    List<ComponentGroup> rows =
        GridConvertionHelper.buildGroups(parent.getChildrenComponents(), false);
    // sort components in columns and rows
    GridConvertionHelper.sortGroupsByTranspose(columns, rows);
    GridConvertionHelper.sortGroupsByTranspose(rows, columns);
    // ensure that columns and rows are sorted by start coordinates
    GridConvertionHelper.sortGroups(columns);
    GridConvertionHelper.sortGroups(rows);
    // calculate begin/end for each column/row
    GridConvertionHelper.updateBoundsGaps(columns, true);
    GridConvertionHelper.updateBoundsGaps(rows, true);
    // create dimensions in layout
    {
      createDimensions(layout, columns, true);
      createDimensions(layout, rows, false);
    }
    // prepare set of components in groups
    Set<ComponentInGroup> componentsInGroups = Sets.newHashSet();
    for (ComponentGroup column : columns) {
      for (ComponentInGroup componentInGroup : column.getComponents()) {
        componentsInGroups.add(componentInGroup);
      }
    }
    // create constraints for each control
    for (ComponentInGroup componentInGroup : componentsInGroups) {
      ComponentInfo component = (ComponentInfo) componentInGroup.getComponent();
      // layout data
      GeneralLayoutData generalLayoutData = GeneralLayoutData.getFromInfoEx(component);
      CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(component);
      // prepare begin/end column/row
      ComponentGroup beginColumn =
          GridConvertionHelper.getBeginForComponent(columns, componentInGroup);
      ComponentGroup endColumn = GridConvertionHelper.getEndForComponent(columns, componentInGroup);
      ComponentGroup beginRow = GridConvertionHelper.getBeginForComponent(rows, componentInGroup);
      ComponentGroup endRow = GridConvertionHelper.getEndForComponent(rows, componentInGroup);
      // update cell coordinates
      {
        // calculate location/size
        int x = columns.indexOf(beginColumn);
        int y = rows.indexOf(beginRow);
        int sx = 1 + columns.indexOf(endColumn) - x;
        int sy = 1 + rows.indexOf(endRow) - y;
        // set location/size
        constraints.setX(x);
        constraints.setY(y);
        constraints.setWidth(sx);
        constraints.setHeight(sy);
      }
      // alignments
      constraints.setHorizontalAlignment(getHorizontalAlignment(
          columns,
          componentInGroup,
          generalLayoutData));
      constraints.setVerticalAlignment(getVerticalAlignment(
          rows,
          componentInGroup,
          generalLayoutData));
      // write constraints
      constraints.write();
    }
    // write dimensions
    layout.setObject(new MigLayout());
    layout.writeDimensions();
    // remove empty columns
    for (int i = columns.size() - 1; i >= 0; i--) {
      if (columns.get(i).getComponents().isEmpty()) {
        layout.deleteColumn(i);
      }
    }
    // remove empty rows
    for (int i = rows.size() - 1; i >= 0; i--) {
      if (rows.get(i).getComponents().isEmpty()) {
        layout.deleteRow(i);
      }
    }
  }

  /**
   * Creates {@link MigColumnInfo} or {@link MigRowInfo} for given {@link ComponentGroup}'s.
   */
  private static void createDimensions(MigLayoutInfo layout,
      List<ComponentGroup> groups,
      boolean horizontal) throws Exception {
    for (ComponentGroup group : groups) {
      // create new "default" dimension
      MigDimensionInfo dimension;
      if (horizontal) {
        MigColumnInfo column = new MigColumnInfo(layout);
        layout.getColumns().add(column);
        dimension = column;
      } else {
        MigRowInfo row = new MigRowInfo(layout);
        layout.getRows().add(row);
        dimension = row;
      }
      // set constant size
      dimension.setSize(group.getSize() + "px");
    }
  }

  /**
   * Calculate horizontal alignment.
   */
  private static MigColumnInfo.Alignment getHorizontalAlignment(List<ComponentGroup> columns,
      ComponentInGroup componentInGroup,
      GeneralLayoutData generalLayoutData) {
    if (generalLayoutData.horizontalAlignment != null) {
      // from general layout data
      MigColumnInfo.Alignment alignment =
          GeneralLayoutData.getRealValue(
              MigLayoutInfo.m_horizontalAlignmentMap,
              generalLayoutData.horizontalAlignment);
      if (alignment != null && alignment != MigColumnInfo.Alignment.UNKNOWN) {
        return alignment;
      }
    }
    // calculate
    ComponentInfo component = (ComponentInfo) componentInGroup.getComponent();
    ComponentGroup beginColumn =
        GridConvertionHelper.getBeginForComponent(columns, componentInGroup);
    ComponentGroup endColumn = GridConvertionHelper.getEndForComponent(columns, componentInGroup);
    //
    Rectangle bounds = component.getBounds();
    Dimension prefSize = component.getPreferredSize();
    int bl = bounds.x;
    int br = bounds.right();
    int columnLeft = beginColumn.getMin();
    int columnRight = endColumn.getMax();
    int columnCenter = columnLeft + (columnRight - columnLeft) / 2;
    //
    int leftOffset = Math.abs(bl - columnLeft);
    int rightOffset = Math.abs(columnRight - br);
    // prepare how much location of two sides will be changed for each alignment
    int leftDelta = leftOffset + Math.abs(columnLeft + prefSize.width - br);
    int rightDelta = rightOffset + Math.abs(columnRight - prefSize.width - bl);
    int fillDelta = leftOffset + rightOffset;
    int centerDelta =
        Math.abs(bl - (columnCenter - prefSize.width / 2))
            + Math.abs(br - (columnCenter + prefSize.width / 2));
    // set alignment
    return getAlignment(
        new int[]{leftDelta, centerDelta, rightDelta, fillDelta},
        new MigColumnInfo.Alignment[]{
            MigColumnInfo.Alignment.LEFT,
            MigColumnInfo.Alignment.CENTER,
            MigColumnInfo.Alignment.RIGHT,
            MigColumnInfo.Alignment.FILL});
  }

  /**
   * Calculate horizontal alignment.
   */
  private static MigRowInfo.Alignment getVerticalAlignment(List<ComponentGroup> rows,
      ComponentInGroup componentInGroup,
      GeneralLayoutData generalLayoutData) {
    if (generalLayoutData.verticalAlignment != null) {
      // from general layout data
      MigRowInfo.Alignment alignment =
          GeneralLayoutData.getRealValue(
              MigLayoutInfo.m_verticalAlignmentMap,
              generalLayoutData.verticalAlignment);
      if (alignment != null && alignment != MigRowInfo.Alignment.UNKNOWN) {
        return alignment;
      }
    }
    // calculate
    ComponentInfo component = (ComponentInfo) componentInGroup.getComponent();
    // prepare begin/end column/row
    ComponentGroup beginRow = GridConvertionHelper.getBeginForComponent(rows, componentInGroup);
    ComponentGroup endRow = GridConvertionHelper.getEndForComponent(rows, componentInGroup);
    Rectangle bounds = component.getBounds();
    Dimension prefSize = component.getPreferredSize();
    int bt = bounds.y;
    int bb = bounds.bottom();
    int rowTop = beginRow.getMin();
    int rowBottom = endRow.getMax();
    int rowCenter = rowTop + (rowBottom - rowTop) / 2;
    //
    int topOffset = bt - rowTop;
    int bottomOffset = rowBottom - bb;
    // prepare how much location of two sides will be changed for each alignment
    int topDelta = topOffset + Math.abs(rowTop + prefSize.height - bb);
    int bottomDelta = bottomOffset + Math.abs(rowBottom - prefSize.height - bt);
    int fillDelta = topOffset + bottomOffset;
    int centerDelta =
        Math.abs(bt - (rowCenter - prefSize.height / 2))
            + Math.abs(bb - (rowCenter + prefSize.height / 2));
    // set alignment
    return getAlignment(
        new int[]{topDelta, centerDelta, bottomDelta, fillDelta},
        new MigRowInfo.Alignment[]{
            MigRowInfo.Alignment.TOP,
            MigRowInfo.Alignment.CENTER,
            MigRowInfo.Alignment.BOTTOM,
            MigRowInfo.Alignment.FILL});
  }

  /**
   * @return the {@link Alignment} corresponding to the minimum delta value.
   */
  private static <A extends Enum<?>> A getAlignment(int[] deltas, A[] alignments) {
    int minimum;
    {
      minimum = Integer.MAX_VALUE;
      for (int i = 0; i < deltas.length; i++) {
        int delta = deltas[i];
        minimum = Math.min(minimum, delta);
      }
    }
    // return corresponding alignment
    return alignments[ArrayUtils.indexOf(deltas, minimum)];
  }
}
