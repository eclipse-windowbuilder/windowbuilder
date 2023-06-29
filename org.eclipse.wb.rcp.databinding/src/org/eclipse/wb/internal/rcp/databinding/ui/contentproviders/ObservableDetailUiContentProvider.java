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
package org.eclipse.wb.internal.rcp.databinding.ui.contentproviders;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration.LoadedPropertiesCheckedStrategy;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.PropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.AbstractViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.ViewerObservableInfo;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Content provider for edit (choose detail property) {@link DetailBeanObservableInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public class ObservableDetailUiContentProvider
extends
ChooseClassAndTreePropertiesUiContentProvider2 {
	private final DetailBeanObservableInfo m_observable;
	private final DatabindingsProvider m_provider;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ObservableDetailUiContentProvider(ChooseClassAndPropertiesConfiguration configuration,
			DetailBeanObservableInfo observable,
			DatabindingsProvider provider) {
		super(configuration);
		configuration.addDefaultStart("detail(");
		m_observable = observable;
		m_provider = provider;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
		String propertyReference = m_observable.getDetailPropertyReference();
		// sets property reference and type
		if (propertyReference == null) {
			// calculate type over viewer input element type
			if (m_observable.getMasterObservable() instanceof ViewerObservableInfo) {
				Class<?> elementType =
						AbstractViewerInputBindingInfo.getViewerInutElementType(
								m_observable.getMasterObservable(),
								m_provider);
				if (elementType == null) {
					calculateFinish();
				} else {
					setClassName(CoreUtils.getClassName(elementType));
				}
			} else {
				calculateFinish();
			}
		} else if (m_observable.getDetailBeanClass() == null) {
			// prepare property
			Class<?> propertyType = m_observable.getDetailPropertyType();
			String propertyName = StringUtils.substringBetween(propertyReference, "\"");
			BeanPropertyBindableInfo bindableProperty =
					new BeanPropertyBindableInfo(null, null, propertyName, propertyType, propertyName);
			bindableProperty.setProperties(Collections.<PropertyBindableInfo>emptyList());
			PropertyAdapter property = new ObservePropertyAdapter(null, bindableProperty);
			// prepare fake class name
			String className =
					"detail("
							+ propertyReference
							+ ", "
							+ ClassUtils.getShortClassName(propertyType)
							+ ".class)";
			// sets class and property
			setClassNameAndProperty(className, property, false);
		} else {
			// set temporally None strategy for select detail property
			LoadedPropertiesCheckedStrategy strategy =
					m_configuration.getLoadedPropertiesCheckedStrategy();
			m_configuration.setLoadedPropertiesCheckedStrategy(LoadedPropertiesCheckedStrategy.None);
			// sets class and property
			setClassNameAndProperties(
					m_observable.getDetailBeanClass(),
					null,
					Lists.newArrayList(propertyReference));
			// restore strategy
			m_configuration.setLoadedPropertiesCheckedStrategy(strategy);
		}
	}

	@Override
	protected void saveToObject(Class<?> choosenClass, List<PropertyAdapter> choosenProperties)
			throws Exception {
		if (choosenClass != null) {
			Object[] checkedElements = m_propertiesViewer.getCheckedElements();
			ObservePropertyAdapter adapter = (ObservePropertyAdapter) checkedElements[0];
			m_observable.setDetailPropertyType(adapter.getType());
			m_observable.setDetailPropertyReference(choosenClass, adapter.getProperty().getReference());
		}
	}
}