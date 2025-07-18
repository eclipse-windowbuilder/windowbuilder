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
package org.eclipse.wb.internal.swt.model.layout.form;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.swt.Activator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import java.util.List;

/**
 * Dialog allowing to select a widget from list.
 *
 * @author scheglov_ke
 * @author mitin_aa
 */
public class WidgetSelectDialog<C extends IAbstractComponentInfo> extends ResizableDialog {
	private final List<C> m_widgets;
	private final String m_dialogTitle;
	private final String m_listTitle;
	private final String m_columnTitle;
	private TableViewer m_viewer;
	private C m_defaultSelectedWidget;
	private C m_selectedWidget;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WidgetSelectDialog(Shell parentShell,
			List<C> widgets,
			String dialogTitle,
			String listTitle,
			String columnTitle) {
		super(parentShell, Activator.getDefault());
		m_widgets = widgets;
		m_dialogTitle = dialogTitle;
		m_listTitle = listTitle;
		m_columnTitle = columnTitle;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().forceFocus();
		Composite area = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.create(area).spacingV(2);
		{
			Label titleLabel = new Label(area, SWT.NONE);
			titleLabel.setText(m_listTitle);
			GridDataFactory.create(titleLabel).grabH().fillH();
		}
		{
			m_viewer = new TableViewer(area, SWT.BORDER | SWT.FULL_SELECTION);
			m_viewer.setContentProvider(new ControlContentProvider());
			m_viewer.setLabelProvider(new ControlLabelProvider());
			final Table table = m_viewer.getTable();
			table.setLinesVisible(true);
			table.setHeaderVisible(true);
			GridDataFactory.create(table).grab().fill();
			{
				final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
				tableColumn.setWidth(400);
				tableColumn.setText(m_columnTitle);
			}
			m_viewer.setInput(new Object());
			m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					updateButtons();
				}
			});
			m_viewer.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					okPressed();
				}
			});
			table.setFocus();
			table.select(0);
		}
		return area;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control dialogContents = super.createContents(parent);
		// set selection here, because only here "Ok" button is created
		if (m_defaultSelectedWidget != null) {
			m_viewer.setSelection(new StructuredSelection(m_defaultSelectedWidget));
		}
		//
		return dialogContents;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(m_dialogTitle);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events handling
	//
	////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {
		m_selectedWidget = (C) getViewerSelection().getFirstElement();
		super.okPressed();
	}

	private void updateButtons() {
		getButton(IDialogConstants.OK_ID).setEnabled(!getViewerSelection().isEmpty());
	}

	private IStructuredSelection getViewerSelection() {
		return (IStructuredSelection) m_viewer.getSelection();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void setDefaultSelectedWidget(C defaultSelectedWidget) {
		m_defaultSelectedWidget = defaultSelectedWidget;
	}

	public C getSelectedWidget() {
		return m_selectedWidget;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Content provider
	//
	////////////////////////////////////////////////////////////////////////////
	private class ControlContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			return m_widgets.toArray();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Label provider
	//
	////////////////////////////////////////////////////////////////////////////
	private class ControlLabelProvider extends LabelProvider implements ITableLabelProvider {
		private ResourceManager m_resourceManager = new LocalResourceManager(JFaceResources.getResources());

		@Override
		public void dispose() {
			super.dispose();
			m_resourceManager.dispose();
		}

		@Override
		@SuppressWarnings("unchecked")
		public String getColumnText(Object element, int columnIndex) {
			C info = (C) element;
			try {
				return info.getPresentation().getText();
			} catch (Throwable e) {
				throw ReflectionUtils.propagate(e);
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public Image getColumnImage(Object element, int columnIndex) {
			C info = (C) element;
			try {
				ImageDescriptor imageDescriptor = info.getPresentation().getIcon();
				return imageDescriptor == null ? null : m_resourceManager.create(imageDescriptor);
			} catch (Throwable e) {
				throw ReflectionUtils.propagate(e);
			}
		}
	}
}