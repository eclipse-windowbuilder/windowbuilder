/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc and others.
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
package org.eclipse.wb.internal.rcp.databinding.model.beans.observables;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.DataBindingsCodeUtils;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.KnownElementsObservableInfo;

import java.util.List;

/**
 * Model for observable object {@code BeanProperties.value(...).observeDetail(...)}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public class MapsBeanObservableInfo extends ObservableInfo {
	private final ObservableInfo m_domainObservable;
	private Class<?> m_elementType;
	private String[] m_properties;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MapsBeanObservableInfo(DetailBeanObservableInfo observable) {
		this(observable.getMasterObservable(), observable.getDetailBeanClass(),
				new String[] { observable.getDetailPropertyReference() });
	}

	public MapsBeanObservableInfo(ObservableInfo domainObservable,
			Class<?> elementType,
			String[] properties) {
		m_domainObservable = domainObservable;
		m_elementType = elementType;
		m_properties = properties;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public ObservableInfo getDomainObservable() {
		return m_domainObservable;
	}

	public Class<?> getElementType() {
		return m_elementType;
	}

	public void setElementType(Class<?> elementType) {
		m_elementType = elementType;
	}

	public String[] getProperties() {
		return m_properties;
	}

	public void setProperties(String[] properties) throws Exception {
		m_properties = properties;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObservableInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public BindableInfo getBindableObject() {
		return null;
	}

	@Override
	public BindableInfo getBindableProperty() {
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
		// prepare variable
		if (getVariableIdentifier() == null) {
			if (m_properties.length == 1) {
				setVariableIdentifier(generationSupport.generateLocalName("observeMap"));
			} else {
				setVariableIdentifier(generationSupport.generateLocalName("observeMaps"));
			}
		}
		//
		KnownElementsObservableInfo domainObservable = (KnownElementsObservableInfo) m_domainObservable;
		//
		if (m_properties.length == 1) {
			// add code
			lines.add("org.eclipse.core.databinding.observable.map.IObservableMap "
					+ getVariableIdentifier()
					+ getAssignment(m_properties[0], domainObservable));
		} else {
			// add code
			lines.add("org.eclipse.core.databinding.observable.map.IObservableMap[] "
					+ getVariableIdentifier()
					+ " = new org.eclipse.core.databinding.observable.map.IObservableMap[" + m_properties.length + "];");
			for (int i = 0 ; i < m_properties.length; ++i) {
				lines.add(getVariableIdentifier() + "[" + i + "]"
						+ getAssignment(m_properties[i], domainObservable));
			}
		}
	}

	private final String getAssignment(String property, KnownElementsObservableInfo domainObservable) throws Exception {
		String observeMethod = isPojoBean(m_elementType)
				? " = " + DataBindingsCodeUtils.getPojoObservablesClass() + ".value("
				: " = " + DataBindingsCodeUtils.getBeanObservablesClass() + ".value(";
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(observeMethod);
		stringBuilder.append(CoreUtils.getClassName(m_elementType));
		stringBuilder.append(".class, \"");
		stringBuilder.append(property);
		stringBuilder.append("\").observeDetail(");
		stringBuilder.append(domainObservable.getSourceCode());
		stringBuilder.append(");");
		return stringBuilder.toString();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(AstObjectInfoVisitor visitor) throws Exception {
		super.accept(visitor);
		m_domainObservable.accept(visitor);
	}
}