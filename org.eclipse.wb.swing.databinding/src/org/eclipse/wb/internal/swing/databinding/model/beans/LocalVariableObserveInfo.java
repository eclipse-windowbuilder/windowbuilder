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

import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.SimpleObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.FragmentReferenceProvider;
import org.eclipse.wb.internal.swing.databinding.Activator;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.viewers.IDecoration;

/**
 * Model for {@code initDataBindings()} local variables <code>Java Beans</code> objects.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.beans
 */
public final class LocalVariableObserveInfo extends BeanObserveInfo {
	private final VariableDeclarationFragment m_fragment;
	private final IObservePresentation m_presentation;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LocalVariableObserveInfo(BeanSupport beanSupport,
			VariableDeclarationFragment fragment,
			IGenericType objectType) throws Exception {
		super(beanSupport, null, objectType, new FragmentReferenceProvider(fragment));
		setBindingDecoration(IDecoration.TOP_LEFT);
		m_fragment = fragment;
		m_presentation =
				new SimpleObservePresentation(null, null, Activator.getImageDescriptor("localvariable_obj.gif")) {
			@Override
			public String getText() throws Exception {
				return getReference() + " - " + getObjectType().getSimpleTypeName();
			}

			@Override
			public String getTextForBinding() throws Exception {
				return getReference();
			}
		};
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