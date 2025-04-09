/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.CheckboxTreeViewerWrapper;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ICheckboxViewerWrapper;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.ui.providers.ObserveDecoratingLabelProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.PropertyBindableInfo;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Content provider for choose properties from Tree (properties + sub properties) and make order
 * choosen properties.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public abstract class ChooseClassAndTreePropertiesUiContentProvider
extends
org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesUiContentProvider {
	private CheckboxTreeViewer m_treeViewer;
	private TableViewer m_orderPropertiesViewer;
	private final List<PropertyAdapter> m_orderProperties = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ChooseClassAndTreePropertiesUiContentProvider(ChooseClassAndPropertiesConfiguration configuration) {
		super(configuration);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Control createViewers(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		//
		super.createViewers(sashForm);
		//
		m_orderPropertiesViewer =
				new TableViewer(sashForm, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		m_orderPropertiesViewer.setContentProvider(new ArrayContentProvider());
		m_orderPropertiesViewer.setLabelProvider(new PropertyAdapterLabelProvider(
				m_orderPropertiesViewer) {
			@Override
			public String getText(Object element) {
				ObservePropertyAdapter adapter = getAdapter(element);
				return adapter.fullName;
			}
		});
		m_orderPropertiesViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!event.getSelection().isEmpty()) {
					m_treeViewer.setSelection(event.getSelection(), true);
				}
				calculateUpDowButtons();
			}
		});
		m_orderPropertiesViewer.setInput(m_orderProperties);
		//
		return sashForm;
	}

	@Override
	protected ICheckboxViewerWrapper createPropertiesViewer(Composite parent) {
		m_treeViewer =
				new CheckboxTreeViewer(parent, SWT.BORDER
						| SWT.FULL_SELECTION
						| SWT.H_SCROLL
						| SWT.V_SCROLL);
		m_treeViewer.setContentProvider(new PropertyAdapterContentProvider());
		m_treeViewer.setLabelProvider(new PropertyAdapterLabelProvider(m_treeViewer));
		m_treeViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (m_orderPropertiesViewer != null) {
					PropertyAdapter adapter = (PropertyAdapter) event.getElement();
					//
					if (event.getChecked()) {
						m_orderProperties.add(adapter);
					} else {
						m_orderProperties.remove(adapter);
					}
					//
					m_orderPropertiesViewer.refresh();
				}
			}
		});
		return new CheckboxTreeViewerWrapper(m_treeViewer);
	}

	@Override
	protected void configureDND() {
		configureDND(m_orderPropertiesViewer, m_orderProperties);
	}

	@Override
	protected void calculateUpDowButtons() {
		calculateUpDowButtons(m_orderPropertiesViewer, m_orderProperties);
	}

	@Override
	protected void handleMoveElement(boolean up) {
		handleMoveElement(up, m_orderPropertiesViewer, m_orderProperties);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	protected void setClassNameAndProperties(Class<?> beanClass,
			String beanClassName,
			List<String> properties) throws Exception {
		if (beanClassName == null) {
			setClassName(CoreUtils.getClassName(beanClass));
		} else {
			setClassName(beanClassName);
		}
		//
		ClassLoader classLoader = JavaInfoUtils.getClassLoader(EditorState.getActiveJavaInfo());
		BeanSupport beanSupport = new BeanSupport(classLoader, null);
		BeanBindableInfo beanObjectInfo =
				new BeanBindableInfo(beanSupport, null, beanClass, null, (IObservePresentation) null);
		//
		Object[] adapters = new Object[properties.size()];
		for (int i = 0; i < adapters.length; i++) {
			adapters[i] =
					convertPropertyToAdapter(beanObjectInfo.resolvePropertyReference(properties.get(i)));
		}
		//
		setCheckedAndExpand(adapters);
		//
		if (m_orderPropertiesViewer != null) {
			CollectionUtils.addAll(m_orderProperties, (PropertyAdapter[]) adapters);
			m_orderPropertiesViewer.refresh();
		}
		//
		calculatePropertiesFinish();
	}

	protected final void setCheckedAndExpand(Object[] adapters) {
		for (int i = 0; i < adapters.length; i++) {
			if (adapters[i] == null) {
				return;
			}
			m_treeViewer.expandToLevel(adapters[i], 0);
		}
		m_treeViewer.setCheckedElements(adapters);
		if (adapters.length > 0) {
			m_treeViewer.setSelection(new StructuredSelection(adapters[0]), true);
		}
	}

	protected ObservePropertyAdapter convertPropertyToAdapter(IObserveInfo observe) throws Exception {
		if (observe instanceof PropertyBindableInfo property) {
			ObservePropertyAdapter adapter =
					new ObservePropertyAdapter(convertPropertyToAdapter(property.getParent()), property);
			adapter.addToParent();
			return adapter;
		}
		return null;
	}

	@Override
	protected final List<PropertyAdapter> getChoosenProperties() {
		return m_orderProperties;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<PropertyAdapter> getProperties(Class<?> choosenClass) throws Exception {
		m_orderProperties.clear();
		if (m_orderPropertiesViewer != null) {
			m_orderPropertiesViewer.refresh();
		}
		//
		List<PropertyAdapter> adapters = new ArrayList<>();
		//
		ClassLoader classLoader = JavaInfoUtils.getClassLoader(EditorState.getActiveJavaInfo());
		BeanSupport beanSupport = new BeanSupport(classLoader, null);
		BeanBindableInfo beanObjectInfo =
				new BeanBindableInfo(beanSupport, null, choosenClass, null, (IObservePresentation) null);
		//
		for (PropertyBindableInfo property : beanSupport.getProperties(beanObjectInfo)) {
			adapters.add(new ObservePropertyAdapter(null, property));
		}
		//
		return adapters;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Classes
	//
	////////////////////////////////////////////////////////////////////////////
	public static class ObservePropertyAdapter extends PropertyAdapter {
		private final ObservePropertyAdapter m_parent;
		protected final BindableInfo m_property;
		protected List<ObservePropertyAdapter> m_children;
		public String fullName;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public ObservePropertyAdapter(ObservePropertyAdapter parent, BindableInfo property)
				throws Exception {
			super(property.getPresentation().getText(), property.getObjectType());
			m_parent = parent;
			m_property = property;
			if (m_parent == null) {
				fullName = getName();
			} else {
				fullName = m_parent.fullName + "." + getName();
			}
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		public ObservePropertyAdapter getParent() {
			return m_parent;
		}

		public BindableInfo getProperty() {
			return m_property;
		}

		public List<ObservePropertyAdapter> getChildren() {
			if (m_children == null) {
				m_children = new ArrayList<>();
				List<BindableInfo> properties =
						CoreUtils.cast(m_property.getChildren(ChildrenContext.ChildrenForPropertiesTable));
				for (BindableInfo property : properties) {
					try {
						m_children.add(new ObservePropertyAdapter(this, property));
					} catch (Throwable e) {
						DesignerPlugin.log(e);
					}
				}
			}
			return m_children;
		}

		public void setChildren(List<ObservePropertyAdapter> children) {
			m_children = children;
		}

		public void addToParent() {
			if (m_parent != null) {
				m_parent.m_children = new ArrayList<>();
				m_parent.m_children.add(this);
			}
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Object
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public int hashCode() {
			int parentHash = m_parent == null ? 1 : m_parent.hashCode();
			return parentHash * super.hashCode();
		}

		@Override
		public boolean equals(Object object) {
			// self check
			if (object == this) {
				return true;
			}
			// compare with other adapter
			if (object instanceof ObservePropertyAdapter adapter) {
				if (m_parent == null && adapter.m_parent == null) {
				} else if (m_parent != null
						&& adapter.m_parent == null
						|| m_parent == null
						&& adapter.m_parent != null) {
					return false;
				} else if (m_parent != null && adapter.m_parent != null) {
					if (!m_parent.equals(adapter.m_parent)) {
						return false;
					}
				}
				return m_name.equals(adapter.m_name) && m_type == adapter.m_type;
			}
			// default
			return false;
		}
	}
	private static class PropertyAdapterContentProvider implements ITreeContentProvider {
		@Override
		public Object[] getElements(Object input) {
			return ((List<?>) input).toArray();
		}

		@Override
		public Object getParent(Object element) {
			return getAdapter(element).getParent();
		}

		@Override
		public boolean hasChildren(Object element) {
			return !getAdapter(element).getChildren().isEmpty();
		}

		@Override
		public Object[] getChildren(Object element) {
			return getAdapter(element).getChildren().toArray();
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}
	}
	private static class PropertyAdapterLabelProvider extends LabelProvider
	implements
	IColorProvider,
	IFontProvider {
		private final ObserveDecoratingLabelProvider m_labelProvider;
		private final ResourceManager m_resourceManager;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public PropertyAdapterLabelProvider(StructuredViewer viewer) {
			m_labelProvider = new ObserveDecoratingLabelProvider(viewer);
			m_resourceManager = new LocalResourceManager(JFaceResources.getResources());
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// LabelProvider
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public void dispose() {
			super.dispose();
			m_labelProvider.dispose();
			m_resourceManager.dispose();
		}

		@Override
		public String getText(Object element) {
			return getAdapter(element).getName();
		}

		@Override
		public Image getImage(Object element) {
			try {
				return m_resourceManager.createImage(getAdapterProperty(element).getPresentation().getImageDescriptor());
			} catch (Throwable e) {
			}
			return super.getImage(element);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Decoration
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Color getForeground(Object element) {
			return m_labelProvider.getForeground(getAdapterProperty(element));
		}

		@Override
		public Color getBackground(Object element) {
			return m_labelProvider.getBackground(getAdapterProperty(element));
		}

		@Override
		public Font getFont(Object element) {
			return m_labelProvider.getFont(getAdapterProperty(element));
		}
	}

	private static ObservePropertyAdapter getAdapter(Object element) {
		return (ObservePropertyAdapter) element;
	}

	private static BindableInfo getAdapterProperty(Object element) {
		return getAdapter(element).getProperty();
	}
}