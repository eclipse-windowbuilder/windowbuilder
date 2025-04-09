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
package org.eclipse.wb.internal.core.databinding.model.reference;

/**
 * Implementation of {@link IReferenceProvider} that consists of two other
 * {@link IReferenceProvider}.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public final class CompoundReferenceProvider implements IReferenceProvider {
	private final IReferenceProvider m_leftReferenceProvider;
	private final String m_rightReference;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CompoundReferenceProvider(IReferenceProvider leftReferenceProvider, String rightReference) {
		m_leftReferenceProvider = leftReferenceProvider;
		m_rightReference = rightReference;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IReferenceProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getReference() throws Exception {
		return m_leftReferenceProvider.getReference() + m_rightReference;
	}
}