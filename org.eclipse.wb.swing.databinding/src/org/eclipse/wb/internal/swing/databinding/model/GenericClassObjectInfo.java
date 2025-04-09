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
package org.eclipse.wb.internal.swing.databinding.model;

import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Model for objects with generic parameters.
 *
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public abstract class GenericClassObjectInfo extends SimpleClassObjectInfo {
	private boolean m_generic;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GenericClassObjectInfo(String abstractClassName) {
		super(abstractClassName, null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	protected boolean isGeneric() {
		return m_generic;
	}

	public void setClass(IGenericType type) {
		setClassName(CoreUtils.getClassName(type.getRawType()));
		m_generic = !(type.isEmpty() && ArrayUtils.isEmpty(type.getRawType().getTypeParameters()));
	}

	/**
	 * @return the array of generic type arguments.
	 */
	@Override
	protected abstract IGenericType[] getTypeArguments();
}