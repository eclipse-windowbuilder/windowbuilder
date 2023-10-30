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
package org.eclipse.wb.internal.swing.MigLayout.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.util.grid.GridAlignmentHelper;
import org.eclipse.wb.internal.core.model.util.grid.GridAlignmentHelper.IAlignmentProcessor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.MigLayout.Activator;
import org.eclipse.wb.internal.swing.MigLayout.model.ui.CellConstraintsAssistantPage;
import org.eclipse.wb.internal.swing.MigLayout.model.ui.ColumnsDialog;
import org.eclipse.wb.internal.swing.MigLayout.model.ui.RowsDialog;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutAssistantSupport;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import net.miginfocom.layout.AC;
import net.miginfocom.layout.ConstraintParser;
import net.miginfocom.layout.DimConstraint;
import net.miginfocom.layout.IDEUtil;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import java.awt.Container;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Model for {@link MigLayout}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.model
 */
public final class MigLayoutInfo extends LayoutInfo implements IPreferenceConstants {
	private final List<MigColumnInfo> m_columns = new ArrayList<>();
	private final List<MigRowInfo> m_rows = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MigLayoutInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		// force UnitValue initialization in "design time", to be able to get strings for alignments
		ExecutionUtils.runDesignTime(new RunnableEx() {
			@Override
			public void run() throws Exception {
				IDEUtil.LEFT.toString();
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Initialize
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initialize() throws Exception {
		super.initialize();
		initializeMigLayoutGridAfterParse();
		new SelectionActionsSupport(this);
		new LayoutAssistantSupport(this) {
			@Override
			protected AbstractAssistantPage createConstraintsPage(Composite parent,
					List<ObjectInfo> objects) {
				return new CellConstraintsAssistantPage(parent, MigLayoutInfo.this, objects);
			}
		};
		addContextMenuActions();
		addConstraintsProperty();
		addSplitFlowDirectionListener();
	}

	private void initializeMigLayoutGridAfterParse() {
		addBroadcastListener(new ObjectInfoTreeComplete() {
			@Override
			public void invoke() throws Exception {
				removeBroadcastListener(this);
				if (isActive()) {
					try {
						getContainer().getContainer().doLayout();
					} catch (Throwable e) {
						DesignerPlugin.log(e);
					}
				}
			}
		});
	}

	private void addContextMenuActions() {
		addBroadcastListener(new ObjectEventListener() {
			@Override
			public void addContextMenu(List<? extends ObjectInfo> objects,
					ObjectInfo object,
					IMenuManager manager) throws Exception {
				addContextMenuActions(object, manager);
			}
		});
	}

	private void addConstraintsProperty() {
		addBroadcastListener(new JavaInfoAddProperties() {
			@Override
			public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
				if (isManagedObject(javaInfo)) {
					ComponentInfo component = (ComponentInfo) javaInfo;
					CellConstraintsSupport constraints = getConstraints(component);
					properties.add(constraints.getCellProperty());
				}
			}
		});
	}

	/**
	 * Updates flow direction specification, when operation with first component of splitted cell.
	 */
	private void addSplitFlowDirectionListener() {
		addBroadcastListener(new ObjectInfoDelete() {
			@Override
			public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
				if (isManagedObject(child)) {
					ComponentInfo component = (ComponentInfo) child;
					updateSplitFlowDirection(component);
				}
			}
		});
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				if (isManagedObject(child)) {
					ComponentInfo component = (ComponentInfo) child;
					updateSplitFlowDirection(component);
				}
			}
		});
	}

	private void updateSplitFlowDirection(ComponentInfo component) throws Exception {
		CellConstraintsSupport constraints = getConstraints(component);
		List<ComponentInfo> cellComponents = getCellComponents(component);
		if (cellComponents.size() > 1) {
			if (cellComponents.get(0) == component) {
				// remove flow direction for component
				boolean horizontalSplit = constraints.isHorizontalSplit();
				constraints.setHorizontalSplit(null);
				constraints.write();
				// set flow direction for next component
				if (cellComponents.size() > 2) {
					ComponentInfo nextComponent = cellComponents.get(1);
					CellConstraintsSupport nextConstraints = getConstraints(nextComponent);
					nextConstraints.setHorizontalSplit(horizontalSplit);
					nextConstraints.write();
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link LC} (parsed if needed) for this {@link MigLayout} instance.
	 */
	LC getLC() {
		MigLayout layout = (MigLayout) getObject();
		Object layoutConstraints = layout.getLayoutConstraints();
		if (layoutConstraints instanceof LC) {
			return (LC) layoutConstraints;
		} else {
			return ConstraintParser.parseLayoutConstraint((String) layoutConstraints);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds {@link MigLayoutInfo} actions into context menu.
	 */
	private void addContextMenuActions(ObjectInfo object, IMenuManager manager) throws Exception {
		if (object == getContainer()) {
			manager.appendToGroup(
					IContextMenuConstants.GROUP_TOP,
					new EditDimensionsAction(ModelMessages.MigLayoutInfo_editColumns, true));
			manager.appendToGroup(
					IContextMenuConstants.GROUP_TOP,
					new EditDimensionsAction(ModelMessages.MigLayoutInfo_editRows, false));
		}
		if (object instanceof ComponentInfo component && object.getParent() == getContainer()) {
			CellConstraintsSupport support = getConstraints(component);
			support.addContextMenu(manager);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu: EditDimensionsAction
	//
	////////////////////////////////////////////////////////////////////////////
	private final class EditDimensionsAction extends Action {
		private final boolean m_horizontal;

		public EditDimensionsAction(String text, boolean horizontal) {
			super(text);
			m_horizontal = horizontal;
		}

		@Override
		public void run() {
			Shell shell = DesignerPlugin.getShell();
			if (m_horizontal) {
				new ColumnsDialog(shell, MigLayoutInfo.this).open();
			} else {
				new RowsDialog(shell, MigLayoutInfo.this).open();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access columns/rows
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link List} of {@link MigColumnInfo}.
	 */
	public List<MigColumnInfo> getColumns() {
		return m_columns;
	}

	/**
	 * @return the {@link List} of {@link MigRowInfo}.
	 */
	public List<MigRowInfo> getRows() {
		return m_rows;
	}

	/**
	 * Sets the new {@link List} of {@link MigColumnInfo}.
	 */
	public void setColumns(List<MigColumnInfo> columns) throws Exception {
		m_columns.clear();
		m_columns.addAll(columns);
		writeDimensions();
	}

	/**
	 * Sets the new {@link List} of {@link MigRowInfo}.
	 */
	public void setRows(List<MigRowInfo> rows) throws Exception {
		m_rows.clear();
		m_rows.addAll(rows);
		writeDimensions();
	}

	/**
	 * Ensures that we have enough columns/rows models, for each column/row in {@link MigLayout}
	 * instance.
	 */
	private void updateColumnsRows() throws Exception {
		// update columns
		{
			int columnCount = getGridInfo().getColumnCount();
			if (m_columns.size() != columnCount) {
				m_columns.clear();
				for (int i = 0; i < columnCount; i++) {
					m_columns.add(new MigColumnInfo(this));
				}
			}
			// fetch constraint
			for (MigColumnInfo column : m_columns) {
				column.updateConstraint();
			}
		}
		// update rows
		{
			int rowCount = getGridInfo().getRowCount();
			if (m_rows.size() != rowCount) {
				m_rows.clear();
				for (int i = 0; i < rowCount; i++) {
					m_rows.add(new MigRowInfo(this));
				}
			}
			// fetch constraint
			for (MigRowInfo row : m_rows) {
				row.updateConstraint();
			}
		}
	}

	/**
	 * If there are components that span multiple columns/rows, and no other "real" components in
	 * these columns/rows, then removes these excess columns/rows.
	 */
	public void normalizeSpanning() throws Exception {
		boolean columnRowDeleted = true;
		while (columnRowDeleted) {
			columnRowDeleted = false;
			// prepare filled columns/rows
			final boolean[] filledColumns = new boolean[m_columns.size()];
			final boolean[] filledRows = new boolean[m_rows.size()];
			visitGridComponents(new MigComponentVisitor() {
				@Override
				public void visit(ComponentInfo bean, CellConstraintsSupport constraints) throws Exception {
					filledColumns[constraints.getX()] = true;
					filledRows[constraints.getY()] = true;
				}
			});
			// remove empty columns
			for (int column = filledColumns.length - 1; column >= 0; column--) {
				if (!filledColumns[column]) {
					deleteColumn(column);
					columnRowDeleted = true;
					break;
				}
			}
			// remove empty rows
			for (int row = filledRows.length - 1; row >= 0; row--) {
				if (!filledRows[row]) {
					deleteRow(row);
					columnRowDeleted = true;
					break;
				}
			}
		}
		// write dimensions
		writeDimensions();
	}

	/**
	 * @return <code>true</code> if dimensions of this layout can be changed. We can change them only
	 *         if we created layout using constructor.
	 */
	public boolean canChangeDimensions() {
		return getCreationSupport() instanceof ConstructorCreationSupport;
	}

	/**
	 * Writes columns/rows to the source.
	 */
	public void writeDimensions() throws Exception {
		MigLayout layout = (MigLayout) getObject();
		// prepare source for LC
		String layoutConstraintsSource;
		{
			String layoutConstraintsString;
			Object layoutConstraints = layout.getLayoutConstraints();
			if (layoutConstraints instanceof LC) {
				layoutConstraintsString = IDEUtil.getConstraintString((LC) layoutConstraints, false);
			} else {
				layoutConstraintsString = (String) layoutConstraints;
			}
			layoutConstraintsSource =
					StringConverter.INSTANCE.toJavaSource(this, layoutConstraintsString);
		}
		// prepare source for columns/rows
		String columnsSource = getDimensionsSource(m_columns, true);
		String rowsSource = getDimensionsSource(m_rows, false);
		// write constructor
		{
			ConstructorCreationSupport creationSupport =
					(ConstructorCreationSupport) getCreationSupport();
			ClassInstanceCreation creation = creationSupport.getCreation();
			getEditor().replaceCreationArguments(
					creation,
					List.of(MessageFormat.format(
							"{0}, {1}, {2}",
							layoutConstraintsSource,
							columnsSource,
							rowsSource)));
			setCreationSupport(new ConstructorCreationSupport(creation));
		}
	}

	/**
	 * @return the source for dimension constraints.
	 */
	private static String getDimensionsSource(List<? extends MigDimensionInfo> dimensions,
			boolean cols) throws Exception {
		DimConstraint[] constraints = new DimConstraint[dimensions.size()];
		for (int i = 0; i < dimensions.size(); i++) {
			constraints[i] = dimensions.get(i).m_constraint;
		}
		//
		String constraintString;
		{
			AC ac = new AC();
			ac.setConstaints(constraints);
			constraintString = IDEUtil.getConstraintString(ac, false, cols);
		}
		// return as source
		return StringConverter.INSTANCE.toJavaSource(null, constraintString);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Property> getPropertyList() throws Exception {
		List<Property> properties = super.getPropertyList();
		properties.add(new DimensionsProperty(this, true));
		properties.add(new DimensionsProperty(this, false));
		return properties;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CellConstraintsSupport access
	//
	////////////////////////////////////////////////////////////////////////////
	private final Map<ComponentInfo, CellConstraintsSupport> m_constraints = new HashMap<>();

	/**
	 * @return the {@link CellConstraintsSupport} for given {@link ComponentInfo}.
	 */
	public static CellConstraintsSupport getConstraints(ComponentInfo component) {
		ContainerInfo container = (ContainerInfo) component.getParent();
		Assert.isTrue(container.getChildrenComponents().contains(component));
		Assert.instanceOf(MigLayoutInfo.class, container.getLayout());
		//
		MigLayoutInfo layout = (MigLayoutInfo) container.getLayout();
		CellConstraintsSupport support = layout.m_constraints.get(component);
		if (support == null) {
			support = new CellConstraintsSupport(layout, component);
			layout.m_constraints.put(component, support);
		}
		return support;
	}

	@Override
	public void onSet() throws Exception {
		super.onSet();
		MigLayoutConverter.convert(getContainer(), this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Column commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Inserts new {@link MigColumnInfo} into target index.
	 */
	public void insertColumn(int targetIndex) throws Exception {
		moveComponentsForInsert(targetIndex, true, -1, false);
		m_columns.add(targetIndex, new MigColumnInfo(this));
		writeDimensions();
	}

	/**
	 * Deletes the {@link MigColumnInfo} with given index.
	 */
	public void deleteColumn(int index) throws Exception {
		moveComponentsForDelete(index, true, -1, false);
		m_columns.remove(index);
		writeDimensions();
	}

	/**
	 * Deletes the {@link ComponentInfo}'s that located in {@link MigColumnInfo} with given index.
	 */
	public void clearColumn(final int index) throws Exception {
		visitGridComponents(new MigComponentVisitor() {
			@Override
			public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
				if (cell.getX() == index) {
					component.delete();
				}
			}
		});
	}

	/**
	 * Splits the {@link MigColumnInfo} with given index, i.e. adds duplicate of this
	 * {@link MigColumnInfo}.
	 */
	public void splitColumn(final int index) throws Exception {
		// update dimensions
		{
			MigColumnInfo column = m_columns.get(index);
			{
				MigColumnInfo newColumn = new MigColumnInfo(this);
				m_columns.add(index, newColumn);
				newColumn.setString(column.getString(false));
			}
			writeDimensions();
		}
		// update constraints
		visitGridComponents(new MigComponentVisitor() {
			@Override
			public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
				if (cell.getX() > index) {
					cell.updateX(1);
				} else if (cell.getX() + cell.getWidth() > index) {
					cell.updateWidth(1);
				}
				cell.write();
			}
		});
	}

	/**
	 * Moves {@link MigColumnInfo} at given index into target index.
	 */
	public void moveColumn(final int index, final int targetIndex) throws Exception {
		MigColumnInfo column = m_columns.remove(index);
		if (index < targetIndex) {
			// add column
			m_columns.add(targetIndex - 1, column);
			// change constraints
			visitGridComponents(new MigComponentVisitor() {
				@Override
				public void visit(ComponentInfo bean, CellConstraintsSupport constraints) throws Exception {
					int x = constraints.getX();
					int w = constraints.getWidth();
					if (x < index) {
						// if component contains source and doesn't contain target, decrease size
						if (x + w - 1 >= index && x + w - 1 < targetIndex) {
							constraints.updateWidth(-1);
						}
					} else if (x == index) {
						constraints.setX(targetIndex - 1);
						constraints.setWidth(1);
					} else if (x > index && x < targetIndex) {
						constraints.updateX(-1);
						// if component contains target, increase size
						if (x + w - 1 >= targetIndex) {
							constraints.updateWidth(1);
						}
					} else if (x >= 1 + targetIndex) {
						// after source and target - do nothing
					}
					constraints.write();
				}
			});
		} else {
			// add column
			m_columns.add(targetIndex, column);
			// change constraints
			visitGridComponents(new MigComponentVisitor() {
				@Override
				public void visit(ComponentInfo bean, CellConstraintsSupport constraints) throws Exception {
					int x = constraints.getX();
					int w = constraints.getWidth();
					if (x < targetIndex) {
						// if component doesn't contains source and contains target, increase size
						if (x + w - 1 >= targetIndex && x + w - 1 < index) {
							constraints.updateWidth(1);
						}
					} else if (x < index) {
						constraints.updateX(1);
						// if component contains source, decrease size
						if (x + w - 1 >= index) {
							constraints.updateWidth(-1);
						}
					} else if (x == index) {
						constraints.setX(targetIndex);
						constraints.setWidth(1);
					} else if (x > index) {
						// after source and target - do nothing
					}
					constraints.write();
				}
			});
		}
		// finalize
		writeDimensions();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Row commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Inserts new {@link MigRowInfo} into target index.
	 */
	public void insertRow(int targetIndex) throws Exception {
		moveComponentsForInsert(-1, false, targetIndex, true);
		m_rows.add(targetIndex, new MigRowInfo(this));
		writeDimensions();
	}

	/**
	 * Deletes the {@link MigRowInfo} with given index.
	 */
	public void deleteRow(int index) throws Exception {
		moveComponentsForDelete(-1, false, index, true);
		m_rows.remove(index);
		writeDimensions();
	}

	/**
	 * Deletes the {@link ComponentInfo}'s that located in {@link MigRowInfo} with given index.
	 */
	public void clearRow(final int index) throws Exception {
		visitGridComponents(new MigComponentVisitor() {
			@Override
			public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
				if (cell.getY() == index) {
					component.delete();
				}
			}
		});
	}

	/**
	 * Splits the {@link MigRowInfo} with given index, i.e. adds duplicate of this {@link MigRowInfo}.
	 */
	public void splitRow(final int index) throws Exception {
		// update dimensions
		{
			MigRowInfo row = m_rows.get(index);
			{
				MigRowInfo newRow = new MigRowInfo(this);
				m_rows.add(index, newRow);
				newRow.setString(row.getString(false));
			}
			writeDimensions();
		}
		// update constraints
		visitGridComponents(new MigComponentVisitor() {
			@Override
			public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
				if (cell.getY() > index) {
					cell.updateY(1);
				} else if (cell.getY() + cell.getHeight() > index) {
					cell.updateHeight(1);
				}
				cell.write();
			}
		});
	}

	/**
	 * Moves {@link MigRowInfo} at given index into target index.
	 */
	public void moveRow(final int index, final int targetIndex) throws Exception {
		MigRowInfo row = m_rows.remove(index);
		if (index < targetIndex) {
			// add column
			m_rows.add(targetIndex - 1, row);
			// change constraints
			visitGridComponents(new MigComponentVisitor() {
				@Override
				public void visit(ComponentInfo bean, CellConstraintsSupport constraints) throws Exception {
					int y = constraints.getY();
					int h = constraints.getHeight();
					if (y < index) {
						// if component contains source and doesn't contain target, decrease size
						if (y + h - 1 >= index && y + h - 1 < targetIndex) {
							constraints.updateHeight(-1);
						}
					} else if (y == index) {
						constraints.setY(targetIndex - 1);
						constraints.setHeight(1);
					} else if (y > index && y < targetIndex) {
						constraints.updateY(-1);
						// if component contains target, increase size
						if (y + h - 1 >= targetIndex) {
							constraints.updateHeight(1);
						}
					} else if (y >= 1 + targetIndex) {
						// after source and target - do nothing
					}
					constraints.write();
				}
			});
		} else {
			// add column
			m_rows.add(targetIndex, row);
			// change constraints
			visitGridComponents(new MigComponentVisitor() {
				@Override
				public void visit(ComponentInfo bean, CellConstraintsSupport constraints) throws Exception {
					int y = constraints.getY();
					int h = constraints.getHeight();
					if (y < targetIndex) {
						// if component doesn't contains source and contains target, increase size
						if (y + h - 1 >= targetIndex && y + h - 1 < index) {
							constraints.updateHeight(1);
						}
					} else if (y < index) {
						constraints.updateY(1);
						// if component contains source, decrease size
						if (y + h - 1 >= index) {
							constraints.updateHeight(-1);
						}
					} else if (y == index) {
						constraints.setY(targetIndex);
						constraints.setHeight(1);
					} else if (y > index) {
						// after source and target - do nothing
					}
					constraints.write();
				}
			});
		}
		// finalize
		writeDimensions();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets cells bounds for given {@link ComponentInfo}, for example during spanning.
	 */
	public void command_setCells(ComponentInfo component, Rectangle cells) throws Exception {
		// force dimensions in MigLayout creation, because in some cases MigLayout can "optimize"
		// columns/rows, if these columns/rows contain only spanned parts of components
		writeDimensions();
		makeExplicitCell();
		// OK, now we can safely update constraints
		CellConstraintsSupport constraints = getConstraints(component);
		constraints.setX(cells.x);
		constraints.setY(cells.y);
		constraints.setWidth(cells.width);
		constraints.setHeight(cells.height);
		constraints.write();
		// write all constraints (in ideal case - only this one)
		writeAllConstraints();
	}

	/**
	 * Creates new {@link ComponentInfo} in given cell.
	 *
	 * @param newComponent
	 *          the new {@link ComponentInfo} to create.
	 * @param column
	 *          the column in {@link MigLayout} terms (0 based).
	 * @param row
	 *          the row in {@link MigLayout} terms (0 based).
	 */
	public void command_CREATE(ComponentInfo newComponent,
			int column,
			boolean insertColumn,
			int row,
			boolean insertRow) throws Exception {
		makeExplicitCell();
		Point cell = prepareCell(column, insertColumn, row, insertRow);
		// do add
		ComponentInfo nextComponent = getReference(column, row, null);
		add(newComponent, "\"\"", nextComponent);
		// set bounds
		{
			CellConstraintsSupport constraints = getConstraints(newComponent);
			constraints.setX(cell.x);
			constraints.setY(cell.y);
		}
		// write all constraints (in ideal case - only this one)
		writeAllConstraints();
		//
		doAutomaticAlignment(newComponent);
	}

	/**
	 * Moves existing {@link ComponentInfo} into new cell.
	 */
	public void command_MOVE(ComponentInfo component,
			int column,
			boolean insertColumn,
			int row,
			boolean insertRow) throws Exception {
		makeExplicitCell();
		Point cell = prepareCell(column, insertColumn, row, insertRow);
		// move in components
		{
			ComponentInfo nextComponent = getReference(column, row, component);
			move(component, "\"\"", nextComponent);
		}
		// move in grid
		{
			CellConstraintsSupport constraints = getConstraints(component);
			// new bounds
			constraints.setX(cell.x);
			constraints.setY(cell.y);
			constraints.setWidth(1);
			constraints.setHeight(1);
			// now component in cell, not docked
			constraints.setDockSide(null);
			// write all constraints (in ideal case - only this one)
			writeAllConstraints();
		}
	}

	/**
	 * Forces explicit <code>cell</code> tags for all components.
	 *
	 * @see {@link CellConstraintsSupport#makeExplicitCell()}.
	 */
	private void makeExplicitCell() {
		for (ComponentInfo component : getContainer().getChildrenComponents()) {
			CellConstraintsSupport constraints = getConstraints(component);
			constraints.makeExplicitCell();
		}
	}

	/**
	 * Invokes {@link CellConstraintsSupport#write()} for all constraints.
	 */
	private void writeAllConstraints() throws Exception {
		for (ComponentInfo component : getContainer().getChildrenComponents()) {
			CellConstraintsSupport constraints = getConstraints(component);
			constraints.write();
		}
	}

	/**
	 * @return the {@link ComponentInfo} that should be used as reference of adding into given cell.
	 *
	 * @param exclude
	 *          the {@link ComponentInfo} that should not be checked, for example because we move it
	 *          now.
	 */
	private ComponentInfo getReference(int column, int row, ComponentInfo exclude) throws Exception {
		for (ComponentInfo component : getContainer().getChildrenComponents()) {
			if (component != exclude) {
				CellConstraintsSupport constraints = getConstraints(component);
				if (constraints.getY() > row || constraints.getY() == row && constraints.getX() >= column) {
					return component;
				}
			}
		}
		// no reference
		return null;
	}

	/**
	 * Prepares cell with given column/row - inserts columns/rows if necessary.
	 */
	private Point prepareCell(int column, boolean insertColumn, int row, boolean insertRow)
			throws Exception {
		boolean writeDimensions = false;
		if (insertColumn || insertRow) {
			// move existing components
			moveComponentsForInsert(column, insertColumn, row, insertRow);
			// insert empty column/row
			if (insertColumn) {
				m_columns.add(column, new MigColumnInfo(this));
			}
			if (insertRow) {
				m_rows.add(row, new MigRowInfo(this));
			}
			// write dimensions
			writeDimensions = true;
		}
		// append columns
		while (m_columns.size() <= column) {
			m_columns.add(new MigColumnInfo(this));
			writeDimensions = true;
		}
		// append rows
		while (m_rows.size() <= row) {
			m_rows.add(new MigRowInfo(this));
			writeDimensions = true;
		}
		// write dimensions
		if (writeDimensions) {
			writeDimensions();
		}
		// return new cell
		return new Point(column, row);
	}

	/**
	 * Moves/resizes components constraints for inserting single column/row.
	 */
	private void moveComponentsForInsert(final int column,
			final boolean insertColumn,
			final int row,
			final boolean insertRow) throws Exception {
		visitGridComponents(new MigComponentVisitor() {
			@Override
			public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
				if (insertColumn) {
					if (cell.getX() >= column) {
						cell.updateX(1);
					} else if (cell.getX() + cell.getWidth() > column) {
						cell.updateWidth(1);
					}
				}
				if (insertRow) {
					if (cell.getY() >= row) {
						cell.updateY(1);
					} else if (cell.getY() + cell.getHeight() > row) {
						cell.updateHeight(1);
					}
				}
				cell.write();
			}
		});
	}

	/**
	 * Moves/resizes components constraints for deleting single column/row.
	 */
	private void moveComponentsForDelete(final int column,
			final boolean deleteColumn,
			final int row,
			final boolean deleteRow) throws Exception {
		visitGridComponents(new MigComponentVisitor() {
			@Override
			public void visit(ComponentInfo component, CellConstraintsSupport cell) throws Exception {
				if (deleteColumn) {
					if (cell.getX() == column) {
						component.delete();
					} else if (cell.getX() >= column) {
						cell.updateX(-1);
					} else if (cell.getX() + cell.getWidth() > column) {
						cell.updateWidth(-1);
					}
				}
				if (deleteRow) {
					if (cell.getY() == row) {
						component.delete();
					} else if (cell.getY() >= row) {
						cell.updateY(-1);
					} else if (cell.getY() + cell.getHeight() > row) {
						cell.updateHeight(-1);
					}
				}
				cell.write();
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Split support
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link List} of components that are located in same cell as given
	 *         {@link ComponentInfo}.
	 */
	public List<ComponentInfo> getCellComponents(ComponentInfo component) {
		CellConstraintsSupport constraints = getConstraints(component);
		return getCellComponents(constraints.getX(), constraints.getY());
	}

	/**
	 * @return the {@link List} of components that are located in given cell (usually zero or one, but
	 *         in case of splitted cell - more than one).
	 */
	public List<ComponentInfo> getCellComponents(final int column, final int row) {
		final List<ComponentInfo> components = new ArrayList<>();
		visitGridComponents(new MigComponentVisitor() {
			@Override
			public void visit(ComponentInfo component, CellConstraintsSupport constraints)
					throws Exception {
				int x = constraints.getX();
				int y = constraints.getY();
				int w = constraints.getWidth();
				int h = constraints.getHeight();
				if (x <= column && column < x + w && y <= row && row < y + h) {
					components.add(component);
				}
			}
		});
		return components;
	}

	/**
	 * Adds new component into cell that already has one or more (in case of already splitted cell)
	 * components.
	 *
	 * @param column
	 *          the target column for new component.
	 * @param row
	 *          the target row for new component.
	 * @param horizontalFlow
	 *          specifies if <code>flowx</code> or <code>flowy</code> should be set, not used, if cell
	 *          was already splitted.
	 */
	public void command_splitCREATE(int column,
			int row,
			boolean horizontalFlow,
			ComponentInfo newComponent,
			ComponentInfo nextComponent) throws Exception {
		horizontalFlow = getUpdatedSplitFlow(column, row, horizontalFlow);
		// add new component
		add(newComponent, "\"\"", nextComponent);
		split_updateCellFlow(column, row, horizontalFlow, newComponent);
	}

	/**
	 * Moves component into cell that already has one or more (in case of already splitted cell)
	 * components.
	 *
	 * @param column
	 *          the target column for moved component.
	 * @param row
	 *          the target row for moved component.
	 * @param horizontalFlow
	 *          specifies if <code>flowx</code> or <code>flowy</code> should be set, not used, if cell
	 *          was already splitted.
	 */
	public void command_splitMOVE(int column,
			int row,
			boolean horizontalFlow,
			ComponentInfo movedComponent,
			ComponentInfo nextComponent) throws Exception {
		horizontalFlow = getUpdatedSplitFlow(column, row, horizontalFlow);
		move(movedComponent, "\"\"", nextComponent);
		split_updateCellFlow(column, row, horizontalFlow, movedComponent);
	}

	/**
	 * Places given component into cell (already occupied by one or more components), updates flow
	 * direction of splitted cell.
	 */
	private void split_updateCellFlow(int column,
			int row,
			boolean horizontalFlow,
			ComponentInfo movedComponent) throws Exception {
		// set bounds
		{
			CellConstraintsSupport constraints = getConstraints(movedComponent);
			constraints.setX(column);
			constraints.setY(row);
		}
		// set flow for first component, remove for next components
		{
			List<ComponentInfo> components = getCellComponents(column, row);
			boolean isFirst = true;
			for (ComponentInfo component : components) {
				CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(component);
				if (isFirst) {
					isFirst = false;
					constraints.setHorizontalSplit(Boolean.valueOf(horizontalFlow));
				} else {
					constraints.setHorizontalSplit(null);
				}
			}
		}
		// write all constraints
		writeAllConstraints();
	}

	/**
	 * @return the updated flow direction (use existing direction, if cell is already splitted), or
	 *         requested direction.
	 */
	private boolean getUpdatedSplitFlow(int column, int row, boolean horizontalFlow) {
		List<ComponentInfo> components = getCellComponents(column, row);
		if (components.size() > 1) {
			horizontalFlow = MigLayoutInfo.getConstraints(components.get(0)).isHorizontalSplit();
		}
		return horizontalFlow;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Visitor for {@link ComponentInfo} and their {@link CellConstraintsSupport}.
	 */
	private interface MigComponentVisitor {
		void visit(ComponentInfo component, CellConstraintsSupport constraints) throws Exception;
	}

	/**
	 * Visits grid {@link ComponentInfo}'s of this {@link ContainerInfo}.
	 */
	private void visitGridComponents(final MigComponentVisitor visitor) {
		ExecutionUtils.runRethrow(new RunnableEx() {
			@Override
			public void run() throws Exception {
				visitAllComponents(new MigComponentVisitor() {
					@Override
					public void visit(ComponentInfo component, CellConstraintsSupport constraints)
							throws Exception {
						if (constraints.getDockSide() == null) {
							visitor.visit(component, constraints);
						}
					}
				});
			}
		});
	}

	/**
	 * Visits all {@link ComponentInfo}'s of this {@link ContainerInfo}.
	 */
	private void visitAllComponents(MigComponentVisitor visitor) throws Exception {
		for (ComponentInfo component : getContainer().getChildrenComponents()) {
			CellConstraintsSupport cell = getConstraints(component);
			visitor.visit(component, cell);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Automatic alignment
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Performs automatic alignment, such as grab/fill for {@link JTextField} or {@link JTable}, right
	 * alignment for {@link JLabel}.
	 */
	private void doAutomaticAlignment(ComponentInfo component) throws Exception {
		final IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		GridAlignmentHelper.doAutomaticAlignment(component, new IAlignmentProcessor<ComponentInfo>() {
			@Override
			public boolean grabEnabled() {
				return preferences.getBoolean(P_ENABLE_GRAB);
			}

			@Override
			public boolean rightEnabled() {
				return preferences.getBoolean(P_ENABLE_RIGHT_ALIGNMENT);
			}

			@Override
			public ComponentInfo getComponentAtLeft(ComponentInfo component) {
				CellConstraintsSupport constraints = getConstraints(component);
				return getComponentAt(constraints.getX() - 1, constraints.getY());
			}

			@Override
			public ComponentInfo getComponentAtRight(ComponentInfo component) {
				CellConstraintsSupport constraints = getConstraints(component);
				return getComponentAt(constraints.getX() + 1, constraints.getY());
			}

			@Override
			public void setGrabFill(ComponentInfo component, boolean horizontal) throws Exception {
				CellConstraintsSupport constraints = getConstraints(component);
				if (horizontal) {
					getColumns().get(constraints.getX()).setGrow(100f);
					constraints.setHorizontalAlignment(MigColumnInfo.Alignment.FILL);
				} else {
					getRows().get(constraints.getY()).setGrow(100f);
					constraints.setVerticalAlignment(MigRowInfo.Alignment.FILL);
				}
				writeDimensions();
				constraints.write();
			}

			@Override
			public void setRightAlignment(ComponentInfo component) throws Exception {
				CellConstraintsSupport constraints = getConstraints(component);
				constraints.setHorizontalAlignment(MigColumnInfo.Alignment.TRAILING);
				constraints.write();
			}
		});
	}

	/**
	 * @return the {@link ComponentInfo} with given top-left cell, may be <code>null</code>.
	 */
	private ComponentInfo getComponentAt(int x, int y) {
		for (ComponentInfo component : getContainer().getChildrenComponents()) {
			CellConstraintsSupport constraints = getConstraints(component);
			if (constraints.getX() == x && constraints.getY() == y) {
				return component;
			}
		}
		// no such component
		return null;
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
	protected void refresh_afterCreate2() throws Exception {
		super.refresh_afterCreate2();
		Container container = getContainer().getContainer();
		// do layout, use some reasonable size
		{
			java.awt.Dimension containerSizeOriginal = container.getSize();
			try {
				container.setSize(450, 300);
				container.doLayout();
			} finally {
				container.setSize(containerSizeOriginal);
			}
		}
		// set constant size for empty columns/rows
		{
			Interval[] columnIntervals = getIntervalsForOrigins(IDEUtil.getColumnSizes(container), 0);
			Interval[] rowIntervals = getIntervalsForOrigins(IDEUtil.getRowSizes(container), 0);
			// update columns
			for (int i = 0; i < columnIntervals.length; i++) {
				Interval interval = columnIntervals[i];
				if (interval.length() == 0) {
					String cons = "cell " + i + " 0,width " + m_defaultColumnSize + "px";
					container.add(new JLabel(), cons);
				}
			}
			// update rows
			for (int i = 0; i < rowIntervals.length; i++) {
				Interval interval = rowIntervals[i];
				if (interval.length() == 0) {
					String cons = "cell 0 " + i + ",height " + m_defaultRowSize + "px";
					container.add(new JLabel(), cons);
				}
			}
			// may be column/row constraints were updated, force layout
			container.doLayout();
		}
	}

	@Override
	protected void refresh_fetch() throws Exception {
		super.refresh_fetch();
		updateColumnsRows();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IGridInfo support
	//
	////////////////////////////////////////////////////////////////////////////
	private IGridInfo m_gridInfo;
	private final int m_defaultColumnSize = 25;
	private final int m_gapColumnSize = 4;
	private final int m_defaultRowSize = 25;
	private final int m_gapRowSize = 4;

	/**
	 * @return the {@link IGridInfo} that describes this layout.
	 */
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
	 * Initializes {@link #m_gridInfo}.
	 */
	private void createGridInfo() throws Exception {
		final Insets insets = getContainer().getInsets();
		// prepare intervals
		final Interval[] columnIntervals;
		final Interval[] rowIntervals;
		{
			Container container = getContainer().getContainer();
			if (IDEUtil.getColumnSizes(container) == null) {
				container.doLayout();
			}
			columnIntervals = getIntervalsForOrigins(IDEUtil.getColumnSizes(container), insets.left);
			rowIntervals = getIntervalsForOrigins(IDEUtil.getRowSizes(container), insets.top);
		}
		// prepare cells
		final Map<ComponentInfo, Rectangle> componentToCells = new HashMap<>();
		final Map<Point, ComponentInfo> occupiedCells = new HashMap<>();
		visitGridComponents(new MigComponentVisitor() {
			@Override
			public void visit(ComponentInfo component, CellConstraintsSupport support) throws Exception {
				Rectangle cells =
						new Rectangle(support.getX(), support.getY(), support.getWidth(), support.getHeight());
				// fill map: ComponentInfo -> cells Rectangle
				componentToCells.put(component, cells);
				// fill occupied cells map
				for (int x = cells.x; x < cells.right(); x++) {
					for (int y = cells.y; y < cells.bottom(); y++) {
						occupiedCells.put(new Point(x, y), component);
					}
				}
			}
		});
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
				Assert.instanceOf(ComponentInfo.class, component);
				// component in splitted cell
				{
					List<ComponentInfo> components = getCellComponents((ComponentInfo) component);
					if (components.size() > 1 && components.get(0) != component) {
						return getComponentCells(components.get(0));
					}
				}
				// normal component
				return componentToCells.get(component);
			}

			@Override
			public Rectangle getCellsRectangle(Rectangle cells) {
				if (cells == null) {
					return new Rectangle(0, 0, 0, 0);
				}
				int x = columnIntervals[cells.x].begin();
				int y = rowIntervals[cells.y].begin();
				if (cells.isEmpty()) {
					return new Rectangle(x, y, 0, 0);
				} else {
					int w = columnIntervals[cells.right() - 1].end() - x;
					int h = rowIntervals[cells.bottom() - 1].end() - y;
					return new Rectangle(x, y, w + 1, h + 1);
				}
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
				return insets;
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
				return m_defaultColumnSize;
			}

			@Override
			public int getVirtualColumnGap() {
				return m_gapColumnSize;
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
				return m_defaultRowSize;
			}

			@Override
			public int getVirtualRowGap() {
				return m_gapRowSize;
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
	// Manage general layout data.
	//
	////////////////////////////////////////////////////////////////////////////
	//MigColumnInfo.Alignment
	public static final BiMap<GeneralLayoutData.HorizontalAlignment, MigColumnInfo.Alignment> m_horizontalAlignmentMap =
			ImmutableBiMap.of(
					GeneralLayoutData.HorizontalAlignment.LEFT,
					MigColumnInfo.Alignment.LEFT,
					GeneralLayoutData.HorizontalAlignment.CENTER,
					MigColumnInfo.Alignment.CENTER,
					GeneralLayoutData.HorizontalAlignment.RIGHT,
					MigColumnInfo.Alignment.RIGHT,
					GeneralLayoutData.HorizontalAlignment.FILL,
					MigColumnInfo.Alignment.FILL,
					GeneralLayoutData.HorizontalAlignment.NONE,
					MigColumnInfo.Alignment.UNKNOWN);
	public static final BiMap<GeneralLayoutData.VerticalAlignment, MigRowInfo.Alignment> m_verticalAlignmentMap =
			ImmutableBiMap.of(
					GeneralLayoutData.VerticalAlignment.TOP,
					MigRowInfo.Alignment.TOP,
					GeneralLayoutData.VerticalAlignment.CENTER,
					MigRowInfo.Alignment.CENTER,
					GeneralLayoutData.VerticalAlignment.BOTTOM,
					MigRowInfo.Alignment.BOTTOM,
					GeneralLayoutData.VerticalAlignment.FILL,
					MigRowInfo.Alignment.FILL,
					GeneralLayoutData.VerticalAlignment.NONE,
					MigRowInfo.Alignment.UNKNOWN);

	@Override
	protected void storeLayoutData(ComponentInfo component) throws Exception {
		CellConstraintsSupport gridData = getConstraints(component);
		if (gridData != null) {
			GeneralLayoutData generalLayoutData = new GeneralLayoutData();
			generalLayoutData.gridX = gridData.getX();
			generalLayoutData.gridY = gridData.getY();
			generalLayoutData.spanX = gridData.getWidth();
			generalLayoutData.spanY = gridData.getHeight();
			generalLayoutData.horizontalGrab = null;
			generalLayoutData.verticalGrab = null;
			// alignments
			generalLayoutData.horizontalAlignment =
					GeneralLayoutData.getGeneralValue(
							m_horizontalAlignmentMap,
							gridData.getHorizontalAlignment());
			generalLayoutData.verticalAlignment =
					GeneralLayoutData.getGeneralValue(m_verticalAlignmentMap, gridData.getVerticalAlignment());
			generalLayoutData.putToInfo(component);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static final int DOCK_INDEX = 30000;

	/**
	 * @return the array of {@link Interval}'s for given array like from
	 *         {@link IDEUtil#getColumnSizes(Object)}.
	 */
	private static Interval[] getIntervalsForOrigins(int[][] sizes, int startOffset) {
		Assert.isTrue(sizes.length != 0);
		List<Interval> intervals = new ArrayList<>();
		// prepare number of "normal" dimensions
		int begin = startOffset;
		for (int index = 0; index < sizes[0].length; index++) {
			int intervalIndex = sizes[0][index];
			int gap = sizes[1][2 * index + 0];
			int size = sizes[1][2 * index + 1];
			begin += gap;
			if (Math.abs(intervalIndex) < DOCK_INDEX) {
				intervals.add(new Interval(begin, size));
			}
			begin += size;
		}
		// OK, we have intervals.
		return intervals.toArray(new Interval[intervals.size()]);
	}
}
