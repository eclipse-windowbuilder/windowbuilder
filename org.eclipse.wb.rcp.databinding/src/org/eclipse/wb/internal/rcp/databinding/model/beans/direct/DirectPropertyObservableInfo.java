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
package org.eclipse.wb.internal.rcp.databinding.model.beans.direct;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

import java.util.List;

/**
 * {@link ObservableInfo} model for properties with observable types.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public class DirectPropertyObservableInfo extends ObservableInfo {
	protected final BindableInfo m_bindableObject;
	protected final BindableInfo m_property;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public DirectPropertyObservableInfo(BindableInfo bindableObject, BindableInfo property) {
		m_bindableObject = bindableObject;
		m_property = property;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Variable
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getVariableIdentifier() throws Exception {
		return m_bindableObject.getReference() + "." + m_property.getReference();
	}

	@Override
	public final void setVariableIdentifier(String identifier) {
		throw new UnsupportedOperationException();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObservableInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final BindableInfo getBindableObject() {
		return m_bindableObject;
	}

	@Override
	public final BindableInfo getBindableProperty() {
		return m_property;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
	}
}