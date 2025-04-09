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
package org.eclipse.wb.internal.swing.databinding.model.beans;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.presentation.ObservePresentation;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Presentation for {@link FieldBeanObserveInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.beans
 */
public final class FieldBeanObservePresentation extends ObservePresentation {
	private final FieldBeanObserveInfo m_observe;
	private final JavaInfo m_javaInfo;
	private final ImageDescriptor m_beanImage;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FieldBeanObservePresentation(FieldBeanObserveInfo observe,
			JavaInfo javaInfo,
			ImageDescriptor beanImage) {
		m_observe = observe;
		m_javaInfo = javaInfo;
		m_beanImage = beanImage;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObservePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ImageDescriptor getInternalImageDescriptor() throws Exception {
		if (m_beanImage == null && m_javaInfo == null) {
			return null;
		}
		return m_beanImage == null ? m_javaInfo.getPresentation().getIcon() : m_beanImage;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IObservePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText() throws Exception {
		return m_observe.getReference() + " - " + m_observe.getObjectType().getSimpleTypeName();
	}

	@Override
	public String getTextForBinding() throws Exception {
		return m_observe.getReference();
	}
}