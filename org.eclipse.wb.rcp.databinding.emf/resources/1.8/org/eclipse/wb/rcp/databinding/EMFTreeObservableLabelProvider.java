/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.rcp.databinding;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;

/**
 * This class may be freely distributed as part of any application or plugin.
 * 
 * @author lobas_av
 */
public class EMFTreeObservableLabelProvider extends LabelProvider {
	private final EStructuralFeature m_textProperty;
	private final EStructuralFeature m_imageProperty;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EMFTreeObservableLabelProvider(IObservableSet allElementsObservable,
			EStructuralFeature textProperty,
			EStructuralFeature imageProperty) {
		m_observable = allElementsObservable;
		m_textProperty = textProperty;
		m_imageProperty = imageProperty;
		List<EStructuralFeature> properties = new ArrayList<EStructuralFeature>();
		if (m_textProperty != null) {
			properties.add(m_textProperty);
		}
		if (m_imageProperty != null) {
			properties.add(m_imageProperty);
		}
		m_listenerSupport = new EMFListenerSupport(properties) {
			@Override
			protected void fireLabelPropertyChanged(Object element) {
				fireLabelProviderChanged(new LabelProviderChangedEvent(EMFTreeObservableLabelProvider.this,
					element));
			}
		};
		m_observable.addSetChangeListener(m_setListener);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// LabelProvider
	//
	/////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText(Object element) {
		if (m_textProperty == null) {
			return null;
		}
		EObject eObject = (EObject) element;
		return (String) eObject.eGet(m_textProperty);
	}
	@Override
	public Image getImage(Object element) {
		if (m_imageProperty == null) {
			return null;
		}
		EObject eObject = (EObject) element;
		return (Image) eObject.eGet(m_imageProperty);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	private final IObservableSet m_observable;
	private final EMFListenerSupport m_listenerSupport;
	private final ISetChangeListener m_setListener = new ISetChangeListener() {
		@Override
		public void handleSetChange(SetChangeEvent event) {
			for (Object removedElement : event.diff.getRemovals()) {
				m_listenerSupport.unhookListener(removedElement);
			}
			for (Object addedElement : event.diff.getAdditions()) {
				m_listenerSupport.hookListener(addedElement);
			}
		}
	};
	@Override
	public void dispose() {
		m_observable.removeSetChangeListener(m_setListener);
		m_listenerSupport.dispose();
		super.dispose();
	}
}