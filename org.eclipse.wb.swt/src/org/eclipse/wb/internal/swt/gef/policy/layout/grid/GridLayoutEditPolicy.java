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
package org.eclipse.wb.internal.swt.gef.policy.layout.grid;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.policies.IRefreshableEditPolicy;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.state.IPasteComponentProcessor;
import org.eclipse.wb.internal.swt.gef.ControlsLayoutRequestValidator;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.edit.ColumnHeaderEditPart;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.edit.RowHeaderEditPart;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.layout.ColumnsLayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.layout.RowsLayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.layout.grid.GridColumnInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridRowInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.IGridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IMenuManager;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link IGridLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gef.GridLayout
 */
public final class GridLayoutEditPolicy<C extends IControlInfo>
extends
AbstractGridLayoutEditPolicy implements IRefreshableEditPolicy {
	private final IGridLayoutInfo<C> m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridLayoutEditPolicy(IGridLayoutInfo<C> layout) {
		super(layout.getUnderlyingModel());
		m_layout = layout;
		m_gridTargetHelper = new GridHelper(this, true);
		m_gridSelectionHelper = new GridHelper(this, false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Requests
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ILayoutRequestValidator getRequestValidator() {
		return ControlsLayoutRequestValidator.INSTANCE;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IGridInfo getGridInfo() {
		return m_layout.getGridInfo();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if given object is {@link IControlInfo}.
	 */
	private boolean isControl(Object model) {
		return model instanceof IControlInfo;
	}

	/**
	 * @return the casted model.
	 */
	@SuppressWarnings("unchecked")
	private C toControl(Object model) {
		return (C) model;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void decorateChild(EditPart child) {
		Object model = child.getModel();
		if (m_layout.isManagedObject(model)) {
			C control = toControl(model);
			EditPolicy selectionPolicy = new GridSelectionEditPolicy<>(m_layout, control);
			child.installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, selectionPolicy);
		}
	}

	@Override
	public void refreshEditPolicy() {
		List<? extends EditPart> children = getHost().getChildren();
		for (EditPart child : children) {
			Object model = child.getModel();
			if (isControl(model)) {
				// not managed: never was or excluded
				if (!m_layout.isManagedObject(model)) {
					child.installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new NonResizableSelectionEditPolicy());
					continue;
				}
				// managed: may be was excluded
				if (!(child.getEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE) instanceof GridSelectionEditPolicy<?>)) {
					decorateChild(child);
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if prepared "target" can be used.
	 */
	private boolean isValidTarget() {
		// may be invalid cell
		if (!m_target.m_valid) {
			return false;
		}
		// check for dimension operations limitations
		if (!m_layout.canChangeDimensions()) {
			// can not change columns
			if (m_target.m_columnInsert || m_target.m_column >= m_layout.getColumns().size()) {
				return false;
			}
			// can not insert new row before "implicit" row
			if (m_target.m_rowInsert && !m_layout.isExplicitRow(m_target.m_row)) {
				return false;
			}
		}
		// OK
		return true;
	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		if (isValidTarget()) {
			Object newObject = request.getNewObject();
			if (isControl(newObject)) {
				final C control = toControl(newObject);
				return new EditCommand(m_layout) {
					@Override
					protected void executeEdit() throws Exception {
						m_layout.command_CREATE(
								control,
								m_target.m_column,
								m_target.m_columnInsert,
								m_target.m_row,
								m_target.m_rowInsert);
					}
				};
			}
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Command getPasteCommand(PasteRequest request) {
		List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) request.getMemento();
		if (isValidTarget() && mementos.size() == 1) {
			return GlobalState.getPasteRequestProcessor().getPasteCommand(
					request,
					new IPasteComponentProcessor() {
						@Override
						public void process(Object component) throws Exception {
							m_layout.command_CREATE(
									toControl(component),
									m_target.m_column,
									m_target.m_columnInsert,
									m_target.m_row,
									m_target.m_rowInsert);
						}
					});
		}
		return null;
	}

	@Override
	protected Command getMoveCommand(ChangeBoundsRequest request) {
		if (isValidTarget() && request.getEditParts().size() == 1) {
			EditPart moveEditPart = request.getEditParts().get(0);
			Object model = moveEditPart.getModel();
			if (isControl(model)) {
				final C control = toControl(model);
				return new EditCommand(m_layout) {
					@Override
					protected void executeEdit() throws Exception {
						m_layout.command_MOVE(
								control,
								m_target.m_column,
								m_target.m_columnInsert,
								m_target.m_row,
								m_target.m_rowInsert);
					}
				};
			}
		}
		return null;
	}

	@Override
	protected Command getAddCommand(ChangeBoundsRequest request) {
		if (isValidTarget() && request.getEditParts().size() == 1) {
			EditPart moveEditPart = request.getEditParts().get(0);
			Object model = moveEditPart.getModel();
			if (isControl(model)) {
				final C control = toControl(model);
				return new EditCommand(m_layout) {
					@Override
					protected void executeEdit() throws Exception {
						m_layout.command_ADD(
								control,
								m_target.m_column,
								m_target.m_columnInsert,
								m_target.m_row,
								m_target.m_rowInsert);
					}
				};
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Grid target
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Determines parameters of insert feedback.
	 *
	 * @return the array of: visual gap, begin/end of insert feedback, begin/end of target feedback.
	 */
	public static int[] getInsertFeedbackParameters(Interval interval,
			Interval nextInterval,
			int minGap) {
		int gap = nextInterval.begin() - interval.end();
		int visualGap = Math.max(gap, minGap);
		// determine x1/x2
		int x1, x2;
		{
			int a = interval.end();
			int b = nextInterval.begin();
			int x1_2 = a + b - visualGap;
			x1 = x1_2 % 2 == 0 ? x1_2 / 2 : x1_2 / 2 - 1;
			x2 = a + b - x1;
			// we don't want to have insert feedback be same as intervals
			if (x1 == a - 1) {
				x1--;
				x2++;
			}
		}
		//
		return new int[]{visualGap, x1, x2, x1 - minGap, x2 + minGap};
	}

	@Override
	protected void updateGridTarget(Point mouseLocation) throws Exception {
		m_target = new GridTarget();
		// prepare location in model
		Point location = mouseLocation.getCopy();
		PolicyUtils.translateAbsoluteToModel(this, location);
		// prepare grid information
		IGridInfo gridInfo = m_layout.getGridInfo();
		Interval[] columnIntervals = gridInfo.getColumnIntervals();
		Interval[] rowIntervals = gridInfo.getRowIntervals();
		int lastX =
				columnIntervals.length != 0
				? columnIntervals[columnIntervals.length - 1].end()
						: gridInfo.getInsets().left;
		int lastY =
				rowIntervals.length != 0
				? rowIntervals[rowIntervals.length - 1].end()
						: gridInfo.getInsets().top;
		// prepare insert bounds
		{
			if (columnIntervals.length != 0) {
				m_target.m_rowInsertBounds.x = columnIntervals[0].begin() - INSERT_MARGINS;
				m_target.m_rowInsertBounds.setRight(lastX + INSERT_MARGINS);
			} else {
				m_target.m_rowInsertBounds.x = 0;
				m_target.m_rowInsertBounds.setRight(getHostFigure().getSize().width);
			}
			if (rowIntervals.length != 0) {
				m_target.m_columnInsertBounds.y = rowIntervals[0].begin() - INSERT_MARGINS;
				m_target.m_columnInsertBounds.setBottom(lastY + INSERT_MARGINS);
			} else {
				m_target.m_columnInsertBounds.y = 0;
				m_target.m_columnInsertBounds.setBottom(getHostFigure().getSize().height);
			}
		}
		// find existing column
		for (int columnIndex = 0; columnIndex < columnIntervals.length; columnIndex++) {
			boolean isLast = columnIndex == columnIntervals.length - 1;
			Interval interval = columnIntervals[columnIndex];
			Interval nextInterval = !isLast ? columnIntervals[columnIndex + 1] : null;
			// before first
			if (location.x < columnIntervals[0].begin()) {
				m_target.m_column = 0;
				m_target.m_columnInsert = true;
				// prepare parameters
				int[] parameters =
						getInsertFeedbackParameters(new Interval(0, 0), interval, INSERT_COLUMN_SIZE);
				// feedback
				m_target.m_feedbackBounds.x = parameters[3];
				m_target.m_feedbackBounds.width = parameters[4] - parameters[3];
				// insert
				m_target.m_columnInsertBounds.x = parameters[1];
				m_target.m_columnInsertBounds.width = parameters[2] - parameters[1];
				// stop
				break;
			}
			// gap or near to end of interval
			if (!isLast) {
				int gap = nextInterval.begin() - interval.end();
				boolean directGap = interval.end() <= location.x && location.x < nextInterval.begin();
				boolean narrowGap = gap < 2 * INSERT_COLUMN_SIZE;
				boolean nearEnd = Math.abs(location.x - interval.end()) < INSERT_COLUMN_SIZE;
				boolean nearBegin = Math.abs(location.x - nextInterval.begin()) < INSERT_COLUMN_SIZE;
				if (directGap || narrowGap && (nearEnd || nearBegin)) {
					m_target.m_column = columnIndex + 1;
					m_target.m_columnInsert = true;
					// prepare parameters
					int[] parameters =
							getInsertFeedbackParameters(interval, nextInterval, INSERT_COLUMN_SIZE);
					// feedback
					m_target.m_feedbackBounds.x = parameters[3];
					m_target.m_feedbackBounds.width = parameters[4] - parameters[3];
					// insert
					m_target.m_columnInsertBounds.x = parameters[1];
					m_target.m_columnInsertBounds.width = parameters[2] - parameters[1];
					// stop
					break;
				}
			}
			// column
			if (interval.contains(location.x)) {
				m_target.m_column = columnIndex;
				// feedback
				m_target.m_feedbackBounds.x = interval.begin();
				m_target.m_feedbackBounds.width = interval.length()+ 1;
				// stop
				break;
			}
		}
		// find virtual column
		if (m_target.m_column == -1) {
			int columnGap = gridInfo.getVirtualColumnGap();
			int columnSize = gridInfo.getVirtualColumnSize();
			//
			int newWidth = columnSize + columnGap;
			int newDelta = (location.x - lastX - columnGap / 2) / newWidth;
			//
			m_target.m_column = columnIntervals.length + newDelta;
			m_target.m_feedbackBounds.x = lastX + columnGap + newWidth * newDelta;
			m_target.m_feedbackBounds.width = columnSize + 1;
		}
		// find existing row
		for (int rowIndex = 0; rowIndex < rowIntervals.length; rowIndex++) {
			boolean isLast = rowIndex == rowIntervals.length - 1;
			Interval interval = rowIntervals[rowIndex];
			Interval nextInterval = !isLast ? rowIntervals[rowIndex + 1] : null;
			// before first
			if (location.y < rowIntervals[0].begin()) {
				m_target.m_row = 0;
				m_target.m_rowInsert = true;
				// prepare parameters
				int[] parameters =
						getInsertFeedbackParameters(new Interval(0, 0), interval, INSERT_ROW_SIZE);
				// feedback
				m_target.m_feedbackBounds.y = parameters[3];
				m_target.m_feedbackBounds.height = parameters[4] - parameters[3];
				// insert
				m_target.m_rowInsertBounds.y = parameters[1];
				m_target.m_rowInsertBounds.height = parameters[2] - parameters[1];
				// stop
				break;
			}
			// gap or near to end of interval
			if (!isLast) {
				int gap = nextInterval.begin() - interval.end();
				boolean directGap = interval.end() <= location.y && location.y < nextInterval.begin();
				boolean narrowGap = gap < 2 * INSERT_ROW_SIZE;
				boolean nearEnd = Math.abs(location.y - interval.end()) < INSERT_ROW_SIZE;
				boolean nearBegin = Math.abs(location.y - nextInterval.begin()) < INSERT_ROW_SIZE;
				if (directGap || narrowGap && (nearEnd || nearBegin)) {
					m_target.m_row = rowIndex + 1;
					m_target.m_rowInsert = true;
					// prepare parameters
					int[] parameters = getInsertFeedbackParameters(interval, nextInterval, INSERT_ROW_SIZE);
					// feedback
					m_target.m_feedbackBounds.y = parameters[3];
					m_target.m_feedbackBounds.height = parameters[4] - parameters[3];
					// insert
					m_target.m_rowInsertBounds.y = parameters[1];
					m_target.m_rowInsertBounds.height = parameters[2] - parameters[1];
					// stop
					break;
				}
			}
			// row
			if (interval.contains(location.y)) {
				m_target.m_row = rowIndex;
				// feedback
				m_target.m_feedbackBounds.y = interval.begin();
				m_target.m_feedbackBounds.height = interval.length()+ 1;
				// stop
				break;
			}
		}
		// find virtual row
		if (m_target.m_row == -1) {
			int rowGap = gridInfo.getVirtualRowGap();
			int rowSize = gridInfo.getVirtualRowSize();
			//
			int newHeight = rowSize + rowGap;
			int newDelta = (location.y - lastY - rowGap / 2) / newHeight;
			//
			m_target.m_row = rowIntervals.length + newDelta;
			m_target.m_feedbackBounds.y = lastY + rowGap + newHeight * newDelta;
			m_target.m_feedbackBounds.height = rowSize + 1;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IHeadersProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public LayoutEditPolicy getContainerLayoutPolicy(boolean horizontal) {
		if (horizontal) {
			return new ColumnsLayoutEditPolicy<>(this, m_layout);
		} else {
			return new RowsLayoutEditPolicy<>(this, m_layout);
		}
	}

	@Override
	public List<?> getHeaders(boolean horizontal) {
		return horizontal ? m_layout.getColumns() : m_layout.getRows();
	}

	@Override
	@SuppressWarnings("unchecked")
	public org.eclipse.wb.gef.core.EditPart createHeaderEditPart(boolean horizontal, Object model) {
		if (horizontal) {
			return new ColumnHeaderEditPart<>(m_layout, (GridColumnInfo<C>) model, getHostFigure());
		} else {
			return new RowHeaderEditPart<>(m_layout, (GridRowInfo<C>) model, getHostFigure());
		}
	}

	@Override
	public void buildContextMenu(IMenuManager manager, boolean horizontal) {
	}

	@Override
	public void handleDoubleClick(boolean horizontal) {
	}
}