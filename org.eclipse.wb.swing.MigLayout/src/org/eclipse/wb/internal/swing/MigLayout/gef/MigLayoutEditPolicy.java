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
package org.eclipse.wb.internal.swing.MigLayout.gef;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.helpers.BroadcastListenerHelper;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2.IPasteProcessor;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.edit.ColumnHeaderEditPart;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.edit.RowHeaderEditPart;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.layout.ColumnsLayoutEditPolicy;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.layout.RowsLayoutEditPolicy;
import org.eclipse.wb.internal.swing.MigLayout.model.CellConstraintsSupport;
import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo;
import org.eclipse.wb.internal.swing.gef.ComponentsLayoutRequestValidator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IMenuManager;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link MigLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.policy
 */
public final class MigLayoutEditPolicy extends AbstractGridLayoutEditPolicy {
	private final MigLayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MigLayoutEditPolicy(MigLayoutInfo layout) {
		super(layout);
		m_layout = layout;
		m_gridTargetHelper = new MigGridHelper(this, true);
		m_gridSelectionHelper = new MigGridHelper(this, false);
		// add listeners
		new BroadcastListenerHelper(layout, this, new ObjectEventListener() {
			@Override
			public void refreshed() throws Exception {
				decorateChildren();
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Requests
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ILayoutRequestValidator getRequestValidator() {
		return ComponentsLayoutRequestValidator.INSTANCE_EXT;
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
	// Selection
	//
	////////////////////////////////////////////////////////////////////////////
	public static final String SECONDARY_SELECTION_FEEDBACK_ROLE = "Secondary Selection Feedback";

	/**
	 * Decorates (with {@link SelectionEditPolicy}) all components children.
	 * <p>
	 * We need this because when we move component from dock side to usual cell, we should use
	 * different {@link SelectionEditPolicy}.
	 */
	private void decorateChildren() {
		for (org.eclipse.wb.gef.core.EditPart child : getHost().getChildren()) {
			decorateChild(child);
		}
	}

	@Override
	protected void decorateChild(EditPart child) {
		if (child.getModel() instanceof ComponentInfo) {
			ComponentInfo component = (ComponentInfo) child.getModel();
			CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(component);
			if (constraints.getDockSide() == null) {
				setSelectionEditPolicy(child, new MigSelectionEditPolicy(m_layout, component));
				// secondary selection for component in splitted cell
				if (m_layout.getCellComponents(constraints.getX(), constraints.getY()).size() > 1) {
					child.installEditPolicy(
							SECONDARY_SELECTION_FEEDBACK_ROLE,
							new NonResizableSelectionEditPolicy());
				} else {
					child.installEditPolicy(SECONDARY_SELECTION_FEEDBACK_ROLE, null);
				}
			} else {
				setSelectionEditPolicy(child, new NonResizableSelectionEditPolicy());
			}
		}
	}

	/**
	 * If {@link EditPart} has different type of {@link SelectionEditPolicy}, replaces it with new
	 * one.
	 */
	private static void setSelectionEditPolicy(EditPart child, SelectionEditPolicy selectionPolicy) {
		EditPolicy oldPolicy = child.getEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE);
		if (oldPolicy == null || oldPolicy.getClass() != selectionPolicy.getClass()) {
			child.installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, selectionPolicy);
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Additional feedback
	//
	////////////////////////////////////////////////////////////////////////////
	private LayoutEditPolicy m_flowPolicy;
	private Command m_flowCommand;

	@Override
	protected boolean showOccupiedLayoutTargetFeedback(Request request) {
		if (m_flowPolicy == null) {
			m_flowPolicy = new MigLayoutSplitEditPolicy(m_layout, m_target.m_column, m_target.m_row);
			m_flowPolicy.setHost(getHost());
		}
		m_flowPolicy.showTargetFeedback(request);
		m_flowCommand = m_flowPolicy.getCommand(request);
		return true;
	}

	@Override
	protected void eraseOccupiedLayoutTargetFeedback(Request request) {
		if (m_flowPolicy != null) {
			m_flowPolicy.eraseTargetFeedback(request);
			m_flowPolicy.setHost(null);
			m_flowPolicy = null;
			m_flowCommand = null;
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
			// can not insert
			if (m_target.m_columnInsert || m_target.m_rowInsert) {
				return false;
			}
			// can not append
			if (m_target.m_column >= m_layout.getColumns().size()
					|| m_target.m_row >= m_layout.getRows().size()) {
				return false;
			}
		}
		// OK
		return true;
	}

	@Override
	public Command getCommand(Request request) {
		if (m_flowCommand != null) {
			return m_flowCommand;
		}
		return super.getCommand(request);
	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		if (isValidTarget()) {
			Object newObject = request.getNewObject();
			if (newObject instanceof final ComponentInfo component) {
				return new EditCommand(m_layout) {
					@Override
					protected void executeEdit() throws Exception {
						m_layout.command_CREATE(
								component,
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
			return LayoutPolicyUtils2.getPasteCommand(
					m_layout,
					request,
					ComponentInfo.class,
					new IPasteProcessor<ComponentInfo>() {
						@Override
						public void process(ComponentInfo component) throws Exception {
							m_layout.command_CREATE(
									component,
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
			if (moveEditPart.getModel() instanceof ComponentInfo) {
				final ComponentInfo component = (ComponentInfo) moveEditPart.getModel();
				return new EditCommand(m_layout) {
					@Override
					protected void executeEdit() throws Exception {
						m_layout.command_MOVE(
								component,
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
		return getMoveCommand(request);
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
		int lastX = columnIntervals.length != 0 ? columnIntervals[columnIntervals.length - 1].end() : 0;
		int lastY = rowIntervals.length != 0 ? rowIntervals[rowIntervals.length - 1].end() : 0;
		// prepare insert bounds
		{
			m_target.m_rowInsertBounds.x = columnIntervals[0].begin() - INSERT_MARGINS;
			m_target.m_rowInsertBounds.setRight(lastX + INSERT_MARGINS);
			m_target.m_columnInsertBounds.y = rowIntervals[0].begin() - INSERT_MARGINS;
			m_target.m_columnInsertBounds.setBottom(lastY + INSERT_MARGINS);
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
			return new ColumnsLayoutEditPolicy(this, m_layout);
		} else {
			return new RowsLayoutEditPolicy(this, m_layout);
		}
	}

	@Override
	public List<?> getHeaders(boolean horizontal) {
		return horizontal ? m_layout.getColumns() : m_layout.getRows();
	}

	@Override
	public org.eclipse.wb.gef.core.EditPart createHeaderEditPart(boolean horizontal, Object model) {
		if (horizontal) {
			return new ColumnHeaderEditPart(m_layout, (MigColumnInfo) model, getHostFigure());
		} else {
			return new RowHeaderEditPart(m_layout, (MigRowInfo) model, getHostFigure());
		}
	}

	@Override
	public void buildContextMenu(IMenuManager manager, boolean horizontal) {
		if (horizontal) {
			manager.add(new ObjectInfoAction(m_layout, GefMessages.MigLayoutEditPolicy_appendColumn) {
				@Override
				protected void runEx() throws Exception {
					int index = m_layout.getColumns().size();
					m_layout.insertColumn(index);
				}
			});
		} else {
			manager.add(new ObjectInfoAction(m_layout, GefMessages.MigLayoutEditPolicy_appendRow) {
				@Override
				protected void runEx() throws Exception {
					int index = m_layout.getRows().size();
					m_layout.insertRow(index);
				}
			});
		}
	}

	@Override
	public void handleDoubleClick(boolean horizontal) {
		/*if (horizontal) {
    	new ColumnsDialog(DesignerPlugin.getShell(), m_layout).open();
    } else {
    	new RowsDialog(DesignerPlugin.getShell(), m_layout).open();
    }*/
	}
}
