/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.ICompleteListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.GlobalFactoryHelper;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ObservableCollectionTreeContentProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.TreeViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeBeanAdvisorInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeObservableLabelProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.TypeImageProvider;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.apache.commons.lang3.ArrayUtils;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Content provider for choose properties (over checked tree): parent/children/has
 * children/text/image for content and label providers for tree viewer in Designer mode.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class TreeContentLabelProvidersUiContentProvider implements IUiContentProvider {
	private static final String PARENT_GROUP_NAME =
			Messages.TreeContentLabelProvidersUiContentProvider_parentGroup;
	private static final String CHILDREN_GROUP_NAME =
			Messages.TreeContentLabelProvidersUiContentProvider_childrenGroup;
	private static final String HAS_CHILDREN_GROUP_NAME =
			Messages.TreeContentLabelProvidersUiContentProvider_hasChildrenGroup;
	private static final String TEXT_GROUP_NAME =
			Messages.TreeContentLabelProvidersUiContentProvider_textGroup;
	private static final String IMAGE_GROUP_NAME =
			Messages.TreeContentLabelProvidersUiContentProvider_imageGroup;
	//
	private final TreeViewerInputBindingInfo m_binding;
	private CheckboxTreeViewer m_propertiesViewer;
	private ICompleteListener m_listener;
	private String m_errorMessage;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TreeContentLabelProvidersUiContentProvider(TreeViewerInputBindingInfo binding) {
		m_binding = binding;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Complete
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setCompleteListener(ICompleteListener listener) {
		m_listener = listener;
	}

	@Override
	public String getErrorMessage() {
		return m_errorMessage;
	}

	private void setErrorMessage(String message) {
		m_errorMessage = message;
		m_listener.calculateFinish();
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
		// properties title
		Label title = new Label(parent, SWT.NONE);
		title.setText(Messages.TreeContentLabelProvidersUiContentProvider_propertiesLabel);
		// properties viewer
		m_propertiesViewer = new CheckboxTreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridDataFactory.create(m_propertiesViewer.getControl()).fill().grab().spanH(columns - 1);
		m_propertiesViewer.setContentProvider(new TreePropertiesContentProvider());
		m_propertiesViewer.setLabelProvider(new TreePropertiesLabelProvider());
		m_propertiesViewer.addCheckStateListener(new PropertiesCheckStateListener());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handle
	//
	////////////////////////////////////////////////////////////////////////////
	private void calculateFinish() {
		if (!checkChoosenElement(CHILDREN_GROUP_NAME)) {
			setErrorMessage(Messages.TreeContentLabelProvidersUiContentProvider_errorMessage1);
		} else if (!checkChoosenElement(TEXT_GROUP_NAME)) {
			setErrorMessage(Messages.TreeContentLabelProvidersUiContentProvider_errorMessage2);
		} else {
			setErrorMessage(null);
		}
	}

	/**
	 * @return <code>true</code> if given group <code>name</name> is checked.
	 */
	private boolean checkChoosenElement(String name) {
		for (Object element : m_propertiesViewer.getCheckedElements()) {
			if (element instanceof PropertiesGroup group) {
				if (name.equals(group.name)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Sets {@link Class} for choose properties.
	 */
	public void setElementType(Class<?> elementType) throws Exception {
		try {
			// prepare properties
			List<PropertyDescriptor> descriptors = BeanSupport.getPropertyDescriptors(elementType);
			//
			GlobalFactoryHelper.filterElementPropertiesForViewerInput(
					m_binding.getInputObservable(),
					elementType,
					descriptors);
			// prepare groups
			List<PropertiesGroup> groups = new ArrayList<>();
			groups.add(new PropertiesGroup(PARENT_GROUP_NAME, filterProperties(descriptors, Object.class)));
			groups.add(new PropertiesGroup(CHILDREN_GROUP_NAME, filterCollectionProperties(descriptors)));
			groups.add(new PropertiesGroup(HAS_CHILDREN_GROUP_NAME, filterBooleanProperties(descriptors)));
			groups.add(new PropertiesGroup(TEXT_GROUP_NAME, filterProperties(descriptors, String.class)));
			groups.add(new PropertiesGroup(IMAGE_GROUP_NAME, filterProperties(descriptors, Image.class)));
			// fill viewer
			m_propertiesViewer.setInput(groups);
			m_propertiesViewer.setCheckedElements(ArrayUtils.EMPTY_OBJECT_ARRAY);
			m_propertiesViewer.expandAll();
		} finally {
			calculateFinish();
		}
	}

	/**
	 * Helper method that filter given <code>properties</code> include only {@link Class}
	 * <code>testType</code> properties.
	 */
	private static List<PropertyDescriptor> filterProperties(List<PropertyDescriptor> properties,
			Class<?> testType) {
		List<PropertyDescriptor> newProperties = new ArrayList<>();
		for (PropertyDescriptor property : properties) {
			Class<?> type = property.getPropertyType();
			if (type != null && (testType == type || testType.isAssignableFrom(type))) {
				newProperties.add(property);
			}
		}
		return newProperties;
	}

	/**
	 * Helper method that filter given <code>properties</code> include only boolean properties.
	 */
	private static List<PropertyDescriptor> filterBooleanProperties(List<PropertyDescriptor> properties) {
		List<PropertyDescriptor> newProperties = new ArrayList<>();
		for (PropertyDescriptor property : properties) {
			Class<?> type = property.getPropertyType();
			if (type == boolean.class || type == Boolean.class) {
				newProperties.add(property);
			}
		}
		return newProperties;
	}

	/**
	 * Helper method that filter given <code>properties</code> include only {@link Collection} and
	 * array properties.
	 */
	private static List<PropertyDescriptor> filterCollectionProperties(List<PropertyDescriptor> properties) {
		List<PropertyDescriptor> newProperties = new ArrayList<>();
		for (PropertyDescriptor property : properties) {
			Class<?> type = property.getPropertyType();
			if (type != null && (type.isArray() || Collection.class.isAssignableFrom(type))) {
				newProperties.add(property);
			}
		}
		return newProperties;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
		if (m_binding.isDesignerMode()) {
			Class<?> elementType = m_binding.getElementType();
			if (elementType != null) {
				// set element type
				setElementType(elementType);
				// prepare checked groups
				List<Object> checkedObjects = new ArrayList<>();
				// check parent/children/has children
				TreeBeanAdvisorInfo advisor =
						(TreeBeanAdvisorInfo) m_binding.getContentProvider().getAdvisorInfo();
				extractProperties(checkedObjects, PARENT_GROUP_NAME, advisor.getParentProperty());
				extractProperties(checkedObjects, CHILDREN_GROUP_NAME, advisor.getChildrenProperty());
				extractProperties(checkedObjects, HAS_CHILDREN_GROUP_NAME, advisor.getHasChildrenProperty());
				// check text/image
				TreeObservableLabelProviderInfo labelProvider =
						(TreeObservableLabelProviderInfo) m_binding.getLabelProvider();
				extractProperties(checkedObjects, TEXT_GROUP_NAME, labelProvider.getTextProperty());
				extractProperties(checkedObjects, IMAGE_GROUP_NAME, labelProvider.getImageProperty());
				// apply checked
				if (!checkedObjects.isEmpty()) {
					m_propertiesViewer.setCheckedElements(checkedObjects.toArray());
				}
			}
		}
		//
		calculateFinish();
	}

	/**
	 * Find and fill <code>objects</code>, properties viewer elements ({@link PropertiesGroup} and
	 * {@link TreePropertyWrapper}) that association with given <code>property</code>.
	 */
	@SuppressWarnings("unchecked")
	private void extractProperties(List<Object> objects, String groupName, String property) {
		if (property != null) {
			List<PropertiesGroup> input = (List<PropertiesGroup>) m_propertiesViewer.getInput();
			for (PropertiesGroup group : input) {
				if (groupName.equals(group.name)) {
					for (TreePropertyWrapper wrapper : group.properties) {
						if (property.equals(wrapper.descriptor.getName())) {
							objects.add(group);
							objects.add(wrapper);
							return;
						}
					}
					return;
				}
			}
		}
	}

	@Override
	public void saveToObject() throws Exception {
		// prepare content provider
		ObservableCollectionTreeContentProviderInfo contentProvider = m_binding.getContentProvider();
		// prepare factory
		BeansObservableFactoryInfo factory =
				(BeansObservableFactoryInfo) contentProvider.getFactoryInfo();
		// prepare advisor
		TreeBeanAdvisorInfo advisor = (TreeBeanAdvisorInfo) contentProvider.getAdvisorInfo();
		// prepare label provider
		TreeObservableLabelProviderInfo labelProvider =
				(TreeObservableLabelProviderInfo) m_binding.getLabelProvider();
		// extract checked properties
		for (Object element : m_propertiesViewer.getCheckedElements()) {
			if (element instanceof TreePropertyWrapper wrapper) {
				String propertyName = wrapper.descriptor.getName();
				String groupName = wrapper.parent.name;
				// check group
				if (PARENT_GROUP_NAME.equals(groupName)) {
					advisor.setParentProperty(propertyName);
				} else if (CHILDREN_GROUP_NAME.equals(groupName)) {
					advisor.setChildrenProperty(propertyName);
					factory.setPropertyName(propertyName);
				} else if (HAS_CHILDREN_GROUP_NAME.equals(groupName)) {
					advisor.setHasChildrenProperty(propertyName);
				} else if (TEXT_GROUP_NAME.equals(groupName)) {
					labelProvider.setTextProperty(propertyName);
				} else if (IMAGE_GROUP_NAME.equals(groupName)) {
					labelProvider.setImageProperty(propertyName);
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Inner model
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Group.
	 */
	private static class PropertiesGroup {
		public String name;
		public TreePropertyWrapper[] properties;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public PropertiesGroup(String name, List<PropertyDescriptor> descriptors) {
			this.name = name;
			int size = descriptors.size();
			properties = new TreePropertyWrapper[size];
			for (int i = 0; i < size; i++) {
				properties[i] = new TreePropertyWrapper(this, descriptors.get(i));
			}
		}
	}
	/**
	 * Property.
	 */
	private static class TreePropertyWrapper {
		public PropertiesGroup parent;
		public PropertyDescriptor descriptor;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public TreePropertyWrapper(PropertiesGroup parent, PropertyDescriptor descriptor) {
			this.parent = parent;
			this.descriptor = descriptor;
		}
	}
	/**
	 * Tree content provider.
	 */
	private static class TreePropertiesContentProvider implements ITreeContentProvider {
		@Override
		public Object[] getElements(Object input) {
			List<?> list = (List<?>) input;
			return list.toArray();
		}

		@Override
		public Object[] getChildren(Object element) {
			if (element instanceof PropertiesGroup group) {
				return group.properties;
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof PropertiesGroup group) {
				return group.properties.length > 0;
			}
			return false;
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof TreePropertyWrapper wrapper) {
				return wrapper.parent;
			}
			return null;
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	/**
	 * Tree label provider.
	 */
	private static class TreePropertiesLabelProvider extends LabelProvider {
		private final ResourceManager m_resourceManager = new LocalResourceManager(JFaceResources.getResources());

		@Override
		public String getText(Object element) {
			if (element instanceof PropertiesGroup group) {
				return group.name;
			}
			//
			TreePropertyWrapper wrapper = (TreePropertyWrapper) element;
			return wrapper.descriptor.getName();
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof PropertiesGroup) {
				return m_resourceManager.createImageWithDefault(TypeImageProvider.OBJECT_IMAGE);
			}
			//
			TreePropertyWrapper wrapper = (TreePropertyWrapper) element;
			return m_resourceManager.createImageWithDefault(TypeImageProvider.getImageDescriptor(wrapper.descriptor.getPropertyType()));
		}

		@Override
		public void dispose() {
			super.dispose();
			m_resourceManager.dispose();
		}
	}
	/**
	 * {@link ICheckStateListener} listener that supported only one checked element into one group.
	 */
	private class PropertiesCheckStateListener implements ICheckStateListener {
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			CheckboxTreeViewer viewer = (CheckboxTreeViewer) event.getCheckable();
			Object element = event.getElement();
			//
			if (event.getChecked()) {
				if (element instanceof PropertiesGroup group) {
					if (ArrayUtils.isEmpty(group.properties)) {
						// clear checked for empty group
						viewer.setChecked(element, false);
					} else {
						// checked fist element if checked group
						viewer.setChecked(group.properties[0], true);
					}
				} else {
					TreePropertyWrapper wrapper = (TreePropertyWrapper) element;
					// ensure checked group
					if (!viewer.getChecked(wrapper.parent)) {
						viewer.setChecked(wrapper.parent, true);
					}
					// clear checked with other elements
					for (TreePropertyWrapper property : wrapper.parent.properties) {
						if (property != wrapper && viewer.getChecked(property)) {
							viewer.setChecked(property, false);
						}
					}
				}
			} else {
				if (element instanceof PropertiesGroup group) {
					for (TreePropertyWrapper property : group.properties) {
						if (viewer.getChecked(property)) {
							viewer.setChecked(property, false);
						}
					}
				} else {
					// clear checked group if element unchecked
					TreePropertyWrapper wrapper = (TreePropertyWrapper) element;
					viewer.setChecked(wrapper.parent, false);
				}
			}
			//
			calculateFinish();
		}
	}
}