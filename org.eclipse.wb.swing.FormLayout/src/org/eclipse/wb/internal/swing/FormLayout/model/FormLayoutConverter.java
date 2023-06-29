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
package org.eclipse.wb.internal.swing.FormLayout.model;

import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper.ComponentGroup;
import org.eclipse.wb.internal.core.model.util.grid.GridConvertionHelper.ComponentInGroup;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.CellConstraints.Alignment;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormSpecs;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.Set;

/**
 * Helper for converting coordinates of {@link ComponentInfo} children to {@link FormLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public class FormLayoutConverter {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private FormLayoutConverter() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Conversion
	//
	////////////////////////////////////////////////////////////////////////////
	public static void convert(ContainerInfo parent, FormLayoutInfo layout) throws Exception {
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
			CellConstraintsSupport constraints = FormLayoutInfo.getConstraints(component);
			// update cell coordinates
			{
				// prepare begin/end column/row
				ComponentGroup beginColumn =
						GridConvertionHelper.getBeginForComponent(columns, componentInGroup);
				ComponentGroup endColumn =
						GridConvertionHelper.getEndForComponent(columns, componentInGroup);
				ComponentGroup beginRow = GridConvertionHelper.getBeginForComponent(rows, componentInGroup);
				ComponentGroup endRow = GridConvertionHelper.getEndForComponent(rows, componentInGroup);
				// calculate location/size
				int x = columns.indexOf(beginColumn);
				int y = rows.indexOf(beginRow);
				int sx = 1 + columns.indexOf(endColumn) - x;
				int sy = 1 + rows.indexOf(endRow) - y;
				// set location/size
				constraints.x = 1 + x;
				constraints.y = 1 + y;
				constraints.width = sx;
				constraints.height = sy;
			}
			// alignments
			constraints.alignH = getHorizontalAlignment(columns, componentInGroup, generalLayoutData);
			constraints.alignV = getVerticalAlignment(rows, componentInGroup, generalLayoutData);
			// write constraints
			constraints.write();
		}
		// convert empty columns into gaps
		{
			List<FormColumnInfo> dimensions = layout.getColumns();
			int[] counts = layout.getColumnComponentsCounts();
			for (int i = 0; i < dimensions.size(); i++) {
				if (counts[i] == 0) {
					FormDimensionInfo dimension = dimensions.get(i);
					dimension.convertToNearestGap(5);
				}
			}
		}
		// convert empty rows into gaps
		{
			List<FormRowInfo> dimensions = layout.getRows();
			int[] counts = layout.getRowComponentsCounts();
			for (int i = 0; i < dimensions.size(); i++) {
				if (counts[i] == 0) {
					FormDimensionInfo dimension = dimensions.get(i);
					dimension.convertToNearestGap(5);
				}
			}
		}
		// write dimensions
		layout.writeDimensions();
	}

	/**
	 * Creates {@link FormColumnInfo} or {@link FormRowInfo} for given {@link ComponentGroup}'s.
	 */
	private static void createDimensions(FormLayoutInfo layout,
			List<ComponentGroup> groups,
			boolean horizontal) throws Exception {
		for (ComponentGroup group : groups) {
			// create new "default" dimension
			FormDimensionInfo dimension;
			if (horizontal) {
				FormColumnInfo column = new FormColumnInfo(FormSpecs.DEFAULT_COLSPEC);
				layout.getColumns().add(column);
				dimension = column;
			} else {
				FormRowInfo row = new FormRowInfo(FormSpecs.DEFAULT_ROWSPEC);
				layout.getRows().add(row);
				dimension = row;
			}
			// set constant size
			{
				FormSizeInfo size = dimension.getSize();
				size.setConstantSize(new FormSizeConstantInfo(group.getSize(), ConstantSize.PIXEL));
			}
		}
	}

	/**
	 * Calculate horizontal alignment.
	 */
	private static CellConstraints.Alignment getHorizontalAlignment(List<ComponentGroup> columns,
			ComponentInGroup componentInGroup,
			GeneralLayoutData generalLayoutData) {
		if (generalLayoutData.horizontalAlignment != null) {
			// from general layout data
			CellConstraints.Alignment alignment =
					GeneralLayoutData.getRealValue(
							FormLayoutInfo.m_horizontalAlignmentMap,
							generalLayoutData.horizontalAlignment);
			if (alignment != null && alignment != CellConstraints.DEFAULT) {
				return alignment;
			}
		}
		// calculate
		IAbstractComponentInfo component = componentInGroup.getComponent();
		// prepare begin/end column
		ComponentGroup beginColumn =
				GridConvertionHelper.getBeginForComponent(columns, componentInGroup);
		ComponentGroup endColumn = GridConvertionHelper.getEndForComponent(columns, componentInGroup);
		int columnLeft = beginColumn.getMin();
		int columnRight = endColumn.getMax();
		int columnCenter = columnLeft + (columnRight - columnLeft) / 2;
		Rectangle bounds = component.getBounds();
		Dimension prefSize = component.getPreferredSize();
		int bl = bounds.x;
		int br = bounds.right();
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
		return getAlignment(new int[]{leftDelta, centerDelta, rightDelta, fillDelta}, new Alignment[]{
				CellConstraints.LEFT,
				CellConstraints.CENTER,
				CellConstraints.RIGHT,
				CellConstraints.FILL});
	}

	/**
	 * Calculate vertical alignment.
	 */
	private static CellConstraints.Alignment getVerticalAlignment(List<ComponentGroup> rows,
			ComponentInGroup componentInGroup,
			GeneralLayoutData generalLayoutData) {
		if (generalLayoutData.verticalAlignment != null) {
			// from general layout data
			CellConstraints.Alignment alignment =
					GeneralLayoutData.getRealValue(
							FormLayoutInfo.m_verticalAlignmentMap,
							generalLayoutData.verticalAlignment);
			if (alignment != null && alignment != CellConstraints.DEFAULT) {
				return alignment;
			}
		}
		// calculate
		IAbstractComponentInfo component = componentInGroup.getComponent();
		// prepare begin/end row
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
		return getAlignment(new int[]{topDelta, centerDelta, bottomDelta, fillDelta}, new Alignment[]{
				CellConstraints.TOP,
				CellConstraints.CENTER,
				CellConstraints.BOTTOM,
				CellConstraints.FILL});
	}

	/**
	 * @return the {@link Alignment} corresponding to the minimum delta value.
	 */
	private static Alignment getAlignment(int[] deltas, Alignment[] alignments) {
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
