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
package org.eclipse.wb.internal.swing.MigLayout.model;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.util.surround.ISurroundProcessor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ISurroundProcessor} that places enclosing {@link ComponentInfo}'s into same relative
 * cells, as they were before enclosing. It works only if source {@link ContainerInfo} has
 * {@link MigLayoutInfo} and sets also {@link MigLayoutInfo} on target {@link ContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.model
 */
public final class MigLayoutSurroundProcessor
implements
ISurroundProcessor<ContainerInfo, ComponentInfo> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final Object INSTANCE = new MigLayoutSurroundProcessor();

	private MigLayoutSurroundProcessor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ISurroundProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean filter(ContainerInfo sourceContainer, ContainerInfo targetContainer)
			throws Exception {
		String targetClassName = targetContainer.getDescription().getComponentClass().getName();
		boolean isJPanel = targetClassName.equals("javax.swing.JPanel");
		return sourceContainer.hasLayout()
				&& sourceContainer.getLayout() instanceof MigLayoutInfo
				&& isJPanel;
	}

	@Override
	public void move(ContainerInfo sourceContainer,
			ContainerInfo targetContainer,
			List<ComponentInfo> components) throws Exception {
		// set MigLayout for target
		MigLayoutInfo targetLayout;
		{
			targetLayout =
					(MigLayoutInfo) JavaInfoUtils.createJavaInfo(
							targetContainer.getEditor(),
							"net.miginfocom.swing.MigLayout",
							new ConstructorCreationSupport());
			targetContainer.setLayout(targetLayout);
		}
		// prepare cells of "targetContainer"
		Point locationOffset;
		{
			Rectangle targetBounds =
					(Rectangle) targetContainer.getArbitraryValue(MigLayoutSurroundSupport.CELLS_KEY);
			MigLayoutInfo sourceLayout = (MigLayoutInfo) sourceContainer.getLayout();
			// copy columns
			{
				List<MigColumnInfo> targetColumns = new ArrayList<>();
				for (int columnIndex = targetBounds.x; columnIndex < targetBounds.right(); columnIndex++) {
					MigColumnInfo sourceColumn = sourceLayout.getColumns().get(columnIndex);
					MigColumnInfo targetColumn = new MigColumnInfo(targetLayout);
					targetColumn.setString(sourceColumn.getString(false));
					targetColumns.add(targetColumn);
				}
				targetLayout.setColumns(targetColumns);
			}
			// copy rows
			{
				List<MigRowInfo> targetRows = new ArrayList<>();
				for (int rowIndex = targetBounds.y; rowIndex < targetBounds.bottom(); rowIndex++) {
					MigRowInfo sourceRow = sourceLayout.getRows().get(rowIndex);
					MigRowInfo targetRow = new MigRowInfo(targetLayout);
					targetRow.setString(sourceRow.getString(false));
					targetRows.add(targetRow);
				}
				targetLayout.setRows(targetRows);
			}
			//
			locationOffset = targetBounds.getLocation().getNegated();
		}
		// move components
		for (ComponentInfo component : components) {
			CellConstraintsSupport oldConstraints = MigLayoutInfo.getConstraints(component);
			// move component
			{
				Rectangle cells = MigLayoutSurroundSupport.getCells(component);
				cells = cells.getTranslated(locationOffset);
				targetLayout.command_MOVE(component, cells.x, false, cells.y, false);
			}
			// update constraints
			{
				CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(component);
				constraints.setWidth(oldConstraints.getWidth());
				constraints.setHeight(oldConstraints.getHeight());
				constraints.setHorizontalAlignment(oldConstraints.getHorizontalAlignment());
				constraints.setVerticalAlignment(oldConstraints.getVerticalAlignment());
				constraints.write();
			}
		}
	}
}
