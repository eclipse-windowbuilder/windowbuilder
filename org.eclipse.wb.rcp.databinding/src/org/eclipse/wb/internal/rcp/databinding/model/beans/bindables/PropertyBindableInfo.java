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

import org.eclipse.wb.internal.core.databinding.model.IObserveDecoration;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.SimpleObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.TypeImageProvider;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;

/**
 * Abstract model for <code>Java Beans</code> object properties.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public abstract class PropertyBindableInfo extends BeanBindableInfo implements IObserveDecoration {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public PropertyBindableInfo(BeanSupport beanSupport,
			IObserveInfo parent,
			Class<?> objectType,
			IReferenceProvider referenceProvider,
			IObservePresentation presentation) {
		super(beanSupport, parent, objectType, referenceProvider, presentation);
		setBindingDecoration(IDecoration.TOP_LEFT);
	}

	public PropertyBindableInfo(BeanSupport beanSupport,
			IObserveInfo parent,
			String text,
			ImageDescriptor image,
			Class<?> objectType,
			IReferenceProvider referenceProvider) {
		this(beanSupport, parent, objectType, referenceProvider, new SimpleObservePresentation(text,
				image));
	}

	public PropertyBindableInfo(BeanSupport beanSupport,
			IObserveInfo parent,
			String text,
			ImageDescriptor image,
			Class<?> objectType,
			String reference) {
		this(beanSupport, parent, text, image, objectType, new StringReferenceProvider(reference));
	}

	public PropertyBindableInfo(BeanSupport beanSupport,
			IObserveInfo parent,
			String text,
			Class<?> objectType,
			String reference) {
		this(beanSupport, parent, text, TypeImageProvider.getImage(objectType), objectType, reference);
	}

	public PropertyBindableInfo(BeanSupport beanSupport,
			IObserveInfo parent,
			String text,
			Class<?> objectType,
			IReferenceProvider referenceProvider) {
		this(beanSupport,
				parent,
				text,
				TypeImageProvider.getImage(objectType),
				objectType,
				referenceProvider);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link IObservableFactory} for create observables with this property.
	 */
	@Override
	public abstract IObservableFactory getObservableFactory() throws Exception;
}