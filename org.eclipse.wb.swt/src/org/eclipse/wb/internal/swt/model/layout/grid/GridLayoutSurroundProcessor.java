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

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.util.surround.ISurroundProcessor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import java.util.List;

/**
 * {@link ISurroundProcessor} that places enclosing {@link ControlInfo}'s into same relative cells,
 * as they were before enclosing. It works only if source {@link CompositeInfo} has
 * {@link GridLayoutInfo} and sets also {@link GridLayoutInfo} on target {@link CompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class GridLayoutSurroundProcessor
implements
ISurroundProcessor<CompositeInfo, ControlInfo> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final Object INSTANCE = new GridLayoutSurroundProcessor();

	private GridLayoutSurroundProcessor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ISurroundProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean filter(CompositeInfo sourceContainer, CompositeInfo targetContainer)
			throws Exception {
		String targetClassName = targetContainer.getDescription().getComponentClass().getName();
		boolean isComposite = targetClassName.equals("org.eclipse.swt.widgets.Composite");
		boolean isGroup = targetClassName.equals("org.eclipse.swt.widgets.Group");
		return sourceContainer.hasLayout()
				&& sourceContainer.getLayout() instanceof GridLayoutInfo
				&& (isComposite || isGroup);
	}

	@Override
	public void move(CompositeInfo sourceContainer,
			CompositeInfo targetContainer,
			List<ControlInfo> components) throws Exception {
		// set GridLayout for target
		GridLayoutInfo targetLayout;
		{
			targetLayout =
					(GridLayoutInfo) JavaInfoUtils.createJavaInfo(
							targetContainer.getEditor(),
							"org.eclipse.swt.layout.GridLayout",
							new ConstructorCreationSupport());
			targetContainer.setLayout(targetLayout);
		}
		// prepare cells of "targetContainer"
		Point locationOffset;
		{
			Rectangle targetBounds =
					(Rectangle) targetContainer.getArbitraryValue(GridLayoutSurroundSupport.CELLS_KEY);
			locationOffset = targetBounds.getLocation().getNegated();
			//
			targetLayout.prepareCell(targetBounds.width - 1, false, targetBounds.height - 1, false);
		}
		// move components
		for (ControlInfo component : components) {
			// remember old alignments
			int horizontalAlignment;
			int verticalAlignment;
			{
				GridDataInfo gridData = GridLayoutInfo.getGridData(component);
				horizontalAlignment = gridData.getHorizontalAlignment();
				verticalAlignment = gridData.getVerticalAlignment();
			}
			// move component
			{
				Rectangle cells = GridLayoutSurroundSupport.getCells(component);
				cells = cells.getTranslated(locationOffset);
				targetLayout.command_ADD(component, cells.x, false, cells.y, false);
				targetLayout.command_setCells(component, cells, false);
			}
			// set old alignments
			{
				GridDataInfo gridData = GridLayoutInfo.getGridData(component);
				gridData.setHorizontalAlignment(horizontalAlignment);
				gridData.setVerticalAlignment(verticalAlignment);
			}
		}
	}
}
