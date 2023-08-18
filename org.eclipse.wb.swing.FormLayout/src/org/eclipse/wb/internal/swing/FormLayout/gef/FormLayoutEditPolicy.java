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
package org.eclipse.wb.internal.swing.FormLayout.gef;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2.IPasteProcessor;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.edit.ColumnHeaderEditPart;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.edit.RowHeaderEditPart;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.layout.ColumnsLayoutEditPolicy;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.layout.RowsLayoutEditPolicy;
import org.eclipse.wb.internal.swing.FormLayout.model.FormColumnInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormRowInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.ui.ColumnsDialog;
import org.eclipse.wb.internal.swing.FormLayout.model.ui.RowsDialog;
import org.eclipse.wb.internal.swing.gef.ComponentsLayoutRequestValidator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.action.IMenuManager;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link FormLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.policy
 */
public final class FormLayoutEditPolicy extends AbstractGridLayoutEditPolicy {
	private final FormLayoutInfo m_layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormLayoutEditPolicy(FormLayoutInfo layout) {
		super(layout);
		m_layout = layout;
		m_gridTargetHelper = new FormGridHelper(this, true);
		m_gridSelectionHelper = new FormGridHelper(this, false);
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
	@Override
	protected void decorateChild(EditPart child) {
		if (child.getModel() instanceof ComponentInfo) {
			ComponentInfo component = (ComponentInfo) child.getModel();
			EditPolicy selectionPolicy = new FormSelectionEditPolicy(m_layout, component);
			child.installEditPolicy(EditPolicy.SELECTION_ROLE, selectionPolicy);
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
	protected Command getCreateCommand(CreateRequest request) {
		if (isValidTarget()) {
			Object newObject = request.getNewObject();
			if (newObject instanceof final ComponentInfo component) {
				return new EditCommand(m_layout) {
					@Override
					protected void executeEdit() throws Exception {
						m_layout.command_CREATE(
								component,
								1 + m_target.m_column,
								m_target.m_columnInsert,
								1 + m_target.m_row,
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
									1 + m_target.m_column,
									m_target.m_columnInsert,
									1 + m_target.m_row,
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
								1 + m_target.m_column,
								m_target.m_columnInsert,
								1 + m_target.m_row,
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
			if (moveEditPart.getModel() instanceof ComponentInfo) {
				final ComponentInfo component = (ComponentInfo) moveEditPart.getModel();
				return new EditCommand(m_layout) {
					@Override
					protected void executeEdit() throws Exception {
						m_layout.command_ADD(
								component,
								1 + m_target.m_column,
								m_target.m_columnInsert,
								1 + m_target.m_row,
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
	@Override
	protected void updateGridTarget(Point mouseLocation) throws Exception {
		m_target = new GridTarget();
		//
		mouseLocation = mouseLocation.getCopy();
		FigureUtils.translateAbsoluteToFigure2(getHostFigure(), mouseLocation);
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
		int insertDelta = 3;
		// prepare insert bounds
		if (rowIntervals.length != 0) {
			m_target.m_columnInsertBounds.y = rowIntervals[0].begin() - INSERT_MARGINS;
			m_target.m_columnInsertBounds.setBottom(lastY + INSERT_MARGINS);
		}
		if (columnIntervals.length != 0) {
			m_target.m_rowInsertBounds.x = columnIntervals[0].begin() - INSERT_MARGINS;
			m_target.m_rowInsertBounds.setRight(lastX + INSERT_MARGINS);
		}
		// find existing column
		for (int columnIndex = 0; columnIndex < columnIntervals.length; columnIndex++) {
			Interval interval = columnIntervals[columnIndex];
			if (interval.contains(mouseLocation.x)) {
				List<FormColumnInfo> columns = m_layout.getColumns();
				FormColumnInfo column = columns.get(columnIndex);
				// check for prev/next gaps
				boolean prevGap;
				boolean nextGap;
				boolean isLast = columnIndex == columnIntervals.length - 1;
				{
					FormColumnInfo prevColumn =
							columnIndex != 0 ? (FormColumnInfo) columns.get(columnIndex - 1) : null;
					prevGap = prevColumn != null && prevColumn.isGap();
					FormColumnInfo nextColumn =
							columnIndex < columns.size() - 1
							? (FormColumnInfo) columns.get(columnIndex + 1)
									: null;
					nextGap = nextColumn != null && nextColumn.isGap();
				}
				//
				m_target.m_column = columnIndex;
				if (column.isGap()) {
					m_target.m_columnInsert = true;
					//
					m_target.m_feedbackBounds.width = 3 * interval.length();
					m_target.m_feedbackBounds.x = interval.center() - m_target.m_feedbackBounds.width / 2;
					//
					m_target.m_columnInsertBounds.x = interval.begin();
					m_target.m_columnInsertBounds.width = interval.length()+ 1;
				} else if (mouseLocation.x - interval.begin() <= insertDelta && !prevGap) {
					m_target.m_columnInsert = true;
					//
					m_target.m_feedbackBounds.width = 3 * INSERT_COLUMN_SIZE;
					m_target.m_feedbackBounds.x = interval.begin() - m_target.m_feedbackBounds.width / 2;
					//
					m_target.m_columnInsertBounds.x = interval.begin() - INSERT_COLUMN_SIZE / 2;
					m_target.m_columnInsertBounds.width = INSERT_COLUMN_SIZE;
				} else if (!isLast && interval.end() - mouseLocation.x <= insertDelta && !nextGap) {
					m_target.m_column++;
					m_target.m_columnInsert = true;
					//
					m_target.m_feedbackBounds.width = 3 * INSERT_COLUMN_SIZE;
					m_target.m_feedbackBounds.x = interval.end() - m_target.m_feedbackBounds.width / 2;
					//
					m_target.m_columnInsertBounds.x = interval.end() - INSERT_COLUMN_SIZE / 2;
					m_target.m_columnInsertBounds.width = INSERT_COLUMN_SIZE;
				} else {
					m_target.m_feedbackBounds.x = interval.begin();
					m_target.m_feedbackBounds.width = interval.length()+ 1;
				}
			}
		}
		// find virtual column
		if (m_target.m_column == -1) {
			int columnGap = gridInfo.getVirtualColumnGap();
			int newWidth = gridInfo.getVirtualColumnSize() + columnGap;
			int newDelta = 1 + Math.max(mouseLocation.x - lastX - columnGap / 2, 0) / newWidth;
			//
			m_target.m_column = columnIntervals.length + 2 * newDelta - 1;
			m_target.m_feedbackBounds.x = lastX + newWidth * (newDelta - 1) + columnGap;
			m_target.m_feedbackBounds.width = gridInfo.getVirtualColumnSize() + 1;
		}
		// find existing row
		for (int rowIndex = 0; rowIndex < rowIntervals.length; rowIndex++) {
			Interval interval = rowIntervals[rowIndex];
			if (interval.contains(mouseLocation.y)) {
				List<FormRowInfo> rows = m_layout.getRows();
				FormRowInfo row = rows.get(rowIndex);
				//
				// check for prev/next gaps
				boolean prevGap;
				boolean nextGap;
				boolean isLast = rowIndex == rowIntervals.length - 1;
				{
					FormRowInfo prevRow = rowIndex != 0 ? (FormRowInfo) rows.get(rowIndex - 1) : null;
					prevGap = prevRow != null && prevRow.isGap();
					FormRowInfo nextRow =
							rowIndex < rows.size() - 1 ? (FormRowInfo) rows.get(rowIndex + 1) : null;
					nextGap = nextRow != null && nextRow.isGap();
				}
				//
				m_target.m_row = rowIndex;
				if (row.isGap()) {
					m_target.m_rowInsert = true;
					//
					m_target.m_feedbackBounds.height = 3 * interval.length();
					m_target.m_feedbackBounds.y = interval.center() - m_target.m_feedbackBounds.height / 2;
					//
					m_target.m_rowInsertBounds.y = interval.begin();
					m_target.m_rowInsertBounds.height = interval.length()+ 1;
				} else if (mouseLocation.y - interval.begin() <= insertDelta && !prevGap) {
					m_target.m_rowInsert = true;
					//
					m_target.m_feedbackBounds.height = 3 * INSERT_ROW_SIZE;
					m_target.m_feedbackBounds.y = interval.begin() - m_target.m_feedbackBounds.height / 2;
					//
					m_target.m_rowInsertBounds.y = interval.begin() - INSERT_ROW_SIZE / 2;
					m_target.m_rowInsertBounds.height = INSERT_ROW_SIZE;
				} else if (!isLast && interval.end() - mouseLocation.y <= insertDelta && !nextGap) {
					m_target.m_row++;
					m_target.m_rowInsert = true;
					//
					m_target.m_feedbackBounds.height = 3 * INSERT_ROW_SIZE;
					m_target.m_feedbackBounds.y = interval.end() - m_target.m_feedbackBounds.height / 2;
					//
					m_target.m_rowInsertBounds.y = interval.end() - INSERT_ROW_SIZE / 2;
					m_target.m_rowInsertBounds.height = INSERT_ROW_SIZE;
				} else {
					m_target.m_feedbackBounds.y = interval.begin();
					m_target.m_feedbackBounds.height = interval.length()+ 1;
				}
			}
		}
		// find virtual row
		if (m_target.m_row == -1) {
			int rowGap = gridInfo.getVirtualRowGap();
			int newHeight = gridInfo.getVirtualRowSize() + rowGap;
			int newDelta = 1 + Math.max(mouseLocation.y - lastY - rowGap / 2, 0) / newHeight;
			//
			m_target.m_row = rowIntervals.length + 2 * newDelta - 1;
			m_target.m_feedbackBounds.y = lastY + newHeight * (newDelta - 1) + rowGap;
			m_target.m_feedbackBounds.height = gridInfo.getVirtualRowSize() + 1;
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
	public EditPart createHeaderEditPart(boolean horizontal, Object model) {
		if (horizontal) {
			return new ColumnHeaderEditPart(m_layout, (FormColumnInfo) model, getHostFigure());
		} else {
			return new RowHeaderEditPart(m_layout, (FormRowInfo) model, getHostFigure());
		}
	}

	@Override
	public void buildContextMenu(IMenuManager manager, boolean horizontal) {
		if (horizontal) {
			manager.add(new ObjectInfoAction(m_layout,
					GefMessages.FormLayoutEditPolicy_appendColumnAction) {
				@Override
				protected void runEx() throws Exception {
					m_layout.insertColumn(m_layout.getColumns().size());
				}
			});
		} else {
			manager.add(new ObjectInfoAction(m_layout, GefMessages.FormLayoutEditPolicy_appendRowAction) {
				@Override
				protected void runEx() throws Exception {
					m_layout.insertRow(m_layout.getRows().size());
				}
			});
		}
	}

	@Override
	public void handleDoubleClick(boolean horizontal) {
		if (horizontal) {
			new ColumnsDialog(DesignerPlugin.getShell(), m_layout).open();
		} else {
			new RowsDialog(DesignerPlugin.getShell(), m_layout).open();
		}
	}
}
