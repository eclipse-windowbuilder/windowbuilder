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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;

/**
 * {@link IObservePresentation} for presentation {@link JavaInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class JavaInfoObservePresentation
extends
org.eclipse.wb.internal.core.databinding.model.presentation.JavaInfoObservePresentation {
	private final IReferenceProvider m_referenceProvider;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JavaInfoObservePresentation(JavaInfo javaInfo, IReferenceProvider referenceProvider) {
		super(javaInfo);
		m_referenceProvider = referenceProvider;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IObservePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTextForBinding() throws Exception {
		return m_referenceProvider.getReference();
	}
}