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

import org.eclipse.wb.internal.core.databinding.model.IObserveDecoration;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.SimpleObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.swing.databinding.model.ObserveCreationType;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.ObjectPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;
import org.eclipse.wb.internal.swing.databinding.ui.providers.TypeImageProvider;

import org.eclipse.jface.viewers.IDecoration;

import java.util.Collections;
import java.util.List;

/**
 * {@link ObserveInfo} model for {@link org.jdesktop.beansbinding.ObjectProperty}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.beans
 */
public final class ObjectPropertyObserveInfo extends ObserveInfo implements IObserveDecoration {
	private final ObserveCreationType m_creationType;
	private final IObservePresentation m_presentation;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ObjectPropertyObserveInfo(IGenericType objectType) {
		super(objectType, StringReferenceProvider.EMPTY);
		setBindingDecoration(IDecoration.TOP_LEFT);
		m_creationType =
				java.util.List.class.isAssignableFrom(getObjectClass())
				? ObserveCreationType.ListSelfProperty
						: ObserveCreationType.SelfProperty;
		m_presentation =
				new SimpleObservePresentation("<Self Object>", "", TypeImageProvider.OBJECT_PROPERTY_IMAGE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canShared() {
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObserveType
	//
	////////////////////////////////////////////////////////////////////////////
	public ObserveType getType() {
		return null;
	}

	@Override
	public ObserveCreationType getCreationType() {
		return m_creationType;
	}

	@Override
	public PropertyInfo createProperty(ObserveInfo observeObject) throws Exception {
		return new ObjectPropertyInfo(getObjectType());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Hierarchy
	//
	////////////////////////////////////////////////////////////////////////////
	public IObserveInfo getParent() {
		return null;
	}

	public List<IObserveInfo> getChildren(ChildrenContext context) {
		return Collections.emptyList();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	public IObservePresentation getPresentation() {
		return m_presentation;
	}

	public IObserveDecorator getDecorator() {
		return IObserveDecorator.BOLD;
	}
}