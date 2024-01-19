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
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders;

import org.eclipse.wb.internal.swing.databinding.model.bindings.DetailBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Editor for {@link DetailBindingInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class DetailBindingUiContentProvider extends ChooseClassAndPropertiesUiContentProvider {
	private final DetailBindingInfo m_binding;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DetailBindingUiContentProvider(ChooseClassAndPropertiesConfiguration configuration,
			DetailBindingInfo binding) {
		super(configuration);
		m_binding = binding;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
		String elementClassName = m_binding.getJListBinding().getInputElementType().getFullTypeName();
		setClassNameAndProperty(
				StringUtils.substringBefore(elementClassName, "<")
				+ StringUtils.substringAfterLast(elementClassName, ">"),
				m_binding.getDetailProperty());
	}

	@Override
	protected void saveToObject0(Class<?> choosenClass, List<PropertyInfo> choosenProperties)
			throws Exception {
		m_binding.setDetailProperty(choosenProperties.get(0));
	}
}