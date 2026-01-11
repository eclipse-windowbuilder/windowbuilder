/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.model.property.editor.models.table;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import swingintegration.example.EmbeddedSwingComposite;

/**
 * {@link Composite} for displaying {@link TableModel}.
 */
public class TableModelPreviewCanvas extends EmbeddedSwingComposite {
	/* package */ static final String PROP_MODEL_CHANGED = "modify";
	private final PropertyChangeSupport m_propertyChangeSupport;
	private final TableModelDescription m_model;
	private JTable m_table;

	public TableModelPreviewCanvas(TableModelDescription model, Composite parent, int style) {
		super(parent, style);
		m_propertyChangeSupport = new PropertyChangeSupport(this);
		m_model = model;
	}

	@Override
	protected JComponent createSwingComponent() {
		TableModel model = m_model.createTableModel();
		m_table = new JTable(model);
		m_table.setCellSelectionEnabled(true);
		m_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		updateTableModel();
		trackTableSelection();
		return new JScrollPane(m_table);
	}

	/* package */ static final record TableSelection(int row, int column) {

	}

	/* package */ static interface TableOperationRunnable {
		void run(int row, int column);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection in JTable
	//
	////////////////////////////////////////////////////////////////////////////
	private int m_tableSelectedColumn;
	private int m_tableSelectedRow;
	private boolean m_processTableSelectionEvent = true;

	private void trackTableSelection() {
		m_tableSelectedRow = -1;
		m_tableSelectedColumn = -1;
		ListSelectionListener listener = e -> {
			if (m_processTableSelectionEvent) {
				m_tableSelectedColumn = m_table.getSelectedColumn();
				m_tableSelectedRow = m_table.getSelectedRow();
				firePropertyChange();
			}
		};
		m_table.getSelectionModel().addListSelectionListener(listener);
		m_table.getColumnModel().getSelectionModel().addListSelectionListener(listener);
		firePropertyChange();
		// XXX
		final PropertyChangeListener columnWidthListener = evt -> {
			if (m_processTableSelectionEvent) {
				TableColumn column = (TableColumn) evt.getSource();
				int index = column.getModelIndex();
				m_model.getColumn(index).m_preferredWidth = (Integer) evt.getNewValue();
				firePropertyChange();
			}
		};
		m_table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnAdded(TableColumnModelEvent e) {
				TableColumnModel columnModel = (TableColumnModel) e.getSource();
				int columnIndex = e.getToIndex();
				columnModel.getColumn(columnIndex).addPropertyChangeListener(columnWidthListener);
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
			}

			@Override
			public void columnMarginChanged(ChangeEvent e) {
			}
		});
	}

	/* package */ void setTableSelection(int row, int column) {
		Assert.isTrue(SwingUtilities.isEventDispatchThread(), "Must be called from AWT event dispatcher thread");
		m_table.changeSelection(row, column, false, false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Updates
	//
	////////////////////////////////////////////////////////////////////////////

	private final TableOperationRunnable DEFAULT_TABLE_UPDATE = (row, column) -> {
		setTableSelection(row, column);
		firePropertyChange();
	};

	/* package */ void addJTableOperationListener(Widget eventTarget,
			int eventType,
			TableOperationRunnable operation) {
		addJTableOperationSelectionListener(eventTarget, eventType, operation, DEFAULT_TABLE_UPDATE);
	}

	/* package */ void addJTableOperationSelectionListener(Widget eventTarget,
			TableOperationRunnable operation) {
		addJTableOperationSelectionListener(eventTarget, operation, DEFAULT_TABLE_UPDATE);
	}

	/* package */ void addJTableOperationSelectionListener(Widget eventTarget,
			final TableOperationRunnable operation,
			final TableOperationRunnable tableUpdateRunnable) {
		addJTableOperationSelectionListener(eventTarget, SWT.Selection, operation, tableUpdateRunnable);
	}

	/* package */ void addJTableOperationSelectionListener(Widget eventTarget,
			int eventType,
			final TableOperationRunnable operation,
			final TableOperationRunnable tableUpdateRunnable) {
		eventTarget.addListener(eventType, event -> {
			final int row = m_table.getSelectedRow();
			final int column = m_table.getSelectedColumn();
			operation.run(row, column);
			// update JTable and controls
			EventQueue.invokeLater(() -> {
				updateTableModel();
				tableUpdateRunnable.run(row, column);
				firePropertyChange();
			});
		});
	}

	private void updateTableModel() {
		m_processTableSelectionEvent = false;
		try {
			m_table.tableChanged(null);
			m_model.applyModel(m_table);
		} finally {
			m_processTableSelectionEvent = true;
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// PropertyChangeSupport
	//
	////////////////////////////////////////////////////////////////////////////

	/* package */ void addPropertyChangeListener(PropertyChangeListener listener) {
		m_propertyChangeSupport.addPropertyChangeListener(listener);
	}

	private void firePropertyChange() {
		TableSelection selection = new TableSelection(m_tableSelectedRow, m_tableSelectedColumn);
		Display.getDefault().asyncExec(() -> m_propertyChangeSupport.firePropertyChange(PROP_MODEL_CHANGED, null, selection));
	}
}
