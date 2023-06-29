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
package org.eclipse.wb.internal.swing.FormLayout.model.ui;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableTitleAreaDialog;
import org.eclipse.wb.internal.swing.FormLayout.Activator;
import org.eclipse.wb.internal.swing.FormLayout.model.FormDimensionInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormDimensionTemplate;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.ModelMessages;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import java.util.List;

/**
 * Dialog for editing {@link List} of {@link FormDimensionInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.ui
 */
abstract class DimensionsDialog<T extends FormDimensionInfo> extends ResizableTitleAreaDialog {
	protected final FormLayoutInfo m_layout;
	private final List<T> m_dimensions;
	private final int m_minimumDimensions;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DimensionsDialog(Shell parentShell,
			FormLayoutInfo layout,
			List<T> dimensions,
			int minimumDimensions) {
		super(parentShell, Activator.getDefault());
		setShellStyle(SWT.RESIZE | SWT.CLOSE);
		//
		m_layout = layout;
		m_dimensions = dimensions;
		m_minimumDimensions = minimumDimensions;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		//
		Composite container = new Composite(area, SWT.NONE);
		GridDataFactory.create(container).grab().fill();
		GridLayoutFactory.create(container).columns(2);
		// title
		{
			Label label = new Label(container, SWT.NONE);
			GridDataFactory.create(label).spanH(2);
			label.setText(getViewerTitle());
		}
		// create viewer
		createViewer(container);
		// buttons
		{
			createButtonsComposite(container);
			updateButtons();
		}
		// configure title area
		setTitle(getDialogTitle());
		setMessage(getDialogMessage());
		return area;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(getDialogTitle());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dialog buttons
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			try {
				try {
					m_layout.startEdit();
					updateLayoutInfo(m_dimensions);
				} finally {
					m_layout.endEdit();
				}
			} catch (Throwable e) {
				DesignerPlugin.log(e);
			}
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Updates {@link FormLayoutInfo} using given {@link List} of {@link FormDimensionInfo}.
	 */
	protected abstract void updateLayoutInfo(List<T> dimensions) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Viewer
	//
	////////////////////////////////////////////////////////////////////////////
	private TableViewer m_viewer;

	/**
	 * Creates {@link TableViewer} for {@link FormDimensionInfo}'s.
	 */
	private void createViewer(Composite container) {
		m_viewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		// configure table
		Table table = m_viewer.getTable();
		GridDataFactory.create(table).hintC(120, 15).grab().fill();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		createColumn(ModelMessages.DimensionsDialog_numberColumn, 5);
		createColumn(ModelMessages.DimensionsDialog_templateColumn, 35);
		createColumn(ModelMessages.DimensionsDialog_alignmentColumn, 15);
		createColumn(ModelMessages.DimensionsDialog_sizeColumn, 35);
		createColumn(ModelMessages.DimensionsDialog_resizingColumn, 15);
		// configure viewer
		m_viewer.setContentProvider(new ArrayContentProvider());
		m_viewer.setLabelProvider(new DimensionsLabelProvider());
		m_viewer.setInput(m_dimensions);
		// add listeners
		m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
		m_viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				editSelectedDimension();
			}
		});
	}

	/**
	 * Creates {@link TableColumn} with given parameters.
	 */
	private void createColumn(String text, int widthInChars) {
		TableColumn column = new TableColumn(m_viewer.getTable(), SWT.NONE);
		column.setText(text);
		column.setWidth(convertWidthInCharsToPixels(widthInChars));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Buttons composite
	//
	////////////////////////////////////////////////////////////////////////////
	private Button m_editButton;
	private Button m_removeButton;
	private Button m_moveUpButton;
	private Button m_moveDownButton;

	/**
	 * Creates {@link Composite} with {@link Button}'s.
	 */
	private void createButtonsComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.create(composite).fill();
		GridLayoutFactory.create(composite).marginsV(0);
		//
		createButton(composite, ModelMessages.DimensionsDialog_insertButton, new Listener() {
			@Override
			public void handleEvent(Event event) {
				addNewDimension(0);
			}
		});
		createButton(composite, ModelMessages.DimensionsDialog_appendButton, new Listener() {
			@Override
			public void handleEvent(Event event) {
				addNewDimension(1);
			}
		});
		m_editButton =
				createButton(composite, ModelMessages.DimensionsDialog_editButton, new Listener() {
					@Override
					public void handleEvent(Event event) {
						editSelectedDimension();
					}
				});
		m_removeButton =
				createButton(composite, ModelMessages.DimensionsDialog_removeButton, new Listener() {
					@Override
					public void handleEvent(Event event) {
						int index = 0;
						for (T dimension : GenericsUtils.<T>iterable(m_viewer.getSelection())) {
							index = m_dimensions.indexOf(dimension);
							m_dimensions.remove(dimension);
						}
						// set selection
						m_viewer.refresh();
						index = Math.min(index, m_dimensions.size() - 1);
						m_viewer.getTable().select(index);
						// validate
						updateButtons();
						validateMinimumDimensions();
					}
				});
		//
		new Label(composite, SWT.NONE);
		m_moveUpButton =
				createButton(composite, ModelMessages.DimensionsDialog_moveUpButton, new Listener() {
					@Override
					public void handleEvent(Event event) {
						for (T dimension : GenericsUtils.<T>iterable(m_viewer.getSelection())) {
							int index = m_dimensions.indexOf(dimension);
							m_dimensions.remove(dimension);
							m_dimensions.add(index - 1, dimension);
						}
						m_viewer.refresh();
						updateButtons();
					}
				});
		m_moveDownButton =
				createButton(composite, ModelMessages.DimensionsDialog_moveDownButton, new Listener() {
					@Override
					public void handleEvent(Event event) {
						for (T dimension : GenericsUtils.<T>iterable(m_viewer.getSelection())) {
							int index = m_dimensions.indexOf(dimension);
							m_dimensions.remove(dimension);
							m_dimensions.add(index + 1, dimension);
						}
						m_viewer.refresh();
						updateButtons();
					}
				});
	}

	/**
	 * @return the new {@link Button} with given text and {@link SWT#Selection} {@link Listener}.
	 */
	private static Button createButton(Composite parent, String text, Listener listener) {
		Button button = new Button(parent, SWT.NONE);
		GridDataFactory.create(button).grabH().fillH();
		button.setText(text);
		button.addListener(SWT.Selection, listener);
		return button;
	}

	/**
	 * Updates buttons according to the selection in viewer.
	 */
	private void updateButtons() {
		IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
		boolean empty = selection.isEmpty();
		//
		m_editButton.setEnabled(selection.size() == 1);
		m_removeButton.setEnabled(!empty);
		// up/down buttons
		{
			m_moveUpButton.setEnabled(!empty);
			m_moveDownButton.setEnabled(!empty);
			for (T dimension : GenericsUtils.<T>iterable(selection)) {
				int index = m_dimensions.indexOf(dimension);
				if (index == 0) {
					m_moveUpButton.setEnabled(false);
				}
				if (index == m_dimensions.size() - 1) {
					m_moveDownButton.setEnabled(false);
				}
			}
		}
	}

	/**
	 * Checks that count of {@link FormDimensionInfo} is not less than required.
	 */
	private void validateMinimumDimensions() {
		if (m_dimensions.size() < m_minimumDimensions) {
			setErrorMessage(getMinimalErrorMessage(m_minimumDimensions));
		} else {
			setErrorMessage(null);
		}
	}

	/**
	 * Adds new {@link FormDimensionInfo}.
	 *
	 * @param indexOffset
	 *          the offset to add to the current selection index, <code>0</code> to implement insert
	 *          and <code>1</code> for append.
	 */
	private void addNewDimension(int indexOffset) {
		try {
			T newDimension = createNewDimension();
			// add new dimension
			int index = m_viewer.getTable().getSelectionIndex();
			if (index == -1) {
				m_dimensions.add(newDimension);
			} else {
				m_dimensions.add(index + indexOffset, newDimension);
			}
			// edit dimension
			if (!editSelectedDimension(m_dimensions, newDimension)) {
				m_dimensions.remove(newDimension);
			}
			// refresh
			m_viewer.refresh();
			// validate
			validateMinimumDimensions();
		} catch (Throwable e) {
			DesignerPlugin.log(e);
		}
	}

	/**
	 * Edits the selected {@link FormDimensionInfo}.
	 */
	private void editSelectedDimension() {
		T dimension = GenericsUtils.<T>first(m_viewer.getSelection());
		editSelectedDimension(m_dimensions, dimension);
		m_viewer.refresh();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Methods to implement
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the title for dialog title area.
	 */
	protected abstract String getDialogTitle();

	/**
	 * @return the description for dialog title area.
	 */
	protected abstract String getDialogMessage();

	/**
	 * @return the description for dialog title area.
	 */
	protected abstract String getMinimalErrorMessage(int minimumDimensions);

	/**
	 * @return the title for dimensions viewer.
	 */
	protected abstract String getViewerTitle();

	/**
	 * Edits given {@link FormDimensionInfo}.
	 *
	 * @return <code>true</code> if edit was successful.
	 */
	protected abstract boolean editSelectedDimension(List<T> dimensions, T dimension);

	/**
	 * @return the new {@link FormDimensionInfo} instance to append/insert.
	 */
	protected abstract T createNewDimension() throws Exception;

	//protected abstract String getDimensionName();
	//protected abstract FormDimensionInfo createNewDimension(FormLayoutInfo layout) throws Exception;
	//protected abstract DimensionEditDialog createDimensionDialog(FormDimensionInfo dimension, List dimensions, int index);
	////////////////////////////////////////////////////////////////////////////
	//
	// Dimensions providers
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link ITableLabelProvider} for {@link FormDimensionInfo}.
	 */
	private class DimensionsLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object element, int columnIndex) {
			FormDimensionInfo dimension = (FormDimensionInfo) element;
			if (columnIndex == 0) {
				int index = m_dimensions.indexOf(element);
				return "" + (index + 1);
			}
			if (columnIndex == 1) {
				FormDimensionTemplate template = dimension.getTemplate();
				if (template != null) {
					return template.getTitle();
				}
				return "";
			}
			if (columnIndex == 2) {
				return dimension.getAlignment().toString();
			}
			if (columnIndex == 3) {
				return dimension.getSize().getDisplayString();
			}
			if (columnIndex == 4) {
				return "" + dimension.getWeight();
			}
			return element.toString();
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
}
