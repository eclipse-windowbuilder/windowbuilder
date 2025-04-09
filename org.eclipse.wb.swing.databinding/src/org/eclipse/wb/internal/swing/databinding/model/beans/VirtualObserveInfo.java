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
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.swing.databinding.Activator;
import org.eclipse.wb.internal.swing.databinding.model.ObserveCreationType;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;

import org.eclipse.jface.viewers.IDecoration;

import java.util.List;

/**
 * @author lobas_av
 * @coverage bindings.swing.model.beans
 */
public final class VirtualObserveInfo extends BeanObserveInfo {
	private final IObservePresentation m_presentation;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public VirtualObserveInfo() {
		super(null, null, ClassGenericType.LIST_CLASS, StringReferenceProvider.EMPTY);
		setBindingDecoration(IDecoration.TOP_LEFT);
		m_presentation =
				new SimpleObservePresentation("[Virtual]", "[Virtual]", Activator.getImageDescriptor("virtual.png"));
		setProperties(List.of(new ObjectPropertyObserveInfo(getObjectType())));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Type
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ObserveCreationType getCreationType() {
		return ObserveCreationType.VirtualBinding;
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