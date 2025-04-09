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
package org.eclipse.wb.internal.rcp.databinding.model.beans.bindables;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.presentation.ObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;

import org.eclipse.jface.resource.ImageDescriptor;

import org.apache.commons.lang3.ClassUtils;

/**
 * Presentation for {@link BeanBindableInfo} models.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public final class BeanBindablePresentation extends ObservePresentation {
	private Class<?> m_objectType;
	private final IReferenceProvider m_presentation;
	private ObjectInfo m_javaInfo;
	private ImageDescriptor m_beanImage;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BeanBindablePresentation(Class<?> objectType,
			IReferenceProvider presentation,
			ObjectInfo javaInfo,
			ImageDescriptor beanImage) {
		m_objectType = objectType;
		m_presentation = presentation;
		m_javaInfo = javaInfo;
		m_beanImage = beanImage;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public JavaInfo getJavaInfo() {
		return (JavaInfo) m_javaInfo;
	}

	public void setJavaInfo(JavaInfo javaInfo) {
		m_javaInfo = javaInfo;
	}

	public void setObjectType(Class<?> objectType) {
		m_objectType = objectType;
	}

	public void setBeanImage(ImageDescriptor beanImage) {
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
		return m_presentation.getReference() + " - " + ClassUtils.getShortClassName(m_objectType);
	}

	@Override
	public String getTextForBinding() throws Exception {
		return m_presentation.getReference();
	}
}