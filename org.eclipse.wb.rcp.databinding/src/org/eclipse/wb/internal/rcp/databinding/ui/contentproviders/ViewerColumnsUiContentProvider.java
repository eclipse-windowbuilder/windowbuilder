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
package org.eclipse.wb.internal.rcp.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.ui.SimpleBindDialog;
import org.eclipse.wb.internal.core.databinding.ui.UiUtils;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.UiContentProviderAdapter;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.TableFactory;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.VirtualEditingSupportInfo;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import java.util.ArrayList;
import java.util.List;

/**
 * Content provider for ViewerColumn's bindings.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public class ViewerColumnsUiContentProvider extends UiContentProviderAdapter {
	private static final String SETTING_KEY = "Expand_State";
	private final ViewerColumnsConfiguration m_configuration;
	private final IDialogSettings m_settings;
	private ExpandableComposite m_expandableComposite;
	private TableViewer m_tableViewer;
	private Button m_editButton;
	private Button m_deleteButton;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewerColumnsUiContentProvider(ViewerColumnsConfiguration configuration) {
		m_configuration = configuration;
		IDialogSettings mainSettings = Activator.getDefault().getDialogSettings();
		m_settings = UiUtils.getSettings(mainSettings, getClass().getName());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int getNumberOfControls() {
		return 1;
	}

	@Override
	public void createContent(final Composite parent, int columns) {
		// create expandable composite
		m_expandableComposite = new ExpandableComposite(parent, SWT.NONE);
		m_expandableComposite.setText(Messages.ViewerColumnsUiContentProvider_viewerColumns);
		m_expandableComposite.setExpanded(true);
		GridDataFactory.create(m_expandableComposite).fillH().grabH().spanH(columns);
		m_expandableComposite.addExpansionListener(new IExpansionListener() {
			@Override
			public void expansionStateChanging(ExpansionEvent e) {
				m_settings.put(SETTING_KEY, !m_expandableComposite.isExpanded());
				if (m_expandableComposite.isExpanded()) {
					m_expandableComposite.setText(Messages.ViewerColumnsUiContentProvider_viewerColumnsDots);
				} else {
					m_expandableComposite.setText(Messages.ViewerColumnsUiContentProvider_viewerColumns);
				}
			}

			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				parent.layout();
			}
		});
		// create sub content providers
		Composite clientComposite = new Composite(m_expandableComposite, SWT.NONE);
		GridLayoutFactory.create(clientComposite).columns(2).noMargins();
		m_expandableComposite.setClient(clientComposite);
		// buttons
		Composite toolbar = new Composite(clientComposite, SWT.NONE);
		GridLayoutFactory.create(toolbar).noMargins();
		// edit
		m_editButton = new Button(toolbar, SWT.NONE);
		GridDataFactory.create(m_editButton).fillH().grabH();
		m_editButton.setText(Messages.ViewerColumnsUiContentProvider_editButton);
		m_editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				configureEditingSupport();
			}
		});
		// delete
		m_deleteButton = new Button(toolbar, SWT.NONE);
		GridDataFactory.create(m_deleteButton).fillH().grabH();
		m_deleteButton.setText(Messages.ViewerColumnsUiContentProvider_deleteButton);
		m_deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteEditingSupport();
			}
		});
		// viewer for supports
		m_tableViewer =
				new TableViewer(clientComposite, SWT.BORDER
						| SWT.FULL_SELECTION
						| SWT.H_SCROLL
						| SWT.V_SCROLL);
		GridDataFactory.create(m_tableViewer.getTable()).fillH().grabH().hintVC(7);
		m_tableViewer.setContentProvider(new ArrayContentProvider());
		m_tableViewer.setLabelProvider(new EditingSupportLabelProvider());
		m_tableViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				calculateButtons();
			}
		});
		m_tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (!event.getSelection().isEmpty()) {
					configureEditingSupport();
				}
			}
		});
		//
		TableFactory factory =
				TableFactory.modify(m_tableViewer).headerVisible(true).linesVisible(true);
		factory.newColumn().text(Messages.ViewerColumnsUiContentProvider_viewerColumn).width(250);
		factory.newColumn().text(Messages.ViewerColumnsUiContentProvider_cellEditorColumn).width(300);
		factory.newColumn().text(Messages.ViewerColumnsUiContentProvider_elementPropertyColumn).width(
				300);
	}

	private void calculateButtons() {
		IStructuredSelection selection = (IStructuredSelection) m_tableViewer.getSelection();
		if (selection.isEmpty()) {
			m_editButton.setEnabled(false);
			m_deleteButton.setEnabled(false);
		} else {
			m_editButton.setEnabled(true);
			VirtualEditingSupportInfo editingSupport =
					(VirtualEditingSupportInfo) selection.getFirstElement();
			m_deleteButton.setEnabled(!editingSupport.isEmpty());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handle
	//
	////////////////////////////////////////////////////////////////////////////
	private void configureEditingSupport() {
		try {
			// prepare selection
			IStructuredSelection selection = (IStructuredSelection) m_tableViewer.getSelection();
			VirtualEditingSupportInfo editingSupport =
					(VirtualEditingSupportInfo) selection.getFirstElement();
			// create providers
			List<IUiContentProvider> providers = new ArrayList<>();
			editingSupport.createContentProviders(providers);
			// open dialog
			SimpleBindDialog dialog =
					new SimpleBindDialog(m_editButton.getShell(),
							Activator.getDefault(),
							providers,
							Messages.ViewerColumnsUiContentProvider_configureEditingSupportLabel,
							Messages.ViewerColumnsUiContentProvider_configureEditingSupportProperties,
							Messages.ViewerColumnsUiContentProvider_configureEditingSupportMessage);
			if (dialog.open() == Window.OK) {
				m_tableViewer.refresh();
			}
		} catch (Throwable e) {
			DesignerPlugin.log(e);
		}
	}

	private void deleteEditingSupport() {
		// prepare selection
		IStructuredSelection selection = (IStructuredSelection) m_tableViewer.getSelection();
		VirtualEditingSupportInfo editingSupport =
				(VirtualEditingSupportInfo) selection.getFirstElement();
		// do delete
		editingSupport.delete();
		// update table
		m_tableViewer.refresh();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
		m_tableViewer.setInput(m_configuration.getEditingSupports());
		calculateButtons();
	}

	@Override
	public void saveToObject() throws Exception {
		m_configuration.saveObjects();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// LabelProvider
	//
	////////////////////////////////////////////////////////////////////////////
	private static class EditingSupportLabelProvider extends LabelProvider
	implements
	ITableLabelProvider {
		private final ResourceManager m_resourceManager = new LocalResourceManager(JFaceResources.getResources());

		@Override
		public void dispose() {
			super.dispose();
			m_resourceManager.dispose();
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// ITableLabelProvider
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public String getColumnText(Object element, int column) {
			final VirtualEditingSupportInfo editingSupport = (VirtualEditingSupportInfo) element;
			switch (column) {
			case 0 :
				// Viewer column
				return ExecutionUtils.runObjectLog(() -> editingSupport.getViewerColumn().getPresentation().getText(), null);
			case 1 :
				// CellEditor
				return ExecutionUtils.runObjectLog(() -> editingSupport.getCellEditorPresentationText(), null);
			case 2 :
				// Element property
				return ExecutionUtils.runObjectLog(() -> editingSupport.getElementPropertyPresentationText(), null);
			}
			return null;
		}

		@Override
		public Image getColumnImage(Object element, int column) {
			if (column == 0) {
				// Viewer column
				final VirtualEditingSupportInfo editingSupport = (VirtualEditingSupportInfo) element;
				return ExecutionUtils.runObjectLog(() -> m_resourceManager.create(editingSupport.getViewerColumn().getPresentation().getImageDescriptor()), null);
			}
			return null;
		}
	}
}