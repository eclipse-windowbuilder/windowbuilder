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

import org.eclipse.wb.internal.core.databinding.model.IObserveDecoration;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.SimpleObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.swing.databinding.model.ObserveCreationType;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.BeanPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;
import org.eclipse.wb.internal.swing.databinding.ui.providers.TypeImageProvider;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;

/**
 * Model for <code>Java Beans</code> object properties.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.beans
 */
public class BeanPropertyObserveInfo extends BeanObserveInfo implements IObserveDecoration {
	private final ObserveCreationType m_creationType;
	private final IObservePresentation m_presentation;
	private final IObserveDecorator m_decorator;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BeanPropertyObserveInfo(BeanSupport beanSupport,
			ObserveInfo parent,
			String text,
			IGenericType objectType,
			IReferenceProvider referenceProvider,
			IObserveDecorator decorator) throws Exception {
		super(beanSupport,
				parent instanceof BeanPropertyObserveInfo ? parent : null,
						objectType,
						referenceProvider);
		setBindingDecoration(IDecoration.TOP_LEFT);
		m_creationType =
				java.util.List.class.isAssignableFrom(getObjectClass())
				? ObserveCreationType.ListProperty
						: ObserveCreationType.AnyProperty;
		ImageDescriptor beenImage = beanSupport.getBeanImageDescriptor(getObjectClass(), null, false);
		m_presentation =
				new SimpleObservePresentation(text, text, beenImage == null
				? TypeImageProvider.getImageDescriptor(getObjectClass())
						: beenImage);
		m_decorator = decorator;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public final void setHostedType(IGenericType objectType) {
		setObjectType(objectType);
		setProperties(null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Type
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ObserveType getType() {
		return null;
	}

	@Override
	public ObserveCreationType getCreationType() {
		return m_creationType;
	}

	@Override
	public boolean canShared() {
		return true;
	}

	@Override
	public PropertyInfo createProperty(ObserveInfo observeObject) throws Exception {
		StringBuffer reference = new StringBuffer(getReference());
		ObserveInfo parent = (ObserveInfo) getParent();
		while (parent != null) {
			reference.insert(0, parent.getReference() + ".");
			parent = (ObserveInfo) parent.getParent();
		}
		return new BeanPropertyInfo(observeObject.getObjectType(),
				getObjectType(),
				null,
				reference.toString());
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

	@Override
	public final IObserveDecorator getDecorator() {
		return m_decorator;
	}
}