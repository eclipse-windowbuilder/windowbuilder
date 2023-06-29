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
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanSupport;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.PropertyAdapterLabelProvider;

import java.beans.PropertyDescriptor;
import java.util.List;

/**
 * {@link ChooseClassAndPropertiesUiContentProvider} with RCP implementation for
 * {@link #getProperties(Class)} over {@link BeanSupport#getPropertyDescriptors(Class)}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public abstract class ChooseClassAndPropertiesUiContentProvider
extends
org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesUiContentProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ChooseClassAndPropertiesUiContentProvider(ChooseClassAndPropertiesConfiguration configuration) {
		super(configuration);
		if (configuration.getPropertiesLabelProvider() == null) {
			configuration.setPropertiesLabelProvider(new PropertyAdapterLabelProvider());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected final List<PropertyAdapter> getProperties(Class<?> choosenClass) throws Exception {
		List<PropertyAdapter> properties = Lists.newArrayList();
		for (PropertyDescriptor descriptor : BeanSupport.getPropertyDescriptors(choosenClass)) {
			properties.add(new PropertyAdapter(descriptor));
		}
		return properties;
	}
}