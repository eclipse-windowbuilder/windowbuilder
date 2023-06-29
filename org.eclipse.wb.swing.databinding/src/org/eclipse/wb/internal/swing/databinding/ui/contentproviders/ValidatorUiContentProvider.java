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
import org.eclipse.wb.internal.swing.databinding.model.bindings.ValidatorInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

/**
 * Content provider for edit (choose class over dialog and combo) {@link ValidatorInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public class ValidatorUiContentProvider extends ChooseClassUiContentProvider {
	private final BindingInfo m_binding;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ValidatorUiContentProvider(ChooseClassConfiguration configuration, BindingInfo binding) {
		super(configuration);
		m_binding = binding;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	public void updateFromObject() throws Exception {
		ValidatorInfo validator = m_binding.getValidator();
		setClassName(validator == null ? "N/S" : validator.getFullClassName());
	}

	public void saveToObject() throws Exception {
		String className = getClassName();
		// check set or clear value
		if ("N/S".equals(className)) {
			m_binding.setValidator(null);
		} else {
			String parameters = null;
			int index = className.indexOf('(');
			if (index != -1) {
				parameters = className.substring(index);
				className = className.substring(0, index);
			}
			IGenericType validatorType = new ClassGenericType(loadClass(className), null, null);
			ValidatorInfo validator = m_binding.getValidator();
			// check new converter or edit value
			if (validator == null) {
				validator = new ValidatorInfo(validatorType, m_binding);
				m_binding.setValidator(validator);
			} else {
				validator.setClass(validatorType);
			}
			validator.setParameters(parameters);
		}
	}
}