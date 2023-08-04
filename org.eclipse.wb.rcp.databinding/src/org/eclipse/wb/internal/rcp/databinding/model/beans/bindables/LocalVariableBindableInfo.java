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

import org.eclipse.wb.internal.core.databinding.model.reference.FragmentReferenceProvider;
import org.eclipse.wb.internal.rcp.databinding.Activator;

import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.viewers.IDecoration;

/**
 * Model for {@code initDataBindings()} local variables <code>Java Beans</code> objects.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public class LocalVariableBindableInfo extends BeanBindableInfo {
	private final VariableDeclarationFragment m_fragment;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LocalVariableBindableInfo(BeanSupport beanSupport,
			VariableDeclarationFragment fragment,
			Class<?> objectType) throws Exception {
		super(beanSupport,
				null,
				objectType,
				new FragmentReferenceProvider(fragment),
				new BeanBindablePresentation(objectType,
						new FragmentReferenceProvider(fragment),
						null,
						Activator.getImage("localvariable_obj.gif")));
		setBindingDecoration(IDecoration.TOP_RIGHT);
		m_fragment = fragment;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public VariableDeclarationFragment getFragment() {
		return m_fragment;
	}
}