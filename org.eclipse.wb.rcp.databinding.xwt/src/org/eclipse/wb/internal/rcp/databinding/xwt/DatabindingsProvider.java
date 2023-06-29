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
package org.eclipse.wb.internal.rcp.databinding.xwt;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.UiUtils;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.filter.PropertyFilter;
import org.eclipse.wb.internal.core.databinding.ui.providers.ObserveDecoratingLabelProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.databinding.xml.model.SynchronizeManager;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.core.xml.editor.AbstractXmlEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.rcp.databinding.model.AbstractBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory.Type;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.AttributeDocumentEditor;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.BindingInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.ObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.XmlObjectDecorator;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.XmlObjectDeleteManager;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.beans.BeansObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.widgets.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.widgets.WidgetsObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.xwt.ui.property.XmlObjectPropertiesManager;
import org.eclipse.wb.internal.rcp.databinding.xwt.ui.providers.BindingLabelProvider;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorPart;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author lobas_av
 *
 */
public final class DatabindingsProvider implements IDatabindingsProvider {
	private final XmlObjectInfo m_xmlObjectRoot;
	private final List<ObserveTypeContainer> m_containers = Lists.newArrayList();
	private final List<BindingInfo> m_bindings = Lists.newArrayList();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DatabindingsProvider(XmlObjectInfo xmlObjectRoot) throws Exception {
		m_xmlObjectRoot = xmlObjectRoot;
		// prepare containers
		m_containers.add(new BeansObserveTypeContainer());
		m_containers.add(new WidgetsObserveTypeContainer());
		//
		for (ObserveTypeContainer container : m_containers) {
			container.initialize(this);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internals
	//
	////////////////////////////////////////////////////////////////////////////
	public XmlObjectInfo getXmlObjectRoot() {
		return m_xmlObjectRoot;
	}

	public void addWarning(String message, Throwable e) {
		m_xmlObjectRoot.getContext().addWarning(new EditorWarning(message, e));
	}

	public List<ObserveTypeContainer> getContainers() {
		return m_containers;
	}

	public ObserveTypeContainer getContainer(ObserveType type) {
		if (type == ObserveType.BEANS) {
			return m_containers.get(0);
		}
		if (type == ObserveType.WIDGETS) {
			return m_containers.get(1);
		}
		return null;
	}

	public void hookXmlObjectEvents() throws Exception {
		// update observes
		new SynchronizeManager(this, m_xmlObjectRoot);
		// handle delete info's
		new XmlObjectDeleteManager(this);
		// decorate info's
		new XmlObjectDecorator(this);
		// properties
		new XmlObjectPropertiesManager(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Bindings
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void configureBindingViewer(IDialogSettings settings, TableViewer viewer) {
		// prepare table
		Table table = viewer.getTable();
		// binding type image column
		TableColumn bindingColumn = new TableColumn(table, SWT.NONE);
		bindingColumn.setWidth(23);
		bindingColumn.setResizable(false);
		// target column
		TableColumn targetColumn = UiUtils.createSmartColumn(table, settings, "TargetColumn", 250);
		targetColumn.setText(Messages.DatabindingsProvider_targetColumn);
		// model column
		TableColumn modelColumn = UiUtils.createSmartColumn(table, settings, "ModelColumn", 250);
		modelColumn.setText(Messages.DatabindingsProvider_modelColumn);
		// mode column
		TableColumn modeColumn = new TableColumn(table, SWT.NONE);
		modeColumn.setText(Messages.DatabindingsProvider_modeColumn);
		modeColumn.setWidth(100);
		// label provider
		viewer.setLabelProvider(new BindingLabelProvider());
	}

	@Override
	public List<IBindingInfo> getBindings() {
		return CoreUtils.cast(m_bindings);
	}

	public List<BindingInfo> getBindings0() {
		return m_bindings;
	}

	@Override
	public String getBindingPresentationText(IBindingInfo binding) throws Exception {
		return null;
	}

	@Override
	public void gotoDefinition(IBindingInfo ibinding) {
		BindingInfo binding = (BindingInfo) ibinding;
		int position = binding.getDefinitionOffset();
		if (position != -1) {
			IDesignPageSite site = IDesignPageSite.Helper.getSite(m_xmlObjectRoot);
			site.openSourcePosition(position);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Types
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public List<ObserveType> getTypes() {
		return ObserveType.TYPES;
	}

	@Override
	public ObserveType getTargetStartType() {
		return ObserveType.WIDGETS;
	}

	@Override
	public ObserveType getModelStartType() {
		return ObserveType.BEANS;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Observes
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IBaseLabelProvider createPropertiesViewerLabelProvider(TreeViewer viewer) {
		return new ObserveDecoratingLabelProvider(viewer);
	}

	@Override
	public List<PropertyFilter> getObservePropertyFilters() {
		return org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider.observePropertyFilters();
	}

	@Override
	public List<IObserveInfo> getObserves(ObserveType type) {
		ObserveTypeContainer container = getContainer(type);
		return container == null ? Collections.<IObserveInfo>emptyList() : container.getObservables();
	}

	@Override
	public void synchronizeObserves() throws Exception {
		// XXX synchronize for bindings
		for (ObserveTypeContainer container : m_containers) {
			container.synchronizeObserves();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// UI editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public List<IUiContentProvider> getContentProviders(IBindingInfo ibinding, IPageListener listener)
			throws Exception {
		List<IUiContentProvider> providers = Lists.newArrayList();
		BindingInfo binding = (BindingInfo) ibinding;
		binding.createContentProviders(providers, listener, this);
		return providers;
	}

	@Override
	public void fillExternalBindingActions(ToolBar toolBar, Menu contextMenu) {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean validate(IObserveInfo target,
			IObserveInfo targetProperty,
			IObserveInfo model,
			IObserveInfo modelProperty) throws Exception {
		if (!(target instanceof WidgetBindableInfo)) {
			return false;
		}
		return org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider.doValidate(
				CoreUtils.<AbstractBindingInfo>cast(m_bindings),
				target,
				targetProperty,
				model,
				modelProperty);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation/Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IBindingInfo createBinding(IObserveInfo target,
			IObserveInfo targetProperty,
			IObserveInfo model,
			IObserveInfo modelProperty) throws Exception {
		// prepare target
		BindableInfo targetBindable = (BindableInfo) target;
		BindableInfo targetBindableProperty = (BindableInfo) targetProperty;
		IObservableFactory targetFactory = targetBindableProperty.getObservableFactory();
		// prepare model
		BindableInfo modelBindable = (BindableInfo) model;
		BindableInfo modelBindableProperty = (BindableInfo) modelProperty;
		IObservableFactory modelFactory = modelBindableProperty.getObservableFactory();
		// calculate type
		Type type =
				org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider.calculateObserveType(
						targetFactory,
						modelFactory);
		// handle input type
		if (type == Type.Input) {
			// XXX
			throw new UnsupportedOperationException();
		}
		// create binding
		BindingInfo binding =
				new BindingInfo(targetBindable,
						targetBindableProperty,
						modelBindable,
						modelBindableProperty);
		binding.setDocumentEditor(new AttributeDocumentEditor(binding));
		//
		return binding;
	}

	@Override
	public void addBinding(final IBindingInfo ibinding) {
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				BindingInfo binding = (BindingInfo) ibinding;
				binding.create();
				binding.getDocumentEditor().add();
				m_bindings.add(binding);
			}
		});
	}

	@Override
	public void editBinding(final IBindingInfo ibinding) {
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				BindingInfo binding = (BindingInfo) ibinding;
				binding.getDocumentEditor().update();
			}
		});
	}

	@Override
	public void deleteBinding(final IBindingInfo ibinding) {
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				BindingInfo binding = (BindingInfo) ibinding;
				binding.delete();
				binding.getDocumentEditor().delete();
				m_bindings.remove(binding);
			}
		});
	}

	@Override
	public void deleteAllBindings() {
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				for (BindingInfo binding : m_bindings) {
					binding.delete();
					binding.getDocumentEditor().delete();
				}
				m_bindings.clear();
			}
		});
	}

	@Override
	public void deleteBindings(JavaInfo javaInfo) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canMoveBinding(IBindingInfo binding, int targetIndex, boolean upDown) {
		return false;
	}

	@Override
	public void moveBinding(IBindingInfo binding, int sourceIndex, int targetIndex, boolean upDown) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBindingPage(Object bindingPage) {
	}

	@Override
	public void refreshDesigner() {
		IEditorPart editor =
				DesignerPlugin.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof AbstractXmlEditor) {
			AbstractXmlEditor xmlEditor = (AbstractXmlEditor) editor;
			xmlEditor.getDesignPage().refreshGEF();
		}
	}

	@Override
	public void saveEdit() {
	}
}