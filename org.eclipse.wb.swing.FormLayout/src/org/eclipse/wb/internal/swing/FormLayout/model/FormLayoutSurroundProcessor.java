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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.util.surround.ISurroundProcessor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import java.util.List;

/**
 * {@link ISurroundProcessor} that places enclosing {@link ComponentInfo}'s into same relative
 * cells, as they were before enclosing. It works only if source {@link ContainerInfo} has
 * {@link FormLayoutInfo} and sets also {@link FormLayoutInfo} on target {@link ContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public final class FormLayoutSurroundProcessor
implements
ISurroundProcessor<ContainerInfo, ComponentInfo> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final Object INSTANCE = new FormLayoutSurroundProcessor();

	private FormLayoutSurroundProcessor() {
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
				&& sourceContainer.getLayout() instanceof FormLayoutInfo
				&& isJPanel;
	}

	@Override
	public void move(ContainerInfo sourceContainer,
			ContainerInfo targetContainer,
			List<ComponentInfo> components) throws Exception {
		// set FormLayout for target
		FormLayoutInfo targetLayout;
		{
			targetLayout =
					(FormLayoutInfo) JavaInfoUtils.createJavaInfo(
							targetContainer.getEditor(),
							"com.jgoodies.forms.layout.FormLayout",
							new ConstructorCreationSupport());
			targetContainer.setLayout(targetLayout);
		}
		// prepare cells of "targetContainer"
		Point locationOffset;
		{
			Rectangle targetBounds =
					(Rectangle) targetContainer.getArbitraryValue(FormLayoutSurroundSupport.CELLS_KEY);
			FormLayoutInfo sourceLayout = (FormLayoutInfo) sourceContainer.getLayout();
			// copy columns
			{
				List<FormColumnInfo> targetColumns = Lists.newArrayList();
				for (int columnIndex = targetBounds.x; columnIndex < targetBounds.right(); columnIndex++) {
					FormColumnInfo sourceColumn = sourceLayout.getColumns().get(columnIndex - 1);
					targetColumns.add(sourceColumn.copy());
				}
				targetLayout.setColumns(targetColumns);
			}
			// copy rows
			{
				List<FormRowInfo> targetRows = Lists.newArrayList();
				for (int rowIndex = targetBounds.y; rowIndex < targetBounds.bottom(); rowIndex++) {
					FormRowInfo sourceRow = sourceLayout.getRows().get(rowIndex - 1);
					targetRows.add(sourceRow.copy());
				}
				targetLayout.setRows(targetRows);
			}
			//
			locationOffset = targetBounds.getLocation().getNegated();
		}
		// move components
		for (ComponentInfo component : components) {
			CellConstraintsSupport oldConstraints = FormLayoutInfo.getConstraints(component);
			// move component
			{
				Rectangle cells = FormLayoutSurroundSupport.getCells(component);
				cells = cells.getTranslated(locationOffset);
				targetLayout.command_ADD(component, 1 + cells.x, false, 1 + cells.y, false);
			}
			// update constraints
			{
				CellConstraintsSupport constraints = FormLayoutInfo.getConstraints(component);
				constraints.width = oldConstraints.width;
				constraints.height = oldConstraints.height;
				constraints.alignH = oldConstraints.alignH;
				constraints.alignV = oldConstraints.alignV;
				constraints.write();
			}
		}
	}
}
