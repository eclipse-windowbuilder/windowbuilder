/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.rcp.databinding.emf.model.observables.designer;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport.PropertyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansObservableFactoryInfo;

import java.util.List;

/**
 * Model for {@link org.eclipse.wb.rcp.databinding.EMFEditBeansListObservableFactory}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public final class EmfBeansObservableFactoryInfo extends BeansObservableFactoryInfo {
	private final String m_baseClassName;
	private final Class<?>[] m_constructorParameters;
	private final PropertiesSupport m_propertiesSupport;
	private PropertyInfo m_emfProperty;

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	////////////////////////////////////////////////////////////////////////////
	private EmfBeansObservableFactoryInfo(String className,
			String baseClassName,
			Class<?>[] constructorParameters,
			PropertiesSupport propertiesSupport) {
		super(className);
		m_baseClassName = baseClassName;
		m_constructorParameters = constructorParameters;
		m_propertiesSupport = propertiesSupport;
	}

	public static EmfBeansObservableFactoryInfo create(String className,
			PropertiesSupport propertiesSupport) {
		String baseClassName;
		Class<?>[] constructorParameters;
		if (propertiesSupport.isEditingDomainMode()) {
			baseClassName = "org.eclipse.wb.rcp.databinding.EMFEditBeansListObservableFactory";
			constructorParameters =
					new Class[]{
							Class.class,
							propertiesSupport.getEditingDomainClass(),
							propertiesSupport.getEStructuralFeature()};
		} else {
			baseClassName = "org.eclipse.wb.rcp.databinding.EMFBeansListObservableFactory";
			constructorParameters = new Class[]{Class.class, propertiesSupport.getEStructuralFeature()};
		}
		if (className == null) {
			className = baseClassName;
		}
		return new EmfBeansObservableFactoryInfo(className,
				baseClassName,
				constructorParameters,
				propertiesSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setPropertyName(String propertyName) throws Exception {
		super.setPropertyName(propertyName);
		//
		m_emfProperty = m_propertiesSupport.getProperty(getElementType(), propertyName);
		Assert.isNotNull(m_emfProperty);
	}

	public void setEMFPropertyReference(String propertyReference) throws Exception {
		Object[] result = m_propertiesSupport.getClassInfoForProperty(propertyReference);
		Assert.isNotNull(result);
		//
		m_emfProperty = (PropertyInfo) result[1];
		m_propertyName = m_emfProperty.name;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configure(ChooseClassConfiguration configuration) {
		configuration.setValueScope(m_baseClassName);
		configuration.setClearValue(m_baseClassName);
		configuration.setBaseClassName(m_baseClassName);
		configuration.setConstructorParameters(m_constructorParameters);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addSourceCode(List<String> lines) throws Exception {
		String domainCode =
				m_propertiesSupport.isEditingDomainMode() ? m_propertiesSupport.getEditingDomainReference()
						+ ", " : "";
		lines.add(m_className
				+ " "
				+ getVariableIdentifier()
				+ " = new "
				+ m_className
				+ "("
				+ m_elementType.getName()
				+ ".class, "
				+ domainCode
				+ m_emfProperty.reference
				+ ");");
	}
}