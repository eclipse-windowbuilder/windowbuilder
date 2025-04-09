/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.rcp.databinding.emf.model.bindables;

import org.eclipse.wb.internal.core.databinding.model.presentation.ObservePresentation;
import org.eclipse.wb.internal.rcp.databinding.emf.Activator;

import org.eclipse.jface.resource.ImageDescriptor;

import org.apache.commons.lang3.ClassUtils;

/**
 * Presentation for {@link EObjectBindableInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public final class EObjectObservePresentation extends ObservePresentation {
	private static final ImageDescriptor IMAGE = Activator.getImageDescriptor("EObject.gif");
	private final EObjectBindableInfo m_eObject;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EObjectObservePresentation(EObjectBindableInfo eObject) {
		m_eObject = eObject;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObservePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ImageDescriptor getInternalImageDescriptor() throws Exception {
		return IMAGE;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IObservePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText() throws Exception {
		return m_eObject.getReference()
				+ " - "
				+ ClassUtils.getShortClassName(m_eObject.getObjectType());
	}

	@Override
	public String getTextForBinding() throws Exception {
		return m_eObject.getReference();
	}
}