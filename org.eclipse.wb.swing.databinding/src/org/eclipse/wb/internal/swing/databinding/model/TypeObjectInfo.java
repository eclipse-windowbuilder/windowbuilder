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

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

/**
 * {@link AstObjectInfo} model for objects with generic parameters.
 *
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public final class TypeObjectInfo extends AstObjectInfo {
	private final IGenericType m_objectType;
	private final String m_parameters;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TypeObjectInfo(IGenericType objectType, String parameters) {
		m_objectType = objectType;
		m_parameters = parameters;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public IGenericType getObjectType() {
		return m_objectType;
	}

	public String getParameters() {
		return m_parameters;
	}
}