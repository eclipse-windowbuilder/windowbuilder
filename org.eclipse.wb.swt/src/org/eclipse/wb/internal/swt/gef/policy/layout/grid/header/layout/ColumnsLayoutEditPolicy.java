/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.layout;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.core.gef.header.AbstractHeaderLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.GefMessages;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.GridLayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.edit.ColumnHeaderEditPart;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.edit.DimensionHeaderEditPart;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.selection.ColumnSelectionEditPolicy;
import org.eclipse.wb.internal.swt.model.layout.grid.GridColumnInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.IGridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.DropRequest;

import java.util.List;

/**
 * {@link LayoutEditPolicy} for {@link ColumnHeaderEditPart}'s.
 *
 * @author scheglov_ke
 * @coverage swt.gef.GridLayout
 */
public final class ColumnsLayoutEditPolicy<C extends IControlInfo>
extends
AbstractHeaderLayoutEditPolicy {
	private final GridLayoutEditPolicy<C> m_mainPolicy;
	private final IGridLayoutInfo<C> m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnsLayoutEditPolicy(GridLayoutEditPolicy<C> mainPolicy, IGridLayoutInfo<C> layout) {
		super(mainPolicy);
		m_mainPolicy = mainPolicy;
		m_layout = layout;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void decorateChild(EditPart child) {
		child.installEditPolicy(
				EditPolicy.SELECTION_FEEDBACK_ROLE,
				new ColumnSelectionEditPolicy<>(m_mainPolicy));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Move
	//
	////////////////////////////////////////////////////////////////////////////
	private final IFigure m_insertFeedback = AbstractGridLayoutEditPolicy.createInsertFigure();
	private TextFeedback m_feedback;
	private Command m_moveCommand;

	@Override
	protected Command getMoveCommand(ChangeBoundsRequest request) {
		if (!m_layout.canChangeDimensions()) {
			return null;
		}
		return m_moveCommand;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void showLayoutTargetFeedback(Request request) {
		// prepare header
		ColumnHeaderEditPart<C> headerEditPart;
		{
			ChangeBoundsRequest changeBoundsRequest = (ChangeBoundsRequest) request;
			headerEditPart = (ColumnHeaderEditPart<C>) changeBoundsRequest.getEditParts().get(0);
		}
		// prepare location
		Point location;
		{
			DropRequest dropRequest = (DropRequest) request;
			location = dropRequest.getLocation().getCopy();
		}
		// prepare grid information
		IGridInfo gridInfo = m_layout.getGridInfo();
		Interval[] columnIntervals = gridInfo.getColumnIntervals();
		Interval[] rowIntervals = gridInfo.getRowIntervals();
		int y1 = rowIntervals[0].begin() - 5;
		int y2 = rowIntervals[rowIntervals.length - 1].end() + 5;
		// prepare target header
		DimensionHeaderEditPart<C> target = null;
		{
			List<? extends EditPart> children = getHost().getChildren();
			for (EditPart child : children) {
				DimensionHeaderEditPart<C> columnEditPart = (DimensionHeaderEditPart<C>) child;
				Rectangle bounds = columnEditPart.getFigure().getBounds();
				if (gridInfo.isRTL()) {
					if (location.x > bounds.getCenter().x) {
						target = columnEditPart;
						break;
					}
				} else {
					if (location.x < bounds.getCenter().x) {
						target = columnEditPart;
						break;
					}
				}
			}
		}
		// prepare index of target column and position for insert feedbacks
		final int index;
		int x;
		int size;
		if (target != null) {
			index = target.getDimension().getIndex();
			// prepare previous interval
			Interval prevInterval;
			if (index == 0) {
				prevInterval = new Interval(0, 0);
			} else {
				prevInterval = columnIntervals[index - 1];
			}
			// prepare parameters
			int[] parameters =
					GridLayoutEditPolicy.getInsertFeedbackParameters(
							prevInterval,
							columnIntervals[index],
							AbstractGridLayoutEditPolicy.INSERT_COLUMN_SIZE);
			x = parameters[1];
			size = parameters[2] - parameters[1];
		} else {
			index = m_layout.getColumns().size();
			m_mainPolicy.showInsertFeedbacks(null, null);
			// prepare parameters
			x = columnIntervals[columnIntervals.length - 1].end() + 1;
			size = AbstractGridLayoutEditPolicy.INSERT_COLUMN_SIZE;
		}
		// show insert feedbacks
		{
			// ...on main viewer
			m_mainPolicy.showInsertFeedbacks(new Rectangle(x, y1, size, y2 - y1), null);
			// ...on header viewer
			{
				if (m_insertFeedback.getParent() == null) {
					addFeedback(m_insertFeedback);
				}
				//
				Rectangle bounds = new Rectangle(x, 0, size, getHostFigure().getSize().height);
				headerEditPart.translateModelToFeedback(bounds);
				m_insertFeedback.setBounds(bounds);
			}
		}
		// show text feedback
		{
			Layer feedbackLayer = getMainLayer(IEditPartViewer.FEEDBACK_LAYER);
			// add feedback
			if (m_feedback == null) {
				m_feedback = new TextFeedback(feedbackLayer);
				m_feedback.add();
			}
			// set feedback bounds
			{
				Point feedbackLocation = new Point(location.x + 30, 10);
				FigureUtils.translateAbsoluteToFigure(feedbackLayer, feedbackLocation);
				m_feedback.setLocation(feedbackLocation);
			}
			// set text
			m_feedback.setText(GefMessages.ColumnsLayoutEditPolicy_feedbackTextPrefix + index);
		}
		// prepare command
		{
			GridColumnInfo<C> column = (GridColumnInfo<C>) headerEditPart.getDimension();
			final int sourceIndex = column.getIndex();
			if (index == sourceIndex || index == sourceIndex + 1) {
				m_moveCommand = new Command(){};
			} else {
				m_moveCommand = new EditCommand(m_layout) {
					@Override
					protected void executeEdit() throws Exception {
						m_layout.command_MOVE_COLUMN(sourceIndex, index);
					}
				};
			}
		}
	}

	@Override
	protected void eraseLayoutTargetFeedback(Request request) {
		m_mainPolicy.eraseInsertFeedbacks();
		FigureUtils.removeFigure(m_insertFeedback);
		if (m_feedback != null) {
			m_feedback.remove();
			m_feedback = null;
		}
	}
}
