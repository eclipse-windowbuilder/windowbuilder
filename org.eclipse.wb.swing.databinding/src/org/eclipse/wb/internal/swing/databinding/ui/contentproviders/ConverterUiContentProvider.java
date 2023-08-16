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

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.model.bindings.BindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.ConverterInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

/**
 * Editor for {@link ConverterInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public class ConverterUiContentProvider extends ChooseClassUiContentProvider {
	private final BindingInfo m_binding;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ConverterUiContentProvider(ChooseClassConfiguration configuration, BindingInfo binding) {
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
		ConverterInfo converter = m_binding.getConverter();
		setClassName(converter == null ? "N/S" : converter.getFullClassName());
	}

	@Override
	public void saveToObject() throws Exception {
		String className = getClassName();
		// check set or clear value
		if ("N/S".equals(className)) {
			m_binding.setConverter(null);
		} else {
			String parameters = null;
			int index = className.indexOf('(');
			if (index != -1) {
				parameters = className.substring(index);
				className = className.substring(0, index);
			}
			IGenericType converterType = new ClassGenericType(loadClass(className), null, null);
			ConverterInfo converter = m_binding.getConverter();
			// check new converter or edit value
			if (converter == null) {
				converter = new ConverterInfo(converterType, m_binding);
				m_binding.setConverter(converter);
			} else {
				converter.setClass(converterType);
			}
			converter.setParameters(parameters);
		}
	}
}