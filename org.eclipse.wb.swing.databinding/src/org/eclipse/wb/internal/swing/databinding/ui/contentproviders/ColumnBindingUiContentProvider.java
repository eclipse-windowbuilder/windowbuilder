/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.ICompleteListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.ui.providers.ObserveDecoratingLabelProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.swing.databinding.Messages;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanSupport;
import org.eclipse.wb.internal.swing.databinding.model.beans.ElPropertyObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.ColumnBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.ElPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.el.ElPropertyUiConfiguration;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Editor for {@link ColumnBindingInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class ColumnBindingUiContentProvider implements IUiContentProvider {
	private Label propertiesLabel;
	private CheckboxTreeViewer treeViewer;
	private ElPropertyUiContentProvider elPropertyUIContentProvider;
	private boolean elProperty;
	private String errorMessage;
	private List<PropertyAdapter> properties = Collections.emptyList();
	private ICompleteListener listener;
	private final ColumnBindingInfo binding;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnBindingUiContentProvider(ColumnBindingInfo binding) {
		this.binding = binding;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Complete
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setCompleteListener(ICompleteListener listener) {
		this.listener = listener;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Sets or clears the error message for this provider.
	 */
	private final void setErrorMessage(String message) {
		errorMessage = message;
		if (listener != null) {
			listener.calculateFinish();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int getNumberOfControls() {
		return 2;
	}

	@Override
	public void createContent(Composite parent, int columns) {
		// create properties title
		propertiesLabel = new Label(parent, SWT.NONE);
		propertiesLabel.setText(Messages.ColumnBindingUiContentProvider_properties);
		// create properties viewer
		treeViewer =
				new CheckboxTreeViewer(parent, SWT.BORDER
						| SWT.FULL_SELECTION
						| SWT.H_SCROLL
						| SWT.V_SCROLL);
		treeViewer.setContentProvider(new PropertyAdapterContentProvider());
		treeViewer.setLabelProvider(new PropertyAdapterLabelProvider(treeViewer));
		treeViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					treeViewer.setCheckedElements(new Object[]{event.getElement()});
				}
				calculateFinish();
			}
		});
		GridDataFactory.create(treeViewer.getTree()).fill().grab().spanH(columns - 1).minVC(5);
		// create EL property
		ElPropertyUiConfiguration configuration = new ElPropertyUiConfiguration();
		configuration.setTitle(Messages.ColumnBindingUiContentProvider_elExpression);
		elPropertyUIContentProvider = new ElPropertyUiContentProvider(configuration, null);
		elPropertyUIContentProvider.setCompleteListener(new ICompleteListener() {
			@Override
			public void calculateFinish() {
				ColumnBindingUiContentProvider.this.calculateFinish();
			}
		});
		elPropertyUIContentProvider.createContent(parent, columns);
		//
		treeViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				handleELProperty();
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handling
	//
	////////////////////////////////////////////////////////////////////////////
	private void calculateFinish() {
		// prepare checked elements
		Object[] checkedElements = treeViewer.getCheckedElements();
		// check properties state
		if (checkedElements.length == 0) {
			setErrorMessage(Messages.ColumnBindingUiContentProvider_erorrMessage);
		} else {
			if (elProperty) {
				setErrorMessage(elPropertyUIContentProvider.getErrorMessage());
			} else {
				setErrorMessage(null);
			}
		}
	}

	private void handleELProperty() {
		IGenericType objectType = binding.getJTableBinding().getInputElementType();
		try {
			Object[] elements = treeViewer.getCheckedElements();
			if (elProperty) {
				if (elements.length == 1) {
					ObservePropertyAdapter adapter = (ObservePropertyAdapter) elements[0];
					if (adapter.getProperty() instanceof ElPropertyObserveInfo) {
						elPropertyUIContentProvider.setProperty((ElPropertyInfo) convertAdapterToProperty(
								new BeanSupport(),
								objectType,
								adapter));
						return;
					}
				}
				elProperty = false;
				elPropertyUIContentProvider.setProperty(null);
			} else if (elements.length == 1) {
				ObservePropertyAdapter adapter = (ObservePropertyAdapter) elements[0];
				if (adapter.getProperty() instanceof ElPropertyObserveInfo) {
					elProperty = true;
					elPropertyUIContentProvider.setProperty((ElPropertyInfo) convertAdapterToProperty(
							new BeanSupport(),
							objectType,
							adapter));
				}
			}
		} catch (Throwable e) {
			DesignerPlugin.log(e);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
		IGenericType objectType = binding.getJTableBinding().getInputElementType();
		setObjectType(objectType);
		PropertyInfo property = binding.getDetailProperty();
		elProperty = property instanceof ElPropertyInfo;
		elPropertyUIContentProvider.setProperty(elProperty ? (ElPropertyInfo) property : null);
		ObservePropertyAdapter adapter =
				convertPropertyToAdapter(new BeanSupport(), objectType, property);
		setCheckedAdExpand(adapter);
		calculateFinish();
	}

	@Override
	public void saveToObject() throws Exception {
		if (elProperty) {
			elPropertyUIContentProvider.saveToObject();
			binding.setDetailProperty(elPropertyUIContentProvider.getProperty());
		} else {
			IGenericType objectType = binding.getJTableBinding().getInputElementType();
			List<PropertyAdapter> choosenProperties = getChoosenProperties();
			PropertyAdapter propertyAdapter = choosenProperties.get(0);
			PropertyInfo property =
					convertAdapterToProperty(
							new BeanSupport(),
							objectType,
							(ObservePropertyAdapter) propertyAdapter);
			binding.setDetailProperty(property);
		}
	}

	protected void setObjectType(IGenericType objectType) {
		// check properties
		if (getErrorMessage() == null) {
			try {
				// load properties
				properties = getProperties(objectType);
				treeViewer.setInput(properties);
				// checked properties
				if (!properties.isEmpty()) {
					treeViewer.setCheckedElements(ArrayUtils.EMPTY_OBJECT_ARRAY);
				}
			} catch (Throwable e) {
				setEmptyProperties();
			}
		} else {
			setEmptyProperties();
		}
	}

	private void setEmptyProperties() {
		properties = Collections.emptyList();
		treeViewer.setInput(properties);
		treeViewer.setCheckedElements(ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	private void setCheckedAdExpand(Object... adapters) {
		for (int i = 0; i < adapters.length; i++) {
			treeViewer.expandToLevel(adapters[i], 0);
		}
		treeViewer.setCheckedElements(adapters);
		if (adapters.length > 0) {
			treeViewer.setSelection(new StructuredSelection(adapters[0]), true);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	private List<PropertyAdapter> getChoosenProperties() {
		return new ArrayList<>(List.of((PropertyAdapter[])treeViewer.getCheckedElements()));
	}

	private List<PropertyAdapter> getProperties(IGenericType objectType) throws Exception {
		List<PropertyAdapter> adapters = new ArrayList<>();
		BeanSupport beanSupport = new BeanSupport();
		beanSupport.doAddELProperty(true);
		for (ObserveInfo property : beanSupport.createProperties(null, objectType)) {
			adapters.add(new ObservePropertyAdapter(null, property));
		}
		return adapters;
	}

	private PropertyInfo convertAdapterToProperty(BeanSupport beanSupport,
			IGenericType objectType,
			ObservePropertyAdapter adapter) throws Exception {
		ObserveInfo observe =
				new SubBeanObserveInfo(beanSupport, null, objectType, StringReferenceProvider.EMPTY);
		return adapter.getProperty().createProperty(observe);
	}

	private ObservePropertyAdapter convertPropertyToAdapter(BeanSupport beanSupport,
			IGenericType objectType,
			PropertyInfo property) throws Exception {
		ObserveInfo observe =
				property.getObserveProperty(new SubBeanObserveInfo(beanSupport,
						null,
						objectType,
						StringReferenceProvider.EMPTY));
		Assert.isNotNull(observe);
		return convertObserveToAdapter(observe);
	}

	private ObservePropertyAdapter convertObserveToAdapter(ObserveInfo observe) throws Exception {
		if (observe != null) {
			ObservePropertyAdapter adapter =
					new ObservePropertyAdapter(convertObserveToAdapter((ObserveInfo) observe.getParent()),
							observe);
			adapter.addToParent();
			return adapter;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Classes
	//
	////////////////////////////////////////////////////////////////////////////
	private static class ObservePropertyAdapter extends PropertyAdapter {
		private final ObservePropertyAdapter m_parent;
		private final ObserveInfo m_property;
		private List<ObservePropertyAdapter> m_children;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public ObservePropertyAdapter(ObservePropertyAdapter parent, ObserveInfo property)
				throws Exception {
			super(property.getPresentation().getText(), property.getObjectClass());
			m_parent = parent;
			m_property = property;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		public ObservePropertyAdapter getParent() {
			return m_parent;
		}

		public ObserveInfo getProperty() {
			return m_property;
		}

		public List<ObservePropertyAdapter> getChildren() {
			if (m_children == null) {
				m_children = new ArrayList<>();
				List<ObserveInfo> properties =
						CoreUtils.cast(m_property.getChildren(ChildrenContext.ChildrenForPropertiesTable));
				for (ObserveInfo property : properties) {
					try {
						m_children.add(new ObservePropertyAdapter(this, property));
					} catch (Throwable e) {
						DesignerPlugin.log(e);
					}
				}
			}
			return m_children;
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
		public PropertyAdapterLabelProvider(TreeViewer viewer) {
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

	private static ObserveInfo getAdapterProperty(Object element) {
		return getAdapter(element).getProperty();
	}

	private static class SubBeanObserveInfo extends BeanObserveInfo {
		public SubBeanObserveInfo(BeanSupport beanSupport,
				ObserveInfo parent,
				IGenericType objectType,
				IReferenceProvider referenceProvider) {
			super(beanSupport, parent, objectType, referenceProvider);
		}

		@Override
		public IObservePresentation getPresentation() {
			return null;
		}
	}
}