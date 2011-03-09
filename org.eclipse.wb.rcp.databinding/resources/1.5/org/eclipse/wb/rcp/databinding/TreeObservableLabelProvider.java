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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;

/**
 * This class may be freely distributed as part of any application or plugin.
 * 
 * @author lobas_av
 */
public class TreeObservableLabelProvider extends LabelProvider {
	private final Class<?> m_beanClass;
	private final Method m_getTextMethod;
	private final Method m_getImageMethod;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TreeObservableLabelProvider(IObservableSet allElementsObservable,
			Class<?> beanClass,
			String textProperty,
			String imageProperty) {
		m_observable = allElementsObservable;
		m_beanClass = beanClass;
		m_getTextMethod = Utils.getMethod(m_beanClass, textProperty);
		m_getImageMethod = Utils.getMethod(m_beanClass, imageProperty);
		List<String> properties = new ArrayList<String>();
		if (m_getTextMethod != null) {
			properties.add(textProperty);
		}
		if (m_getImageMethod != null) {
			properties.add(imageProperty);
		}
		m_listenerSupport = new ListenerSupport(m_propertiesListener, properties);
		m_observable.addSetChangeListener(m_setListener);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// LabelProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText(Object element) {
		Object text = Utils.invokeMethod(m_getTextMethod, m_beanClass, element);
		return text == null ? null : text.toString();
	}
	@Override
	public Image getImage(Object element) {
		return (Image) Utils.invokeMethod(m_getImageMethod, m_beanClass, element);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	private final IObservableSet m_observable;
	private final ListenerSupport m_listenerSupport;
	private final PropertyChangeListener m_propertiesListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			LabelProviderChangedEvent newEvent =
					new LabelProviderChangedEvent(TreeObservableLabelProvider.this, event.getSource());
			fireLabelProviderChanged(newEvent);
		}
	};
	private final ISetChangeListener m_setListener = new ISetChangeListener() {
		@Override
		public void handleSetChange(SetChangeEvent event) {
			for (Object removedElement : event.diff.getRemovals()) {
				if (Utils.instanceOf(m_beanClass, removedElement)) {
					m_listenerSupport.unhookListener(removedElement);
				}
			}
			for (Object addedElement : event.diff.getAdditions()) {
				if (Utils.instanceOf(m_beanClass, addedElement)) {
					m_listenerSupport.hookListener(addedElement);
				}
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