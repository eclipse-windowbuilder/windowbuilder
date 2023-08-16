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
package org.eclipse.wb.internal.swing.databinding.model.beans;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.FragmentReferenceProvider;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.viewers.IDecoration;

/**
 * Model for field based <code>Java Beans</code> objects.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.beans
 */
public final class FieldBeanObserveInfo extends BeanObserveInfo {
	private final VariableDeclarationFragment m_fragment;
	private final IObservePresentation m_presentation;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FieldBeanObserveInfo(BeanSupport beanSupport,
			VariableDeclarationFragment fragment,
			IGenericType objectType,
			JavaInfo javaInfo) throws Exception {
		super(beanSupport, null, objectType, new FragmentReferenceProvider(fragment));
		setBindingDecoration(IDecoration.TOP_LEFT);
		m_fragment = fragment;
		m_presentation =
				new FieldBeanObservePresentation(this, javaInfo, beanSupport.getBeanImage(
						getObjectClass(),
						javaInfo,
						true));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public VariableDeclarationFragment getFragment() {
		return m_fragment;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IObservePresentation getPresentation() {
		return m_presentation;
	}
}