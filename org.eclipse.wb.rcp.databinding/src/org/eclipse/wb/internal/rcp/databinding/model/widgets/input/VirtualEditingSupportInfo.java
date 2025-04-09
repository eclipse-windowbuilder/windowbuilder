/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.LabelUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.SeparatorUiContentProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.PropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.SwtProperties;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.ChooseClassAndTreePropertiesUiContentProvider2;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Editable model for {@link EditingSupportInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class VirtualEditingSupportInfo {
	private final IElementTypeProvider m_elementTypeProvider;
	private final AbstractViewerInputBindingInfo m_viewerBinding;
	private final WidgetBindableInfo m_viewerColumn;
	private EditingSupportInfo m_editingSupport;
	private String m_cellEditorClassName;
	private String m_cellEditorProperty;
	private String m_elementProperty;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public VirtualEditingSupportInfo(IElementTypeProvider elementTypeProvider,
			EditingSupportInfo editingSupport) throws Exception {
		this(elementTypeProvider, editingSupport.getViewerBinding(), editingSupport.getViewerColumn());
		m_editingSupport = editingSupport;
		m_cellEditorClassName = m_editingSupport.getCellEditorInfo().getClassName();
		m_cellEditorProperty = m_editingSupport.getCellEditorProperty().getParsePropertyReference();
		m_elementProperty = m_editingSupport.getElementProperty().getParserPropertyReference();
	}

	public VirtualEditingSupportInfo(IElementTypeProvider elementTypeProvider,
			AbstractViewerInputBindingInfo viewerBinding,
			WidgetBindableInfo viewerColumn) {
		m_elementTypeProvider = elementTypeProvider;
		m_viewerBinding = viewerBinding;
		m_viewerColumn = viewerColumn;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public WidgetBindableInfo getViewerColumn() {
		return m_viewerColumn;
	}

	public boolean isEmpty() {
		return m_cellEditorClassName == null;
	}

	public void delete() {
		m_cellEditorClassName = null;
		m_cellEditorProperty = null;
		m_elementProperty = null;
		m_editingSupport = null;
	}

	public EditingSupportInfo createOrUpdateEditingSupport() throws Exception {
		// configure viewer
		m_viewerBinding.getViewer().ensureConvertToField();
		// configure column
		m_viewerColumn.ensureConvertToField();
		// create new support
		if (m_editingSupport == null) {
			return new EditingSupportInfo(m_viewerBinding,
					m_viewerColumn,
					m_cellEditorClassName,
					m_cellEditorProperty,
					m_elementProperty);
		}
		// edit exist support
		m_editingSupport.getCellEditorInfo().setClassName0(m_cellEditorClassName);
		m_editingSupport.getCellEditorProperty().setParsePropertyReference(m_cellEditorProperty);
		m_editingSupport.getElementProperty().setParserPropertyReference(m_elementProperty);
		return m_editingSupport;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	public String getCellEditorPresentationText() throws Exception {
		return isEmpty() ? "" : ClassUtils.getShortClassName(StringUtils.substringBefore(
				m_cellEditorClassName,
				"(")) + "." + StringUtils.remove(m_cellEditorProperty, '"');
	}

	public String getElementPropertyPresentationText() throws Exception {
		return isEmpty() ? "" : ClassUtils.getShortClassName(m_elementTypeProvider.getElementType())
				+ "."
				+ StringUtils.remove(m_elementProperty, '"');
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	public void createContentProviders(List<IUiContentProvider> providers) throws Exception {
		// CellEditor
		providers.add(new LabelUiContentProvider(Messages.VirtualEditingSupportInfo_cellEditorLabel,
				getCellEditorPresentationText()));
		//
		ChooseClassAndPropertiesConfiguration cellEditorConfiguration =
				new ChooseClassAndPropertiesConfiguration();
		cellEditorConfiguration.setBaseClassName("org.eclipse.jface.viewers.CellEditor");
		cellEditorConfiguration.setDialogFieldLabel(Messages.VirtualEditingSupportInfo_chooseLabel);
		cellEditorConfiguration.setEmptyClassErrorMessage(Messages.VirtualEditingSupportInfo_chooseEmptyMessage);
		cellEditorConfiguration.setErrorMessagePrefix(Messages.VirtualEditingSupportInfo_chooseErrorPrefix);
		cellEditorConfiguration.setPropertiesLabel(Messages.VirtualEditingSupportInfo_choosePropertiesLabel);
		cellEditorConfiguration.setLoadedPropertiesCheckedStrategy(ChooseClassAndPropertiesConfiguration.LoadedPropertiesCheckedStrategy.None);
		cellEditorConfiguration.setPropertiesErrorMessage(Messages.VirtualEditingSupportInfo_choosePropertyMessage);
		cellEditorConfiguration.setDefaultValues(new String[]{
				"org.eclipse.jface.viewers.TextCellEditor",
				"org.eclipse.jface.viewers.ComboBoxCellEditor",
		"org.eclipse.jface.viewers.CheckboxCellEditor"});
		//
		providers.add(new CellEditorUiContentProvider(cellEditorConfiguration));
		//
		providers.add(new SeparatorUiContentProvider());
		// Element property
		providers.add(new LabelUiContentProvider(Messages.VirtualEditingSupportInfo_elementProperty,
				getElementPropertyPresentationText()));
		//
		ChooseClassAndPropertiesConfiguration elementConfiguration =
				new ChooseClassAndPropertiesConfiguration();
		elementConfiguration.setDialogFieldLabel(Messages.VirtualEditingSupportInfo_chooseBeanLabel);
		elementConfiguration.setEmptyClassErrorMessage(Messages.VirtualEditingSupportInfo_chooseBeanEmptyMessage);
		elementConfiguration.setErrorMessagePrefix(Messages.VirtualEditingSupportInfo_chooseBeanErrorPrefix);
		elementConfiguration.setPropertiesLabel(Messages.VirtualEditingSupportInfo_chooseBeanPropertiesLabel);
		elementConfiguration.setLoadedPropertiesCheckedStrategy(ChooseClassAndPropertiesConfiguration.LoadedPropertiesCheckedStrategy.None);
		elementConfiguration.setPropertiesErrorMessage(Messages.VirtualEditingSupportInfo_chooseBeanPropertiesErrorMessage);
		//
		providers.add(new ElementPropertyUiContentProvider(elementConfiguration));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ContentProviders
	//
	////////////////////////////////////////////////////////////////////////////
	private class CellEditorUiContentProvider extends ChooseClassAndTreePropertiesUiContentProvider2 {
		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public CellEditorUiContentProvider(ChooseClassAndPropertiesConfiguration configuration) {
			super(configuration);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Properties
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected Class<?> loadClass(String className) throws ClassNotFoundException {
			int index = className.indexOf('(');
			if (index != -1) {
				className = className.substring(0, index);
			}
			return super.loadClass(className);
		}

		@Override
		protected List<PropertyAdapter> getProperties(final Class<?> choosenClass) throws Exception {
			List<PropertyAdapter> properties = super.getProperties(choosenClass);
			int size = properties.size();
			for (int i = 0; i < size; i++) {
				ObservePropertyAdapter oldAdapter = (ObservePropertyAdapter) properties.get(i);
				// configure control property
				if ("control".equals(oldAdapter.getName())) {
					properties.set(i, new ObservePropertyAdapter(null, oldAdapter.getProperty()) {
						@Override
						public List<ObservePropertyAdapter> getChildren() {
							if (m_children == null) {
								try {
									// prepare control SWT properties
									m_children = new ArrayList<>();
									List<WidgetPropertyBindableInfo> swtProperties =
											getWidgetProperties(
													JavaInfoUtils.getClassLoader(EditorState.getActiveJavaInfo()),
													choosenClass,
													m_property.getObjectType());
									// create adapters for all properties exclude "items"
									for (WidgetPropertyBindableInfo swtProperty : swtProperties) {
										if (!"observeItems".equals(swtProperty.getReference())) {
											ObservePropertyAdapter swtAdapter =
													new ObservePropertyAdapter(this, swtProperty);
											swtAdapter.setChildren(Collections.<ObservePropertyAdapter>emptyList());
											m_children.add(swtAdapter);
										}
									}
								} catch (Throwable e) {
									m_children = Collections.emptyList();
								}
							}
							return m_children;
						}
					});
				}
			}
			return properties;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Update
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public void updateFromObject() throws Exception {
			if (isEmpty()) {
				calculateFinish();
			} else {
				Class<?> beanClass = loadClass(m_cellEditorClassName);
				if (m_cellEditorProperty.startsWith("\"control.")) {
					// prepare class
					setClassName(m_cellEditorClassName);
					// prepare properties
					ClassLoader classLoader = JavaInfoUtils.getClassLoader(EditorState.getActiveJavaInfo());
					BeanSupport beanSupport = new BeanSupport(classLoader, null);
					BeanBindableInfo beanObjectInfo =
							new BeanBindableInfo(beanSupport, null, beanClass, null, (IObservePresentation) null);
					// prepare control adapter
					PropertyBindableInfo controlProperty =
							beanObjectInfo.resolvePropertyReference("\"control\"");
					ObservePropertyAdapter parent = convertPropertyToAdapter(controlProperty);
					// prepare widget adapter
					String swtPropertyReference =
							StringUtils.substringBetween(m_cellEditorProperty, ".", "\"");
					//
					List<WidgetPropertyBindableInfo> swtProperties =
							getWidgetProperties(classLoader, beanClass, controlProperty.getObjectType());
					//
					for (WidgetPropertyBindableInfo swtProperty : swtProperties) {
						if (swtPropertyReference.equals(SwtProperties.SWT_OBSERVABLES_TO_WIDGET_PROPERTIES.get(swtProperty.getReference()))) {
							ObservePropertyAdapter swtAdapter = new ObservePropertyAdapter(parent, swtProperty);
							swtAdapter.setChildren(Collections.<ObservePropertyAdapter>emptyList());
							swtAdapter.addToParent();
							setCheckedAndExpand(new Object[]{swtAdapter});
							break;
						}
					}
					//
					calculatePropertiesFinish();
				} else {
					setClassNameAndProperties(
							beanClass,
							m_cellEditorClassName,
							Arrays.asList(m_cellEditorProperty));
				}
			}
		}

		@Override
		protected void saveToObject(Class<?> choosenClass, List<PropertyAdapter> choosenProperties)
				throws Exception {
			m_cellEditorClassName = getClassName();
			Object[] checkedElements = m_propertiesViewer.getCheckedElements();
			ObservePropertyAdapter adapter = (ObservePropertyAdapter) checkedElements[0];
			if (adapter.getProperty() instanceof WidgetPropertyBindableInfo) {
				m_cellEditorProperty =
						"\"control."
								+ SwtProperties.SWT_OBSERVABLES_TO_WIDGET_PROPERTIES.get(adapter.getProperty().getReference())
								+ "\"";
			} else {
				m_cellEditorProperty = adapter.getProperty().getReference();
			}
			adapter.getProperty();
		}
	}

	private static List<WidgetPropertyBindableInfo> getWidgetProperties(ClassLoader classLoader,
			Class<?> choosenClass,
			Class<?> widgetType) throws Exception {
		if (classLoader.loadClass("org.eclipse.jface.viewers.TextCellEditor").isAssignableFrom(
				choosenClass)) {
			widgetType = classLoader.loadClass("org.eclipse.swt.widgets.Text");
		} else if (classLoader.loadClass("org.eclipse.jface.viewers.ComboBoxCellEditor").isAssignableFrom(
				choosenClass)) {
			widgetType = classLoader.loadClass("org.eclipse.swt.custom.CCombo");
		}
		return PropertiesSupport.getProperties(classLoader, widgetType);
	}

	private class ElementPropertyUiContentProvider
	extends
	ChooseClassAndTreePropertiesUiContentProvider2 {
		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public ElementPropertyUiContentProvider(ChooseClassAndPropertiesConfiguration configuration) {
			super(configuration);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Update
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public void updateFromObject() throws Exception {
			getDialogField().setEnabled(false);
			Class<?> elementType = m_elementTypeProvider.getElementType();
			if (isEmpty()) {
				if (elementType == null) {
					calculateFinish();
				} else {
					setClassName(CoreUtils.getClassName(elementType));
				}
			} else {
				setClassNameAndProperties(elementType, null, Arrays.asList(m_elementProperty));
			}
		}

		@Override
		protected void saveToObject(Class<?> choosenClass, List<PropertyAdapter> choosenProperties)
				throws Exception {
			Object[] checkedElements = m_propertiesViewer.getCheckedElements();
			ObservePropertyAdapter adapter = (ObservePropertyAdapter) checkedElements[0];
			m_elementProperty = adapter.getProperty().getReference();
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// ElementType
	//
	////////////////////////////////////////////////////////////////////////////
	public static interface IElementTypeProvider {
		Class<?> getElementType() throws Exception;
	}
}