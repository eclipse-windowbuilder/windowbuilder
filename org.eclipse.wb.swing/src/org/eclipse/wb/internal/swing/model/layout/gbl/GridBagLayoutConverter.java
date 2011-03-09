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
package org.eclipse.wb.internal.swing.model.layout.gbl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper.ComponentGroup;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper.ComponentInGroup;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.apache.commons.lang.ArrayUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Helper for converting coordinates of {@link ComponentInfo} children to
 * {@link AbstractGridBagLayoutInfo} constraints.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public class GridBagLayoutConverter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private GridBagLayoutConverter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void convert(ContainerInfo parent, AbstractGridBagLayoutInfo layout)
      throws Exception {
    layout.getRoot().refreshLight();
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
      layout.getColumns().clear();
      layout.getRows().clear();
      createDimensions(layout.getColumnOperations(), columns);
      createDimensions(layout.getRowOperations(), rows);
    }
    // prepare set of components in groups
    Set<ComponentInGroup> componentsInGroups = Sets.newHashSet();
    for (ComponentGroup column : columns) {
      for (ComponentInGroup componentInGroup : column.getComponents()) {
        componentsInGroups.add(componentInGroup);
      }
    }
    // create constraints for each control
    List<ComponentInfo> appliedControls = Lists.newArrayList();
    for (ComponentInGroup componentInGroup : componentsInGroups) {
      ComponentInfo component = (ComponentInfo) componentInGroup.getComponent();
      if (appliedControls.contains(component)) {
        continue;
      }
      // layout data
      GeneralLayoutData generalLayoutData = GeneralLayoutData.getFromInfoEx(component);
      AbstractGridBagConstraintsInfo constraints = layout.getConstraints(component);
      // location/size
      Rectangle cell = getCells(columns, rows, componentInGroup, generalLayoutData);
      // alignments
      ColumnInfo.Alignment horizontalAlignment =
          getHorzontalAlignment(columns, componentInGroup, generalLayoutData);
      RowInfo.Alignment verticalAlignment =
          getVerticalAlignment(rows, componentInGroup, generalLayoutData);
      // apply layout data
      layout.command_setCells(component, cell);
      constraints.setAlignment(horizontalAlignment, verticalAlignment);
      appliedControls.add(component);
    }
    // remove empty/small columns/rows
    {
      final Set<Integer> filledColumns = Sets.newHashSet();
      final Set<Integer> filledRows = Sets.newHashSet();
      layout.visitComponents(new IComponentVisitor() {
        public void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints)
            throws Exception {
          filledColumns.add(constraints.x);
          filledRows.add(constraints.y);
        }
      });
      // do remove
      removeEmptyDimensions(layout.getColumnOperations(), filledColumns);
      removeEmptyDimensions(layout.getRowOperations(), filledRows);
    }
  }

  /**
   * Calculate cell position & size.
   */
  private static Rectangle getCells(List<ComponentGroup> columns,
      List<ComponentGroup> rows,
      ComponentInGroup componentInGroup,
      GeneralLayoutData generalLayoutData) {
    Rectangle cell;
    /*
    if (generalLayoutData.gridX != null
    	&& generalLayoutData.gridY != null
    	&& generalLayoutData.spanX != null
    	&& generalLayoutData.spanY != null) {
    	// from general layout data
    	cell =
    			new Rectangle(generalLayoutData.gridX,
    				generalLayoutData.gridY,
    				generalLayoutData.spanX,
    				generalLayoutData.spanY);
    } else */{
      // calculate
      ComponentGroup beginColumn =
          GridConvertionHelper.getBeginForComponent(columns, componentInGroup);
      ComponentGroup endColumn = GridConvertionHelper.getEndForComponent(columns, componentInGroup);
      ComponentGroup beginRow = GridConvertionHelper.getBeginForComponent(rows, componentInGroup);
      ComponentGroup endRow = GridConvertionHelper.getEndForComponent(rows, componentInGroup);
      int x = columns.indexOf(beginColumn);
      int y = rows.indexOf(beginRow);
      int sx = 1 + columns.indexOf(endColumn) - x;
      int sy = 1 + rows.indexOf(endRow) - y;
      cell = new Rectangle(x, y, sx, sy);
    }
    return cell;
  }

  /**
   * Calculate horizontal alignment.
   */
  private static ColumnInfo.Alignment getHorzontalAlignment(List<ComponentGroup> columns,
      ComponentInGroup componentInGroup,
      GeneralLayoutData generalLayoutData) {
    if (generalLayoutData.horizontalAlignment != null) {
      // from general layout data
      ColumnInfo.Alignment alignment =
          GeneralLayoutData.getRealValue(
              AbstractGridBagLayoutInfo.m_horizontalAlignmentMap,
              generalLayoutData.horizontalAlignment);
      if (alignment != null && alignment != ColumnInfo.Alignment.UNKNOWN) {
        return alignment;
      }
    }
    // calculate
    ComponentInfo component = (ComponentInfo) componentInGroup.getComponent();
    Rectangle bounds = component.getBounds();
    Dimension prefSize = component.getPreferredSize();
    ComponentGroup beginColumn =
        GridConvertionHelper.getBeginForComponent(columns, componentInGroup);
    ComponentGroup endColumn = GridConvertionHelper.getEndForComponent(columns, componentInGroup);
    int columnLeft = beginColumn.getMin();
    int columnRight = endColumn.getMax();
    int columnCenter = columnLeft + (columnRight - columnLeft) / 2;
    int leftOffset = Math.abs(bounds.x - columnLeft);
    int rightOffset = Math.abs(columnRight - bounds.right());
    // prepare how much location of two sides will be changed for each alignment
    int leftDelta = leftOffset + Math.abs(columnLeft + prefSize.width - bounds.right());
    int rightDelta = rightOffset + Math.abs(columnRight - prefSize.width - bounds.x);
    int fillDelta = leftOffset + rightOffset;
    int centerDelta =
        Math.abs(bounds.x - (columnCenter - prefSize.width / 2))
            + Math.abs(bounds.right() - (columnCenter + prefSize.width / 2));
    // prepare alignment
    return getAlignment(
        new int[]{leftDelta, centerDelta, rightDelta, fillDelta},
        new ColumnInfo.Alignment[]{
            ColumnInfo.Alignment.LEFT,
            ColumnInfo.Alignment.CENTER,
            ColumnInfo.Alignment.RIGHT,
            ColumnInfo.Alignment.FILL});
  }

  /**
   * Calculate vertical alignment.
   */
  private static RowInfo.Alignment getVerticalAlignment(List<ComponentGroup> rows,
      ComponentInGroup componentInGroup,
      GeneralLayoutData generalLayoutData) {
    if (generalLayoutData.verticalAlignment != null) {
      // from general layout data
      RowInfo.Alignment alignment =
          GeneralLayoutData.getRealValue(
              AbstractGridBagLayoutInfo.m_verticalAlignmentMap,
              generalLayoutData.verticalAlignment);
      if (alignment != null && alignment != RowInfo.Alignment.UNKNOWN) {
        return alignment;
      }
    }
    // calculate
    ComponentInfo component = (ComponentInfo) componentInGroup.getComponent();
    Rectangle bounds = component.getBounds();
    Dimension prefSize = component.getPreferredSize();
    ComponentGroup beginRow = GridConvertionHelper.getBeginForComponent(rows, componentInGroup);
    ComponentGroup endRow = GridConvertionHelper.getEndForComponent(rows, componentInGroup);
    int rowTop = beginRow.getMin();
    int rowBottom = endRow.getMax();
    int rowCenter = rowTop + (rowBottom - rowTop) / 2;
    int topOffset = bounds.y - rowTop;
    int bottomOffset = rowBottom - bounds.bottom();
    // prepare how much location of two sides will be changed for each alignment
    int topDelta = topOffset + Math.abs(rowTop + prefSize.height - bounds.bottom());
    int bottomDelta = bottomOffset + Math.abs(rowBottom - prefSize.height - bounds.y);
    int fillDelta = topOffset + bottomOffset;
    int centerDelta =
        Math.abs(bounds.y - (rowCenter - prefSize.height / 2))
            + Math.abs(bounds.bottom() - (rowCenter + prefSize.height / 2));
    // prepare alignment
    return getAlignment(
        new int[]{topDelta, centerDelta, bottomDelta, fillDelta},
        new RowInfo.Alignment[]{
            RowInfo.Alignment.TOP,
            RowInfo.Alignment.CENTER,
            RowInfo.Alignment.BOTTOM,
            RowInfo.Alignment.FILL});
  }

  /**
   * Creates {@link DimensionInfo}'s for given {@link ComponentGroup}'s.
   */
  private static <T extends DimensionInfo> void createDimensions(DimensionOperations<T> operations,
      List<ComponentGroup> groups) throws Exception {
    int index = 0;
    for (ComponentGroup group : groups) {
      T dimension = operations.insert(index++);
      dimension.setSize(group.getSize());
    }
  }

  /**
   * Removes {@link DimensionInfo}'s without components, and small size.
   */
  private static <T extends DimensionInfo> void removeEmptyDimensions(DimensionOperations<T> operations,
      Set<Integer> filledDimensions) throws Exception {
    LinkedList<T> dimensions = operations.getDimensions();
    for (int i = dimensions.size() - 1; i >= 0; i--) {
      T dimension = dimensions.get(i);
      if (!filledDimensions.contains(i) && dimension.getSize() < 30) {
        operations.delete(i);
      }
    }
  }

  /**
   * @return the alignment corresponding to the minimum delta value.
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
