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
package org.eclipse.wb.internal.rcp.databinding.model.beans.bindables;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Model for any <code>Java Beans</code> objects.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public class BeanBindableInfo extends BindableInfo {
	private final BeanSupport m_beanSupport;
	private final IObservePresentation m_presentation;
	protected List<BeanBindableInfo> m_children = Collections.emptyList();
	private List<PropertyBindableInfo> m_properties;
	private final IObserveInfo m_parent;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public BeanBindableInfo(BeanSupport beanSupport,
			IObserveInfo parent,
			Class<?> objectType,
			IReferenceProvider referenceProvider,
			ObjectInfo javaInfo) throws Exception {
		this(beanSupport, parent, objectType, referenceProvider, referenceProvider, javaInfo);
	}

	public BeanBindableInfo(BeanSupport beanSupport,
			IObserveInfo parent,
			Class<?> objectType,
			IReferenceProvider referenceProvider,
			IReferenceProvider presentationReferenceProvider,
			ObjectInfo javaInfo) throws Exception {
		this(beanSupport,
				parent,
				objectType,
				referenceProvider,
				new BeanBindablePresentation(objectType,
						presentationReferenceProvider,
						javaInfo,
						beanSupport.getBeanImage(objectType, javaInfo)));
	}

	public BeanBindableInfo(BeanSupport beanSupport,
			IObserveInfo parent,
			Class<?> objectType,
			IReferenceProvider referenceProvider,
			IObservePresentation presentation) {
		super(objectType, referenceProvider);
		m_beanSupport = beanSupport;
		m_parent = parent;
		m_presentation = presentation;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// BindableInfo
	//
	////////////////////////////////////////////////////////////////////////////
	public final BeanSupport getBeanSupport() {
		return m_beanSupport;
	}

	public final void setProperties(List<PropertyBindableInfo> properties) {
		m_properties = properties;
	}

	@Override
	protected final List<BindableInfo> getChildren() {
		return CoreUtils.cast(m_children);
	}

	/**
	 * Access to properties.
	 */
	public final List<PropertyBindableInfo> getProperties() {
		// prepare properties
		if (m_properties == null) {
			m_properties = m_beanSupport.getProperties(this);
		}
		return m_properties;
	}

	/**
	 * @return {@link PropertyBindableInfo} property that association with given reference or or
	 *         <code>null</code>.
	 */
	@Override
	public final PropertyBindableInfo resolvePropertyReference(String reference) throws Exception {
		if (reference.indexOf('.') == -1 || !reference.startsWith("\"")) {
			for (PropertyBindableInfo property : getProperties()) {
				if (reference.equals(property.getReference())) {
					return property;
				}
			}
		} else {
			String localReference = StringUtils.removeStart(reference, "\"");
			localReference = StringUtils.removeEnd(localReference, "\"");
			return resolvePropertyReference(reference, StringUtils.split(localReference, "."), 0);
		}
		return null;
	}

	protected final PropertyBindableInfo resolvePropertyReference(String reference,
			String[] references,
			int index) throws Exception {
		if (index == references.length - 1) {
			for (PropertyBindableInfo property : getProperties()) {
				if (reference.equals(property.getReference())) {
					return property;
				}
			}
		} else {
			String localReference = references[index];
			//
			for (PropertyBindableInfo property : getProperties()) {
				String propertyReference = StringUtils.removeStart(property.getReference(), "\"");
				propertyReference = StringUtils.removeEnd(propertyReference, "\"");
				int pointIndex = propertyReference.lastIndexOf('.');
				//
				if (pointIndex != -1) {
					propertyReference = propertyReference.substring(pointIndex + 1);
				}
				if (localReference.equals(propertyReference)) {
					return property.resolvePropertyReference(reference, references, index + 1);
				}
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Hierarchy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final IObserveInfo getParent() {
		return m_parent;
	}

	@Override
	public List<IObserveInfo> getChildren(ChildrenContext context) {
		if (context == ChildrenContext.ChildrenForMasterTable) {
			return CoreUtils.cast(m_children);
		}
		if (context == ChildrenContext.ChildrenForPropertiesTable) {
			return CoreUtils.cast(getProperties());
		}
		return Collections.emptyList();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final IObservePresentation getPresentation() {
		return m_presentation;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObserveType
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final ObserveType getType() {
		return ObserveType.BEANS;
	}
}