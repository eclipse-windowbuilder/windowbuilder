/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.databinding.model.beans.bindables;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.presentation.ObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.ClassUtils;

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
	private Image m_beanImage;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BeanBindablePresentation(Class<?> objectType,
			IReferenceProvider presentation,
			ObjectInfo javaInfo,
			Image beanImage) {
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

	public void setBeanImage(Image beanImage) {
		m_beanImage = beanImage;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObservePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ImageDescriptor getInternalImage() throws Exception {
		if (m_beanImage == null && m_javaInfo == null) {
			return null;
		}
		return m_beanImage == null ? m_javaInfo.getPresentation().getIcon() : new ImageImageDescriptor(m_beanImage);
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