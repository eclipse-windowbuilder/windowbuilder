/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.forms.layout.table;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildGraphical;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildTree;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.ComponentClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData;
import org.eclipse.wb.internal.core.model.util.grid.GridAlignmentHelper;
import org.eclipse.wb.internal.core.model.util.grid.GridAlignmentHelper.IAlignmentProcessor;
import org.eclipse.wb.internal.core.model.variable.EmptyPureVariableSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.actions.SelectionActionsSupport;
import org.eclipse.wb.internal.swt.model.layout.LayoutClipboardCommand;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.VirtualLayoutDataStatementGenerator;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.support.LabelSupport;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.bidimap.UnmodifiableBidiMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Model for {@link TableWrapLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class TableWrapLayoutInfo extends LayoutInfo
implements
ITableWrapLayoutInfo<ControlInfo>,
IPreferenceConstants {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TableWrapLayoutInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		new TableWrapLayoutAssistant(this);
		new SelectionActionsSupport<>(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void onSet() throws Exception {
		super.onSet();
		TableWrapLayoutConverter.convert(getComposite(), this);
	}

	@Override
	protected void onDelete() throws Exception {
		// delete filler's
		for (ControlInfo control : getComposite().getChildrenControls()) {
			if (isFiller(control)) {
				control.delete();
			}
		}
		// delete other
		super.onDelete();
	}

	@Override
	protected void initialize() throws Exception {
		super.initialize();
		// add listeners
		addBroadcastListener(new ObjectInfoChildTree() {
			@Override
			public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
				if (object instanceof ControlInfo) {
					visible[0] &= !isFiller((ControlInfo) object);
				}
			}
		});
		addBroadcastListener(new ObjectInfoChildGraphical() {
			@Override
			public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
				if (object instanceof ControlInfo) {
					visible[0] &= !isFiller((ControlInfo) object);
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void refresh_dispose() throws Exception {
		m_gridInfo = null;
		super.refresh_dispose();
	}

	@Override
	protected void refresh_afterCreate() throws Exception {
		replaceLayoutObjects();
		super.refresh_afterCreate();
		fixEmptyColumns();
	}

	/**
	 * Ensures that empty columns (only with fillers) have reasonable width.
	 */
	private void fixEmptyColumns() throws Exception {
		ControlInfo[][] grid = getControlsGrid();
		int columnCount = grid.length != 0 ? grid[0].length : 0;
		m_columnWidths = TableWrapLayoutSupport.getColumnWidths(getObject());
		// set empty text for fillers in empty column
		for (int column = 0; column < columnCount; column++) {
			int width = m_columnWidths[column];
			if (width == 0) {
				for (int row = 0; row < grid.length; row++) {
					ControlInfo control = grid[row][column];
					if (control != null && isFiller(control)) {
						LabelSupport.setText(control.getObject(), "      ");
					}
				}
			}
		}
	}

	@Override
	protected void refresh_fetch() throws Exception {
		super.refresh_fetch();
		Object layoutObject = getObject();
		//
		m_columnOrigins = TableWrapLayoutSupport.getColumnOrigins(layoutObject);
		m_rowOrigins = TableWrapLayoutSupport.getRowOrigins(layoutObject);
		m_columnWidths = TableWrapLayoutSupport.getColumnWidths(layoutObject);
		m_rowHeights = TableWrapLayoutSupport.getRowHeights(layoutObject);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Replace
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Replaces standard {@link TableWrapLayout} and {@link TableWrapData} with our
	 * {@link TableWrapLayout2} and {@link TableWrapData2}.
	 */
	private void replaceLayoutObjects() throws Exception {
		Composite composite = (Composite) getComposite().getObject();
		Layout layout = composite.getLayout();
		// update TableWrapLayout
		if (layout.getClass().getName().equals("org.eclipse.ui.forms.widgets.TableWrapLayout")) {
			TableWrapLayout2 newLayout = new TableWrapLayout2();
			copyFields(layout, newLayout);
			// update Composite and model
			composite.setLayout(newLayout);
			setObject(newLayout);
		}
		// update TableWrapData's
		for (Control child : composite.getChildren()) {
			Object layoutData = child.getLayoutData();
			if (layoutData != null
					&& layoutData.getClass().getName().equals("org.eclipse.ui.forms.widgets.TableWrapData")) {
				TableWrapData2 newLayoutData = new TableWrapData2();
				copyFields(layoutData, newLayoutData);
				// set new TableWrapData into Control
				child.setLayoutData(newLayoutData);
			}
		}
		// force layout() to recalculate "design" fields
		composite.layout();
		// update TableWrapDataInfo's
		for (ControlInfo controlInfo : getComposite().getChildrenControls()) {
			Control control = (Control) controlInfo.getObject();
			Object layoutData = control.getLayoutData();
			if (layoutData != null) {
				TableWrapDataInfo layoutDataInfo = getTableWrapData(controlInfo);
				layoutDataInfo.setObject(layoutData);
			}
		}
	}

	/**
	 * Copies values of public instance {@link Field}'s from source object to target object. It is
	 * expected that every source field exists in target.
	 */
	private static void copyFields(Object source, Object target) throws Exception {
		for (Field field : source.getClass().getFields()) {
			int modifiers = field.getModifiers();
			if (!Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) {
				Object value = field.get(source);
				ReflectionUtils.setField(target, field.getName(), value);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Components/constraints
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean m_replaceWithFillers = true;
	private TableWrapDataInfo m_removingLayoutData;

	@Override
	protected Object getDefaultVirtualDataObject() throws Exception {
		return TableWrapLayoutSupport.createTableWrapData();
	}

	@Override
	protected void onControlRemoveBefore(ControlInfo control) throws Exception {
		// remember TableWrapDataInfo for using later in "remove after"
		m_removingLayoutData = getTableWrapData(control);
		// continue
		super.onControlRemoveBefore(control);
	}

	@Override
	protected void onControlRemoveAfter(ControlInfo control) throws Exception {
		// replace control with fillers
		if (m_replaceWithFillers && !isFiller(control)) {
			// replace with fillers
			{
				TableWrapDataInfo layoutData = m_removingLayoutData;
				for (int x = layoutData.x; x < layoutData.x + layoutData.width; x++) {
					for (int y = layoutData.y; y < layoutData.y + layoutData.height; y++) {
						addFiller(x, y);
					}
				}
			}
			// delete empty columns/rows
			deleteEmptyColumnsRows(m_removingLayoutData);
			m_removingLayoutData = null;
		}
		// continue
		super.onControlRemoveAfter(control);
	}

	@Override
	public ITableWrapDataInfo getTableWrapData2(ControlInfo control) {
		return getTableWrapData(control);
	}

	/**
	 * @return {@link TableWrapDataInfo} association with given {@link ControlInfo}.
	 */
	public TableWrapDataInfo getTableWrapData(final ControlInfo control) {
		return ExecutionUtils.runObject(() -> {
			TableWrapDataInfo layoutData = (TableWrapDataInfo) getLayoutData(control);
			layoutData.initialize(TableWrapLayoutInfo.this, control);
			return layoutData;
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dimensions operations
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Removes empty (only with fillers) columns/rows.
	 */
	void deleteEmptyColumnsRows(TableWrapDataInfo removingData) throws Exception {
		deleteEmptyColumns(removingData);
		deleteEmptyRows(removingData);
	}

	/**
	 * Deletes empty (only with fillers) columns.
	 */
	private void deleteEmptyColumns(TableWrapDataInfo removingData) throws Exception {
		ControlInfo[][] grid = getControlsGrid();
		boolean deleteOnlyIfIsRemovingColumn = false;
		for (int column = grid[0].length - 1; column >= 0; column--) {
			boolean isRemovingColumn =
					removingData != null
					&& removingData.x <= column
					&& column < removingData.x + removingData.width;
			// check if empty
			boolean isEmpty = true;
			for (int row = 0; row < grid.length; row++) {
				ControlInfo control = grid[row][column];
				isEmpty &= isFiller(control);
			}
			// delete if empty
			if (isEmpty && (!deleteOnlyIfIsRemovingColumn || isRemovingColumn)) {
				command_deleteColumn(column, false);
			} else {
				deleteOnlyIfIsRemovingColumn = true;
			}
		}
	}

	/**
	 * Deletes empty (only with fillers) rows.
	 */
	private void deleteEmptyRows(TableWrapDataInfo removingData) throws Exception {
		ControlInfo[][] grid = getControlsGrid();
		boolean deleteOnlyIfIsRemovingRow = false;
		for (int row = grid.length - 1; row >= 0; row--) {
			boolean isRemovingRow =
					removingData != null
					&& removingData.y <= row
					&& row < removingData.y + removingData.height;
			// check if empty
			boolean isEmpty = true;
			for (int column = 0; column < grid[row].length; column++) {
				ControlInfo control = grid[row][column];
				isEmpty &= isFiller(control);
			}
			// delete if empty
			if (isEmpty && (!deleteOnlyIfIsRemovingRow || isRemovingRow)) {
				command_deleteRow(row, false);
			} else {
				deleteOnlyIfIsRemovingRow = true;
			}
		}
	}

	@Override
	public void command_deleteColumn(int column, boolean deleteEmptyRows) throws Exception {
		int columnCount = getControlsGridSize().width;
		// update TableWrapData, delete controls
		m_replaceWithFillers = false;
		try {
			for (ControlInfo control : getComposite().getChildrenControls()) {
				TableWrapDataInfo layoutData = getTableWrapData(control);
				//
				if (layoutData.x == column) {
					control.delete();
				} else if (layoutData.x > column) {
					layoutData.x--;
				} else if (layoutData.x + layoutData.width > column) {
					layoutData.setHorizontalSpan(layoutData.width - 1);
				}
			}
		} finally {
			m_replaceWithFillers = true;
		}
		// update count
		if (columnCount >= 2) {
			getPropertyByTitle("numColumns").setValue(columnCount - 1);
		}
		// it is possible, that we have now empty rows, so delete them too
		if (deleteEmptyRows) {
			deleteEmptyRows(null);
		}
	}

	@Override
	public void command_deleteRow(int row, boolean deleteEmptyColumn) throws Exception {
		// update TableWrapData, delete controls
		m_replaceWithFillers = false;
		try {
			for (ControlInfo control : getComposite().getChildrenControls()) {
				TableWrapDataInfo layoutData = getTableWrapData(control);
				//
				if (layoutData.y == row) {
					control.delete();
				} else if (layoutData.y > row) {
					layoutData.y--;
				} else if (layoutData.y + layoutData.height > row) {
					layoutData.setVerticalSpan(layoutData.height - 1);
				}
			}
		} finally {
			m_replaceWithFillers = true;
		}
		// it is possible, that we have now empty columns, so delete them too
		if (deleteEmptyColumn) {
			deleteEmptyColumns(null);
		}
	}

	@Override
	public void command_MOVE_COLUMN(int fromIndex, int toIndex) throws Exception {
		fixGrid();
		// move column in columns list
		{
			getColumns(); // kick to initialize columns
			TableWrapColumnInfo<ControlInfo> column = m_columns.remove(fromIndex);
			if (fromIndex < toIndex) {
				m_columns.add(toIndex - 1, column);
			} else {
				m_columns.add(toIndex, column);
			}
		}
		// prepare new column
		prepareCell(toIndex, true, -1, false);
		if (toIndex < fromIndex) {
			fromIndex++;
		}
		// move children
		for (ControlInfo control : getComposite().getChildrenControls()) {
			if (!isFiller(control)) {
				TableWrapDataInfo layoutData = getTableWrapData(control);
				if (layoutData.x == fromIndex) {
					command_setCells(
							control,
							new Rectangle(toIndex, layoutData.y, 1, layoutData.height),
							true);
				}
			}
		}
		// delete old column
		command_deleteColumn(fromIndex, false);
		deleteEmptyColumnsRows(null);
	}

	@Override
	public void command_MOVE_ROW(int fromIndex, int toIndex) throws Exception {
		fixGrid();
		// move row in rows list
		{
			getRows(); // kick to initialize rows
			TableWrapRowInfo<ControlInfo> row = m_rows.remove(fromIndex);
			if (fromIndex < toIndex) {
				m_rows.add(toIndex - 1, row);
			} else {
				m_rows.add(toIndex, row);
			}
		}
		// prepare new row
		prepareCell(-1, false, toIndex, true);
		if (toIndex < fromIndex) {
			fromIndex++;
		}
		// move children
		for (ControlInfo control : getComposite().getChildrenControls()) {
			if (!isFiller(control)) {
				TableWrapDataInfo layoutData = getTableWrapData(control);
				if (layoutData.y == fromIndex) {
					command_setCells(control, new Rectangle(layoutData.x, toIndex, layoutData.width, 1), true);
				}
			}
		}
		// delete old row
		command_deleteRow(fromIndex, false);
		deleteEmptyColumnsRows(null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dimensions access
	//
	////////////////////////////////////////////////////////////////////////////
	private final List<TableWrapColumnInfo<ControlInfo>> m_columns = new ArrayList<>();
	private final List<TableWrapRowInfo<ControlInfo>> m_rows = new ArrayList<>();

	@Override
	public List<TableWrapColumnInfo<ControlInfo>> getColumns() {
		Dimension size = getControlsGridSize();
		if (m_columns.size() != size.width) {
			m_columns.clear();
			for (int i = 0; i < size.width; i++) {
				m_columns.add(new TableWrapColumnInfo<>(this));
			}
		}
		return m_columns;
	}

	@Override
	public List<TableWrapRowInfo<ControlInfo>> getRows() {
		Dimension size = getControlsGridSize();
		if (m_rows.size() != size.height) {
			m_rows.clear();
			for (int i = 0; i < size.height; i++) {
				m_rows.add(new TableWrapRowInfo<>(this));
			}
		}
		return m_rows;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void command_CREATE(ControlInfo newControl,
			int column,
			boolean insertColumn,
			int row,
			boolean insertRow) throws Exception {
		startEdit();
		try {
			command_CREATE(newControl, null);
			// move to required cell
			prepareCell(column, insertColumn, row, insertRow);
			command_setCells(newControl, new Rectangle(column, row, 1, 1), false);
			// perform automatic alignment
			doAutomaticAlignment(newControl);
		} finally {
			endEdit();
		}
	}

	@Override
	public void command_MOVE(ControlInfo control,
			int column,
			boolean insertColumn,
			int row,
			boolean insertRow) throws Exception {
		startEdit();
		try {
			prepareCell(column, insertColumn, row, insertRow);
			command_setCells(control, new Rectangle(column, row, 1, 1), true);
			deleteEmptyColumnsRows(null);
		} finally {
			endEdit();
		}
	}

	@Override
	public void command_ADD(ControlInfo control,
			int column,
			boolean insertColumn,
			int row,
			boolean insertRow) throws Exception {
		startEdit();
		try {
			command_MOVE(control, null);
			// move to required cell
			prepareCell(column, insertColumn, row, insertRow);
			command_setCells(control, new Rectangle(column, row, 1, 1), false);
		} finally {
			endEdit();
		}
	}

	/**
	 * Prepares cell with given column/row - inserts columns/rows if necessary.
	 */
	private void prepareCell(int column, boolean insertColumn, int row, boolean insertRow)
			throws Exception {
		// prepare count of columns/rows
		int columnCount;
		int rowCount;
		{
			Dimension gridSize = getControlsGridSize();
			columnCount = gridSize.width;
			rowCount = gridSize.height;
		}
		// append
		{
			int newColumnCount = Math.max(columnCount, 1 + column);
			int newRowCount = Math.max(rowCount, 1 + row);
			// append rows
			for (int newRow = rowCount; newRow <= row; newRow++) {
				for (int columnIndex = 0; columnIndex < newColumnCount; columnIndex++) {
					addFiller(columnIndex, newRow);
				}
			}
			// append columns
			getPropertyByTitle("numColumns").setValue(newColumnCount);
			for (int newColumn = columnCount; newColumn <= column; newColumn++) {
				for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
					addFiller(newColumn, rowIndex);
				}
			}
			// set new count of columns/rows
			columnCount = newColumnCount;
			rowCount = newRowCount;
		}
		// insert row
		if (insertRow) {
			rowCount++;
			// update TableWrapData for all controls
			boolean[] columnsToIgnore = new boolean[columnCount];
			for (ControlInfo control : getComposite().getChildrenControls()) {
				TableWrapDataInfo layoutData = getTableWrapData(control);
				//
				if (layoutData.y >= row) {
					layoutData.y++;
				} else if (layoutData.y + layoutData.height > row) {
					layoutData.setVerticalSpan(layoutData.height + 1);
					for (int i = layoutData.x; i < layoutData.x + layoutData.width; i++) {
						columnsToIgnore[i] = true;
					}
				}
			}
			// add fillers for new row
			for (int i = 0; i < columnCount; i++) {
				if (!columnsToIgnore[i]) {
					addFiller(i, row);
				}
			}
		}
		// insert column
		if (insertColumn) {
			// update TableWrapData for all controls
			boolean[] rowsToIgnore = new boolean[rowCount];
			for (ControlInfo control : getComposite().getChildrenControls()) {
				TableWrapDataInfo layoutData = getTableWrapData(control);
				//
				if (layoutData.x >= column) {
					layoutData.x++;
				} else if (layoutData.x + layoutData.width > column) {
					layoutData.setHorizontalSpan(layoutData.width + 1);
					for (int i = layoutData.y; i < layoutData.y + layoutData.height; i++) {
						rowsToIgnore[i] = true;
					}
				}
			}
			// insert fillers for new column
			getPropertyByTitle("numColumns").setValue(columnCount + 1);
			for (int i = 0; i < rowCount; i++) {
				if (!rowsToIgnore[i]) {
					addFiller(column, i);
				}
			}
		}
	}

	@Override
	public void command_setCells(ControlInfo control, Rectangle cells, boolean forMove)
			throws Exception {
		TableWrapDataInfo layoutData = getTableWrapData(control);
		// prepare grid
		fixGrid();
		ControlInfo[][] grid = getControlsGrid();
		Set<Point> cellsToAddFillers = new HashSet<>();
		Set<Point> cellsToRemoveFillers = new HashSet<>();
		// replace control with fillers
		if (forMove) {
			for (int x = layoutData.x; x < layoutData.x + layoutData.width; x++) {
				for (int y = layoutData.y; y < layoutData.y + layoutData.height; y++) {
					Point cell = new Point(x, y);
					cellsToAddFillers.add(cell);
				}
			}
		}
		// remove fillers from occupied cells
		for (int x = cells.x; x < cells.right(); x++) {
			for (int y = cells.y; y < cells.bottom(); y++) {
				Point cell = new Point(x, y);
				cellsToAddFillers.remove(cell);
				if (isFiller(grid[y][x])) {
					cellsToRemoveFillers.add(cell);
				}
			}
		}
		// do edit operations
		startEdit();
		try {
			// move
			if (layoutData.x != cells.x || layoutData.y != cells.y) {
				// update TableWrapData
				{
					layoutData.x = cells.x;
					layoutData.y = cells.y;
				}
				// move model
				{
					ControlInfo reference = getReferenceControl(cells.y, cells.x, control);
					command_MOVE(control, reference);
				}
			}
			// set span
			{
				layoutData.setHorizontalSpan(cells.width);
				layoutData.setVerticalSpan(cells.height);
			}
			// remove fillers
			for (Point cell : cellsToRemoveFillers) {
				ControlInfo filler = grid[cell.y][cell.x];
				filler.delete();
			}
			// add fillers
			for (Point cell : cellsToAddFillers) {
				addFiller(cell.x, cell.y);
			}
		} finally {
			endEdit();
		}
	}

	@Override
	public void command_setHeightHint(ControlInfo control, int size) throws Exception {
		startEdit();
		try {
			TableWrapDataInfo layoutData = getTableWrapData(control);
			layoutData.setHeightHint(size);
		} finally {
			endEdit();
		}
	}

	/**
	 * @return the {@link Point} with size of controls grid..
	 */
	private Dimension getControlsGridSize() {
		int columnCount = 0;
		int rowCount = 0;
		for (ControlInfo control : getComposite().getChildrenControls()) {
			TableWrapDataInfo layoutData = getTableWrapData(control);
			//
			columnCount = Math.max(columnCount, layoutData.x + layoutData.width);
			rowCount = Math.max(rowCount, layoutData.y + layoutData.height);
		}
		// OK, we have grid
		return new Dimension(columnCount, rowCount);
	}

	/**
	 * @return the double array of {@link ControlInfo} where each element contains {@link ControlInfo}
	 *         that occupies this cell.
	 */
	private ControlInfo[][] getControlsGrid() throws Exception {
		Dimension gridSize = getControlsGridSize();
		// prepare empty grid
		ControlInfo[][] grid;
		{
			grid = new ControlInfo[gridSize.height][];
			for (int rowIndex = 0; rowIndex < grid.length; rowIndex++) {
				grid[rowIndex] = new ControlInfo[gridSize.width];
			}
		}
		// fill grid
		for (ControlInfo control : getComposite().getChildrenControls()) {
			// prepare cells
			Rectangle cells;
			{
				TableWrapDataInfo layoutData = getTableWrapData(control);
				cells = new Rectangle(layoutData.x, layoutData.y, layoutData.width, layoutData.height);
			}
			// fill grid cells
			for (int x = cells.x; x < cells.right(); x++) {
				for (int y = cells.y; y < cells.bottom(); y++) {
					// ignore newly added controls without real cell
					if (x != -1 && y != -1) {
						grid[y][x] = control;
					}
				}
			}
		}
		// OK, we have grid
		return grid;
	}

	@Override
	public void fixGrid() throws Exception {
		ControlInfo[][] grid = getControlsGrid();
		for (int row = 0; row < grid.length; row++) {
			for (int column = 0; column < grid[row].length; column++) {
				if (grid[row][column] == null) {
					addFiller(column, row);
				}
			}
		}
	}

	/**
	 * @return the {@link ControlInfo} that should be used as reference of adding into specified cell.
	 *
	 * @param exclude
	 *          the {@link ControlInfo} that should not be checked, for example because we move it now
	 */
	private ControlInfo getReferenceControl(int row, int column, ControlInfo exclude)
			throws Exception {
		for (ControlInfo control : getComposite().getChildrenControls()) {
			if (control != exclude) {
				TableWrapDataInfo layoutData = getTableWrapData(control);
				if (layoutData.y > row || layoutData.y == row && layoutData.x >= column) {
					return control;
				}
			}
		}
		// no reference
		return null;
	}

	/**
	 * Adds filler {@link ControlInfo} into given cell.
	 */
	private void addFiller(int column, int row) throws Exception {
		// prepare creation support
		ConstructorCreationSupport creationSupport = new ConstructorCreationSupport(null, false);
		// prepare filler
		ControlInfo filler =
				(ControlInfo) JavaInfoUtils.createJavaInfo(
						getEditor(),
						LabelSupport.getLabelClass(),
						creationSupport);
		// add filler
		ControlInfo reference = getReferenceControl(row, column, null);
		JavaInfoUtils.add(
				filler,
				new EmptyPureVariableSupport(filler),
				PureFlatStatementGenerator.INSTANCE,
				null,
				getComposite(),
				reference);
		// set x/y for new filler
		TableWrapDataInfo layoutData = getTableWrapData(filler);
		layoutData.x = column;
		layoutData.y = row;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Automatic alignment
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Performs automatic alignment, such as grab/fill for Text/Table/etc, right alignment for Label.
	 */
	private void doAutomaticAlignment(ControlInfo control) throws Exception {
		final IPreferenceStore preferences = getDescription().getToolkit().getPreferences();
		GridAlignmentHelper.doAutomaticAlignment(control, new IAlignmentProcessor<ControlInfo>() {
			@Override
			public boolean grabEnabled() {
				return preferences.getBoolean(P_ENABLE_GRAB);
			}

			@Override
			public boolean rightEnabled() {
				return preferences.getBoolean(P_ENABLE_RIGHT_ALIGNMENT);
			}

			@Override
			public ControlInfo getComponentAtLeft(ControlInfo component) {
				TableWrapDataInfo layoutData = getTableWrapData(component);
				return getControlAt(layoutData.x - 1, layoutData.y);
			}

			@Override
			public ControlInfo getComponentAtRight(ControlInfo component) {
				TableWrapDataInfo layoutData = getTableWrapData(component);
				return getControlAt(layoutData.x + 1, layoutData.y);
			}

			@Override
			public void setGrabFill(ControlInfo component, boolean horizontal) throws Exception {
				TableWrapDataInfo layoutData = getTableWrapData(component);
				if (horizontal) {
					layoutData.setHorizontalGrab(true);
					layoutData.setHorizontalAlignment(TableWrapData.FILL);
				} else {
					layoutData.setVerticalGrab(true);
					layoutData.setVerticalAlignment(TableWrapData.FILL);
				}
			}

			@Override
			public void setRightAlignment(ControlInfo component) throws Exception {
				TableWrapDataInfo layoutData = getTableWrapData(component);
				layoutData.setHorizontalAlignment(TableWrapData.RIGHT);
			}
		});
	}

	/**
	 * @return the {@link ControlInfo} with given top-left cell, may be <code>null</code>.
	 */
	private ControlInfo getControlAt(int x, int y) {
		for (ControlInfo control : getComposite().getChildrenControls()) {
			TableWrapDataInfo layoutData = getTableWrapData(control);
			if (layoutData.x == x && layoutData.y == y) {
				return control;
			}
		}
		// no such control
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IGridInfo support
	//
	////////////////////////////////////////////////////////////////////////////
	private IGridInfo m_gridInfo;
	private int[] m_columnOrigins;
	private int[] m_rowOrigins;
	private int[] m_columnWidths;
	private int[] m_rowHeights;

	/**
	 * @return the {@link IGridInfo} that describes this layout.
	 */
	@Override
	public IGridInfo getGridInfo() {
		if (m_gridInfo == null) {
			ExecutionUtils.runRethrow(new RunnableEx() {
				@Override
				public void run() throws Exception {
					createGridInfo();
				}
			});
		}
		return m_gridInfo;
	}

	/**
	 * @return the origins of the columns.
	 */
	public int[] getColumnOrigins() {
		return m_columnOrigins;
	}

	/**
	 * @return the origins of the rows.
	 */
	public int[] getRowOrigins() {
		return m_rowOrigins;
	}

	/**
	 * Initializes {@link #m_gridInfo}.
	 */
	private void createGridInfo() throws Exception {
		// prepare intervals
		final Interval[] columnIntervals = getIntervals(m_columnOrigins, m_columnWidths);
		final Interval[] rowIntervals = getIntervals(m_rowOrigins, m_rowHeights);
		// prepare cells
		final Map<ControlInfo, Rectangle> componentToCells = new HashMap<>();
		final Map<Point, ControlInfo> occupiedCells = new HashMap<>();
		{
			for (ControlInfo control : getComposite().getChildrenControls()) {
				// prepare cells
				Rectangle cells;
				{
					TableWrapDataInfo layoutData = getTableWrapData(control);
					cells = new Rectangle(layoutData.x, layoutData.y, layoutData.width, layoutData.height);
				}
				// fill map: ControlInfo -> cells Rectangle
				componentToCells.put(control, cells);
				// ignore filler
				if (isFiller(control)) {
					continue;
				}
				// fill occupied cells map: cell -> ControlInfo
				for (int x = cells.x; x < cells.right(); x++) {
					for (int y = cells.y; y < cells.bottom(); y++) {
						occupiedCells.put(new Point(x, y), control);
					}
				}
			}
		}
		// create IGridInfo instance
		m_gridInfo = new IGridInfo() {
			////////////////////////////////////////////////////////////////////////////
			//
			// Dimensions
			//
			////////////////////////////////////////////////////////////////////////////
			@Override
			public int getColumnCount() {
				return columnIntervals.length;
			}

			@Override
			public int getRowCount() {
				return rowIntervals.length;
			}

			////////////////////////////////////////////////////////////////////////////
			//
			// Intervals
			//
			////////////////////////////////////////////////////////////////////////////
			@Override
			public Interval[] getColumnIntervals() {
				return columnIntervals;
			}

			@Override
			public Interval[] getRowIntervals() {
				return rowIntervals;
			}

			////////////////////////////////////////////////////////////////////////////
			//
			// Cells
			//
			////////////////////////////////////////////////////////////////////////////
			@Override
			public Rectangle getComponentCells(IAbstractComponentInfo component) {
				Assert.instanceOf(ControlInfo.class, component);
				return componentToCells.get(component);
			}

			@Override
			public Rectangle getCellsRectangle(Rectangle cells) {
				int x = columnIntervals[cells.x].begin();
				int y = rowIntervals[cells.y].begin();
				int w = columnIntervals[cells.right() - 1].end() - x;
				int h = rowIntervals[cells.bottom() - 1].end() - y;
				return new Rectangle(x, y, w, h);
			}

			////////////////////////////////////////////////////////////////////////////
			//
			// Feedback
			//
			////////////////////////////////////////////////////////////////////////////
			@Override
			public boolean isRTL() {
				return false;
			}

			@Override
			public Insets getInsets() {
				return new Insets();
			}

			////////////////////////////////////////////////////////////////////////////
			//
			// Virtual columns
			//
			////////////////////////////////////////////////////////////////////////////
			@Override
			public boolean hasVirtualColumns() {
				return true;
			}

			@Override
			public int getVirtualColumnSize() {
				return 25;
			}

			@Override
			public int getVirtualColumnGap() {
				return 5;
			}

			////////////////////////////////////////////////////////////////////////////
			//
			// Virtual rows
			//
			////////////////////////////////////////////////////////////////////////////
			@Override
			public boolean hasVirtualRows() {
				return true;
			}

			@Override
			public int getVirtualRowSize() {
				return 25;
			}

			@Override
			public int getVirtualRowGap() {
				return 5;
			}

			////////////////////////////////////////////////////////////////////////////
			//
			// Checks
			//
			////////////////////////////////////////////////////////////////////////////
			@Override
			public IAbstractComponentInfo getOccupied(int column, int row) {
				return occupiedCells.get(new Point(column, row));
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the array of {@link Interval}'s for given arrays of origins/sizes.
	 */
	private static Interval[] getIntervals(int[] origins, int[] sizes) {
		Interval[] intervals = new Interval[origins.length];
		for (int i = 0; i < intervals.length; i++) {
			intervals[i] = new Interval(origins[i], sizes[i]);
		}
		return intervals;
	}

	@Override
	public boolean isFiller(ControlInfo control) {
		return control.getVariableSupport() instanceof EmptyVariableSupport
				&& control.getDescription().getComponentClass().getName().equals(
						"org.eclipse.swt.widgets.Label");
	}

	//////////////////////////////////////////////////////////////////////////
	//
	// Clipboard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void clipboardCopy_addControlCommands(ControlInfo control,
			List<ClipboardCommand> commands) throws Exception {
		if (!isFiller(control)) {
			TableWrapDataInfo layoutData = getTableWrapData(control);
			final int column = layoutData.x;
			final int row = layoutData.y;
			//
			commands.add(new LayoutClipboardCommand<TableWrapLayoutInfo>(control) {
				private static final long serialVersionUID = 0L;

				@Override
				protected void add(TableWrapLayoutInfo layout, ControlInfo control) throws Exception {
					layout.command_CREATE(control, column, false, row, false);
				}
			});
			// command for TableWrapData
			if (!(layoutData.getCreationSupport() instanceof IImplicitCreationSupport)) {
				final int index = getComposite().getChildrenControls().indexOf(control);
				final JavaInfoMemento dataMemento = JavaInfoMemento.createMemento(layoutData);
				commands.add(new ComponentClipboardCommand<CompositeInfo>() {
					private static final long serialVersionUID = 0L;

					@Override
					public void execute(CompositeInfo composite) throws Exception {
						ControlInfo control = composite.getChildrenControls().get(index);
						control.removeChild(LayoutInfo.getLayoutData(control));
						//
						TableWrapDataInfo layoutData = (TableWrapDataInfo) dataMemento.create(composite);
						AssociationObject association =
								AssociationObjects.invocationChild("%parent%.setLayoutData(%child%)", false);
						JavaInfoUtils.add(
								layoutData,
								new LocalUniqueVariableSupport(layoutData),
								VirtualLayoutDataStatementGenerator.INSTANCE,
								association,
								control,
								null);
						dataMemento.apply();
					}
				});
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Manage general layout data.
	//
	////////////////////////////////////////////////////////////////////////////
	static final BidiMap<GeneralLayoutData.HorizontalAlignment, Integer> m_horizontalAlignmentMap;
	static {
		BidiMap<GeneralLayoutData.HorizontalAlignment, Integer> horizontalAlignmentMap = new DualHashBidiMap<>();
		horizontalAlignmentMap.put(GeneralLayoutData.HorizontalAlignment.LEFT, TableWrapData.LEFT);
		horizontalAlignmentMap.put(GeneralLayoutData.HorizontalAlignment.CENTER, TableWrapData.CENTER);
		horizontalAlignmentMap.put(GeneralLayoutData.HorizontalAlignment.RIGHT, TableWrapData.RIGHT);
		horizontalAlignmentMap.put(GeneralLayoutData.HorizontalAlignment.FILL, TableWrapData.FILL);
		m_horizontalAlignmentMap = UnmodifiableBidiMap.unmodifiableBidiMap(horizontalAlignmentMap);
	}
	static final BidiMap<GeneralLayoutData.VerticalAlignment, Integer> m_verticalAlignmentMap;
	static {
		BidiMap<GeneralLayoutData.VerticalAlignment, Integer> verticalAlignmentMap = new DualHashBidiMap<>();
		verticalAlignmentMap.put(GeneralLayoutData.VerticalAlignment.TOP, TableWrapData.TOP);
		verticalAlignmentMap.put(GeneralLayoutData.VerticalAlignment.CENTER, TableWrapData.CENTER);
		verticalAlignmentMap.put(GeneralLayoutData.VerticalAlignment.BOTTOM, TableWrapData.BOTTOM);
		verticalAlignmentMap.put(GeneralLayoutData.VerticalAlignment.FILL, TableWrapData.FILL);
		m_verticalAlignmentMap = UnmodifiableBidiMap.unmodifiableBidiMap(verticalAlignmentMap);
	}

	@Override
	protected void storeLayoutData(ControlInfo control, LayoutDataInfo layoutData) throws Exception {
		TableWrapDataInfo tableLayoutData = (TableWrapDataInfo) layoutData;
		GeneralLayoutData generalLayoutData = new GeneralLayoutData();
		generalLayoutData.gridX = tableLayoutData.x;
		generalLayoutData.gridY = tableLayoutData.y;
		generalLayoutData.spanX =
				(Integer) GeneralLayoutData.getLayoutPropertyValue(tableLayoutData, "colspan");
		generalLayoutData.spanY =
				(Integer) GeneralLayoutData.getLayoutPropertyValue(tableLayoutData, "rowspan");
		generalLayoutData.horizontalGrab =
				(Boolean) GeneralLayoutData.getLayoutPropertyValue(tableLayoutData, "grabHorizontal");
		generalLayoutData.verticalGrab =
				(Boolean) GeneralLayoutData.getLayoutPropertyValue(tableLayoutData, "grabVertical");
				// alignments
				generalLayoutData.horizontalAlignment =
						GeneralLayoutData.getGeneralValue(
								m_horizontalAlignmentMap,
								(Integer) GeneralLayoutData.getLayoutPropertyValue(tableLayoutData, "align"));
				generalLayoutData.verticalAlignment =
						GeneralLayoutData.getGeneralValue(
								m_verticalAlignmentMap,
								(Integer) GeneralLayoutData.getLayoutPropertyValue(tableLayoutData, "valign"));
				generalLayoutData.putToInfo(control);
	}
}
