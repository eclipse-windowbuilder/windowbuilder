/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.swing.databinding.model.bindings;

import org.eclipse.wb.internal.swing.databinding.model.GenericClassObjectInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

/**
 * Model for {@link org.jdesktop.beansbinding.Validator}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.bindings
 */
public final class ValidatorInfo extends GenericClassObjectInfo {
	private final BindingInfo m_binding;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ValidatorInfo(IGenericType objectType, BindingInfo binding) {
		super("org.jdesktop.beansbinding.Validator");
		m_binding = binding;
		setClass(objectType);
		if (isGeneric()) {
			GenericUtils.assertEquals(m_binding.getModelPropertyType(), objectType.getSubType(0));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IGenericType[] getTypeArguments() {
		return isGeneric() ? new IGenericType[]{m_binding.getModelPropertyType()} : null;
	}
}