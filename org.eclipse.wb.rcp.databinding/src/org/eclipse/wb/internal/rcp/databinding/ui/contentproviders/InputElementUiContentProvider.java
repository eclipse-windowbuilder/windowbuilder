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

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.CollectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.MapsBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.AbstractViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.CheckedElementsObservableInfo;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Content provider for edit (viewer input, content and label providers)
 * {@link ViewerInputBindingInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class InputElementUiContentProvider
extends
ChooseClassAndTreePropertiesUiContentProvider {
	private final ViewerInputBindingInfo m_viewerBinding;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public InputElementUiContentProvider(ChooseClassAndPropertiesConfiguration configuration,
			ViewerInputBindingInfo viewerBinding) {
		super(configuration);
		m_viewerBinding = viewerBinding;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
		// prepare input element type
		Class<?> elementType = m_viewerBinding.getElementType();
		if (elementType == null) {
			calculateFinish();
		} else {
			// prepare initial properties
			MapsBeanObservableInfo mapsObservable =
					m_viewerBinding.getLabelProvider().getMapsObservable();
			String[] properties = mapsObservable.getProperties();
			if (properties == null) {
				setClassName(CoreUtils.getClassName(elementType));
			} else {
				List<String> checkedProperties = new ArrayList<>();
				for (int i = 0; i < properties.length; i++) {
					checkedProperties.add("\"" + properties[i] + "\"");
				}
				setClassNameAndProperties(elementType, null, checkedProperties);
			}
		}
	}

	@Override
	protected void saveToObject(Class<?> choosenClass, List<PropertyAdapter> choosenProperties)
			throws Exception {
		// sets label provider element type
		MapsBeanObservableInfo mapsObservable = m_viewerBinding.getLabelProvider().getMapsObservable();
		mapsObservable.setElementType(choosenClass);
		// sets label provider element properties
		String[] properties = new String[choosenProperties.size()];
		for (int i = 0; i < properties.length; i++) {
			ObservePropertyAdapter adapter = (ObservePropertyAdapter) choosenProperties.get(i);
			properties[i] = StringUtils.remove(adapter.getProperty().getReference(), '"');
		}
		mapsObservable.setProperties(properties);
		// set input element type
		setElementTypeToInput(m_viewerBinding, choosenClass);
	}

	public static void setElementTypeToInput(AbstractViewerInputBindingInfo viewerBinding,
			Class<?> choosenClass) {
		ObservableInfo observable = viewerBinding.getInputObservable();
		if (observable instanceof DetailBeanObservableInfo inputObservable) {
			inputObservable.setDetailPropertyType(choosenClass);
		} else if (observable instanceof CheckedElementsObservableInfo inputObservable) {
			inputObservable.setElementType(choosenClass);
		} else if (observable instanceof CollectionObservableInfo inputObservable) {
			inputObservable.setElementType(choosenClass);
		}
	}
}