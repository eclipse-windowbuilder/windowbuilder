/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.MigLayout.gef;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.gef.policy.ComponentFlowLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.DropRequest;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for cell split support in {@link MigLayoutInfo}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage swing.MigLayout.policy
 */
public final class MigLayoutSplitEditPolicy extends ComponentFlowLayoutEditPolicy {
	private final MigLayoutInfo m_layout;
	private final int m_column;
	private final int m_row;
	private final List<ComponentInfo> m_components;
	private final boolean m_splitted;
	private final boolean m_splittedHorizontally;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MigLayoutSplitEditPolicy(MigLayoutInfo layout, int column, int row) {
		super(layout);
		m_layout = layout;
		m_column = column;
		m_row = row;
		m_components = m_layout.getCellComponents(column, row);
		Assert.isTrue(m_components.size() != 0);
		m_splitted = m_components.size() > 1;
		m_splittedHorizontally = MigLayoutInfo.getConstraints(m_components.get(0)).isHorizontalSplit();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isHorizontal(Request _request) {
		if (m_splitted) {
			return m_splittedHorizontally;
		} else {
			// prepare single target component (it is really only one, because there are no split yet)
			Figure targetFigure;
			{
				ComponentInfo targetComponent = m_components.get(0);
				EditPart targetEditPart = (EditPart) getHost().getViewer().getEditPartRegistry().get(targetComponent);
				targetFigure = ((GraphicalEditPart) targetEditPart).getFigure();
			}
			// prepare location in target component's Figure
			Point location;
			{
				DropRequest request = (DropRequest) _request;
				location = request.getLocation().getCopy();
				FigureUtils.translateAbsoluteToFigure2(targetFigure, location);
			}
			// prepare bounds where we consider vertical split
			Rectangle verticalBounds;
			{
				Rectangle clientArea = targetFigure.getClientArea().getCopy();
				verticalBounds = clientArea.shrink(clientArea.width / 4, 0);
			}
			// vertical if cursor is inside of center part of component
			return !verticalBounds.contains(location);
		}
	}

	@Override
	protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
		return m_components.contains(editPart.getModel());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean m_horizontalCommand;

	@Override
	public Command getCommand(Request request) {
		m_horizontalCommand = isHorizontal(request);
		return super.getCommand(request);
	}

	@Override
	protected void command_CREATE(ComponentInfo newObject, ComponentInfo referenceObject)
			throws Exception {
		m_layout.command_splitCREATE(m_column, m_row, m_horizontalCommand, newObject, referenceObject);
	}

	@Override
	protected void command_MOVE(ComponentInfo object, ComponentInfo referenceObject) throws Exception {
		m_layout.command_splitMOVE(m_column, m_row, m_horizontalCommand, object, referenceObject);
	}
}
