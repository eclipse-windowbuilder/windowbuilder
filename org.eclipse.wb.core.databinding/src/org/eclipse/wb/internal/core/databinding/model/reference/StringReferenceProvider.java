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
 * {@link IReferenceProvider} for static (not changed) references.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public final class StringReferenceProvider implements IReferenceProvider {
	public static final IReferenceProvider EMPTY = new StringReferenceProvider("");
	private final String m_reference;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public StringReferenceProvider(String reference) {
		m_reference = reference;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IReferenceProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getReference() throws Exception {
		return m_reference;
	}
}